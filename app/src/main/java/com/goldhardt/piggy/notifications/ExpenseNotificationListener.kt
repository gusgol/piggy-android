package com.goldhardt.piggy.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class ExpenseNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val extras = sbn.notification.extras
            val isSelf = sbn.packageName == packageName
            val isTest = extras.getBoolean(TEST_EXTRA_KEY, false)
            if (isSelf && !isTest) return

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            val body = bigText ?: text

            Log.d(TAG, "Notif from ${sbn.packageName} -> title='$title' body='$body'")

            val suggestion = ExpenseNotificationProcessor.parseSuggestion(title, body)
            if (suggestion != null) {
                Log.d(TAG, "Parsed suggestion: $suggestion")
                ExpenseNotificationHelper.showSuggestion(
                    context = applicationContext,
                    suggestion = suggestion,
                    sourceNotificationKey = sbn.key
                )
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Error handling notification", t)
        }
    }

    companion object {
        private const val TAG = "ExpenseNotifListener"
        const val TEST_EXTRA_KEY = "com.goldhardt.piggy.EXTRA_TEST_SOURCE"
    }
}
