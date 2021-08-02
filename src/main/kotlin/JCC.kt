import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.command.parse.CommandCallParser
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.MessageChainBuilder
import net.mamoe.mirai.utils.info
import okhttp3.internal.indexOfNonWhitespace

object JCC : KotlinPlugin(
    JvmPluginDescription(
        id = "me.jie65535.jcc",
        name = "J Compiler Collection",
        version = "0.2",
    ) {
        author("jie65535")
        info("""在线编译器集合""")
    }
) {
    const val CMD_PREFIX = "jcc"
    const val MSG_MAX_LENGTH = 550

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
                    val code = if (index >= 0) {
                        msg.substring(index).trim()
                    } else {
                        return@reply "$CMD_PREFIX $language\n" + GlotAPI.getTemplateFile(language).content
                    }

                    try {
                        // subject.sendMessage("正在执行，请稍等...")
                        logger.info("请求执行代码")
                        val result = GlotAPI.runCode(language, code)
                        val builder = MessageChainBuilder()
                        var c = 0
                        if (result.stdout.isNotEmpty()) c++
                        if (result.stderr.isNotEmpty()) c++
                        if (result.error.isNotEmpty()) c++
                        val title = c >= 2
                        var msgLength = 0
                        if (subject is Group) {
                            builder.add(At(sender))
                            builder.add("\n")
                        }

                        if (c == 0) {
                            builder.add("没有任何结果呢~")
                        } else {
                            if (result.error.isNotEmpty()) {
                                builder.add("error:\n")
                                builder.add(result.error)
                                msgLength += result.error.length + 7
                            }
                            if (result.stdout.isNotEmpty()) {
                                if (title) builder.add("\nstdout:\n")
                                builder.add(result.stdout)
                                msgLength += result.stdout.length
                            }
                            if (result.stderr.isNotEmpty()) {
                                if (title) builder.add("\nstderr:\n")
                                builder.add(result.stderr)
                                msgLength += result.stderr.length
                            }
                        }
                        val messageChain = builder.build()
                        if (msgLength > MSG_MAX_LENGTH) {
                            val messageContent = messageChain.contentToString()
                            return@reply "消息内容过长，已贴到Pastebin：\n" + UbuntuPastebinHelper.paste(messageContent)
                        } else {
                            return@reply messageChain
                        }
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