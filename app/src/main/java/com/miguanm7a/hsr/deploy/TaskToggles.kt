package com.miguanm7a.hsr.deploy

/** 开关分组（设置页用它分节显示）。 */
enum class ToggleGroup(val label: String) {
    REWARD("领取奖励"),
    DAILY("日常任务"),
    ACTIVITY("活动"),
    POWER("清体力 / 体力"),
    AUTOPLOT("自动剧情与战斗"),
    RUNTIME("运行行为"),
}

/**
 * 一个可配置的任务开关。
 *
 * `key` **必须与** `assets/config/config.example.yaml` 里的顶层键完全一致：
 * March7thAssistant 的 `Config._update_config()` 只覆盖已存在的键，
 * 未知键会被静默忽略（早期版本写错 `run_daily_time` 就一直没生效）。
 *
 * `defaultValue` 取自 `config.example.yaml` 的实测值，
 * 这样「没动过」的开关写回去等于原值，不会改变上游默认行为。
 *
 * `description` 只在需要解释时写；与标题重复的一律留空，界面更紧凑。
 */
data class TaskToggle(
    val key: String,
    val label: String,
    val description: String,
    val group: ToggleGroup,
    val defaultValue: Boolean,
)

object TaskToggles {

    val ALL: List<TaskToggle> = listOf(
        // ---------------- 领取奖励 ----------------
        TaskToggle(
            "reward_enable", "领取奖励（总开关）", "关闭后跳过全部领奖",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_dispatch_enable", "委托奖励", "",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_mail_enable", "邮件奖励", "",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_assist_enable", "支援奖励", "",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_quest_enable", "每日实训奖励", "",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_srpass_enable", "无名勋礼", "",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_redemption_code_enable", "兑换码", "",
            ToggleGroup.REWARD, false,
        ),
        TaskToggle(
            "reward_achievement_enable", "成就奖励", "",
            ToggleGroup.REWARD, false,
        ),
        TaskToggle(
            "reward_message_enable", "短信奖励", "",
            ToggleGroup.REWARD, false,
        ),

        // ---------------- 日常任务 ----------------
        TaskToggle(
            "daily_enable", "日常任务", "500 活跃度那套",
            ToggleGroup.DAILY, true,
        ),
        TaskToggle(
            "daily_material_enable", "用「合成材料」完成", "",
            ToggleGroup.DAILY, true,
        ),
        TaskToggle(
            "daily_himeko_try_enable", "用「姬子试用」完成", "",
            ToggleGroup.DAILY, false,
        ),
        TaskToggle(
            "daily_memory_one_enable", "用「回忆一」完成", "需先配好队伍",
            ToggleGroup.DAILY, false,
        ),

        // ---------------- 活动 ----------------
        TaskToggle(
            "activity_enable", "活动任务", "",
            ToggleGroup.ACTIVITY, true,
        ),
        TaskToggle(
            "activity_dailycheckin_enable", "每日签到", "",
            ToggleGroup.ACTIVITY, true,
        ),
        TaskToggle(
            "activity_gardenofplenty_enable", "花藏繁生活动", "",
            ToggleGroup.ACTIVITY, false,
        ),
        TaskToggle(
            "activity_planarfissure_enable", "位面分裂活动", "",
            ToggleGroup.ACTIVITY, false,
        ),
        TaskToggle(
            "activity_realmofthestrange_enable", "异器盈界活动", "",
            ToggleGroup.ACTIVITY, false,
        ),

        // ---------------- 清体力 ----------------
        TaskToggle(
            "power_enable", "清体力（总开关）", "关闭后完整运行跳过清体力",
            ToggleGroup.POWER, true,
        ),
        TaskToggle(
            "echo_of_war_enable", "历战余响", "体力优先刷 3 次",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "use_reserved_trailblaze_power", "使用后备开拓力", "",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "use_fuel", "使用燃料", "",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "merge_immersifier", "优先合成沉浸器", "先合成再打副本",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "borrow_enable", "使用支援角色", "",
            ToggleGroup.POWER, true,
        ),
        TaskToggle(
            "borrow_character_enable", "强制使用支援角色", "每场都借支援",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "instance_team_enable", "自动切换队伍", "按编号自动切队伍",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "tp_before_instance", "进本前先传送", "",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "relic_bag_cleanup_enable", "遗器满包自动分解", "满包时分解低星遗器",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "prefer_fuel_tickets", "优先用助燃券", "上游实验功能",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "power_plan_continue", "计划完成后刷默认副本", "剩余体力继续刷",
            ToggleGroup.POWER, true,
        ),
        TaskToggle(
            "power_plan_keep", "执行后保留体力计划", "不删已完成的计划条目",
            ToggleGroup.POWER, false,
        ),

        // ---------------- 自动剧情与战斗 ----------------
        TaskToggle(
            "auto_battle_detect_enable", "自动战斗 + 二倍速", "开局自动战斗并切二倍速",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_skip_enable", "自动跳过对话", "",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_click_enable", "自动选择对话选项", "",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_battle_detect_enable", "自动战斗检测", "未自动战斗时补按 V",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_phone_detect_enable", "自动处理短信页面", "",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_enable", "自动对话总开关", "记录上次开关状态",
            ToggleGroup.AUTOPLOT, false,
        ),
        TaskToggle(
            "autoplot_hero_chronicle_enable", "自动读英雄纪", "",
            ToggleGroup.AUTOPLOT, false,
        ),
        TaskToggle(
            "autoplot_remember_enable", "记住自动对话状态", "启动时恢复上次状态",
            ToggleGroup.AUTOPLOT, false,
        ),

        // ---------------- 运行行为 ----------------
        TaskToggle(
            "check_update", "启动时检查更新", "",
            ToggleGroup.RUNTIME, true,
        ),
        TaskToggle(
            "pause_after_success", "成功后暂停", "仅成功且非循环时",
            ToggleGroup.RUNTIME, true,
        ),
        TaskToggle(
            "exit_after_failure", "失败后直接退出", "",
            ToggleGroup.RUNTIME, false,
        ),
    )

    /** 「自动战斗 + 二倍速」在 config.yaml 里的键名，界面提示用得到。 */
    const val BATTLE_DETECT_KEY = "auto_battle_detect_enable"

    /** 「历战余响」开关的键名，界面用它把关卡选择挂在该开关下面。 */
    const val ECHO_KEY = "echo_of_war_enable"

    /** 某一分组的全部开关，顺序与 [ALL] 一致。 */
    fun forGroup(group: ToggleGroup): List<TaskToggle> = ALL.filter { it.group == group }

    private val BY_KEY = ALL.associateBy { it.key }

    fun byKey(key: String): TaskToggle? = BY_KEY[key]

    /** 全部键的默认值。 */
    fun defaults(): Map<String, Boolean> = ALL.associate { it.key to it.defaultValue }
}
