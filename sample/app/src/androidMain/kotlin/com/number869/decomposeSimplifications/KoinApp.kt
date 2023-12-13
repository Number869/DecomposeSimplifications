package com.number869.decomposeSimplifications

import android.app.Application
import org.koin.core.context.GlobalContext.startKoin

class KoinApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin() {

        }
    }
}