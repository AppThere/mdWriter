package com.appthere.mdwriter

import com.appthere.mdwriter.data.local.FileSystem

/**
 * JVM (Desktop) app dependencies
 */
actual fun createAppDependencies(): AppDependencies {
    return AppDependencies(
        fileSystem = FileSystem()
    )
}
