package io.github.daisukikaffuchino.han1meviewer.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

actual fun LocalDate.formatPattern(pattern: String): String =
    toJavaLocalDate().format(DateTimeFormatter.ofPattern(pattern))
