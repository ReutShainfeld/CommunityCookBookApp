package com.cookbook.app.room

import androidx.room.RoomDatabase
import com.cookbook.app.room.AppDao

//@Database(entities = [], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}