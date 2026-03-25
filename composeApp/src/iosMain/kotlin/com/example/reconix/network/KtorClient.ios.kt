package com.example.reconix.network

import io.ktor.client.*
import io.ktor.client.engine.darwin.*

/**
 * iOS-specific HTTP Client using Darwin engine
 */
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}

/** Production backend hosted on Render. */
actual fun platformBaseUrl(): String = "https://reconix-api.onrender.com"

