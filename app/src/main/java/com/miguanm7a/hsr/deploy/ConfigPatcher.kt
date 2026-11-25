package com.miguanm7a.hsr.deploy

/**
 * 按顶层键精准修改 YAML，**保留文件里其它所有内容**。
 *
 * ## 为什么必须这么做（而不是整份重写）
 *
 * March7thAssistant 的 `module/config/config.py` 会 `yaml.dump(self.config)`
 * 把**整份合并后的配置**写回 `config.yaml`。首次运行之后，文件里就包含了
 * 全部键，包括大量**状态**字段：
 *
 * ```yaml
 * echo_of_war_timestamp: 0
 * weekly_relic_cleanup_timestamp: 0
 * universe_timestamp: 0
 * ...
 * daily_tasks: []
 * power_plan: []
 * ```
 *
 * 如果我整份覆盖，等于把这些状态全部清零 —— 程序会以为「历战余响没打过」
 * 「每周遗器没清过」而重复执行，用户手填的 `daily_tasks` 也会丢。
 *
 * 另外 `config.py` 的 `_update_config()` 只覆盖**已存在于默认配置中的键**：
 * ```python
 * for key, value in new_config.items():
 *     if key in config:      # ← 未知键被静默忽略
 * ```
 * 所以键名必须与 `assets/config/config.example.yaml` 完全一致，
 * 拼错一个字母就等于没写（早期版本写的 `run_daily_time` 就是这样失效的）。
 */
object ConfigPatcher {

    /** 只匹配不缩进的顶层键，例如 `power_enable:`。 */
    private val TOP_LEVEL_KEY = Regex("^([A-Za-z0-9_]+)\\s*:(.*)$")

    /**
     * 更新（或追加）指定的顶层键。
     *
     * @param yaml 原始内容
     * @param values 键 -> 已格式化好的 YAML 标量（如 `"true"`、`"\"04:00\""`）
     * @return 修改后的内容；其它行（含注释、嵌套结构、状态字段）原样保留
     */
    fun upsertTopLevel(yaml: String, values: Map<String, String>): String {
        if (values.isEmpty()) return yaml

        val remaining = LinkedHashMap(values)
        val out = ArrayList<String>()
        val lines = yaml.split("\n")

        for (raw in lines) {
            val m = TOP_LEVEL_KEY.matchEntire(raw)
            if (m == null) {
                out.add(raw)
                continue
            }
            val key = m.groupValues[1]
            val newValue = remaining.remove(key)
            if (newValue == null) {
                out.add(raw)
                continue
            }
            // 尽量保留该行原有的行尾注释
            val comment = trailingComment(raw)
            out.add("$key: $newValue$comment")
        }

        // 追加文件中不存在的键
        if (remaining.isNotEmpty()) {
            // 去掉末尾空行产生的尾部空白，再统一补一个换行
            while (out.isNotEmpty() && out.last().isBlank()) out.removeAt(out.size - 1)
            out.add("")
            remaining.forEach { (k, v) -> out.add("$k: $v") }
        }

        val text = out.joinToString("\n")
        return if (text.endsWith("\n")) text else "$text\n"
    }

    /**
     * 更新一个**块状映射**，形如：
     *
     * ```yaml
     * instance_names:
     *   拟造花萼（金）: 回忆之蕾
     *   侵蚀隧洞: 睿治之径
     * ```
     *
     * [upsertTopLevel] 只替换单行，遇到这种跨行的块会把旧子项留在文件里，
     * 拼出非法 YAML（或让上游读到旧值），所以单独处理：
     * 写完新的 `key:` + 子项后，把紧跟其后的缩进行 / `- ` 行整段丢掉。
     *
     * @param entries 子键 -> 已格式化好的 YAML 标量（注意中文键可以直接写）
     */
    fun upsertNestedMapping(yaml: String, key: String, entries: Map<String, String>): String {
        if (entries.isEmpty()) return yaml

        val header = Regex("^$key\\s*:(.*)$")
        val lines = yaml.split("\n")
        val out = ArrayList<String>(lines.size + entries.size + 2)
        var i = 0
        var replaced = false

        while (i < lines.size) {
            val raw = lines[i]
            if (header.matches(raw)) {
                out.add("$key:")
                entries.forEach { (k, v) -> out.add("  $k: $v") }
                replaced = true
                i++
                // 丢弃旧块：缩进行、制表符行、以及列首的 "- " 列表项
                while (i < lines.size) {
                    val l = lines[i]
                    val belongs = l.isBlank() || l.startsWith(" ") || l.startsWith("\t") || l.startsWith("- ")
                    if (!belongs) break
                    i++
                }
                continue
            }
            out.add(raw)
            i++
        }

        if (!replaced) {
            while (out.isNotEmpty() && out.last().isBlank()) out.removeAt(out.size - 1)
            out.add("")
            out.add("$key:")
            entries.forEach { (k, v) -> out.add("  $k: $v") }
        }

        val text = out.joinToString("\n")
        return if (text.endsWith("\n")) text else "$text\n"
    }

    /**
     * 取出行尾注释（` # ...`），没有则返回空串。
     *
     * 前提：我们写入的标量本身不含 ` #`，所以按第一个 ` #` 切分是安全的。
     */
    private fun trailingComment(line: String): String {
        val idx = line.indexOf(" #")
        return if (idx < 0) "" else line.substring(idx)
    }
}
