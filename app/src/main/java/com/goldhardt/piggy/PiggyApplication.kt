package com.goldhardt.piggy

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration // <-- CHECK THIS IMPORT
import com.goldhardt.piggy.notifications.ExpenseNotificationHelper
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PiggyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ExpenseNotificationHelper.ensureChannel(this)
    }

    // This is the correct way to override the method from the interface
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}