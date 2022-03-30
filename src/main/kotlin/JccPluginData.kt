import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

object JccPluginData: AutoSavePluginData("GlotCache") {

    /**
     * 支持的语言
     */
    var languages: List<GlotAPI.Language> by value()

    /**
     * 模板文件
     */
    val templateFiles: MutableMap<String, GlotAPI.CodeFile> by value()
}