package com.example.reconix.server.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Database Factory - Manages PostgreSQL/H2 connection via HikariCP
 */
object DatabaseFactory {

    fun init() {
        val database = Database.connect(createHikariDataSource())

        // Create tables if they don't exist
        transaction(database) {
            SchemaUtils.create(
                Users,
                PurchaseOrders,
                PurchaseOrderItems,
                Grns,
                GrnItems,
                Invoices,
                InvoiceItems,
                InvoiceActions,
                ValidationLogs,
                InvoiceFiles
            )
        }

        // Seed sample data for testing
        SeedData.seed()
    }

    private fun createHikariDataSource(): HikariDataSource {
        val useH2 = System.getenv("USE_H2")?.toBoolean() ?: true // Default to H2 for easy testing

        val config = HikariConfig().apply {
            if (useH2) {
                // H2 In-Memory Database (no Docker required!)
                driverClassName = "org.h2.Driver"
                jdbcUrl = "jdbc:h2:mem:invoice_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
                username = "sa"
                password = ""
                println("✅ Using H2 In-Memory Database (No Docker required)")
            } else {
                // PostgreSQL (requires Docker)
                driverClassName = "org.postgresql.Driver"
                jdbcUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/invoice_db"
                username = System.getenv("DATABASE_USER") ?: "user"
                password = System.getenv("DATABASE_PASSWORD") ?: "password"
                println("✅ Using PostgreSQL Database")
            }
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        return HikariDataSource(config)
    }

    /**
     * Execute database operations in a suspended transaction
     */
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}



