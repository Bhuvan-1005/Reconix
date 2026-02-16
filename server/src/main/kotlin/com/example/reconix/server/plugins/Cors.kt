package com.example.reconix.server.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

/**
 * Configure CORS for cross-origin requests
 */
fun Application.configureCors() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)

        // Allow requests from Android emulator and localhost
        allowHost("10.0.2.2:8080")
        allowHost("10.116.40.38:8081") // Laptop IP
        allowHost("localhost:8080")
        allowHost("localhost:3000")

        // For development - allow any host
        anyHost()
    }
}

