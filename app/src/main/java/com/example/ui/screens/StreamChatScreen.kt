package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.messages.ChannelScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.ChannelViewModelFactory
import io.getstream.chat.android.models.InitializationState
import com.example.chat.StreamChatManager

/**
 * Écran "Messages" propulsé par Stream Chat : liste des channels, puis écran de
 * conversation. Les identifiants utilisés sont ceux de démonstration du tutoriel
 * Stream (voir [StreamChatManager]).
 */
@Composable
fun StreamChatScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val chatClient = remember { StreamChatManager.getClient(appContext) }

    LaunchedEffect(Unit) {
        StreamChatManager.connectIfNeeded(appContext)
    }

    val initializationState by chatClient.clientState.initializationState.collectAsState(
        initial = chatClient.clientState.initializationState.value
    )
    var selectedCid by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("stream_chat_screen")
    ) {
        when (initializationState) {
            InitializationState.COMPLETE -> {
                val cid = selectedCid
                if (cid == null) {
                    ChatTheme {
                        ChannelsScreen(
                            title = "Messages",
                            isShowingHeader = true,
                            onChannelClick = { channel -> selectedCid = channel.cid },
                            onBackPressed = onBack
                        )
                    }
                } else {
                    ChatTheme {
                        ChannelScreen(
                            viewModelFactory = remember(cid) {
                                ChannelViewModelFactory(
                                    context = appContext,
                                    channelId = cid
                                )
                            },
                            onBackPressed = { selectedCid = null }
                        )
                    }
                }
            }

            InitializationState.INITIALIZING -> StreamChatStatus(
                message = "Connexion à Stream Chat…",
                showProgress = true
            )

            InitializationState.NOT_INITIALIZED -> StreamChatStatus(
                message = "Préparation du chat…",
                showProgress = true
            )
        }
    }
}

@Composable
private fun StreamChatStatus(
    message: String,
    showProgress: Boolean
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showProgress) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
