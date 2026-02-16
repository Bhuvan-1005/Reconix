package com.example.reconix.server

import com.example.reconix.server.database.DatabaseFactory
import com.example.reconix.server.plugins.configureRouting
import com.example.reconix.server.plugins.configureSerialization
import com.example.reconix.server.plugins.configureStatusPages
import com.example.reconix.server.plugins.configureCors
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(
        Netty,
        port = 8081,
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    // Initialize database connection
    DatabaseFactory.init()

    // Configure plugins
    configureSerialization()
    configureStatusPages()
    configureCors()
    configureRouting()
}


