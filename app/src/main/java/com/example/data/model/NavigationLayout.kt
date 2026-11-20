package com.example.data.model

/** Stable IDs shared by the editor, both navigation layouts, and the Firestore document. */
object NavigationOptions {
    val features = listOf("home", "planner", "focus", "matrix")
    val optionalFeatures = features.drop(1)
    val icons = listOf("bookmark", "star", "circle", "bolt")
    const val maxHidden = 2
}

data class NavigationLayout(
    val order: List<String> = NavigationOptions.features,
    val hidden: List<String> = emptyList(),
    val icons: Map<String, String> = emptyMap(),
) {
    val visible: List<String> get() = order.filterNot { it in hidden }

    fun normalized(): NavigationLayout {
        val known = order.filter { it in NavigationOptions.features }.distinct()
        return NavigationLayout(
            order = known + NavigationOptions.features.filterNot { it in known },
            hidden = hidden.filter { it in NavigationOptions.optionalFeatures }.distinct().take(NavigationOptions.maxHidden),
            icons = icons.filter { (route, icon) -> route in NavigationOptions.features && icon in NavigationOptions.icons },
        )
    }

    fun move(route: String, direction: Int): NavigationLayout {
        val from = order.indexOf(route)
        val to = from + direction
        if (from < 0 || to !in order.indices || direction !in listOf(-1, 1)) return this
        return copy(order = order.toMutableList().apply { add(to, removeAt(from)) })
    }

    fun setVisible(route: String, visible: Boolean): NavigationLayout {
        if (route !in NavigationOptions.optionalFeatures) return this
        if (visible) return copy(hidden = hidden - route)
        if (route in hidden || hidden.size >= NavigationOptions.maxHidden) return this
        return copy(hidden = hidden + route)
    }
}
