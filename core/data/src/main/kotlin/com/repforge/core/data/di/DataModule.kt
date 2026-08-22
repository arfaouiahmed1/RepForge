package com.repforge.core.data.di

import com.repforge.core.data.sync.NoOpSyncEngine
import com.repforge.core.data.sync.SyncEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds abstract fun bindSyncEngine(impl: NoOpSyncEngine): SyncEngine
}
