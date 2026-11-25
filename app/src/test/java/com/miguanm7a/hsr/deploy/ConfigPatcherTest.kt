package com.miguanm7a.hsr.deploy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ConfigPatcher 回归测试。
 *
 * 核心不变式：**只改我们管的顶层键，其它内容一个字节都不能动**。
 *
 * 背景：March7thAssistant 会把整份合并配置 dump 回 config.yaml，
 * 里面有大量状态字段（`*_timestamp`、`daily_tasks`、`power_plan` 等）。
 * 整份覆盖会把这些状态清零，导致程序重复执行「每天/每周一次」的任务。
 */
class ConfigPatcherTest {

    /** 模拟程序运行过一轮之后、包含状态字段的真实 config.yaml 片段。 */
    private val realWorldConfig = """
        locales: zh_CN
        log_level: INFO # 日志等级
        after_finish: None # 任务完成后的操作
        power_enable: true # 是否启用清体力总开关
        echo_of_war_timestamp: 1730000000 # 上次完成历战余响的时间戳
        echo_of_war_enable: false
        weekly_relic_cleanup_timestamp: 1730000001
        last_run_timestamp: 1730000002
        daily_tasks: []
        power_plan:
        - stage: 拟造花萼（金）
          times: 6
        reward_enable: true
        check_update: true
        loop_mode: scheduled
        scheduled_time: 4:00
    """.trimIndent() + "\n"

    @Test
    fun `updates only the requested keys`() {
        val out = ConfigPatcher.upsertTopLevel(
            realWorldConfig,
            mapOf("power_enable" to "false", "check_update" to "false"),
        )

        assertTrue("power_enable 应被改为 false", out.contains("power_enable: false"))
        assertTrue("check_update 应被改为 false", out.contains("check_update: false"))
        assertFalse("不应残留 power_enable: true", out.contains("power_enable: true"))
    }

    /** 这是最重要的断言：状态字段必须原样保留。 */
    @Test
    fun `state fields and nested structures are preserved`() {
        val out = ConfigPatcher.upsertTopLevel(
            realWorldConfig,
            mapOf("reward_enable" to "false"),
        )

        assertTrue(out.contains("echo_of_war_timestamp: 1730000000"))
        assertTrue(out.contains("weekly_relic_cleanup_timestamp: 1730000001"))
        assertTrue(out.contains("last_run_timestamp: 1730000002"))
        assertTrue(out.contains("daily_tasks: []"))
        // 多行嵌套（power_plan）必须完整保留
        assertTrue(out.contains("power_plan:"))
        assertTrue(out.contains("- stage: 拟造花萼（金）"))
        assertTrue(out.contains("times: 6"))
        // 行尾注释尽量保留
        assertTrue("注释应保留", out.contains("# 是否启用清体力总开关"))
    }

    @Test
    fun `missing keys are appended`() {
        val out = ConfigPatcher.upsertTopLevel(
            "log_level: INFO\n",
            mapOf("exit_after_failure" to "true", "use_fuel" to "true"),
        )
        assertTrue(out.contains("exit_after_failure: true"))
        assertTrue(out.contains("use_fuel: true"))
        assertTrue("原有键不变", out.contains("log_level: INFO"))
        assertTrue("应以换行结尾", out.endsWith("\n"))
    }

    /** 嵌套里的同名键**不能**被误改（只匹配不缩进的顶层键）。 */
    @Test
    fun `indented keys with same name are untouched`() {
        val yaml = """
            power_enable: true
            some_section:
              power_enable: true
              nested:
                power_enable: true
        """.trimIndent() + "\n"

        val out = ConfigPatcher.upsertTopLevel(yaml, mapOf("power_enable" to "false"))

        val occurrences = Regex("power_enable:").findAll(out).count()
        assertEquals("应仍有 3 处 power_enable", 3, occurrences)
        assertTrue("顶层被改为 false", out.contains("\npower_enable: false") ||
            out.startsWith("power_enable: false"))
        assertTrue("缩进的 2 处保持 true",
            out.lines().count { it.trim() == "power_enable: true" } == 2)
    }

    /** 前缀相近的键名不能被误伤。 */
    @Test
    fun `similar key names are not confused`() {
        val yaml = """
            reward_enable: true
            reward_enable_extra: true
            reward_enableX: true
        """.trimIndent() + "\n"

        val out = ConfigPatcher.upsertTopLevel(yaml, mapOf("reward_enable" to "false"))

        assertTrue(out.contains("reward_enable: false"))
        assertTrue("reward_enable_extra 不应被改", out.contains("reward_enable_extra: true"))
        assertTrue("reward_enableX 不应被改", out.contains("reward_enableX: true"))
    }

    @Test
    fun `empty input produces just the keys`() {
        val out = ConfigPatcher.upsertTopLevel("", mapOf("a" to "true", "b" to "\"x\""))
        assertTrue(out.contains("a: true"))
        assertTrue(out.contains("b: \"x\""))
    }

    @Test
    fun `no values means unchanged`() {
        assertEquals(realWorldConfig, ConfigPatcher.upsertTopLevel(realWorldConfig, emptyMap()))
    }

    /** 写入的键全部来自配置模板 / 上游真实键名。 */
    @Test
    fun `every toggle key matches upstream naming`() {
        // 上游 config.example.yaml 里的键只含小写字母、数字和下划线
        val re = Regex("^[a-z0-9_]+$")
        for (t in TaskToggles.ALL) {
            assertTrue("键名格式异常: ${t.key}", re.matches(t.key))
        }
        // 不应有重复键
        val dup = TaskToggles.ALL.groupBy { it.key }.filterValues { it.size > 1 }.keys
        assertTrue("存在重复键: $dup", dup.isEmpty())
        assertTrue("开关数量应在合理范围", TaskToggles.ALL.size >= 20)
    }

    /** 应用全部开关的默认值应是幂等的（写回等于原值）。 */
    @Test
    fun `applying defaults is a no-op on values`() {
        val yaml = TaskToggles.ALL.joinToString("\n") { "${it.key}: ${it.defaultValue}" } + "\n"
        val out = ConfigPatcher.upsertTopLevel(
            yaml,
            TaskToggles.defaults().mapValues { (_, v) -> v.toString() },
        )
        assertEquals(yaml, out)
    }

    // ------------------------------------------------------------------
    // 块状映射：清体力副本（instance_names / instance_names_challenge_count）
    // ------------------------------------------------------------------

    /** 模拟运行过一轮后的配置：两个块状映射 + 后面的顶层键。 */
    private val instanceConfig = """
        instance_type: 拟造花萼（金）
        instance_names:
          拟造花萼（金）: 回忆之蕾
          凝滞虚影: 无
        instance_names_challenge_count:
          拟造花萼（金）: 24
          凝滞虚影: 8
        tp_before_instance: false
    """.trimIndent() + "\n"

    @Test
    fun `nested mapping replaces the whole block`() {
        val out = ConfigPatcher.upsertNestedMapping(
            instanceConfig,
            "instance_names",
            linkedMapOf("拟造花萼（金）" to "以太之蕾", "侵蚀隧洞" to "睿治之径"),
        )
        assertTrue("新值应写入", out.contains("  拟造花萼（金）: 以太之蕾"))
        assertTrue(out.contains("  侵蚀隧洞: 睿治之径"))
        assertFalse("旧子项必须被丢弃", out.contains("凝滞虚影: 无"))
        assertTrue("兄弟块不能被误删", out.contains("  凝滞虚影: 8"))
        assertTrue("后面的顶层键必须保留", out.contains("tp_before_instance: false"))
        assertTrue("头部注释/前序键不变", out.contains("instance_type: 拟造花萼（金）"))
    }

    @Test
    fun `nested mapping is appended when the key is absent`() {
        val out = ConfigPatcher.upsertNestedMapping(
            "log_level: INFO\n",
            "instance_names",
            linkedMapOf("侵蚀隧洞" to "睿治之径"),
        )
        assertTrue(out.contains("instance_names:"))
        assertTrue(out.contains("  侵蚀隧洞: 睿治之径"))
        assertTrue("原有键不变", out.contains("log_level: INFO"))
        assertTrue("应以换行结尾", out.endsWith("\n"))
    }

    /** 上游模板里可能是 `instance_names: {}` 这种同行写法，也要能替换。 */
    @Test
    fun `inline block value is replaced`() {
        val out = ConfigPatcher.upsertNestedMapping(
            "instance_names: {}\nfoo: 1\n",
            "instance_names",
            linkedMapOf("a" to "b"),
        )
        assertTrue(out.contains("instance_names:\n  a: b"))
        assertFalse("同行旧值不能残留", out.contains("instance_names: {}"))
        assertTrue(out.contains("foo: 1"))
    }

    /** 副本目录本身的自洽性：默认关卡必须在该类型的可选列表里。 */
    @Test
    fun `instance catalog defaults are consistent`() {
        assertTrue(InstanceCatalog.TYPES.isNotEmpty())
        for (t in InstanceCatalog.TYPES) {
            assertTrue("${t.name} 的默认关卡不在列表里", t.defaultName in t.names)
            assertTrue("${t.name} 的默认次数必须为正", t.defaultCount > 0)
            assertTrue("${t.name} 至少要有两个选项", t.names.size >= 2)
        }
        assertEquals(InstanceCatalog.TYPES.first().name, InstanceCatalog.defaultType())
    }
}
