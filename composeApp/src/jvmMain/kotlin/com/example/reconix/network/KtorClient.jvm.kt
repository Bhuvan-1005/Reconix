package com.example.reconix.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*

/**
 * JVM-specific HTTP Client using CIO engine
 */
actual fun createPlatformHttpClient(): HttpClient {
    return HttpClient(CIO) {
        engine {
            requestTimeout = 30_000
        }
    }
}

