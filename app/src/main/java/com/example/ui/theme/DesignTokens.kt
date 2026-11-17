package com.example.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 8-point rhythm from the UX/UI playbook. Use half-steps only for optical alignment. */
object LightSpacing {
    val hairline: Dp = 1.dp
    val half: Dp = 4.dp
    val one: Dp = 8.dp
    val oneAndHalf: Dp = 12.dp
    val two: Dp = 16.dp
    val three: Dp = 24.dp
    val four: Dp = 32.dp
    val six: Dp = 48.dp
}

/** Recommended mobile hit area; visual icons may be smaller inside this target. */
object LightTouchTarget {
    val minimum: Dp = 48.dp
}

/** Shared readable type sizes; labels never fall below 12sp. */
object LightTypeScale {
    val micro = 12.sp
    val small = 14.sp
    val body = 16.sp
    val lead = 20.sp
    val title = 24.sp
    val display = 32.sp
}
