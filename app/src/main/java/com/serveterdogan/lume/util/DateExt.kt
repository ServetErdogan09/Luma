package com.serveterdogan.lume.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toFormattedString(pattern: String = "dd MMMM yyyy"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale("tr", "TR"))
    return this.format(formatter)
}
/**
 * Returns date in "27 Haz 2025" format.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toShortDateString(): String {
    return this.toFormattedString("dd MMM yyyy")
}

/**
 * Returns date in "27 Haziran 2025" format.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toLongDateString(): String {
    return this.toFormattedString("dd MMMM yyyy")
}
