package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.data.model.NavigationLayout
import com.example.data.model.NavigationOptions
import com.example.ui.components.LightPanel
import com.example.ui.theme.LightSpacing

internal fun navigationIcon(id: String?): ImageVector? = when (id) {
    "bookmark" -> Icons.Rounded.Bookmark
    "star" -> Icons.Rounded.Star
    "circle" -> Icons.Rounded.Circle
    "bolt" -> Icons.Rounded.Bolt
    "home" -> Icons.Rounded.Home
    "calendar" -> Icons.Rounded.DateRange
    "timer" -> Icons.Rounded.Timer
    "grid" -> Icons.Rounded.GridView
    "book" -> Icons.Rounded.Description
    "auto_awesome" -> Icons.Rounded.AutoAwesome
    "search" -> Icons.Rounded.Search
    "bar_chart" -> Icons.Rounded.BarChart
    "self_improvement" -> Icons.Rounded.SelfImprovement
    "event" -> Icons.Rounded.Event
    "date_range" -> Icons.Rounded.DateRange
    "school" -> Icons.Rounded.School
    "insights" -> Icons.Rounded.Insights
    else -> null
}

private fun navigationIconLabel(id: String?) = when (id) {
    "bookmark" -> "Marque-page"
    "star" -> "Étoile"
    "circle" -> "Cercle"
    "bolt" -> "Éclair"
    "home" -> "Accueil"
    "calendar" -> "Calendrier"
    "timer" -> "Focus"
    "grid" -> "Matrice"
    "book" -> "Bibliothèque"
    "auto_awesome" -> "Agent IA"
    "search" -> "Recherche"
    "bar_chart" -> "Progrès"
    "self_improvement" -> "Habitudes"
    "event" -> "Échéances"
    "date_range" -> "Projets"
    "school" -> "Profils Focus"
    "insights" -> "Analytics"
    else -> "Par défaut"
}

@Composable
internal fun SettingsPageHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(LightSpacing.one)) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("settings_page_back")) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Retour")
        }
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
internal fun NavigationSettingsScreen(
    layout: NavigationLayout,
    labels: Map<String, String>,
    defaultIcons: Map<String, ImageVector>,
    onSave: (NavigationLayout) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // While the user is editing, don't replace the draft with an incoming sync update.
    var edits by remember { mutableStateOf<NavigationLayout?>(null) }
    val draft = edits ?: layout
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(LightSpacing.two)
        .testTag("navigation_settings_page"), verticalArrangement = Arrangement.spacedBy(LightSpacing.one)) {
        SettingsPageHeader("Barre de navigation", onBack)
        Text("Choisissez l’ordre et les icônes des raccourcis. Accueil et Explorer restent accessibles ; gardez au moins deux fonctionnalités.",
            style = MaterialTheme.typography.bodyMedium)
        draft.order.forEachIndexed { index, route ->
            androidx.compose.runtime.key(route) {
                val name = labels[route] ?: route
                val visible = route !in draft.hidden
                var showIcons by remember { mutableStateOf(false) }
                LightPanel(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(LightSpacing.one)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(navigationIcon(draft.icons[route]) ?: defaultIcons.getValue(route), null,
                                modifier = Modifier.padding(horizontal = LightSpacing.one))
                            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            Switch(checked = visible, onCheckedChange = { edits = draft.setVisible(route, it) },
                                enabled = route != "home" && (!visible || draft.hidden.size < NavigationOptions.maxHidden),
                                modifier = Modifier.testTag("nav_visible_$route").semantics { contentDescription = "Afficher $name" })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
                                TextButton(onClick = { showIcons = true }, modifier = Modifier.testTag("nav_icon_$route")) {
                                    Text(navigationIconLabel(draft.icons[route]))
                                }
                                DropdownMenu(expanded = showIcons, onDismissRequest = { showIcons = false }) {
                                    (listOf(null) + NavigationOptions.icons).forEach { icon ->
                                        DropdownMenuItem(text = { Text(navigationIconLabel(icon)) },
                                            leadingIcon = { Icon(navigationIcon(icon) ?: defaultIcons.getValue(route), null) },
                                            onClick = {
                                                showIcons = false
                                                edits = draft.copy(icons = if (icon == null) draft.icons - route else draft.icons + (route to icon))
                                            })
                                    }
                                }
                            }
                            IconButton(onClick = { edits = draft.move(route, -1) }, enabled = index > 0,
                                modifier = Modifier.testTag("nav_up_$route")) { Icon(Icons.Rounded.ArrowUpward, "Remonter $name") }
                            IconButton(onClick = { edits = draft.move(route, 1) }, enabled = index < draft.order.lastIndex,
                                modifier = Modifier.testTag("nav_down_$route")) { Icon(Icons.Rounded.ArrowDownward, "Descendre $name") }
                        }
                    }
                }
            }
        }
        Text("Les fonctions masquées restent accessibles depuis Explorer.", style = MaterialTheme.typography.bodySmall)
        Button(onClick = { onSave(draft.normalized()) }, enabled = draft != layout,
            modifier = Modifier.fillMaxWidth().testTag("save_navigation_layout")) { Text("Enregistrer") }
    }
}
