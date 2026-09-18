package com.example.model

data class ActivityLog(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val userName: String = "",
    val userRole: String = "SUB_ADMIN", // ADMIN, SUB_ADMIN
    val actionType: String = "", // ADD_TENANT, EDIT_TENANT, DELETE_TENANT, ADD_SHOP, EDIT_SHOP, DELETE_SHOP, COLLECT_RENT
    val title: String = "",
    val details: String = "",
    val authorDeviceId: String = ""
)
