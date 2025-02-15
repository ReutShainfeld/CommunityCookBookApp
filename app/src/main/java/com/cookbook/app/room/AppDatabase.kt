package com.cookbook.app.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cookbook.app.model.Recipe
import com.cookbook.app.room.AppDao

@Database(entities = [Recipe::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}