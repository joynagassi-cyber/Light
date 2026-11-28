package com.example.a2ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Camera
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Handler appelé avec une ligne JSON `{"a2uiUserAction":…}` à renvoyer au modèle,
 * ou `{"a2uiClientError":…}` quand la surface est inexploitable.
 * Par défaut : no-op (aperçus, tests).
 */
val LocalA2uiActionHandler = compositionLocalOf<(String) -> Unit> { { } }

/**
 * Rend les surfaces A2UI portées par un message d'assistant. [messageId] identifie le
 * message (décision de feed unique lors de la restauration), [isGenerating] évite tout
 * renvoi d'erreur à l'agent pendant le streaming (le flux est incomplet par construction).
 */
@Composable
internal fun A2uiMessageSurfaces(
    messageId: String,
    extraction: A2uiExtraction,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    val surfaceIds = remember(extraction) { extraction.blocks.flatMap { it.surfaceIds }.distinct() }
    val handler = LocalA2uiActionHandler.current
    val blockErrors = remember(extraction) { extraction.blocks.flatMap { it.errors } }

    // Applique les messages une fois par empreinte de contenu (voir LightA2uiRuntime.feed).
    LaunchedEffect(extraction) { LightA2uiRuntime.feed(extraction) }

    // Les erreurs de parsing ne sont signalées au modèle que lorsque la réponse est complète.
    LaunchedEffect(blockErrors, isGenerating) {
        if (!isGenerating && blockErrors.isNotEmpty()) {
            handler(serializeClientError(surfaceIds.firstOrNull() ?: "global", blockErrors.joinToString("\n")))
        }
    }

    if (surfaceIds.isEmpty()) return

    val surfaces by LightA2uiRuntime.surfaces.collectAsState()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (id in surfaceIds) {
            val surface = surfaces[id] ?: continue
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("a2ui_surface"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(Modifier.padding(12.dp).fillMaxWidth()) {
                    A2uiSurfaceContent(surface = surface)
                }
            }
        }
    }
}


/** Contenu d'une surface : rend le composant `root`, avec un placeholder pendant le chargement. */
@Composable
private fun A2uiSurfaceContent(surface: A2uiSurface) {
    val components by surface.components.collectAsState()
    val model by surface.dataModel.collectAsState()
    val root = components["root"]
    if (root == null) {
        Text(
            text = "Chargement de l’interface…",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    A2uiComponentView(
        surface = surface,
        def = root,
        basePath = "",
        model = model,
        all = components,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun JsonElement?.weightOrNull(): Float? =
    (this as? JsonPrimitive)?.doubleOrNull?.toFloat()?.takeIf { it > 0f }

private fun jsonStringList(el: JsonElement?): List<String> =
    (el as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull }

/** Rend un composant A2UI et ses enfants récursivement. */
@Composable
private fun A2uiComponentView(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier = Modifier,
) {
    when (def.component) {
        "Column" -> A2uiColumn(surface, def, basePath, model, all, modifier)
        "Row" -> A2uiRow(surface, def, basePath, model, all, modifier)
        "List" -> A2uiList(surface, def, basePath, model, all, modifier)
        "Text" -> A2uiText(def, basePath, model, modifier)
        "Image" -> A2uiImage(def, basePath, model, modifier)
        "Icon" -> A2uiIcon(def, basePath, model, modifier)
        "Card" -> A2uiCard(surface, def, basePath, model, all, modifier)
        "Divider" -> A2uiDivider(def, modifier)
        "Button" -> A2uiButton(surface, def, basePath, model, all, modifier)
        "TextField" -> A2uiTextField(surface, def, basePath, model, modifier)
        "CheckBox" -> A2uiCheckBox(surface, def, basePath, model, modifier)
        "Slider" -> A2uiSlider(surface, def, basePath, model, modifier)
        "ChoicePicker" -> A2uiChoicePicker(surface, def, basePath, model, modifier)
        "Tabs" -> A2uiTabs(surface, def, basePath, model, all, modifier)
        else -> UnsupportedComponent(def.component, modifier)
    }
}

@Composable
private fun UnsupportedComponent(name: String, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Text(
            text = "Élément d’interface non pris en charge : $name",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(10.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Conteneurs
// ---------------------------------------------------------------------------

@Composable
private fun A2uiColumn(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier,
) {
    val justify = (def.props["justify"] as? JsonPrimitive)?.contentOrNull ?: "start"
    val align = (def.props["align"] as? JsonPrimitive)?.contentOrNull ?: "stretch"
    val arrangement = when (justify) {
        "center" -> Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        "end" -> Arrangement.spacedBy(8.dp, Alignment.Bottom)
        "spaceBetween" -> Arrangement.SpaceBetween
        "spaceAround" -> Arrangement.SpaceAround
        "spaceEvenly" -> Arrangement.SpaceEvenly
        "stretch" -> Arrangement.spacedBy(8.dp, Alignment.Top)
        else -> Arrangement.spacedBy(8.dp, Alignment.Top)
    }
    val horizontalAlignment = when (align) {
        "center" -> Alignment.CenterHorizontally
        "end" -> Alignment.End
        else -> Alignment.Start
    }
    val stretch = align == "stretch"
    Column(
        modifier = if (stretch) modifier.fillMaxWidth() else modifier,
        verticalArrangement = arrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        for ((childId, childBasePath) in childListReferences(def.props["children"], model, basePath)) {
            val childDef = all[childId] ?: continue
            // Le poids doit être créé dans le scope Column ; `key` n'en fournit aucun.
            val weight = childDef.props["weight"].weightOrNull()
            val childModifier = when {
                weight != null -> Modifier.weight(weight)
                stretch -> Modifier.fillMaxWidth()
                else -> Modifier
            }
            androidx.compose.runtime.key(childId, childBasePath) {
                A2uiComponentView(surface, childDef, childBasePath, model, all, childModifier)
            }
        }
    }
}

@Composable
private fun A2uiRow(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier,
) {
    val justify = (def.props["justify"] as? JsonPrimitive)?.contentOrNull ?: "start"
    val align = (def.props["align"] as? JsonPrimitive)?.contentOrNull ?: "start"
    val arrangement = when (justify) {
        "center" -> Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        "end" -> Arrangement.spacedBy(8.dp, Alignment.End)
        "spaceBetween" -> Arrangement.SpaceBetween
        "spaceAround" -> Arrangement.SpaceAround
        "spaceEvenly" -> Arrangement.SpaceEvenly
        else -> Arrangement.spacedBy(8.dp, Alignment.Start)
    }
    val verticalAlignment = when (align) {
        "center" -> Alignment.CenterVertically
        "end", "stretch" -> Alignment.Bottom
        else -> Alignment.Top
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = arrangement,
        verticalAlignment = verticalAlignment,
    ) {
        for ((childId, childBasePath) in childListReferences(def.props["children"], model, basePath)) {
            val childDef = all[childId] ?: continue
            // Idem : le poids doit être créé dans le scope Row.
            val weight = childDef.props["weight"].weightOrNull()
            val childModifier = if (weight != null) Modifier.weight(weight) else Modifier
            androidx.compose.runtime.key(childId, childBasePath) {
                A2uiComponentView(surface, childDef, childBasePath, model, all, childModifier)
            }
        }
    }
}

@Composable
private fun A2uiList(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier,
) {
    val direction = (def.props["direction"] as? JsonPrimitive)?.contentOrNull ?: "vertical"
    val scroll = rememberScrollState()
    val containerModifier = if (direction == "horizontal") {
        modifier.horizontalScroll(scroll)
    } else {
        modifier.verticalScroll(scroll)
    }
    if (direction == "horizontal") {
        Row(modifier = containerModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            A2uiListChildren(surface, def, basePath, model, all, horizontal = true)
        }
    } else {
        Column(modifier = containerModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            A2uiListChildren(surface, def, basePath, model, all, horizontal = false)
        }
    }
}

@Composable
private fun A2uiListChildren(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    horizontal: Boolean,
) {
    for ((childId, childBasePath) in childListReferences(def.props["children"], model, basePath)) {
        val childDef = all[childId] ?: continue
        androidx.compose.runtime.key(childId, childBasePath) {
            A2uiComponentView(
                surface,
                childDef,
                childBasePath,
                model,
                all,
                if (horizontal) Modifier else Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun A2uiCard(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier,
) {
    val childId = (def.props["child"] as? JsonPrimitive)?.contentOrNull
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.padding(12.dp).fillMaxWidth()) {
            val childDef = childId?.let { all[it] }
            if (childDef != null) {
                A2uiComponentView(surface, childDef, basePath, model, all, Modifier.fillMaxWidth())
            } else {
                Text(
                    text = "Carte vide",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun A2uiDivider(def: A2uiComponentDef, modifier: Modifier) {
    val axis = (def.props["axis"] as? JsonPrimitive)?.contentOrNull ?: "horizontal"
    if (axis == "vertical") {
        VerticalDivider(modifier = modifier.height(24.dp), color = MaterialTheme.colorScheme.outlineVariant)
    } else {
        HorizontalDivider(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ---------------------------------------------------------------------------
// Composants feuille
// ---------------------------------------------------------------------------

@Composable
private fun A2uiText(
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val text = resolveDynamicString(def.props["text"], model, basePath) ?: "…"
    val variant = (def.props["variant"] as? JsonPrimitive)?.contentOrNull ?: "body"
    val style = when (variant) {
        "h1" -> MaterialTheme.typography.headlineMedium
        "h2" -> MaterialTheme.typography.headlineSmall
        "h3" -> MaterialTheme.typography.titleLarge
        "h4" -> MaterialTheme.typography.titleMedium
        "h5" -> MaterialTheme.typography.titleSmall
        "caption" -> MaterialTheme.typography.labelMedium
        else -> MaterialTheme.typography.bodyMedium
    }
    val textModifier = if (variant in setOf("h1", "h2", "h3", "h4", "h5")) {
        modifier.semantics { heading() }
    } else {
        modifier
    }
    Text(
        text = text,
        style = style,
        color = if (variant == "caption") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        modifier = textModifier,
    )
}

@Composable
private fun A2uiImage(
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val url = resolveDynamicString(def.props["url"], model, basePath)
    val description = resolveDynamicString(def.props["description"], model, basePath)
    val variant = (def.props["variant"] as? JsonPrimitive)?.contentOrNull ?: "mediumFeature"
    val fit = (def.props["fit"] as? JsonPrimitive)?.contentOrNull ?: "fill"
    val imageHeight = when (variant) {
        "icon" -> 40.dp
        "avatar" -> 56.dp
        "smallFeature" -> 120.dp
        "largeFeature" -> 260.dp
        "header" -> 180.dp
        else -> 160.dp
    }
    val contentScale = when (fit) {
        "contain" -> ContentScale.Fit
        "none" -> ContentScale.None
        "scaleDown" -> ContentScale.Inside
        "cover" -> ContentScale.Crop
        else -> ContentScale.FillBounds
    }
    if (url.isNullOrBlank()) {
        Box(
            modifier = modifier.fillMaxWidth().height(imageHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        return
    }
    if (variant == "avatar") {
        AsyncImage(
            model = url,
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(56.dp).clip(CircleShape),
        )
        return
    }
    AsyncImage(
        model = url,
        contentDescription = description,
        contentScale = contentScale,
        modifier = modifier.fillMaxWidth().height(imageHeight).clip(RoundedCornerShape(12.dp)),
    )
}

private fun builtInIcon(name: String) = when (name.lowercase()) {
    "accountcircle" -> Icons.Rounded.AccountCircle
    "add" -> Icons.Rounded.Add
    "arrowback" -> Icons.Rounded.ArrowBack
    "arrowforward" -> Icons.Rounded.ArrowForward
    "attachfile" -> Icons.Rounded.AttachFile
    "calendartoday" -> Icons.Rounded.CalendarToday
    "call" -> Icons.Rounded.Call
    "camera" -> Icons.Rounded.Camera
    "check" -> Icons.Rounded.Check
    "checkcircle", "check_circle" -> Icons.Rounded.CheckCircle
    "close" -> Icons.Rounded.Close
    "delete" -> Icons.Rounded.Delete
    "download" -> Icons.Rounded.Download
    "edit" -> Icons.Rounded.Edit
    "error" -> Icons.Rounded.Error
    "event", "calendar" -> Icons.Rounded.Event
    "fastforward" -> Icons.Rounded.FastForward
    "favorite" -> Icons.Rounded.Favorite
    "favoriteoff" -> Icons.Rounded.FavoriteBorder
    "folder" -> Icons.Rounded.Folder
    "home" -> Icons.Rounded.Home
    "info" -> Icons.Rounded.Info
    "person", "account" -> Icons.Rounded.Person
    "search" -> Icons.Rounded.Search
    "send" -> Icons.Rounded.Send
    "settings" -> Icons.Rounded.Settings
    "shoppingcart", "cart" -> Icons.Rounded.ShoppingCart
    "star" -> Icons.Rounded.Star
    "warning" -> Icons.Rounded.Warning
    else -> Icons.Rounded.Help
}

@Composable
private fun A2uiIcon(
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val nameElement = def.props["name"]
    val builtInName: String? = when (val resolved = resolveDynamic(nameElement, model, basePath)) {
        is JsonPrimitive -> resolved.contentOrNull
        is JsonObject -> (resolved["name"] as? JsonPrimitive)?.contentOrNull
        else -> null
    }
    // {"svgPath": "…"} : le rendu vectoriel de SVG path n'est pas disponible ; on affiche un pictogramme générique.
    val vector = builtInIcon(builtInName ?: "")
    Icon(
        imageVector = vector,
        contentDescription = builtInName,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.size(24.dp),
    )
}

@Composable
private fun A2uiButton(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val handler = LocalA2uiActionHandler.current
    val variant = (def.props["variant"] as? JsonPrimitive)?.contentOrNull ?: "default"
    val childId = (def.props["child"] as? JsonPrimitive)?.contentOrNull
    val childDef = childId?.let { all[it] }
    val onClick = {
        dispatchA2uiAction(
            context = context,
            surfaceId = surface.id,
            componentId = def.id,
            action = def.props["action"],
            model = model,
            basePath = basePath,
            handler = handler,
        )
    }
    val content: @Composable () -> Unit = {
        if (childDef != null) {
            A2uiComponentView(surface, childDef, basePath, model, all)
        } else {
            Text("Action")
        }
    }
    when (variant) {
        "primary" -> Button(onClick = onClick, modifier = modifier, content = { content() })
        "borderless" -> TextButton(onClick = onClick, modifier = modifier, content = { content() })
        else -> OutlinedButton(onClick = onClick, modifier = modifier, content = { content() })
    }
}

private fun dispatchA2uiAction(
    context: Context,
    surfaceId: String,
    componentId: String,
    action: JsonElement?,
    model: JsonObject,
    basePath: String,
    handler: (String) -> Unit,
) {
    val actionObj = action as? JsonObject ?: return
    (actionObj["event"] as? JsonObject)?.let { event ->
        val name = (event["name"] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() } ?: return
        val resolved = resolveActionContext(event["context"] as? JsonObject, model, basePath)
        handler(serializeUserAction(surfaceId, componentId, name, resolved))
        return
    }
    (actionObj["functionCall"] as? JsonObject)?.let { functionCall ->
        val call = (functionCall["call"] as? JsonPrimitive)?.contentOrNull
        if (call == "openUrl") {
            val url = resolveDynamicString((functionCall["args"] as? JsonObject)?.get("url"), model, basePath)
            if (!url.isNullOrBlank() && (url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true))) {
                runCatching {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url)).addCategory(Intent.CATEGORY_BROWSABLE)
                    )
                }
            }
        } else {
            handler(serializeClientError(surfaceId, "Fonction client non prise en charge : ${call ?: "?"}"))
        }
    }
}

@Composable
private fun A2uiTextField(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val label = resolveDynamicString(def.props["label"], model, basePath) ?: ""
    val rawValue = def.props["value"]
    val path = bindingPath(rawValue, basePath)
    val currentValue = resolveDynamicString(rawValue, model, basePath) ?: ""
    val variant = (def.props["variant"] as? JsonPrimitive)?.contentOrNull ?: "shortText"
    val singleLine = variant == "shortText" || variant == "number" || variant == "obscured"
    val keyboardType = when (variant) {
        "number" -> KeyboardType.Number
        else -> KeyboardType.Text
    }
    val visualTransformation = if (variant == "obscured") PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    OutlinedTextField(
        value = currentValue,
        onValueChange = { newValue ->
            if (path != null) LightA2uiRuntime.writePointer(surface.id, path, JsonPrimitive(newValue))
        },
        label = { Text(label) },
        singleLine = singleLine,
        minLines = if (variant == "longText") 3 else 1,
        readOnly = path == null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(),
    )
}

@Composable
private fun A2uiCheckBox(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val label = resolveDynamicString(def.props["label"], model, basePath) ?: ""
    val rawValue = def.props["value"]
    val path = bindingPath(rawValue, basePath)
    val checked = resolveDynamicBoolean(rawValue, model, basePath) ?: false
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { newValue ->
                if (path != null) LightA2uiRuntime.writePointer(surface.id, path, JsonPrimitive(newValue))
            },
            enabled = path != null,
        )
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun A2uiSlider(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val label = resolveDynamicString(def.props["label"], model, basePath)
    val rawValue = def.props["value"]
    val path = bindingPath(rawValue, basePath)
    val min = resolveDynamicFloat(def.props["min"], model, basePath) ?: 0f
    val max = resolveDynamicFloat(def.props["max"], model, basePath) ?: 100f
    val value = (resolveDynamicFloat(rawValue, model, basePath) ?: min).coerceIn(min, max)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (!label.isNullOrBlank()) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        androidx.compose.material3.Slider(
            value = value,
            onValueChange = { newValue ->
                if (path != null) LightA2uiRuntime.writePointer(surface.id, path, JsonPrimitive(newValue.toDouble()))
            },
            valueRange = if (min < max) min..max else 0f..100f,
            enabled = path != null,
        )
    }
}

private data class A2uiChoiceOption(val label: String, val value: String)

@Composable
private fun A2uiChoicePicker(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    modifier: Modifier,
) {
    val label = resolveDynamicString(def.props["label"], model, basePath)
    val variant = (def.props["variant"] as? JsonPrimitive)?.contentOrNull ?: "mutuallyExclusive"
    val displayStyle = (def.props["displayStyle"] as? JsonPrimitive)?.contentOrNull ?: "checkbox"
    val options = (resolveDynamic(def.props["options"], model, basePath) as? JsonArray).orEmpty().mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        val optionLabel = (obj["label"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
        val optionValue = (obj["value"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
        A2uiChoiceOption(optionLabel, optionValue)
    }
    val rawValue = def.props["value"]
    val path = bindingPath(rawValue, basePath)
    val selected = jsonStringList(resolveDynamic(rawValue, model, basePath))
    fun toggle(target: String) {
        if (path == null) return
        val next = when {
            variant == "multipleSelection" ->
                if (target in selected) selected - target else selected + target
            target in selected -> selected // radio : conserver la sélection courante
            else -> listOf(target)
        }
        LightA2uiRuntime.writePointer(
            surface.id,
            path,
            JsonArray(next.map { JsonPrimitive(it) }),
        )
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (!label.isNullOrBlank()) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        for (option in options) {
            val isSelected = option.value in selected
            if (displayStyle == "chips") {
                FilterChip(
                    selected = isSelected,
                    onClick = { toggle(option.value) },
                    label = { Text(option.label) },
                    enabled = path != null,
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(enabled = path != null) { toggle(option.value) },
                ) {
                    Checkbox(checked = isSelected, onCheckedChange = { toggle(option.value) }, enabled = path != null)
                    Text(text = option.label, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun A2uiTabs(
    surface: A2uiSurface,
    def: A2uiComponentDef,
    basePath: String,
    model: JsonObject,
    all: Map<String, A2uiComponentDef>,
    modifier: Modifier,
) {
    data class A2uiTab(val title: String, val childId: String)

    val tabs = (def.props["tabs"] as? JsonArray).orEmpty().mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        val title = (obj["title"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
        val childId = (obj["childId"] as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
        A2uiTab(title, childId)
    }
    var selected by remember(def.id) { mutableIntStateOf(0) }
    if (tabs.isEmpty()) {
        UnsupportedComponent("Tabs (vides)", modifier)
        return
    }
    val safeIndex = selected.coerceIn(0, tabs.lastIndex)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PrimaryTabRow(selectedTabIndex = safeIndex) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = safeIndex == index,
                    onClick = { selected = index },
                    text = { Text(tab.title) },
                )
            }
        }
        val childDef = all[tabs[safeIndex].childId]
        if (childDef != null) {
            A2uiComponentView(surface, childDef, basePath, model, all, Modifier.fillMaxWidth())
        }
    }
}
