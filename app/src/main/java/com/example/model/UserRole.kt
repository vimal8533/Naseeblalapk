package com.example.model

enum class UserRole {
    ADMIN,
    SUB_ADMIN
}

data class UserSession(
    val username: String,
    val role: UserRole,
    val displayName: String,
    val phone: String = "",
    val subAdminId: String = "",
    val sessionId: String = "",
    val passwordSnapshot: String = "",
    val canManagePersonalTenants: Boolean = false,
    val canDelegateAuthority: Boolean = false
) {
    val isAdmin: Boolean get() = role == UserRole.ADMIN
    val isSubAdmin: Boolean get() = role == UserRole.SUB_ADMIN
}
