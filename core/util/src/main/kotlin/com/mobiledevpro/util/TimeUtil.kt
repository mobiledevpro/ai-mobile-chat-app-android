package com.mobiledevpro.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object TimeUtil {

    fun currentTimeStamp(): String = DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).let { format ->
        LocalDateTime.now().format(format)
    }

    private const val TIMESTAMP_PATTERN = "yyyyMMdd_HHmmssSSS"
    const val PDF_LIST_DATE_FORMAT = "MM/dd/yyyy"
}

fun Long.timeToFormat(format: String): String? {
    if (this == 0L) return ""
    val date = Date(this)
    return SimpleDateFormat(format, Locale.getDefault()).format(date)
}

