package com.example.warming_up.ui.preparation

import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun String.toClockText(): String {
    val date = parseServerDateTime() ?: return "--:--"
    return SimpleDateFormat("HH:mm", Locale.KOREA).format(date)
}

internal fun minutesBetween(start: String, end: String): Int? {
    val startDate = start.parseServerDateTime() ?: return null
    val endDate = end.parseServerDateTime() ?: return null
    return TimeUnit.MILLISECONDS.toMinutes(endDate.time - startDate.time).toInt()
}

private fun String.parseServerDateTime() = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
    "yyyy-MM-dd'T'HH:mm:ssXXX",
    "yyyy-MM-dd'T'HH:mmXXX",
    "yyyy-MM-dd'T'HH:mm:ss.SSS",
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd'T'HH:mm",
).firstNotNullOfOrNull { pattern ->
    runCatching { SimpleDateFormat(pattern, Locale.US).parseFully(normalizedDateTime()) }.getOrNull()
}

private fun String.normalizedDateTime(): String {
    val trimmed = trim()
    val fractionRegex = Regex("""\.(\d+)(?=Z|[+-]\d{2}:?\d{2}$|$)""")
    return fractionRegex.replace(trimmed) { match ->
        val millis = match.groupValues[1].padEnd(3, '0').take(3)
        ".$millis"
    }
}

private fun SimpleDateFormat.parseFully(value: String): Date? {
    isLenient = false
    val position = ParsePosition(0)
    val date = parse(value, position) ?: return null
    return date.takeIf { position.index == value.length }
}
