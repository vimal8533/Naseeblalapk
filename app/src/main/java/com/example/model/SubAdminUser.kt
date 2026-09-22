package com.example.model

data class SubAdminUser(
    val id: String = "",
    val username: String = "",
    val password: String = "",
    val name: String = "",
    val phone: String = "",
    val isActive: Boolean = true,
    val canManagePersonalTenants: Boolean = false,
    val canDelegateAuthority: Boolean = false,
    val delegatedToUsernames: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = 0L,
    val activeSessionId: String = ""
)
