package com.zandeveloper.tiktokly.model

data class UpdateInfo(
    val tag_name: String,
    val assets: List<Asset>
)

data class Asset(
    val name: String,
    val browser_download_url: String
)