plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
    application
}

application {
    mainClass.set("com.example.reconix.server.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // Shared Module - Type-safe DTOs
    implementation(project(":shared"))

    // Ktor Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.json)

    // Exposed ORM
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlinDatetime)

    // Database
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.hikaricp)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.coroutines.core)

    // Logging
    implementation(libs.logback.classic)

    // PDF Generation (OpenPDF - pure Java, no native deps)
    implementation("com.github.librepdf:openpdf:2.0.3")

    // Email (Jakarta Mail - SMTP send & IMAP receive)
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // PDF Text Extraction (Apache PDFBox - lighter than Tika)
    implementation("org.apache.pdfbox:pdfbox:3.0.3")

    // Testing
    testImplementation(libs.kotlin.test)
}


