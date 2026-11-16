package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.CountdownCategory
import com.example.data.model.StudyCountdown
import com.example.ui.components.AppleCard
import com.example.ui.components.GradientHeroCard
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleDivider
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.ui.theme.LocalLightGradient
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val CountdownPalette = listOf(AppleIndigo, AppleSky, AppleEmerald, AppleAmber, AppleRose)

@Composable
fun CountdownScreen(
    countdowns: List<StudyCountdown>,
    onCreateCountdown: (title: String, notes: String, targetDateIso: String, category: String, colorIndex: Long) -> Unit,
    onDeleteCountdown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val today = remember { LocalDate.now() }
    var showCreateDialog by remember { mutableStateOf(false) }

    val sorted = remember(countdowns) {
        countdowns.sortedBy { it.targetDateIso }
    }
    val nextMilestone = remember(sorted, today) {
        sorted.firstOrNull {
            val target = runCatching { LocalDate.parse(it.targetDateIso) }.getOrNull()
            target != null && !target.isBefore(today)
        } ?: sorted.firstOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("countdown_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Compte à rebours",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "Souvenez-vous de chaque jour spécial",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }

                Surface(
                    onClick = { showCreateDialog = true },
                    shape = CircleShape,
                    color = gradient.accentColor,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("add_countdown_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Ajouter un compte à rebours",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        if (nextMilestone != null) {
            item {
                val targetDate = runCatching { LocalDate.parse(nextMilestone.targetDateIso) }.getOrElse { today }
                val daysLeft = ChronoUnit.DAYS.between(today, targetDate)
                val badgeText = when {
                    daysLeft == 0L -> "Aujourd'hui"
                    daysLeft > 0L -> "J-$daysLeft"
                    else -> "J+${-daysLeft}"
                }
                GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROCHAINE ÉCHÉANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = nextMilestone.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 2
                            )
                            if (nextMilestone.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = nextMilestone.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.88f)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White.copy(alpha = 0.24f)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        if (sorted.isEmpty()) {
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showCreateDialog = true }
                ) {
                    Text(
                        text = "Aucun compte à rebours",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ajoutez vos partiels, soutenances de projet ou vacances universitaires.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }
        } else {
            items(sorted, key = { it.id }) { item ->
                CountdownItemCard(
                    item = item,
                    today = today,
                    onDelete = { onDeleteCountdown(item.id) }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateCountdownDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, notes, dateIso, category, colorIdx ->
                onCreateCountdown(title, notes, dateIso, category.key, colorIdx)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CountdownItemCard(
    item: StudyCountdown,
    today: LocalDate,
    onDelete: () -> Unit
) {
    val targetDate = remember(item.targetDateIso) {
        runCatching { LocalDate.parse(item.targetDateIso) }.getOrElse { today }
    }
    val days = ChronoUnit.DAYS.between(today, targetDate)
    val badgeText = when {
        days == 0L -> "Aujourd'hui"
        days > 0L -> "J-$days"
        else -> "Passé ${-days}j"
    }
    val accent = CountdownPalette[(item.colorIndex.toInt()).mod(CountdownPalette.size)]
    val cat = CountdownCategory.fromKey(item.category)
    val dateLabel = remember(targetDate) {
        targetDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.FRENCH))
    }

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Event,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = AppleTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${cat.label} · $dateLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppleTextSecondary
                )
                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = accent.copy(alpha = 0.14f)
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Supprimer",
                    tint = AppleTextSecondary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCountdownDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, notes: String, dateIso: String, category: CountdownCategory, colorIndex: Long) -> Unit
) {
    val gradient = LocalLightGradient.current
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf(LocalDate.now().plusDays(14)) }
    var category by remember { mutableStateOf(CountdownCategory.EXAM) }
    var colorIndex by remember { mutableLongStateOf(4L) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Nouveau compte à rebours") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Événement / Examen") },
                    placeholder = { Text("Ex : Soutenance Projet L3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Détails (optionnel)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Surface(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    color = gradient.softContainer,
                    border = BorderStroke(0.8.dp, gradient.accentColor.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = gradient.accentColor
                        )
                        Text(
                            text = "Date : $targetDate",
                            style = MaterialTheme.typography.titleSmall,
                            color = gradient.accentColor
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CountdownCategory.entries.forEachIndexed { idx, cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                colorIndex = idx.toLong()
                            },
                            label = { Text(cat.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), notes.trim(), targetDate.toString(), category, colorIndex)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Ajouter", color = gradient.accentColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = AppleTextSecondary)
            }
        }
    )

    if (showDatePicker) {
        val initialMillis = remember(targetDate) {
            targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            targetDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Valider", color = gradient.accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler", color = AppleTextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(state = state)
        }
    }
}
