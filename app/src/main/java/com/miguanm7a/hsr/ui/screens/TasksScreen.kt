package com.miguanm7a.hsr.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import com.miguanm7a.hsr.deploy.AfterFinish
import com.miguanm7a.hsr.deploy.M7aTask
import com.miguanm7a.hsr.ui.MainViewModel
import com.miguanm7a.hsr.ui.components.LoginQrCard
import com.miguanm7a.hsr.ui.components.MaaCard
import com.miguanm7a.hsr.ui.components.ExpandableGroup
import com.miguanm7a.hsr.ui.components.InstanceNameRow
import com.miguanm7a.hsr.ui.components.PowerInstanceCard
import com.miguanm7a.hsr.ui.components.ToggleRow
import com.miguanm7a.hsr.deploy.InstanceCatalog
import com.miguanm7a.hsr.deploy.TaskToggles
import com.miguanm7a.hsr.deploy.ToggleGroup
import com.miguanm7a.hsr.ui.components.SectionHeader
import com.miguanm7a.hsr.ui.theme.MaaDesignTokens
import com.miguanm7a.hsr.ui.theme.WarnColor
@Composable
fun TasksScreen(
    vm: MainViewModel,
    onOpenTerminal: (String?) -> Unit,
    onOpenMonitor: () -> Unit = {},
) {
    val ui by vm.ui.collectAsState()
    val qr by vm.qr.collectAsState()
    val toggles by vm.toggles.collectAsState()
    val instanceName = ui.instanceName.ifEmpty { InstanceCatalog.defaultName(ui.instanceType) }
    val instanceCount = ui.instanceCount.takeIf { it > 0 }
        ?: InstanceCatalog.defaultCount(ui.instanceType)
    // 任务选择默认收起，首屏只显示当前选中项，把空间留给下面的任务设置。
    var taskOpen by rememberSaveable { mutableStateOf(false) }
    var powerOpen by rememberSaveable { mutableStateOf(true) }
    var rewardOpen by rememberSaveable { mutableStateOf(false) }
    var dailyOpen by rememberSaveable { mutableStateOf(false) }
    var plotOpen by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = MaaDesignTokens.Spacing.listHorizontal,
            vertical = MaaDesignTokens.Spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(MaaDesignTokens.Spacing.sm),
    ) {
        item {
            Text("任务", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(MaaDesignTokens.Spacing.sm))
            Text(
                "选择要执行的任务，然后点「一键运行」。运行输出会实时显示在「日志」页。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // 扫码登录：首次运行必需，放在最显眼的位置
        item {
            Spacer(Modifier.height(MaaDesignTokens.Spacing.sm))
            LoginQrCard(
                qr = qr,
                runningTask = ui.runningTask,
                onRefresh = { vm.rescanQr() },
            )
        }

        item { SectionHeader("选择任务", icon = Icons.AutoMirrored.Filled.List) }

        // 收起时标题即当前任务，展开后在卡片内直接选中；选完自动收起，
        // 免得整屏被任务列表占满、还要手动收起来才能看到下面的设置。
        item {
            ExpandableGroup(
                title = ui.task.label,
                summary = taskBrief(ui.task, ui.instanceType, instanceName, instanceCount),
                expanded = taskOpen,
                onToggleExpand = { taskOpen = !taskOpen },
            ) {
                M7aTask.entries.forEachIndexed { index, task ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.height(1.dp),
                        )
                    }
                    TaskOptionRow(
                        task = task,
                        brief = taskBrief(task, ui.instanceType, instanceName, instanceCount),
                        selected = ui.task == task,
                        onSelect = {
                            vm.setTask(task)
                            taskOpen = false
                        },
                    )
                }
            }
        }

        item { SectionHeader("任务设置", icon = Icons.Filled.Settings) }
        item {
            Text(
                "选「完整运行」时按开关执行；单项任务只看策略类开关。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ---- 清体力 ----
        item {
            ExpandableGroup(
                title = "清体力",
                summary = "${ui.instanceType} · $instanceName ×$instanceCount",
                expanded = powerOpen,
                onToggleExpand = { powerOpen = !powerOpen },
            ) {
                PowerInstanceCard(
                    type = ui.instanceType,
                    name = instanceName,
                    count = instanceCount,
                    onTypeChange = { vm.setInstanceType(it) },
                    onNameChange = { vm.setInstanceName(it) },
                    onCountChange = { vm.setInstanceCount(it) },
                    card = false,
                )
                if (toggles[TaskToggles.BATTLE_DETECT_KEY] == false) {
                    Text(
                        "「自动战斗 + 二倍速」当前关闭，进副本后不会自动开二倍速。",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarnColor,
                    )
                }
                TaskToggles.forGroup(ToggleGroup.POWER).forEach { t ->
                    ToggleRow(t, checked = toggles[t.key] ?: t.defaultValue) { v -> vm.setToggle(t.key, v) }
                    // 历战余响的关卡跟着开关放在一起，不用回上面找副本类型
                    if (t.key == TaskToggles.ECHO_KEY) {
                        InstanceNameRow(
                            label = "历战余响关卡",
                            type = InstanceCatalog.ECHO,
                            name = ui.echoInstanceName.ifEmpty {
                                InstanceCatalog.defaultName(InstanceCatalog.ECHO)
                            },
                            onNameChange = { vm.setEchoInstanceName(it) },
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }

        // ---- 领取奖励 ----
        item {
            ExpandableGroup(
                title = "领取奖励",
                summary = groupSummary(ToggleGroup.REWARD, toggles),
                expanded = rewardOpen,
                onToggleExpand = { rewardOpen = !rewardOpen },
            ) {
                TaskToggles.forGroup(ToggleGroup.REWARD).forEach { t ->
                    ToggleRow(t, checked = toggles[t.key] ?: t.defaultValue) { v -> vm.setToggle(t.key, v) }
                }
            }
        }

        // ---- 日常与活动 ----
        item {
            ExpandableGroup(
                title = "日常与活动",
                summary = groupSummary(ToggleGroup.DAILY, toggles) +
                    " ／ " + groupSummary(ToggleGroup.ACTIVITY, toggles),
                expanded = dailyOpen,
                onToggleExpand = { dailyOpen = !dailyOpen },
            ) {
                (TaskToggles.forGroup(ToggleGroup.DAILY) + TaskToggles.forGroup(ToggleGroup.ACTIVITY))
                    .forEach { t ->
                        ToggleRow(t, checked = toggles[t.key] ?: t.defaultValue) { v -> vm.setToggle(t.key, v) }
                    }
            }
        }

        // ---- 剧情与运行 ----
        item {
            ExpandableGroup(
                title = "剧情与运行",
                summary = groupSummary(ToggleGroup.AUTOPLOT, toggles) +
                    " ／ " + groupSummary(ToggleGroup.RUNTIME, toggles),
                expanded = plotOpen,
                onToggleExpand = { plotOpen = !plotOpen },
            ) {
                (TaskToggles.forGroup(ToggleGroup.AUTOPLOT) + TaskToggles.forGroup(ToggleGroup.RUNTIME))
                    .forEach { t ->
                        ToggleRow(t, checked = toggles[t.key] ?: t.defaultValue) { v -> vm.setToggle(t.key, v) }
                    }
            }
        }

        item { SectionHeader("完成后行为", icon = Icons.Filled.PowerSettingsNew) }
        item {
            MaaCard {
                AfterFinish.entries.forEach { af ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = ui.afterFinish == af, onClick = { vm.setAfterFinish(af) })
                        Column(Modifier.weight(1f)) {
                            Text(af.value, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                if (af == AfterFinish.EXIT) "任务完成后退出" else "任务完成后循环等待下次执行",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item { SectionHeader("定时运行", icon = Icons.Filled.Schedule) }
        item {
            MaaCard {
                Text("循环模式", style = MaterialTheme.typography.titleSmall)
                listOf(
                    "scheduled" to "按定时时间运行（配合下面的时间）",
                    "power" to "按体力计划循环",
                ).forEach { (mode, desc) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = ui.loopMode == mode,
                            onClick = { vm.setLoopMode(mode) },
                        )
                        Column(Modifier.weight(1f)) {
                            Text(mode, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(MaaDesignTokens.Spacing.sm))
                OutlinedTextField(
                    value = ui.runDailyTime,
                    onValueChange = { vm.setRunDailyTime(it) },
                    label = { Text("定时时间（HH:mm）") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "仅「完成后行为 = 循环等待」时生效",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "改完点下方「生成脚本」才会写入容器。",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarnColor,
                )
            }
        }

        item { SectionHeader("操作", icon = Icons.Filled.PlayArrow) }

        // 生成脚本放最上面：把当前设置写进容器。点「一键运行」也会自动做一次，
        // 单列出来是为了改完设置后能主动确认写入结果。
        item {
            OutlinedButton(
                onClick = { vm.regenerateScripts() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("生成脚本") }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(MaaDesignTokens.Spacing.md)) {
                OutlinedButton(
                    onClick = { vm.stopTask() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = ui.runningTask,
                ) { Text("停止") }

                Button(
                    onClick = { vm.runSelectedTask() },
                    modifier = Modifier.weight(1.8f).height(52.dp),
                    enabled = ui.runtimeReady && !ui.runningTask,
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(6.dp))
                    // 任务名单独一行，避免「一键运行 货币战争」在窄屏被截断
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("一键运行", style = MaterialTheme.typography.titleSmall)
                        Text(
                            ui.task.label,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { onOpenTerminal(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = ui.runtimeReady,
            ) { Text("在终端中手动执行") }
        }

        item {
            OutlinedButton(
                onClick = onOpenMonitor,
                modifier = Modifier.fillMaxWidth(),
                enabled = ui.runtimeReady,
            ) { Text("实时监看画面（CDP）") }
        }

        if (!ui.runtimeReady) {
            item {
                MaaCard {
                    Text(
                        "Termux 运行时尚未安装，请先到「部署」页完成一键部署。",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarnColor,
                    )
                }
            }
        }
    }
}

/** 任务列表里的一行：单选框 + 任务名 + 一句摘要。不用卡片，避免卡片套卡片。 */
@Composable
private fun TaskOptionRow(
    task: M7aTask,
    brief: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Column(Modifier.weight(1f).padding(start = 6.dp)) {
            Text(task.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                brief,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 任务卡片上的摘要：让用户不用展开设置就知道这一项会做什么。 */
private fun taskBrief(
    task: M7aTask,
    instanceType: String,
    instanceName: String,
    instanceCount: Int,
): String = when (task) {
    M7aTask.FULL -> "按「任务设置」里的开关执行全部环节"
    M7aTask.ROUTINE -> "日常准备 → 日常领奖 → 每日实训"
    M7aTask.POWER -> "$instanceType · $instanceName ×$instanceCount"
    M7aTask.DAILY -> "补做未完成的每日实训并领取活跃度奖励"
    else -> task.arg?.let { "main.py $it" } ?: "main.py"
}

/** 分组摘要：显示「已启用 N / M」，收起来也能看出状态。 */
private fun groupSummary(group: ToggleGroup, values: Map<String, Boolean>): String {
    val items = TaskToggles.forGroup(group)
    val on = items.count { values[it.key] ?: it.defaultValue }
    return "${group.label} 已启用 $on/${items.size}"
}
