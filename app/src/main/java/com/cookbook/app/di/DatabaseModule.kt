package com.cookbook.app.di

import android.content.Context
import androidx.room.Room
import com.cookbook.app.firebase.FirebaseRepository
import com.cookbook.app.repository.DatabaseRepository
import com.cookbook.app.retrofit.MealDbRepositoryImpl
import com.cookbook.app.retrofit.MealDbRepository
import com.cookbook.app.retrofit.MealDbService
import com.cookbook.app.room.AppDao
import com.cookbook.app.room.AppDatabase
import com.google.firebase.auth.FirebaseAuth
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
            "recipes_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideMealDbRepository(
        mealDbService: MealDbService,
        appDao: AppDao
    ): MealDbRepository = MealDbRepositoryImpl(mealDbService, appDao)

    @Provides
    fun provideDatabaseRepository(auth: FirebaseAuth,
                                  firebaseRepository: FirebaseRepository, appDao: AppDao,mealDbRepository: MealDbRepository): DatabaseRepository = DatabaseRepository(auth,firebaseRepository,appDao,mealDbRepository)
}