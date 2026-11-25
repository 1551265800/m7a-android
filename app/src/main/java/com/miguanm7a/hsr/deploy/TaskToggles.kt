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
            "reward_enable", "领取奖励（总开关）",
            "关闭后下面所有领取奖励都会跳过；对单独执行的任务无效，只影响完整运行/日常",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_dispatch_enable", "委托奖励", "领取派遣委托奖励",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_mail_enable", "邮件奖励", "领取邮件附件",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_assist_enable", "支援奖励", "领取支援奖励",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_quest_enable", "每日实训奖励", "领取每日实训达成奖励",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_srpass_enable", "无名勋礼", "领取无名勋礼奖励",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_redemption_code_enable", "兑换码", "自动领取兑换码奖励",
            ToggleGroup.REWARD, true,
        ),
        TaskToggle(
            "reward_achievement_enable", "成就奖励", "领取成就奖励（默认关闭）",
            ToggleGroup.REWARD, false,
        ),
        TaskToggle(
            "reward_message_enable", "短信奖励", "领取短信奖励（默认关闭）",
            ToggleGroup.REWARD, false,
        ),

        // ---------------- 日常任务 ----------------
        TaskToggle(
            "daily_enable", "日常任务", "每日实训（500 活跃度那套）",
            ToggleGroup.DAILY, true,
        ),
        TaskToggle(
            "daily_material_enable", "用「合成材料」完成",
            "通过合成材料完成每日实训",
            ToggleGroup.DAILY, true,
        ),
        TaskToggle(
            "daily_himeko_try_enable", "用「姬子试用」完成",
            "通过姬子试用完成每日实训（默认关闭）",
            ToggleGroup.DAILY, false,
        ),
        TaskToggle(
            "daily_memory_one_enable", "用「回忆一」完成",
            "通过回忆一完成每日实训（默认关闭，需配置队伍）",
            ToggleGroup.DAILY, false,
        ),

        // ---------------- 活动 ----------------
        TaskToggle(
            "activity_enable", "活动任务", "执行限时活动相关任务",
            ToggleGroup.ACTIVITY, true,
        ),
        TaskToggle(
            "activity_dailycheckin_enable", "每日签到", "活动每日签到",
            ToggleGroup.ACTIVITY, true,
        ),

        // ---------------- 清体力 ----------------
        TaskToggle(
            "power_enable", "清体力（总开关）",
            "关闭后完整运行会跳过清体力；单独执行「清体力」任务不受影响",
            ToggleGroup.POWER, true,
        ),
        TaskToggle(
            "echo_of_war_enable", "历战余响",
            "体力优先完成 3 次「历战余响」（默认关闭）",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "use_reserved_trailblaze_power", "使用后备开拓力",
            "允许消耗后备开拓力（默认关闭）",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "use_fuel", "使用燃料",
            "允许消耗燃料补充体力（默认关闭）",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "merge_immersifier", "优先合成沉浸器",
            "先合成沉浸器再打副本（默认关闭）",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "borrow_enable", "使用支援角色",
            "打副本时允许借用好友支援角色",
            ToggleGroup.POWER, true,
        ),
        TaskToggle(
            "instance_team_enable", "自动切换队伍",
            "打副本前自动切换到指定队伍（默认关闭，需在 config 里配队伍编号）",
            ToggleGroup.POWER, false,
        ),

        // ---------------- 自动剧情与战斗 ----------------
        TaskToggle(
            "auto_battle_detect_enable", "自动战斗 + 二倍速",
            "启动云游戏后自动开启游戏内自动战斗并把战斗速度切到二倍速；" +
                "上游用 game_optimization_enable 与它做「与」判断，任一为假都不生效",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_skip_enable", "自动跳过对话", "出现跳过按钮时自动点击",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_click_enable", "自动选择对话选项", "按素材选择剧情选项",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_battle_detect_enable", "自动战斗检测",
            "检测到未自动战斗时按 V 开启",
            ToggleGroup.AUTOPLOT, true,
        ),
        TaskToggle(
            "autoplot_phone_detect_enable", "自动处理短信页面", "自动处理游戏内短信界面",
            ToggleGroup.AUTOPLOT, true,
        ),

        // ---------------- 清体力（补充项） ----------------
        TaskToggle(
            "tp_before_instance", "进本前先传送",
            "进入副本前先传送到任意锚点，可减少「副本入口未解锁」类失败",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "relic_bag_cleanup_enable", "遗器满包自动分解",
            "刷副本遇到遗器背包满时，自动分解可清理的低星遗器",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "borrow_character_enable", "强制使用支援角色",
            "开启后每场战斗都借用支援角色，而不是有就用",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "prefer_fuel_tickets", "优先用助燃券",
            "先用助燃券快速刷花萼 / 凝滞虚影（上游实验功能）",
            ToggleGroup.POWER, false,
        ),
        TaskToggle(
            "power_plan_continue", "计划完成后刷默认副本",
            "整份体力计划执行完后，用剩余体力继续刷当前选中副本",
            ToggleGroup.POWER, true,
        ),
        TaskToggle(
            "power_plan_keep", "执行后保留体力计划",
            "关闭时按剩余次数更新并删除已完成的计划条目",
            ToggleGroup.POWER, false,
        ),

        // ---------------- 活动 ----------------
        TaskToggle(
            "activity_gardenofplenty_enable", "花藏繁生活动",
            "活动期间自动完成花藏繁生的关卡",
            ToggleGroup.ACTIVITY, false,
        ),
        TaskToggle(
            "activity_planarfissure_enable", "位面分裂活动",
            "活动期间自动完成位面分裂",
            ToggleGroup.ACTIVITY, false,
        ),
        TaskToggle(
            "activity_realmofthestrange_enable", "异器盈界活动",
            "活动期间自动完成异器盈界",
            ToggleGroup.ACTIVITY, false,
        ),

        // ---------------- 自动剧情与战斗（补充项） ----------------
        TaskToggle(
            "autoplot_enable", "自动对话总开关",
            "配合「记住自动对话状态」使用，记录上一次的开关状态",
            ToggleGroup.AUTOPLOT, false,
        ),
        TaskToggle(
            "autoplot_hero_chronicle_enable", "自动读英雄纪",
            "自动阅读「如我所书」中的翁法罗斯英雄纪",
            ToggleGroup.AUTOPLOT, false,
        ),
        TaskToggle(
            "autoplot_remember_enable", "记住自动对话状态",
            "下次启动时恢复上次的自动对话开关，可能增加后台资源占用",
            ToggleGroup.AUTOPLOT, false,
        ),

        // ---------------- 运行行为 ----------------
        TaskToggle(
            "check_update", "启动时检查更新", "检查 March7thAssistant 自身更新",
            ToggleGroup.RUNTIME, true,
        ),
        TaskToggle(
            "pause_after_success", "成功后暂停", "成功且非循环执行时暂停程序",
            ToggleGroup.RUNTIME, true,
        ),
        TaskToggle(
            "exit_after_failure", "失败后直接退出", "失败时直接退出而不是暂停",
            ToggleGroup.RUNTIME, false,
        ),
    )

    /** 「自动战斗 + 二倍速」在 config.yaml 里的键名，界面提示用得到。 */
    const val BATTLE_DETECT_KEY = "auto_battle_detect_enable"

    /** 某一分组的全部开关，顺序与 [ALL] 一致。 */
    fun forGroup(group: ToggleGroup): List<TaskToggle> = ALL.filter { it.group == group }

    private val BY_KEY = ALL.associateBy { it.key }

    fun byKey(key: String): TaskToggle? = BY_KEY[key]

    /** 全部键的默认值。 */
    fun defaults(): Map<String, Boolean> = ALL.associate { it.key to it.defaultValue }
}
