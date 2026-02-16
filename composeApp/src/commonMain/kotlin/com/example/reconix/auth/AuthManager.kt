package com.example.reconix.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Authentication State Manager
 * Manages user authentication state across the app
 */
object AuthManager {

    var isAuthenticated by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<String?>(null)
        private set

    var userRole by mutableStateOf<UserRole>(UserRole.VENDOR)
        private set

    /**
     * Log in the user
     */
    fun login(username: String) {
        isAuthenticated = true
        currentUser = username
        userRole = determineRole(username)
    }

    /**
     * Log out the user
     */
    fun logout() {
        isAuthenticated = false
        currentUser = null
        userRole = UserRole.VENDOR
    }

    /**
     * Determine user role based on username
     */
    private fun determineRole(username: String): UserRole {
        return when (username.lowercase()) {
            "admin" -> UserRole.ADMIN
            "finance" -> UserRole.FINANCE_MANAGER
            "vendor" -> UserRole.VENDOR
            else -> UserRole.VENDOR
        }
    }

    /**
     * Check if user has admin privileges
     */
    fun isAdmin(): Boolean = userRole == UserRole.ADMIN

    /**
     * Get current role as string for navigation
     */
    val currentRole: String get() = userRole.name
}

/**
 * User Roles
 */
enum class UserRole {
    VENDOR,
    ADMIN,
    FINANCE_MANAGER
}

