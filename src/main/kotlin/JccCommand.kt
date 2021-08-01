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
        sendMessage("直接调用jcc即可运行代码\n例如：jcc python print(\"Hello world\")\n其它指令：\n$usage")
    }
}