package com.example.screenshotcleaner

data class Shot(
    val id: Long,
    val uri: android.net.Uri,
    val name: String,
    val size: Long,
    val dateAdded: Long,
    val relativePath: String,
    val exactHash: String? = null,
    val visualHash: Long? = null
)
