package com.example.reconix.server.plugins

import com.example.reconix.shared.ApiError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

/**
 * Configure Status Pages for error handling
 */
fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiError(400, cause.message ?: "Bad Request")
            )
        }

        exception<IllegalStateException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                ApiError(409, cause.message ?: "Conflict")
            )
        }

        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiError(404, cause.message ?: "Not Found")
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(500, "Internal Server Error: ${cause.message}")
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ApiError(404, "Resource not found")
            )
        }
    }
}

