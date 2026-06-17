package com.privatehealthjournal

import android.app.Application
import com.privatehealthjournal.di.AppContainer

class App : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
