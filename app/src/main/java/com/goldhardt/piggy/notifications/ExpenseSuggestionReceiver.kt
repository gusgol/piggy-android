package com.goldhardt.piggy.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class ExpenseSuggestionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val amount = intent.getDoubleExtra(ExpenseNotificationHelper.Extras.KEY_AMOUNT, Double.NaN)
        val description = intent.getStringExtra(ExpenseNotificationHelper.Extras.KEY_DESCRIPTION)
        val notifId = intent.getIntExtra(ExpenseNotificationHelper.Extras.KEY_SUGGESTION_NOTIF_ID, -1)

        when (action) {
            ExpenseNotificationHelper.ACTION_ADD -> {
                if (!amount.isNaN() && !description.isNullOrBlank()) {
                    val input = Data.Builder()
                        .putDouble(SaveExpenseWorker.KEY_AMOUNT, amount)
                        .putString(SaveExpenseWorker.KEY_DESCRIPTION, description)
                        .build()

                    val request = OneTimeWorkRequestBuilder<SaveExpenseWorker>()
                        .setInputData(input)
                        .build()

                    WorkManager.getInstance(context).enqueue(request)
                    Log.d(TAG, "Enqueued SaveExpenseWorker for '$description' - $amount")
                } else {
                    Log.d(TAG, "Invalid suggestion data; skipping enqueue")
                }
                if (notifId != -1) ExpenseNotificationHelper.cancel(context, notifId)
            }
            ExpenseNotificationHelper.ACTION_DISMISS -> {
                if (notifId != -1) ExpenseNotificationHelper.cancel(context, notifId)
                Log.d(TAG, "Suggestion dismissed")
            }
        }
    }

    companion object {
        private const val TAG = "ExpenseSuggestRx"
    }
}
