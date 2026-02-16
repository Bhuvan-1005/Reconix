package com.example.reconix.network

import io.ktor.client.*
import io.ktor.client.engine.android.*

/**
 * Android-specific HTTP Client using Android engine
 */
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Android) {
        engine {
            connectTimeout = 10_000
            socketTimeout = 30_000
        }
    }
}

