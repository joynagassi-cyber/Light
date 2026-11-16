package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.NotificationFrequency
import com.example.data.model.UserGender
import com.example.data.model.UserProfession

object LightNotificationHelper {
    private const val CHANNEL_ID = "light_study_reminders"
    private const val CHANNEL_NAME = "Rappels Light"
    private const val CHANNEL_DESC = "Rappels d'étude, sessions Focus et suivi d'habitudes"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendOnboardingConfiguredNotification(
        context: Context,
        displayName: String,
        gender: UserGender,
        profession: UserProfession,
        customProfession: String,
        frequency: NotificationFrequency
    ) {
        if (!hasNotificationPermission(context) || frequency == NotificationFrequency.OFF) return
        ensureChannel(context)

        val role = profession.labelFor(gender, customProfession)
        val ready = if (gender == UserGender.FEMININE) "Prête" else "Prêt"
        val body = "$ready pour votre espace $role ! Cadence active : ${frequency.label} (${frequency.description})."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Light configuré pour $displayName")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(1001, notification)
        }
    }
}
