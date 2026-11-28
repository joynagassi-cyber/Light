package com.example.chat

import android.content.Context
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.StateFlow

/**
 * Client Stream Chat partagé pour Light.
 *
 * Les identifiants ci-dessous sont ceux de démonstration PUBLIQUES du tutoriel officiel
 * Stream (Android Chat Messaging avec Jetpack Compose). Ils permettent d'essayer le chat
 * immédiatement, mais toutes les installations de l'app partagent le même utilisateur
 * `tutorial-droid`.
 *
 * Pour aller en production : créer son application sur le dashboard Stream, renseigner
 * sa propre clé API, et surtout émettre les tokens de connexion côté backend (jamais un
 * secret côté client). Remplacer [DEMO_API_KEY] / [DEMO_USER_TOKEN] à ce moment-là.
 */
object StreamChatManager {

    private const val TAG = "StreamChatManager"

    // Credentials de démonstration publics du tutoriel officiel Stream (jetpack compose tutorial)
    private const val DEMO_API_KEY = "uun7ywwamhs9"
    private const val DEMO_USER_ID = "tutorial-droid"
    private const val DEMO_USER_NAME = "Tutorial Droid"
    private const val DEMO_USER_IMAGE = "https://bit.ly/2TIt8NR"
    private const val DEMO_USER_TOKEN =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoidHV0b3JpYWwtZHJvaWQifQ.WwfBzU1GZr0brt_fXnqKdKhz3oj0rbDUm2DqJO_SS5U"

    @Volatile
    private var client: ChatClient? = null

    fun getClient(context: Context): ChatClient = client ?: synchronized(this) {
        client ?: ChatClient.Builder(DEMO_API_KEY, context.applicationContext)
            .config(
                ChatClientConfig(
                    userPresence = true,
                    isLocalUnreadCountEnabled = true
                )
            )
            .logLevel(ChatLogLevel.NOTHING)
            .appName("Light")
            .build()
            .also { client = it }
    }

    /** Connecte l'utilisateur de démonstration une seule fois (appel idempotent). */
    fun connectIfNeeded(context: Context) {
        val chatClient = getClient(context)
        val state = chatClient.clientState.initializationState.value
        if (state != InitializationState.NOT_INITIALIZED) return
        Log.d(TAG, "Connecting tutorial user to Stream Chat…")
        chatClient.connectUser(
            user = User(
                id = DEMO_USER_ID,
                name = DEMO_USER_NAME,
                image = DEMO_USER_IMAGE
            ),
            token = DEMO_USER_TOKEN
        ).enqueue()
    }

    fun initializationState(context: Context): StateFlow<InitializationState> =
        getClient(context).clientState.initializationState
}
