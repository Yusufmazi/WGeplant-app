package com.wgeplant

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the WGeplant app.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation
 * and setup dependency injection for the app.
 *
 * Lifecycle:
 * - onCreate is called when the application is starting, before any activity,
 *   service, or receiver objects have been created.
 */
@HiltAndroidApp
class WGeplantApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}
