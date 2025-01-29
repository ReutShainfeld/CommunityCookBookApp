package com.cookbook.app.repository


import com.cookbook.app.room.AppDao
import javax.inject.Inject

class DatabaseRepository @Inject constructor(private val appDao: AppDao) {
}