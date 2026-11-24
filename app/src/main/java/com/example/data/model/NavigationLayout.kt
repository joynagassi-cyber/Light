package com.example.data.model

/** Stable IDs shared by the editor, both navigation layouts, and the Firestore document. */
object NavigationOptions {
    val features = listOf(
        "home", "planner", "focus", "matrix",
        "library", "agent", "search", "progress", "habits", "countdown", "projects",
        "focus_profiles", "focus_analytics"
    )
    val primaryFeatures = listOf("home", "planner", "focus", "matrix")
    val optionalFeatures = features.filterNot { it == "home" }
    val icons = listOf("bookmark", "star", "circle", "bolt", "home", "calendar", "timer", "grid", "book", "auto_awesome", "search", "bar_chart", "self_improvement", "event", "date_range", "school", "insights")
    const val maxHidden = 8
    const val maxVisible = 4
}

data class NavigationLayout(
    val order: List<String> = NavigationOptions.features,
    val hidden: List<String> = NavigationOptions.features.filterNot { it in NavigationOptions.primaryFeatures },
    val icons: Map<String, String> = emptyMap(),
) {
    val visible: List<String> get() = order.filterNot { it in hidden }.take(NavigationOptions.maxVisible)

    fun normalized(): NavigationLayout {
        val known = order.filter { it in NavigationOptions.features }.distinct()
        val fullOrder = known + NavigationOptions.features.filterNot { it in known }
        return NavigationLayout(
            order = fullOrder,
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
