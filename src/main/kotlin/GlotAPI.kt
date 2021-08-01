import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.security.InvalidKeyException

/**
 * # glot.io api 封装
 * [https://glot.io/] 是一个开源的在线运行代码的网站
 * 它提供了免费API供外界使用，API文档见 [https://github.com/glotcode/glot/blob/master/api_docs]
 * 本类是对该API文档的封装
 * 通过 [listLanguages] 获取支持在线运行的编程语言列表
 * ~~通过 [getVersion] 获取对应语言的最新版本请求地址~~
 * 通过 [getSupport] 判断指定编程语言是否支持
 * 通过 [getFilename] 来获取指定编程语言的文件名（runCode需要）
 * 以上接口均有缓存，仅首次获取不同数据时会发起请求。因此，首次运行可能较慢。
 * 通过 [runCode] 运行代码
 * 若觉得原版 [runCode] 使用复杂，还可以使用另一个更简单的重载 [runCode]
 * @suppress 注意，若传入不支持的语言，或者格式不正确，将无法正确识别
 * @author jie65535@github
 */
object GlotAPI {
    private const val URL = "https://glot.io/"
    private const val URL_NEW = "https://glot.io/new/"
    private const val URL_API = URL + "api/"
    private const val URL_LIST_LANGUAGES = URL_API + "run"
    // 运行代码需要api token，这是的我帐号申请的，可以在[https://glot.io/auth/page/simple/register]注册帐号
    private const val API_TOKEN = "074ef4a7-7a94-47f2-9891-85511ef1fb52"

    data class Language(val name: String, val url: String)
    data class CodeFile(val name: String, val content: String)

    data class RunCodeRequest(@Json(serializeNull = false) val stdin: String?,
                              @Json(serializeNull = false) val command: String?,
                              val files: List<CodeFile>)
    data class RunResult(val stdout: String, val stderr: String, val error: String)

    private var languages: List<Language>? = null
    private val filenames: MutableMap<String, String> = mutableMapOf()
    // val fileExtensions: Map<String, String> = mapOf("assembly" to "asm", "ats" to "dats", "bash" to "sh", "c" to "c", "clojure" to "clj", "cobol" to "cob", "coffeescript" to "coffee", "cpp" to "cpp", "crystal" to "cr", "csharp" to "cs", "d" to "d", "elixir" to "ex", "elm" to "elm", "erlang" to "erl", "fsharp" to "fs", "go" to "go", "groovy" to "groovy", "haskell" to "hs", "idris" to "idr", "java" to "java", "javascript" to "js", "julia" to "jl", "kotlin" to "kt", "lua" to "lua", "mercury" to "m", "nim" to "nim", "nix" to "nix", "ocaml" to "ml", "perl" to "pl", "php" to "php", "python" to "py", "raku" to "raku", "ruby" to "rb", "rust" to "rs", "scala" to "scala", "swift" to "swift", "typescript" to "ts", "plaintext" to "txt", )

    /**
     * 列出所有支持在线运行的语言（缓存）
     * @return 返回支持的语言列表 示例：
     * ```json
     * [
     *   {
     *     "name": "assembly",
     *     "url": "https://glot.io/api/run/assembly"
     *   },
     *   {
     *     "name": "c",
     *     "url": "https://glot.io/api/run/c"
     *   }
     * ]
     * ```
     */
    fun listLanguages(): List<Language> {
        if (languages == null) {
            languages = Klaxon().parseArray(HttpUtil.get(URL_LIST_LANGUAGES)) ?: throw Exception("未获取到任何数据")
        }
        return languages!!
    }

    /**
     * 检查是否支持该语言在线编译
     * @param language 编程语言名字（忽略大小写）
     * @return 是否支持
     */
    fun checkSupport(language: String): Boolean = listLanguages().any { it.name.equals(language, true) }

    /**
     * 获取编程语言请求地址，若不支持将会抛出异常
     * @param language 编程语言名字（忽略大小写）
     * @return 返回语言请求地址
     * @exception InvalidKeyException 不支持的语言
     */
    fun getSupport(language: String): Language =
        listLanguages().find { it.name.equals(language, true) } ?: throw InvalidKeyException("不支持的语言")

    /**
     * 获取指定编程语言文件名（缓存）
     * @exception Exception 若不支持或无法获取，将抛出异常
     * @return 建议文件名（通常是main.c之类的，java比较特殊，是Main.java，所以需要请求，避免硬编码）
     */
    fun getFilename(language: String): String {
        val lang = getSupport(language)
        if (filenames.containsKey(lang.name))
            return filenames[lang.name]!!
        val document = HttpUtil.getDocument(URL_NEW + lang.name)
        val filename = HttpUtil.documentSelect(document, ".filename").firstOrNull()?.text() ?: throw Exception("无法获取文件名")
        filenames[lang.name] = filename
        return filename
    }

    /**
     * # 运行代码
     *
     * ## 简单示例：
     * 请求
     * ```json
     * {
     *   "files": [
     *     {
     *       "name": "main.py",
     *       "content": "print(42)"
     *     }
     *   ]
     * }
     * ```
     * 响应
     * ```json
     * {
     *   "stdout": "42\n",
     *   "stderr": "",
     *   "error": ""
     * }
     * ```
     *
     * ## 读输入流示例：
     * 请求
     * ```json
     * {
     *   "stdin": "42",
     *   "files": [
     *     {
     *       "name": "main.py",
     *       "content": "print(input('Number from stdin: '))"
     *     }
     *   ]
     * }
     * ```
     * 响应
     * ```json
     * {
     *   "stdout": "Number from stdin: 42\n",
     *   "stderr": "",
     *   "error": ""
     * }
     * ```
     *
     * ## 自定义运行命令示例：
     * 请求
     * ```json
     * {
     *   "command": "bash main.sh 42",
     *   "files": [
     *     {
     *       "name": "main.sh",
     *       "content": "echo Number from arg: $1"
     *     }
     *   ]
     * }
     * ```
     * 响应
     * ```json
     * {
     *   "stdout": "Number from arg: 42\n",
     *   "stderr": "",
     *   "error": ""
     * }
     * ```
     * @param language 要运行的编程语言
     * @param requestData 运行代码的请求数据
     * @return 返回运行结果 若执行了死循环或其它阻塞代码，
     * 导致程序无法在限定时间内返回，将会报告超时异常
     */
    fun runCode(language: Language, requestData: RunCodeRequest): RunResult {
        val response = HttpUtil.post(language.url + "/latest", Klaxon().toJsonString(requestData), mapOf("Authorization" to API_TOKEN))
        return Klaxon().parse(response) ?: throw Exception("未获取到任何数据")
    }


    /**
     * # 运行代码
     * 更简单的运行代码重载
     * @param language 编程语言
     * @param code 程序代码
     * @param stdin 可选的输入缓冲区数据
     * @return 返回运行结果 若执行了死循环或其它阻塞代码，
     * 导致程序无法在限定时间内返回，将会报告超时异常
     */
    fun runCode(language: String, code: String, stdin: String? = null): RunResult =
        runCode(getSupport(language), RunCodeRequest(stdin, null, listOf(CodeFile(getFilename(language), code))))
}