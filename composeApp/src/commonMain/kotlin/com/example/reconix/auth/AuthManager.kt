package com.example.reconix.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Authentication State Manager
 * Manages user authentication state across the app.
 * The JWT token returned by the server is stored here and injected into
 * every subsequent HTTP request as an `Authorization: Bearer <token>` header.
 */
object AuthManager {

    var isAuthenticated by mutableStateOf(false)
        private set

    var currentUser by mutableStateOf<String?>(null)
        private set

    var userRole by mutableStateOf<UserRole>(UserRole.VENDOR)
        private set

    /**
     * JWT access token returned by /auth/login.
     * Exposed so InvoiceRepository can add it to request headers.
     */
    var token by mutableStateOf<String?>(null)
        private set

    /**
     * Log in the user.
     *
     * @param username  Authenticated username.
     * @param jwtToken  JWT access token from the server (optional for backward compat).
     * @param serverRole Role string from the server UserDTO (e.g. "FINANCE_MANAGER").
     *                   Falls back to username-based heuristic when null.
     */
    fun login(
        username: String,
        jwtToken: String? = null,
        serverRole: String? = null,
    ) {
        isAuthenticated = true
        currentUser = username
        token = jwtToken
        userRole = serverRole?.let { parseServerRole(it) } ?: determineRole(username)
    }

    /**
     * Log out the user and clear all auth state.
     */
    fun logout() {
        isAuthenticated = false
        currentUser = null
        token = null
        userRole = UserRole.VENDOR
    }

    /** Parse the role string returned by the server into a [UserRole]. */
    private fun parseServerRole(role: String): UserRole = when (role.uppercase()) {
        "ADMIN"            -> UserRole.ADMIN
        "FINANCE_MANAGER"  -> UserRole.FINANCE_MANAGER
        else               -> UserRole.VENDOR
    }

    /**
     * Fallback role determination when the server did not provide a role.
     * Kept for backward compatibility with the demo credential buttons.
     */
    private fun determineRole(username: String): UserRole = when (username.lowercase()) {
        "admin"   -> UserRole.ADMIN
        "finance" -> UserRole.FINANCE_MANAGER
        "vendor"  -> UserRole.VENDOR
        else      -> UserRole.VENDOR
    }

    /** True when the user has admin privileges. */
    fun isAdmin(): Boolean = userRole == UserRole.ADMIN

    /** Current role as plain string, used for navigation decisions. */
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

