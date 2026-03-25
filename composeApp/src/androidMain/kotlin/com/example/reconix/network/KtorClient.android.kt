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

/**
 * Production backend hosted on Render.
 * For local development, switch back to "http://10.0.2.2:8081" (emulator)
 * or your LAN IP (physical device).
 */
actual fun platformBaseUrl(): String = "https://reconix-api.onrender.com"

