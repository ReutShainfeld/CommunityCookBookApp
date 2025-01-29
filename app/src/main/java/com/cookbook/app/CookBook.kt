package com.cookbook.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CookBook : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}