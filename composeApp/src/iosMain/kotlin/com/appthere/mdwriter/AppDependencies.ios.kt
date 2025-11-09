package com.appthere.mdwriter

import com.appthere.mdwriter.data.local.FileSystem

/**
 * iOS app dependencies
 */
actual fun createAppDependencies(): AppDependencies {
    return AppDependencies(
        fileSystem = FileSystem()
    )
}
