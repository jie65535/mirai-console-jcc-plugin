import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.info

object JCompilerCollection : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.mirai-console-jcc-plugin",
        name = "J Compiler Collection",
        version = "1.1.0",
    ) {
        author("jie65535")
        info("""在线编译器集合""")
    }
) {
    const val CMD_PREFIX = "run"
    private const val MSG_MAX_LENGTH = 550

    override fun onEnable() {
        logger.info { "Plugin loaded" }
        JccCommand.register()
        JccPluginData.reload()

        globalEventChannel()
            .parentScope(this)
            .subscribeMessages {
                content {
                    message.firstIsInstanceOrNull<PlainText>()?.content?.trimStart()?.startsWith(CMD_PREFIX) == true
                } reply {
                    val msg = message.firstIsInstance<PlainText>().content.trimStart().removePrefix(CMD_PREFIX).trim()
                    if (msg.isBlank()) {
                        return@reply "请输入正确的命令！例如：\n$CMD_PREFIX python print(\"Hello world\")"
                    }

                    val index = msg.indexOfFirst(Char::isWhitespace)
                    val language = if (index >= 0) msg.substring(0, index) else msg
                    if (!GlotAPI.checkSupport(language))
                        return@reply "不支持这种编程语言\n/jcc list #列出所有支持的编程语言"

                    try {
                        // 检查命令的引用
                        val quote = message[QuoteReply]
                        var input: String? = null
                        // 支持运行引用的消息的代码
                        var code = if (quote != null) {
                            // run c [input]
                            if (index >= 0) {
                                input = msg.substring(index).trim()
                            }
                            quote.source.originalMessage.content
                        } else if (index >= 0) {
                            msg.substring(index).trim()
                        } else {
                            return@reply "$CMD_PREFIX $language\n" + GlotAPI.getTemplateFile(language).content
                        }

                        // 如果是引用消息，则不再从原消息中分析。否则，还要从消息中判断是否存在输入参数
                        val si = if (quote != null) 0 else code.indexOfFirst(Char::isWhitespace)
                        // 尝试得到url
                        val url = if (si > 0) {
                            code.substring(0, si)
                        } else {
                            code
                        }
                        // 如果参数是一个ubuntu pastebin的链接，则去获取具体代码
                        if (UbuntuPastebinHelper.checkUrl(url)) {
                            if (si > 0) {
                                // 如果确实是一个链接，则链接后面跟的内容就是输入内容
                                input = code.substring(si+1)
                            }
                            logger.info("从 $url 中获取代码")
                            code = UbuntuPastebinHelper.get(url)
                            if (code.isBlank()) {
                                return@reply "未获取到有效代码"
                            }
                        }

                        // subject.sendMessage("正在执行，请稍等...")
                        logger.info("请求执行代码\n$code")
                        val result = GlotAPI.runCode(language, code, input)
                        val builder = MessageChainBuilder()
                        var c = 0
                        if (result.stdout.isNotEmpty()) c++
                        if (result.stderr.isNotEmpty()) c++
                        if (result.error.isNotEmpty()) c++
                        val title = c >= 2
                        if (subject is Group) {
                            builder.add(At(sender))
                            builder.add("\n")
                        }

                        if (c == 0) {
                            builder.add("没有任何结果呢~")
                        } else {
                            val sb = StringBuilder()
                            if (result.error.isNotEmpty()) {
                                sb.appendLine("error:")
                                sb.append(result.error)
                            }
                            if (result.stdout.isNotEmpty()) {
                                if (title) sb.appendLine("\nstdout:")
                                sb.append(result.stdout)
                            }
                            if (result.stderr.isNotEmpty()) {
                                if (title) sb.appendLine("\nstderr:")
                                sb.append(result.stderr)
                            }
                            if (sb.length > MSG_MAX_LENGTH) {
                                sb.deleteRange(MSG_MAX_LENGTH, sb.length)
                                sb.append("\n消息内容过长，已截断")
                            }
                            builder.append(sb.toString())
                        }
                        return@reply builder.build()
                    } catch (e: Exception) {
                        logger.warning(e)
                        return@reply "执行失败\n原因：${e.message}"
                    }
                }
        }
    }

    override fun onDisable() {
        JccCommand.unregister()
    }
}