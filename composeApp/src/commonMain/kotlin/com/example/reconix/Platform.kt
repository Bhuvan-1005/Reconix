package com.example.reconix

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform