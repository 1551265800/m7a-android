package com.miguanm7a.hsr.deploy

/**
 * 清体力可选副本目录。
 *
 * 数据来自上游 `assets/config/instance_names.json`（v2026.11 镜像），
 * 只是把「类型 → 名称列表」搬成 Kotlin 常量，避免运行时解析 JSON。
 * 上游新增副本时同步更新这个文件即可。
 */
object InstanceCatalog {

    /** 一个副本类型：可选名称、上游默认名称、默认连续挑战次数。 */
    data class Type(
        val name: String,
        val names: List<String>,
        val defaultName: String,
        val defaultCount: Int,
    )

    val TYPES: List<Type> = listOf(
        Type(
            name = "拟造花萼（金）",
            names = listOf(
                "无", "回忆之蕾", "以太之蕾", "藏珍之蕾",
            ),
            defaultName = "回忆之蕾",
            defaultCount = 24,
        ),
        Type(
            name = "拟造花萼（赤）",
            names = listOf(
                "无", "收容舱段", "支援舱段", "城郊雪原",
                "边缘通路", "铆钉镇", "机械聚落", "大矿区",
                "鳞渊境", "丹鼎司", "「白日梦」酒店-梦境", "绥园",
                "克劳克影视乐园", "苏乐达™热砂海选会场", "匹诺康尼大剧院", "「纷争荒墟」悬锋城",
                "「世界尽头」酒馆", "「葬忆彼岸」时光归墟", "「辉痕圣林」神悟树庭", "海原电视塔",
                "渡画泉隐",
            ),
            defaultName = "收容舱段",
            defaultCount = 24,
        ),
        Type(
            name = "凝滞虚影",
            names = listOf(
                "无", "空海之形", "巽风之形", "鸣雷之形",
                "炎华之形", "锋芒之形", "霜晶之形", "幻光之形",
                "冰棱之形", "震厄之形", "偃偶之形", "孽兽之形",
                "天人之形", "幽府之形", "燔灼之形", "冰酿之形",
                "焦炙之形", "嗔怒之形", "职司之形", "机狼之形",
                "今宵之形", "弦音之形", "凛月之形", "役轮之形",
                "溟簇之形", "烬日之形", "塞壬之形", "残灰之形",
                "音爆之形", "吞噬之形",
            ),
            defaultName = "无",
            defaultCount = 8,
        ),
        Type(
            name = "侵蚀隧洞",
            names = listOf(
                "无", "霜风之径", "迅拳之径", "漂泊之径",
                "睿治之径", "圣颂之径", "野焰之径", "药使之径",
                "幽冥之径", "梦潜之径", "勇骑之径", "迷识之径",
                "弦歌之径", "雳涌之径", "隐救之径", "魔占之径",
                "观火之径", "密伶之径",
            ),
            defaultName = "睿治之径",
            defaultCount = 6,
        ),
        Type(
            name = "饰品提取",
            names = listOf(
                "无", "戏剧中人", "虫虫来袭", "鎏金追忆",
                "西风丛中", "月下朱殷", "纷争不休", "蠹役饥肠",
                "永恒笑剧", "伴你入眠", "天剑如雨", "孽果盘生",
                "百年冻土", "温柔话语", "浴火钢心", "坚城不倒",
            ),
            defaultName = "永恒笑剧",
            defaultCount = 6,
        ),
    )

    fun type(name: String): Type? = TYPES.firstOrNull { it.name == name }

    fun names(typeName: String): List<String> = type(typeName)?.names ?: emptyList()

    fun defaultType(): String = TYPES.first().name

    fun defaultName(typeName: String): String = type(typeName)?.defaultName ?: "无"

    fun defaultCount(typeName: String): Int = type(typeName)?.defaultCount ?: 6
}
