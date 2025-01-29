package com.cookbook.app.di

import android.content.Context
import androidx.room.Room
import com.cookbook.app.repository.DatabaseRepository
import com.cookbook.app.room.AppDao
import com.cookbook.app.room.AppDatabase
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
    fun provideAppDao(appDatabase: AppDatabase): AppDao = appDatabase.appDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "local_event_hub_database"
        )
            .build()

    @Provides
    fun provideDatabaseRepository(appDao: AppDao): DatabaseRepository = DatabaseRepository(appDao)
}