package com.appthere.mdwriter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform