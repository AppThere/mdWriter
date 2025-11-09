package com.appthere.mdwriter.util

/**
 * Get current time as epoch milliseconds
 * Replaces kotlinx-datetime usage to avoid classpath issues
 */
fun now(): Long {
    return System.currentTimeMillis()
}
