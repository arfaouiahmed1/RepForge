package com.repforge.core.database.di

import android.content.Context
import androidx.room.Room
import com.repforge.core.database.RepForgeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RepForgeDatabase =
        Room.databaseBuilder(context, RepForgeDatabase::class.java, "repforge.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .fallbackToDestructiveMigration() // v1->v2 adds profile/metrics/achievements
            .build()

    @Provides fun provideExerciseDao(db: RepForgeDatabase) = db.exerciseDao()
    @Provides fun provideRoutineDao(db: RepForgeDatabase) = db.routineDao()
    @Provides fun provideRoutineExerciseDao(db: RepForgeDatabase) = db.routineExerciseDao()
    @Provides fun provideSessionDao(db: RepForgeDatabase) = db.trainingSessionDao()
    @Provides fun provideSetLogDao(db: RepForgeDatabase) = db.setLogDao()
    @Provides fun provideUserProfileDao(db: RepForgeDatabase) = db.userProfileDao()
    @Provides fun provideBodyMetricDao(db: RepForgeDatabase) = db.bodyMetricDao()
    @Provides fun provideAchievementDao(db: RepForgeDatabase) = db.achievementDao()
    @Provides fun provideSyncDao(db: RepForgeDatabase) = db.syncOperationDao()
}
