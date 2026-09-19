package com.example.model

data class AppUpdateInfo(
    val latestVersionCode: Int = 2,
    val latestVersionName: String = "2.0",
    val updateTitle: String = "New Update Available!",
    val updateMessage: String = "",
    val downloadUrl: String = "",
    val isMandatory: Boolean = false,
    val releasedDate: String = ""
)
