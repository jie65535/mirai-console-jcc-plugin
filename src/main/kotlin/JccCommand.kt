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
            "在线运行代码指令:\n" +
            "$CMD_PREFIX <language> <code>\n" +
            "$CMD_PREFIX <language> <pastebinUrl> [stdin]\n" +
            "引用消息: $CMD_PREFIX <language> [stdin]\n" +
            "仓库地址：https://github.com/jie65535/mirai-console-jcc-plugin\n" +
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