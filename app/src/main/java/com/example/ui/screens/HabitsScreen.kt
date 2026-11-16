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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhonelinkErase
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.SentimentSatisfiedAlt
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.HabitCategory
import com.example.data.model.StudyHabit
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
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private data class HabitPreset(
    val title: String,
    val subtitle: String,
    val category: HabitCategory,
    val iconKey: String,
    val colorIndex: Long
)

private val HabitGalleryPresets = listOf(
    HabitPreset("Levé tôt", "Levez-vous et soyez incroyable", HabitCategory.HEALTH, "sun", 2L),
    HabitPreset("Couché tôt", "Sommeil réparateur avant 23h", HabitCategory.HEALTH, "moon", 5L),
    HabitPreset("Prendre soin de ses yeux", "Pause 20-20-20 loin des écrans", HabitCategory.HEALTH, "eye", 3L),
    HabitPreset("Boire 1,5L d'eau", "Hydratation constante en cours", HabitCategory.HEALTH, "water", 4L),
    HabitPreset("Enregistrement quotidien", "Essayez plus pour être mieux", HabitCategory.LIFE, "smile", 3L),
    HabitPreset("Écouter de la musique", "Soyez de bonne humeur", HabitCategory.LIFE, "music", 0L),
    HabitPreset("Vaincre sa dépendance au téléphone", "Déconnectez-vous et reconcentrez-vous", HabitCategory.LIFE, "phone_off", 6L),
    HabitPreset("Apprendre de nouvelles choses", "Petit nombre, grand résultat", HabitCategory.LIFE, "book", 4L),
    HabitPreset("Apprendre une nouvelle langue", "15 min de vocabulaire technique", HabitCategory.LIFE, "language", 1L),
    HabitPreset("Revue de code 20 min", "Relire ses commits et tests L3", HabitCategory.STUDY, "sparkles", 0L),
    HabitPreset("Fiches de synthèse", "Consolider le cours du jour", HabitCategory.STUDY, "book", 4L),
    HabitPreset("Séance de sport / étirements", "Libérer la tension du dos", HabitCategory.SPORTS, "fitness", 1L),
    HabitPreset("Respiration & pleine conscience", "5 min de calme avant les révisions", HabitCategory.MINDSET, "mind", 3L)
)

private val HabitColors = listOf(
    AppleIndigo,
    AppleRose,
    AppleAmber,
    AppleEmerald,
    AppleSky,
    Color(0xFF7C3AED),
    Color(0xFFEC4899)
)

private fun habitBadgeColor(index: Long): Color =
    HabitColors[(index.toInt()).mod(HabitColors.size)]

private fun habitIcon(iconKey: String): ImageVector = when (iconKey) {
    "sun" -> Icons.Rounded.WbSunny
    "moon" -> Icons.Rounded.Bedtime
    "eye" -> Icons.Rounded.RemoveRedEye
    "water" -> Icons.Rounded.WaterDrop
    "smile" -> Icons.Rounded.SentimentSatisfiedAlt
    "music" -> Icons.Rounded.MusicNote
    "phone_off" -> Icons.Rounded.PhonelinkErase
    "book" -> Icons.AutoMirrored.Rounded.MenuBook
    "language" -> Icons.Rounded.Translate
    "fitness" -> Icons.Rounded.FitnessCenter
    "mind" -> Icons.Rounded.SelfImprovement
    else -> Icons.Rounded.AutoAwesome
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<StudyHabit>,
    onToggleHabitToday: (StudyHabit) -> Unit,
    onCreateHabit: (title: String, subtitle: String, category: String, iconKey: String, colorIndex: Long) -> Unit,
    onDeleteHabit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradient = LocalLightGradient.current
    val today = remember { LocalDate.now() }
    val todayIso = today.toString()
    val last7Days = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }

    var showGallerySheet by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val completedToday = habits.count { it.completedDates.contains(todayIso) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("habits_screen"),
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
                        text = "Suivi des habitudes",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppleTextPrimary
                    )
                    Text(
                        text = "Discipline douce & bien-être étudiant",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showGallerySheet = true },
                        shape = CircleShape,
                        border = BorderStroke(1.dp, gradient.accentColor),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("open_habit_gallery_button")
                    ) {
                        Text("Galerie", color = gradient.accentColor)
                    }
                    Surface(
                        onClick = { showCreateDialog = true },
                        shape = CircleShape,
                        color = gradient.accentColor,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("add_habit_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Créer une habitude",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            GradientHeroCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Check-in du jour",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.86f)
                        )
                        Text(
                            text = "$completedToday / ${habits.size} complétées",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Surface(
                        onClick = { showGallerySheet = true },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "+ Explorer",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (habits.isEmpty()) {
            item {
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showGallerySheet = true }
                ) {
                    Text(
                        text = "Aucune habitude configurée",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ouvrez la Galerie pour choisir des rituels Santé, Vie, Sports ou Études.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary
                    )
                }
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                HabitTrackerCard(
                    habit = habit,
                    todayIso = todayIso,
                    last7Days = last7Days,
                    onToggleToday = { onToggleHabitToday(habit) },
                    onDelete = { onDeleteHabit(habit.id) }
                )
            }
        }
    }

    if (showGallerySheet) {
        HabitGallerySheet(
            existingTitles = habits.map { it.title.lowercase() }.toSet(),
            onAddPreset = { preset ->
                onCreateHabit(
                    preset.title,
                    preset.subtitle,
                    preset.category.key,
                    preset.iconKey,
                    preset.colorIndex
                )
            },
            onOpenCustomDialog = {
                showGallerySheet = false
                showCreateDialog = true
            },
            onDismiss = { showGallerySheet = false }
        )
    }

    if (showCreateDialog) {
        CreateHabitDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, subtitle, category, iconKey, colorIndex ->
                onCreateHabit(title, subtitle, category.key, iconKey, colorIndex)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HabitTrackerCard(
    habit: StudyHabit,
    todayIso: String,
    last7Days: List<LocalDate>,
    onToggleToday: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = habitBadgeColor(habit.colorIndex)
    val doneToday = habit.completedDates.contains(todayIso)

    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = habitIcon(habit.iconKey),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = AppleTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = CircleShape,
                        color = AppleAmber.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocalFireDepartment,
                                contentDescription = null,
                                tint = AppleAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${habit.streakDays}j",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppleAmber
                            )
                        }
                    }
                }
                if (habit.subtitle.isNotBlank()) {
                    Text(
                        text = habit.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppleTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    last7Days.forEach { d ->
                        val checked = habit.completedDates.contains(d.toString())
                        val dayLbl = d.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.FRENCH).uppercase()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (checked) badgeColor else badgeColor.copy(alpha = 0.12f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                            Text(
                                text = dayLbl,
                                style = MaterialTheme.typography.labelSmall,
                                color = AppleTextSecondary
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onToggleToday,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_habit_${habit.id}")
                ) {
                    Icon(
                        imageVector = if (doneToday) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (doneToday) "Annuler pour aujourd'hui" else "Valider aujourd'hui",
                        tint = if (doneToday) badgeColor else AppleTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Supprimer l'habitude",
                        tint = AppleTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitGallerySheet(
    existingTitles: Set<String>,
    onAddPreset: (HabitPreset) -> Unit,
    onOpenCustomDialog: () -> Unit,
    onDismiss: () -> Unit
) {
    val gradient = LocalLightGradient.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf(HabitCategory.HEALTH) }

    val filteredPresets = remember(selectedCategory) {
        if (selectedCategory == HabitCategory.STUDY) {
            HabitGalleryPresets
        } else {
            HabitGalleryPresets.filter { it.category == selectedCategory }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 26.dp)
        ) {
            Text(
                text = "Galerie d'habitudes",
                style = MaterialTheme.typography.headlineSmall,
                color = AppleTextPrimary
            )
            Text(
                text = "Choisissez un rituel ou créez le vôtre",
                style = MaterialTheme.typography.bodySmall,
                color = AppleTextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HabitCategory.entries.forEach { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = gradient.softContainer,
                            selectedLabelColor = gradient.accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPresets, key = { it.title }) { preset ->
                    val badgeColor = habitBadgeColor(preset.colorIndex)
                    val alreadyAdded = existingTitles.contains(preset.title.lowercase())
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF6F7FB),
                        border = BorderStroke(0.8.dp, AppleDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = habitIcon(preset.iconKey),
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = AppleTextPrimary
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppleTextSecondary,
                                    maxLines = 2
                                )
                            }
                            IconButton(
                                onClick = { if (!alreadyAdded) onAddPreset(preset) },
                                enabled = !alreadyAdded,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (alreadyAdded) Icons.Rounded.CheckCircle else Icons.Rounded.Add,
                                    contentDescription = if (alreadyAdded) "Déjà ajoutée" else "Ajouter ${preset.title}",
                                    tint = if (alreadyAdded) AppleEmerald else gradient.accentColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenCustomDialog,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = gradient.accentColor,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("create_custom_habit_button")
            ) {
                Text("Créer une nouvelle habitude", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CreateHabitDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, subtitle: String, category: HabitCategory, iconKey: String, colorIndex: Long) -> Unit
) {
    val gradient = LocalLightGradient.current
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(HabitCategory.STUDY) }
    var iconKey by remember { mutableStateOf("sparkles") }
    var colorIndex by remember { mutableLongStateOf(0L) }

    val iconOptions = listOf("sparkles", "sun", "eye", "book", "fitness", "mind", "music", "water")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Nouvelle habitude") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nom de l'habitude") },
                    placeholder = { Text("Ex : Relire mes notes L3") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Motivation / Rappel") },
                    placeholder = { Text("Ex : 15 min après les cours") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HabitCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEachIndexed { idx, key ->
                        val selected = iconKey == key
                        val col = habitBadgeColor(idx.toLong())
                        Surface(
                            onClick = {
                                iconKey = key
                                colorIndex = idx.toLong()
                            },
                            shape = CircleShape,
                            color = if (selected) col.copy(alpha = 0.22f) else Color(0xFFF3F4F8),
                            border = BorderStroke(
                                if (selected) 1.5.dp else 0.8.dp,
                                if (selected) col else AppleDivider
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = habitIcon(key),
                                    contentDescription = key,
                                    tint = col,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), subtitle.trim(), category, iconKey, colorIndex)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Créer", color = gradient.accentColor, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = AppleTextSecondary)
            }
        }
    )
}
