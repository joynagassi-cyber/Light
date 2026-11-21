package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.data.model.AgentSkillOption

@Composable
internal fun AgentSkillPicker(
    options: List<AgentSkillOption>,
    selectedKey: String?,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compétence du prochain message") },
        text = {
            Column {
                Text("Une méthode de travail, pas une autorisation d’agir.")
                LazyColumn(Modifier.heightIn(max = 340.dp).selectableGroup()) {
                    item { AgentSkillRow("Aucune", "Réponse standard", null, selectedKey, onSelect) }
                    items(options, key = { it.key }) { option ->
                        AgentSkillRow(option.title, option.summary, option.key, selectedKey, onSelect)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } },
    )
}

@Composable
private fun AgentSkillRow(title: String, summary: String, key: String?, selectedKey: String?, onSelect: (String?) -> Unit) {
    val selected = key == selectedKey
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(summary) },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = Modifier.testTag("agent_skill_${key ?: "none"}")
            .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(key) }),
    )
}
