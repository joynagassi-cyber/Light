package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.theme.LightSpacing

/** Connectivity is not inferred from errors returned by Firestore or a provider. */
@Composable
internal fun OfflineBanner(isOnline: Boolean) {
    if (isOnline) return
    val scheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = LightSpacing.two, vertical = LightSpacing.one)
            .testTag("home_offline_banner"),
        shape = MaterialTheme.shapes.medium,
        color = scheme.secondaryContainer,
        contentColor = scheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(LightSpacing.two),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LightSpacing.oneAndHalf)
        ) {
            Icon(Icons.Rounded.CloudOff, contentDescription = null)
            Text(stringResource(R.string.home_offline), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
