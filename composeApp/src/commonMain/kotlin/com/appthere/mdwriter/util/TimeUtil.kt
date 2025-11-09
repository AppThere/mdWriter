package com.appthere.mdwriter.util

import kotlinx.datetime.Instant

/**
 * Temporary workaround for Clock.System.now() classpath issues
 * TODO: Replace with Clock.System.now() once kotlinx-datetime is properly configured
 */
fun now(): Instant {
    return Instant.fromEpochMilliseconds(System.currentTimeMillis())
}
