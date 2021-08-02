import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.File
import java.util.concurrent.TimeUnit


object HttpUtil {
    private val JSON: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    /**
     * ### 下载图片
     */
    fun downloadImage(url: String, file: File): ByteArray {
        val request = Request.Builder().url(url).build()
        val imageByte = okHttpClient.newCall(request).execute().body!!.bytes()
        val fileParent = file.parentFile
        if (!fileParent.exists()) fileParent.mkdirs()
        file.writeBytes(imageByte)
        return imageByte
    }

    /**
     * ### 发送GET请求
     */
    fun get(url: String): String {
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }

    /**
     * ### 发送带Json参数的POST请求
     */
    fun post(url: String, json: String): String {
        val requestBody = json.toRequestBody(JSON)
        val request = Request.Builder().url(url).post(requestBody).build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }
    /**
     * ### 发送带Header与Json参数的POST请求
     */
    fun post(url: String, json: String, params: Map<String, String>): String {
        val requestBody = json.toRequestBody(JSON)
        val requestBuilder = Request.Builder().url(url)
        for (param in params)
            requestBuilder.addHeader(param.key, param.value)
        val request = requestBuilder.post(requestBody).build()
        return okHttpClient.newCall(request).execute().body!!.string()
    }

    /**
     * ### 解析网页响应
     */
    fun parseBody(responseBody: String): Document {
        return Jsoup.parse(responseBody)
    }

    /**
     * ### 发送GET请求并解析
     */
    fun getDocument(url: String): Document {
        return parseBody(get(url))
    }

    /**
     * ### Document 元素选择
     */
    fun documentSelect(document: Document, cssQuery: String): Elements {
        return document.select(cssQuery)
    }
}