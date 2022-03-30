import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

/**
 * # ubuntu pastebin 帮助类
 * [https://paste.ubuntu.com] 是一个用于共享文档的网站
 * 由于pastebin本身没有对外提供api，所以本类使用解析html的方式实现
 * 通过 [getSyntaxList] 获取支持的语法列表（缓存）
 * 通过 [get] 获取链接的内容
 * 通过 [paste] 来粘贴内容
 *
 * @author jie65535@github
 */
object UbuntuPastebinHelper {
    private const val URL = "https://paste.ubuntu.com"
    private var syntaxList: Map<String, String>? = null
    /**
     * 获取支持的语法列表（缓存）
     * @return 返回一个map，其中key是给人看的，value是作为参数传递的
     */
    @Deprecated("paste.ubuntu.com 现在需要登录 首页不再显示符号列表，因此该方法弃用")
    fun getSyntaxList(): Map<String, String> {
        if (syntaxList != null)
            return syntaxList!!
        val document = HttpUtil.getDocument(URL)
        val element = HttpUtil.documentSelect(document, "select#id_syntax > option")
        val map = mutableMapOf<String, String>()
        for (opt in element)
            map[opt.text()] = opt.`val`()
        syntaxList = map
        return map
    }

    fun checkUrl(url: String): Boolean {
        return url.startsWith("https://pastebin.ubuntu.com/p/") && url.endsWith('/') && url.length < 45
    }

    /**
     * 获取内容
     * @param url pastebin地址，如：https://paste.ubuntu.com/p/nmn8yKMtND/
     * @return 返回链接中贴的内容
     */
    fun get(url: String): String {
        if (!checkUrl(url))
            throw Exception("非法的url")
        val document = HttpUtil.getDocument(url)
        return HttpUtil.documentSelect(document, "#hidden-content").text()
    }

    /**
     * 上传内容
     * @param content 上传内容
     * @param syntax 语法（例如c/cpp） 可以通过getSyntaxList得到所有支持的语法，传入pair的value 默认值：text
     * @param poster 主题文本（最大长度30字符） 默认值："temp"
     * @param expiration 过期时间（(empty)/day/week/month/year） 默认值："day"
     * @return 返回访问地址，如：https://paste.ubuntu.com/p/nmn8yKMtND/
     */
    @Deprecated("paste.ubuntu.com 现在需要登录，因此不能再粘贴")
    fun paste(content: String, syntax: String = "text", poster: String = "temp", expiration: String = "day"): String {
        if (poster.length > 30)
            throw Exception("poster length too long!")
        if (content.isEmpty())
            throw Exception("content cannot be empty!")
        val okHttpClient = OkHttpClient().newBuilder()
            .followRedirects(false)
            .build()
        val requestBody = FormBody.Builder()
            .add("poster", poster)
            .add("syntax", syntax)
            .add("expiration", expiration)
            .add("content", content)
            .build()
        val request = Request.Builder()
            .url(URL)
            .post(requestBody)
            .build()
        val response = okHttpClient.newCall(request).execute()
        if (response.code == 200)
            throw Exception("请求已经成功，但无法执行动作，请检查参数")
        return if (response.code == 302)
            URL + response.header("Location")
        else
            throw IOException("请求失败，请检查网络或参数")
    }
}