import JCompilerCollection.CMD_PREFIX
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand

object JccCommand : CompositeCommand(
    JCompilerCollection, "jcc",
    description = "在线编译器集合"
) {
    @SubCommand
    @Description("列出所有支持的编程语言")
    suspend fun CommandSender.list() {
        try {
            sendMessage(GlotAPI.listLanguages().joinToString { it.name })
        } catch (e: Exception) {
            sendMessage("执行失败\n${e.message}")
            JCompilerCollection.logger.warning(e)
        }
    }

    @SubCommand
    @Description("帮助")
    suspend fun CommandSender.help() {
        sendMessage(
            "$CMD_PREFIX <language> <code|pastebin> [stdin]\n" +
            "例如：${CMD_PREFIX} python print(\"Hello world\")\n" +
            "支持从pastebin.ubuntu.com中运行代码：\n" +
            "$CMD_PREFIX c https://pastebin.ubuntu.com/p/KhBB7ZjVbD/\n" +
            "你还可以在后面跟随标准输入（仅pastebin支持）：\n" +
            "$CMD_PREFIX c https://pastebin.ubuntu.com/p/S2PyvRqJNf/ 1 2 3 4\n" +
            "其它指令：\n" +
            usage
        )
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