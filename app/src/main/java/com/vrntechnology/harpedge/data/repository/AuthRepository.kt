package com.vrntechnology.harpedge.data.repository

import com.vrntechnology.harpedge.data.model.UserProfile
import com.vrntechnology.harpedge.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            uid = "adm-001",
            name = "System Administrator",
            email = "admin@harpedge.gov",
            role = UserRole.ADMIN,
            organization = "National Hazard Defense Directorate"
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    fun signInWithRole(role: UserRole, email: String? = null, name: String? = null): UserProfile {
        val user = when (role) {
            UserRole.ADMIN -> UserProfile(
                uid = "adm-001",
                name = name ?: "Dr. Sarah Chen",
                email = email ?: "admin@harpedge.gov",
                role = UserRole.ADMIN,
                organization = "Hazard Intelligence Core"
            )
            UserRole.AUTHORITY -> UserProfile(
                uid = "auth-002",
                name = name ?: "Officer Marcus Vance",
                email = email ?: "m.vance@emergency.gov",
                role = UserRole.AUTHORITY,
                organization = "Civil Protection & Rapid Response"
            )
            UserRole.VIEWER -> UserProfile(
                uid = "view-003",
                name = name ?: "Public Environmental Observer",
                email = email ?: "public.access@citizen.org",
                role = UserRole.VIEWER,
                organization = "Community Environmental Trust"
            )
        }
        _currentUser.value = user
        return user
    }

    suspend fun signIn(email: String, pass: String): Result<UserProfile> {
        if (email.isBlank() || pass.length < 6) {
            return Result.failure(IllegalArgumentException("Please enter a valid email and minimum 6 character password"))
        }

        // Role mapping based on email domain or keyword for convenience in presentation
        val role = when {
            email.contains("admin", ignoreCase = true) -> UserRole.ADMIN
            email.contains("auth", ignoreCase = true) || email.contains("officer", ignoreCase = true) -> UserRole.AUTHORITY
            else -> UserRole.VIEWER
        }

        val user = UserProfile(
            uid = "usr-${System.currentTimeMillis() % 10000}",
            name = email.substringBefore("@").replace(".", " ").capitalizeWords(),
            email = email,
            role = role,
            organization = if (role == UserRole.ADMIN) "Hazard Intelligence Core" else if (role == UserRole.AUTHORITY) "Field Rapid Response" else "Public Observer"
        )
        _currentUser.value = user
        return Result.success(user)
    }

    fun signOut() {
        _currentUser.value = null
    }

    fun updateProfile(name: String, organization: String) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(name = name, organization = organization)
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
