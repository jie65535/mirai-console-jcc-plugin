# JCC - J Compiler Collection
## 基于Glot接口的mirai-console在线编译器插件

### 使用帮助
```
run <language> <code|pastebin> [stdin]
例如：run python print("Hello world")
支持从pastebin.ubuntu.com中运行代码：
run c https://pastebin.ubuntu.com/p/KhBB7ZjVbD/
你还可以在后面跟随标准输入（仅pastebin支持）：
run c https://pastebin.ubuntu.com/p/S2PyvRqJNf/ 1 2 3 4
其它指令：
/jcc help    # 帮助
/jcc list    # 列出所有支持的编程语言
/jcc template <language>    # 获取指定语言的模板
```

---

### 使用方法
本插件基于[Mirai-Console](https://github.com/mamoe/mirai-console)运行，您可以通过阅读[Mirai用户手册](https://docs.mirai.mamoe.net/UserManual.html)来了解如何安装、启动机器人。

`MiraiConsole`成功启动后，只需要将本项目[发布](https://github.com/jie65535/mirai-console-jcc-plugin/releases)的`.jar`文件放入`.\plugins\`目录下即可加载插件。

### 发布地址
本插件在[Mirai论坛](https://mirai.mamoe.net/)发布，[点击这里](https://mirai.mamoe.net/topic/463/jcc-%E5%9F%BA%E4%BA%8Emirai-console%E7%9A%84%E5%9C%A8%E7%BA%BF%E7%BC%96%E8%AF%91%E6%8F%92%E4%BB%B6)查看

### 反馈
如使用或安装插件过程中遇到非本插件功能问题，您首先应该在[Mirai论坛](https://mirai.mamoe.net/)中搜索解决方案，若未解决，可以在[本项目的主题贴](https://mirai.mamoe.net/topic/463/jcc-%E5%9F%BA%E4%BA%8Emirai-console%E7%9A%84%E5%9C%A8%E7%BA%BF%E7%BC%96%E8%AF%91%E6%8F%92%E4%BB%B6)中回帖提问。

如果是插件本身的问题或漏洞，您可以向我提交一个[issue](https://github.com/jie65535/mirai-console-jcc-plugin/issues)。若您有能力且愿意帮助我修复这些问题，请提交[Pull request](https://github.com/jie65535/mirai-console-jcc-plugin/pulls)。
