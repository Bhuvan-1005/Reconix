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
 * Physical device & emulator URL resolution:
 *  - Emulator:       10.0.2.2  → host machine's localhost
 *  - Physical device: 10.199.105.38 → host machine's Wi-Fi LAN IP
 *
 * Using LAN IP so both emulator and physical device work when on the same Wi-Fi.
 */
actual fun platformBaseUrl(): String = "http://10.55.7.38:8081"

