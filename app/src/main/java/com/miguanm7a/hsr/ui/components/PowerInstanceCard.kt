package com.miguanm7a.hsr.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.miguanm7a.hsr.deploy.InstanceCatalog
import com.miguanm7a.hsr.ui.theme.MaaDesignTokens

/**
 * 清体力副本选择卡片。
 *
 * 对应上游 `config.yaml` 的三项：
 * - `instance_type`：副本类型
 * - `instance_names`：每种类型各自选中的关卡（块状映射）
 * - `instance_names_challenge_count`：每种类型的连续挑战次数
 *
 * 用对话框列表而不是下拉框：凝滞虚影有 30 个关卡，下拉菜单在手机上很难点。
 */
@Composable
fun PowerInstanceCard(
    type: String,
    name: String,
    count: Int,
    onTypeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var typeDialog by remember { mutableStateOf(false) }
    var nameDialog by remember { mutableStateOf(false) }

    MaaCard(modifier = modifier) {
        Text("清体力副本", style = MaterialTheme.typography.titleSmall)
        Text(
            "「清体力」和「例行任务 / 完整运行」里的刷本都用这里的选择。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(MaaDesignTokens.Spacing.sm))

        PickerRow(label = "副本类型", value = type) { typeDialog = true }
        PickerRow(label = "副本名称", value = name) { nameDialog = true }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("连续挑战次数", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "打完一轮后继续挑战的次数，体力不足会自动停止",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(MaaDesignTokens.Spacing.sm))
            OutlinedTextField(
                value = count.toString(),
                onValueChange = { text ->
                    val v = text.filter { it.isDigit() }.take(2).toIntOrNull()
                    if (v != null && v in 1..99) onCountChange(v)
                },
                singleLine = true,
                modifier = Modifier.width(88.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
    }

    if (typeDialog) {
        ChoiceDialog(
            title = "选择副本类型",
            options = InstanceCatalog.TYPES.map { it.name },
            selected = type,
            onDismiss = { typeDialog = false },
            onSelect = {
                typeDialog = false
                onTypeChange(it)
            },
        )
    }
    if (nameDialog) {
        ChoiceDialog(
            title = "选择副本（$type）",
            options = InstanceCatalog.names(type),
            selected = name,
            onDismiss = { nameDialog = false },
            onSelect = {
                nameDialog = false
                onNameChange(it)
            },
        )
    }
}

@Composable
private fun PickerRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = MaaDesignTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<String>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = {
            Box(Modifier.heightIn(max = 420.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(options) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(option) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = option == selected, onClick = { onSelect(option) })
                            Text(option, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}
