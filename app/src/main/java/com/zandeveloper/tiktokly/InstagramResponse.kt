package com.zandeveloper.tiktokly

data class InstagramResponse(
    val platform: String?,
    val result: List<ResultItem>?
)

data class ResultItem(
    val contactme: String?,
    val developer: String?,
    val thumbnail: String?,
    val url: String?
)
