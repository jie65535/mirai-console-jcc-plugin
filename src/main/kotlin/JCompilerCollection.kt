import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.info

object JCompilerCollection : KotlinPlugin(
    JvmPluginDescription(
        id = "top.jie65535.mirai-console-jcc-plugin",
        name = "J Compiler Collection",
        version = "1.0",
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


        globalEventChannel().subscribeMessages {
            startsWith(CMD_PREFIX, false) reply {
                if (subject is Group && (subject as Group).isBotMuted)
                    return@reply null
                val msg = it.substring(CMD_PREFIX.length).trim()
                if (msg.isNotEmpty()) {
                    val index = msg.indexOfFirst(Char::isWhitespace)
                    val language =  if (index >= 0) msg.substring(0, index) else msg
                    if (!GlotAPI.checkSupport(language))
                        return@reply "不支持这种编程语言\n/jcc list #列出所有支持的编程语言"
                    var code = if (index >= 0) {
                        msg.substring(index).trim()
                    } else {
                        return@reply "$CMD_PREFIX $language\n" + GlotAPI.getTemplateFile(language).content
                    }

                    try {
                        val si = code.indexOfFirst(Char::isWhitespace)
                        val url = if (si > 0) {
                            code.substring(0, si)
                        } else {
                            code
                        }
                        var input: String? = null
                        // 如果参数是一个ubuntu pastebin的链接，则去获取
                        if (UbuntuPastebinHelper.checkUrl(url)) {
                            if (si > 0) {
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
                return@reply "请输入正确的命令！例如：\n$CMD_PREFIX python print(\"Hello world\")"
            }
        }
    }

    override fun onDisable() {
        JccCommand.unregister()
    }
}