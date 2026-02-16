package com.example.reconix.network

import com.example.reconix.shared.ApiRoutes
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Ktor HTTP Client Singleton
 * Configured for JSON serialization using shared DTOs
 */
expect fun createPlatformHttpClient(): HttpClient

/**
 * Shared Ktor Client configuration
 */
object KtorClient {

    val httpClient: HttpClient by lazy {
        createPlatformHttpClient().config {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }

            defaultRequest {
                contentType(ContentType.Application.Json)
                headers {
                    append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                }
            }
        }
    }

    /**
     * Get the appropriate base URL for the current platform
     */
    fun getBaseUrl(): String = ApiRoutes.BASE_URL
}


