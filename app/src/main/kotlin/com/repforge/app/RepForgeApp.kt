package com.repforge.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.repforge.core.database.seed.DatabaseSeeder
import com.repforge.core.notifications.Channels
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class RepForgeApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var seeder: DatabaseSeeder

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        Channels.ensureAll(this)
        com.repforge.core.notifications.liveupdate.LiveWorkoutNotifier(this).ensureChannel()
        CoroutineScope(Dispatchers.IO).launch { seeder.seedIfNeeded() }
    }
}
