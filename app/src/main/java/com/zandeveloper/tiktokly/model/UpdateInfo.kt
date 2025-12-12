package com.zandeveloper.tiktokly.model

data class UpdateInfo(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<GithubAsset>
)

data class GithubAsset(
    val browser_download_url: String,
    val size: Long
)