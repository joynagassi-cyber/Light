package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import kotlin.math.absoluteValue
import com.example.ui.theme.AppleAmber
import com.example.ui.theme.AppleEmerald
import com.example.ui.theme.AppleIndigo
import com.example.ui.theme.AppleRose
import com.example.ui.theme.AppleSky

internal data class CatalogIconOption(
    val key: String,
    val label: String,
    val category: String
)

internal val CatalogIconOptions = listOf(
    CatalogIconOption("sun", "Soleil", "Habitudes"),
    CatalogIconOption("moon", "Sommeil", "Habitudes"),
    CatalogIconOption("eye", "Vue", "Habitudes"),
    CatalogIconOption("droplet", "Hydratation", "Habitudes"),
    CatalogIconOption("barbell", "Sport", "Habitudes"),
    CatalogIconOption("run", "Course", "Habitudes"),
    CatalogIconOption("heart", "Bien-être", "Habitudes"),
    CatalogIconOption("music", "Musique", "Habitudes"),
    CatalogIconOption("language", "Langues", "Habitudes"),
    CatalogIconOption("leaf", "Nature", "Habitudes"),
    CatalogIconOption("mood-smile", "Humeur", "Habitudes"),
    CatalogIconOption("sparkles", "Routine", "Habitudes"),
    CatalogIconOption("yoga", "Méditation", "Habitudes"),
    CatalogIconOption("clipboard-check", "Suivi", "Habitudes"),
    CatalogIconOption("plant", "Plante", "Habitudes"),
    CatalogIconOption("apple", "Alimentation", "Habitudes"),
    CatalogIconOption("book", "Lecture", "Études"),
    CatalogIconOption("books", "Bibliothèque", "Études"),
    CatalogIconOption("book-2", "Livre ouvert", "Études"),
    CatalogIconOption("brain", "Cognition", "Études"),
    CatalogIconOption("code", "Programmation", "Études"),
    CatalogIconOption("atom-2", "Physique", "Études"),
    CatalogIconOption("database", "Données", "Études"),
    CatalogIconOption("calculator", "Mathématiques", "Études"),
    CatalogIconOption("flask", "Chimie", "Études"),
    CatalogIconOption("chart-line", "Statistiques", "Études"),
    CatalogIconOption("bulb", "Idée", "Études"),
    CatalogIconOption("building-bank", "Économie", "Études"),
    CatalogIconOption("world", "Géographie", "Études"),
    CatalogIconOption("certificate", "Diplôme", "Études"),
    CatalogIconOption("device-laptop", "Informatique", "Études"),
    CatalogIconOption("microphone", "Oral", "Études"),
    CatalogIconOption("stethoscope", "Médecine", "Études"),
    CatalogIconOption("microscope", "Biologie", "Études"),
    CatalogIconOption("math-symbols", "Algèbre", "Études"),
    CatalogIconOption("network", "Réseaux", "Études"),
    CatalogIconOption("cloud", "Cloud", "Études"),
    CatalogIconOption("shield", "Cybersécurité", "Études"),
    CatalogIconOption("camera", "Création", "Autres"),
    CatalogIconOption("palette", "Arts", "Autres"),
    CatalogIconOption("briefcase", "Projet", "Autres"),
    CatalogIconOption("first-aid-kit", "Santé", "Autres"),
    CatalogIconOption("device-mobile-off", "Déconnexion", "Autres")
)

private val CatalogIconPalette = listOf(
    AppleIndigo,
    AppleRose,
    AppleAmber,
    AppleEmerald,
    AppleSky,
    Color(0xFF7C3AED),
    Color(0xFFEC4899)
)

fun catalogIconColor(index: Long): Color = CatalogIconPalette[index.toInt().mod(CatalogIconPalette.size)]

@Composable
fun CatalogColorPicker(
    selectedIndex: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CatalogIconPalette.forEachIndexed { index, color ->
            val selected = selectedIndex.toInt().mod(CatalogIconPalette.size) == index
            Surface(
                onClick = { onColorSelected(index.toLong()) },
                shape = CircleShape,
                color = color,
                border = BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .size(34.dp)
                    .semantics { contentDescription = "Couleur ${index + 1}" }
            ) { }
        }
    }
}

private fun iconResourceId(key: String): Int {
    val resourceName = when (key) {
        // Keep icon keys already stored by older habit records working.
        "water" -> "droplet"
        "smile" -> "mood-smile"
        "fitness" -> "barbell"
        "mind" -> "sparkles"
        "phone_off" -> "device-mobile-off"
        else -> key
    }.replace('-', '_')
    return when (resourceName) {
        "sun" -> R.drawable.ic_catalog_sun
        "moon" -> R.drawable.ic_catalog_moon
        "eye" -> R.drawable.ic_catalog_eye
        "droplet" -> R.drawable.ic_catalog_droplet
        "barbell" -> R.drawable.ic_catalog_barbell
        "run" -> R.drawable.ic_catalog_run
        "heart" -> R.drawable.ic_catalog_heart
        "music" -> R.drawable.ic_catalog_music
        "language" -> R.drawable.ic_catalog_language
        "leaf" -> R.drawable.ic_catalog_leaf
        "mood_smile" -> R.drawable.ic_catalog_mood_smile
        "sparkles" -> R.drawable.ic_catalog_sparkles
        "yoga" -> R.drawable.ic_catalog_yoga
        "clipboard_check" -> R.drawable.ic_catalog_clipboard_check
        "plant" -> R.drawable.ic_catalog_plant
        "apple" -> R.drawable.ic_catalog_apple
        "book" -> R.drawable.ic_catalog_book
        "books" -> R.drawable.ic_catalog_books
        "book_2" -> R.drawable.ic_catalog_book_2
        "brain" -> R.drawable.ic_catalog_brain
        "code" -> R.drawable.ic_catalog_code
        "atom_2" -> R.drawable.ic_catalog_atom_2
        "database" -> R.drawable.ic_catalog_database
        "calculator" -> R.drawable.ic_catalog_calculator
        "flask" -> R.drawable.ic_catalog_flask
        "chart_line" -> R.drawable.ic_catalog_chart_line
        "bulb" -> R.drawable.ic_catalog_bulb
        "building_bank" -> R.drawable.ic_catalog_building_bank
        "world" -> R.drawable.ic_catalog_world
        "certificate" -> R.drawable.ic_catalog_certificate
        "device_laptop" -> R.drawable.ic_catalog_device_laptop
        "microphone" -> R.drawable.ic_catalog_microphone
        "stethoscope" -> R.drawable.ic_catalog_stethoscope
        "microscope" -> R.drawable.ic_catalog_microscope
        "math_symbols" -> R.drawable.ic_catalog_math_symbols
        "network" -> R.drawable.ic_catalog_network
        "cloud" -> R.drawable.ic_catalog_cloud
        "shield" -> R.drawable.ic_catalog_shield
        "camera" -> R.drawable.ic_catalog_camera
        "palette" -> R.drawable.ic_catalog_palette
        "briefcase" -> R.drawable.ic_catalog_briefcase
        "first_aid_kit" -> R.drawable.ic_catalog_first_aid_kit
        "device_mobile_off" -> R.drawable.ic_catalog_device_mobile_off
        else -> R.drawable.ic_catalog_sparkles
    }
}

@Composable
fun CatalogIcon(
    iconKey: String,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(iconResourceId(iconKey)),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogIconPickerSheet(
    selectedIconKey: String,
    selectedColor: Color,
    onIconSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("Toutes") }
    val categories = listOf("Toutes", "Habitudes", "Études", "Autres")
    val filtered = remember(query, category) {
        CatalogIconOptions.filter { option ->
            (category == "Toutes" || option.category == category) &&
                (query.isBlank() || option.label.contains(query.trim(), ignoreCase = true) ||
                    option.key.contains(query.trim(), ignoreCase = true))
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(horizontal = 18.dp)
        ) {
            Text("Choisir une icône", style = MaterialTheme.typography.titleLarge)
            Text(
                "${CatalogIconOptions.size} icônes vectorielles · couleur personnalisable",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Rechercher") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { label ->
                    FilterChip(
                        selected = category == label,
                        onClick = { category = label },
                        label = { Text(label) }
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 74.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.key }) { option ->
                    val selected = selectedIconKey == option.key
                    Surface(
                        onClick = { onIconSelected(option.key); onDismiss() },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            if (selected) 1.5.dp else 0.7.dp,
                            if (selected) selectedColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.height(76.dp).padding(horizontal = 4.dp, vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CatalogIcon(
                                iconKey = option.key,
                                contentDescription = null,
                                tint = if (selected) selectedColor else catalogIconColor(option.key.hashCode().toLong().absoluteValue),
                                modifier = Modifier.size(23.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                option.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
