package com.zandeveloper.tiktokly.utils.stringValidator

class StringValidator {
     fun isValidUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    return url.startsWith("http://") || url.startsWith("https://")

   }
}
