import JCC.CMD_PREFIX
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand

object JccCommand : CompositeCommand(
    JCC, "jcc",
    description = "在线编译器集合"
) {
    @SubCommand
    @Description("列出所有支持的编程语言")
    suspend fun CommandSender.list() {
        try {
            sendMessage(GlotAPI.listLanguages().joinToString { it.name })
        } catch (e: Exception) {
            sendMessage("执行失败\n${e.message}")
            JCC.logger.warning(e)
        }
    }

    @SubCommand
    @Description("帮助")
    suspend fun CommandSender.help() {
        sendMessage("直接调用${CMD_PREFIX}即可运行代码\n例如：${CMD_PREFIX} python print(\"Hello world\")\n其它指令：\n$usage")
    }

    @SubCommand
    @Description("获取指定语言的模板")
    suspend fun CommandSender.template(language: String) {
        if (!GlotAPI.checkSupport(language)) {
            sendMessage("不支持该语言，请使用/jcc list列出所有支持的编程语言")
            return
        }
        val file = GlotAPI.getTemplateFile(language)
        sendMessage("$CMD_PREFIX $language\n" + file.content)
    }
}