package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppleCard
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.LocalLightGradient

enum class MoreSubPage(
    val key: String,
    val title: String,
    val subtitle: String
) {
    AGENT("agent", "Assistant Light", "Préparez et ajustez votre planning par commandes"),
    LIBRARY("library", "Bibliothèque", "Vos documents, classés par dossiers et isolés par compte"),
    FOCUS_PROFILES("focus_profiles", "Profils de Focus", "Apprenez ce que vous voulez & assignez vos tâches"),
    FOCUS_ANALYTICS("focus_analytics", "Focus Analytics", "Durée d'étude quotidienne & courbes Focus"),
    PROGRESS("progress", "Progression", "Objectifs, temps de concentration et résultats"),
    HABITS("habits", "Habitudes & routines", "Rituels, séries et suivi quotidien"),
    COUNTDOWN("countdown", "Échéances & jalons", "Examens, projets et dates importantes"),
    SEARCH("search", "Rechercher", "Tâches, cours, habitudes et échéances"),
    PROJECTS("projects", "Planning Gantt", "Tâches, matières, jalons et échéances"),
}

private data class IndexedPageRow(
    val page: MoreSubPage,
    val icon: ImageVector,
    val color: Color,
    val badge: String? = null
)

@Composable
fun MoreHubScreen(
    habitsCount: Int,
    countdownsCount: Int,
    pendingTasksCount: Int,
    onOpenPage: (MoreSubPage) -> Unit,
    onOpenGradientPicker: () -> Unit,
    modifier: Modifier = Modifier,
    focusProfilesCount: Int = 0
) {
    val gradient = LocalLightGradient.current

    val indexedPages = listOf(
        IndexedPageRow(
            page = MoreSubPage.AGENT,
            icon = Icons.Rounded.AutoAwesome,
            color = gradient.accentColor,
            badge = "IA"
        ),
        IndexedPageRow(
            page = MoreSubPage.LIBRARY,
            icon = Icons.Rounded.AutoAwesome,
            color = AppleSky,
            badge = null
        ),
        IndexedPageRow(
            page = MoreSubPage.FOCUS_PROFILES,
            icon = Icons.Rounded.School,
            color = gradient.accentColor,
            badge = if (focusProfilesCount > 0) "$focusProfilesCount" else null
        ),
        IndexedPageRow(
            page = MoreSubPage.FOCUS_ANALYTICS,
            icon = Icons.Rounded.Insights,
            color = gradient.accentColor,
            badge = "Stats"
        ),
        IndexedPageRow(
            page = MoreSubPage.PROGRESS,
            icon = Icons.Rounded.BarChart,
            color = AppleIndigo
        ),
        IndexedPageRow(
            page = MoreSubPage.HABITS,
            icon = Icons.Rounded.SelfImprovement,
            color = AppleEmerald,
            badge = if (habitsCount > 0) "$habitsCount" else null
        ),
        IndexedPageRow(
            page = MoreSubPage.COUNTDOWN,
            icon = Icons.Rounded.Event,
            color = AppleRose,
            badge = if (countdownsCount > 0) "$countdownsCount" else null
        ),
        IndexedPageRow(
            page = MoreSubPage.SEARCH,
            icon = Icons.Rounded.Search,
            color = AppleSky,
            badge = if (pendingTasksCount > 0) "$pendingTasksCount" else null
        ),
        IndexedPageRow(
            page = MoreSubPage.PROJECTS,
            icon = Icons.Rounded.DateRange,
            color = AppleIndigo,
            badge = if (pendingTasksCount > 0) "$pendingTasksCount" else null
        ),

    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_hub_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Explorer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Vos outils de planification et de suivi réunis au même endroit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    onClick = onOpenGradientPicker,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("more_gradient_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Thèmes dégradés",
                            tint = gradient.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Indexed Pages Directory (iOS Inset Grouped Card)
        item {
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text(
                    text = "OUTILS & ESPACES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                indexedPages.forEachIndexed { idx, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenPage(row.page) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("more_index_${row.page.key}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(row.color.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = row.icon,
                                contentDescription = null,
                                tint = row.color,
                                modifier = Modifier.size(21.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = row.page.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = row.page.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (row.badge != null) {
                            Surface(
                                shape = CircleShape,
                                color = row.color.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = row.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = row.color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Ouvrir ${row.page.title}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (idx < indexedPages.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 68.dp, end = 16.dp)
                        )
                    }
                }
            }
        }


    }
}
