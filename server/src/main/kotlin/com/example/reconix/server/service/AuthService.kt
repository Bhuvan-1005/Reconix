package com.example.reconix.server.service

import com.example.reconix.server.database.Users
import com.example.reconix.shared.LoginRequest
import com.example.reconix.shared.LoginResponse
import com.example.reconix.shared.UserDTO
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import kotlinx.datetime.Clock

/**
 * Authentication Service
 * Handles user authentication against the database
 */
class AuthService {

    /**
     * Authenticate user credentials
     * @param loginRequest Username and password
     * @return LoginResponse with success status and user info
     */
    fun login(loginRequest: LoginRequest): LoginResponse {
        return transaction {
            // Hash the provided password
            val passwordHash = hashPassword(loginRequest.password)

            // Find user by username and password hash
            val userRow = Users.selectAll()
                .where {
                    (Users.username eq loginRequest.username) and
                    (Users.passwordHash eq passwordHash) and
                    (Users.isActive eq true)
                }
                .singleOrNull()

            if (userRow == null) {
                // Authentication failed
                LoginResponse(
                    success = false,
                    message = "Invalid username or password",
                    user = null,
                    token = null
                )
            } else {
                // Authentication successful
                val userId = userRow[Users.id]
                val username = userRow[Users.username]
                val fullName = userRow[Users.fullName]
                val email = userRow[Users.email]
                val role = userRow[Users.role]

                // Update last login time
                Users.update({ Users.id eq userId }) {
                    it[lastLoginAt] = Clock.System.now().toString()
                }

                // Create user DTO
                val userDTO = UserDTO(
                    id = userId,
                    username = username,
                    fullName = fullName,
                    email = email,
                    role = role
                )

                // Generate simple token (In production, use JWT)
                val token = generateToken(userId, username)

                LoginResponse(
                    success = true,
                    message = "Login successful",
                    user = userDTO,
                    token = token
                )
            }
        }
    }

    /**
     * Hash password using SHA-256
     * In production, use BCrypt, Argon2, or PBKDF2
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generate authentication token
     * In production, use JWT with expiration
     */
    private fun generateToken(userId: Int, username: String): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val tokenData = "$userId:$username:$timestamp"
        return java.util.Base64.getEncoder().encodeToString(tokenData.toByteArray())
    }

    /**
     * Validate user credentials (alternative to login)
     */
    fun validateCredentials(username: String, password: String): Boolean {
        return transaction {
            val passwordHash = hashPassword(password)

            Users.selectAll()
                .where {
                    (Users.username eq username) and
                    (Users.passwordHash eq passwordHash) and
                    (Users.isActive eq true)
                }
                .count() > 0
        }
    }

    /**
     * Get user by username
     */
    fun getUserByUsername(username: String): UserDTO? {
        return transaction {
            val userRow = Users.selectAll()
                .where { Users.username eq username }
                .singleOrNull()

            userRow?.let {
                UserDTO(
                    id = it[Users.id],
                    username = it[Users.username],
                    fullName = it[Users.fullName],
                    email = it[Users.email],
                    role = it[Users.role]
                )
            }
        }
    }
}


