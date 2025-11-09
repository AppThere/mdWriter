package com.appthere.mdwriter

import android.content.Context
import com.appthere.mdwriter.data.local.FileSystem

/**
 * Android-specific app dependencies
 */
private lateinit var appContext: Context

fun initializeAndroidDependencies(context: Context) {
    appContext = context.applicationContext
}

actual fun createAppDependencies(): AppDependencies {
    return AppDependencies(
        fileSystem = FileSystem(appContext)
    )
}
