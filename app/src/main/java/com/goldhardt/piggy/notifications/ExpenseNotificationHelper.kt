package com.goldhardt.piggy.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.goldhardt.piggy.R
import java.util.Locale

object ExpenseNotificationHelper {
    const val CHANNEL_ID = "expense_channel"

    const val ACTION_ADD = "com.goldhardt.piggy.action.ADD_EXPENSE"
    const val ACTION_DISMISS = "com.goldhardt.piggy.action.DISMISS_SUGGESTION"

    fun ensureChannel(context: Context) {
        val name = "Expense Suggestions"
        val descriptionText = "Suggestions to add expenses from received notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
            lightColor = Color.GREEN
            enableVibration(true)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun showSuggestion(
        context: Context,
        suggestion: ExpenseSuggestion,
        sourceNotificationKey: String? = null,
        notificationId: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
    ) {
        ensureChannel(context)

        val title = "Add Expense?"
        val text = "${suggestion.description} - ${formatAmount(suggestion.amount)}"

        val baseIntent = Intent(context, ExpenseSuggestionReceiver::class.java).apply {
            putExtra(Extras.KEY_AMOUNT, suggestion.amount)
            putExtra(Extras.KEY_DESCRIPTION, suggestion.description)
            putExtra(Extras.KEY_SOURCE_NOTIF_KEY, sourceNotificationKey)
            putExtra(Extras.KEY_SUGGESTION_NOTIF_ID, notificationId)
        }

        val addIntent = Intent(baseIntent).apply { action = ACTION_ADD }
        val dismissIntent = Intent(baseIntent).apply { action = ACTION_DISMISS }

        val addPending = PendingIntent.getBroadcast(
            context,
            notificationId,
            addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val dismissPending = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(0, "✅ Add", addPending)
            .addAction(0, "❌ Dismiss", dismissPending)
            .setAutoCancel(true)

        val nm = NotificationManagerCompat.from(context)
        try {
            if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                nm.notify(notificationId, builder.build())
            }
        } catch (se: SecurityException) {
            // Silently ignore if notifications are not allowed
        }
    }

    fun cancel(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun formatAmount(amount: Double): String {
        // Simple USD formatting for now
        return "$" + String.format(Locale.getDefault(), "%.2f", amount)
    }

    object Extras {
        const val KEY_AMOUNT = "extra_amount"
        const val KEY_DESCRIPTION = "extra_description"
        const val KEY_SOURCE_NOTIF_KEY = "extra_source_notification_key"
        const val KEY_SUGGESTION_NOTIF_ID = "extra_suggestion_notification_id"
    }
}
