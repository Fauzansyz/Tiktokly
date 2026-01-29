package com.zandeveloper.tiktokly.utils.urlValidator
import android.util.Patterns
import java.util.ArrayList
import java.util.regex.Matcher

class UrlValidator {
   fun extractUrlsFromString(inputString: String): String {
    val matcher = Patterns.WEB_URL.matcher(inputString)

    return if (matcher.find()) {
        matcher.group()
    } else { 
       ""
    }
}

}
