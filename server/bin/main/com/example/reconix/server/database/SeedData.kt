package com.example.reconix.server.database

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock

/**
 * Seed Data - Populates the database with sample data for testing
 */
object SeedData {

    fun seed() {
        transaction {
            // Only seed if tables are empty
            if (PurchaseOrders.selectAll().count() > 0) {
                return@transaction
            }

            val now = Clock.System.now().toString()

            // ========== CREATE USERS ==========
            println("🔐 Seeding Users...")

            // Simple password hashing (In production, use BCrypt or Argon2)
            val hashedPassword = hashPassword("password")

            Users.insert {
                it[username] = "vendor"
                it[Users.passwordHash] = hashedPassword
                it[fullName] = "Vendor User"
                it[email] = "vendor@example.com"
                it[role] = "VENDOR"
                it[isActive] = true
                it[createdAt] = now
            }

            Users.insert {
                it[username] = "admin"
                it[Users.passwordHash] = hashedPassword
                it[fullName] = "Admin User"
                it[email] = "admin@example.com"
                it[role] = "ADMIN"
                it[isActive] = true
                it[createdAt] = now
            }

            Users.insert {
                it[username] = "demo"
                it[Users.passwordHash] = hashedPassword
                it[fullName] = "Demo User"
                it[email] = "demo@example.com"
                it[role] = "VENDOR"
                it[isActive] = true
                it[createdAt] = now
            }

            Users.insert {
                it[username] = "finance"
                it[Users.passwordHash] = hashedPassword
                it[fullName] = "Finance Manager"
                it[email] = "finance@example.com"
                it[role] = "FINANCE_MANAGER"
                it[isActive] = true
                it[createdAt] = now
            }

            println("✅ Created 4 users: vendor, admin, demo, finance (password: password)")

            // ========== CREATE PURCHASE ORDERS ==========
            println("📦 Seeding Purchase Orders...")
            val poIds = listOf("PO-001", "PO-002", "PO-003")

            // PO-001: Office Supplies
            PurchaseOrders.insert {
                it[id] = "PO-001"
                it[vendorName] = "Office Supplies Co."
                it[vendorEmail] = "supplies@officesupplies.com"
                it[totalAmount] = 1500.00
                it[status] = "SENT"
                it[createdAt] = now
            }

            PurchaseOrderItems.insert {
                it[poId] = "PO-001"
                it[itemId] = "ITEM-001"
                it[itemName] = "Printer Paper (Box)"
                it[quantity] = 50
                it[unitPrice] = 25.00
            }

            PurchaseOrderItems.insert {
                it[poId] = "PO-001"
                it[itemId] = "ITEM-002"
                it[itemName] = "Ink Cartridges"
                it[quantity] = 10
                it[unitPrice] = 25.00
            }

            // PO-002: Electronics
            PurchaseOrders.insert {
                it[id] = "PO-002"
                it[vendorName] = "Tech Solutions Ltd."
                it[vendorEmail] = "billing@techsolutions.com"
                it[totalAmount] = 5000.00
                it[status] = "SENT"
                it[createdAt] = now
            }

            PurchaseOrderItems.insert {
                it[poId] = "PO-002"
                it[itemId] = "ITEM-003"
                it[itemName] = "Laptop Stand"
                it[quantity] = 20
                it[unitPrice] = 150.00
            }

            PurchaseOrderItems.insert {
                it[poId] = "PO-002"
                it[itemId] = "ITEM-004"
                it[itemName] = "USB-C Hub"
                it[quantity] = 25
                it[unitPrice] = 80.00
            }

            // PO-003: Furniture
            PurchaseOrders.insert {
                it[id] = "PO-003"
                it[vendorName] = "Modern Furniture Inc."
                it[vendorEmail] = "orders@modernfurniture.com"
                it[totalAmount] = 8500.00
                it[status] = "SENT"
                it[createdAt] = now
            }

            PurchaseOrderItems.insert {
                it[poId] = "PO-003"
                it[itemId] = "ITEM-005"
                it[itemName] = "Office Chair"
                it[quantity] = 10
                it[unitPrice] = 500.00
            }

            PurchaseOrderItems.insert {
                it[poId] = "PO-003"
                it[itemId] = "ITEM-006"
                it[itemName] = "Standing Desk"
                it[quantity] = 5
                it[unitPrice] = 700.00
            }

            // Create GRNs (Goods Receipt Notes)
            // GRN for PO-001 - Partial delivery
            Grns.insert {
                it[id] = "GRN-001"
                it[poId] = "PO-001"
                it[receivedAt] = now
            }

            GrnItems.insert {
                it[grnId] = "GRN-001"
                it[itemId] = "ITEM-001"
                it[receivedQuantity] = 30 // Received 30 of 50 ordered
            }

            GrnItems.insert {
                it[grnId] = "GRN-001"
                it[itemId] = "ITEM-002"
                it[receivedQuantity] = 10 // Received all 10
            }

            // Second GRN for PO-001 - Rest of delivery
            Grns.insert {
                it[id] = "GRN-002"
                it[poId] = "PO-001"
                it[receivedAt] = now
            }

            GrnItems.insert {
                it[grnId] = "GRN-002"
                it[itemId] = "ITEM-001"
                it[receivedQuantity] = 20 // Remaining 20 of 50
            }

            // GRN for PO-002 - Full delivery
            Grns.insert {
                it[id] = "GRN-003"
                it[poId] = "PO-002"
                it[receivedAt] = now
            }

            GrnItems.insert {
                it[grnId] = "GRN-003"
                it[itemId] = "ITEM-003"
                it[receivedQuantity] = 20
            }

            GrnItems.insert {
                it[grnId] = "GRN-003"
                it[itemId] = "ITEM-004"
                it[receivedQuantity] = 25
            }

            // GRN for PO-003 - Partial delivery
            Grns.insert {
                it[id] = "GRN-004"
                it[poId] = "PO-003"
                it[receivedAt] = now
            }

            GrnItems.insert {
                it[grnId] = "GRN-004"
                it[itemId] = "ITEM-005"
                it[receivedQuantity] = 8 // Only 8 of 10 received
            }

            GrnItems.insert {
                it[grnId] = "GRN-004"
                it[itemId] = "ITEM-006"
                it[receivedQuantity] = 5 // All 5 received
            }

            println("✅ Database seeding completed!")
        }
    }

    /**
     * Simple password hashing using SHA-256
     * In production, use BCrypt, Argon2, or PBKDF2
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}




