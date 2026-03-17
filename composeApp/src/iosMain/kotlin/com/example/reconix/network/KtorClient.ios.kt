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

/** iOS simulator on the same Mac can reach localhost directly. */
actual fun platformBaseUrl(): String = "http://localhost:8081"

