package pdstats

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

const val SOB = 9
const val EOB = 17

/**
 * Splits date range by year, also ends by EoD
 */
fun splitByYear(since: Instant, until: Instant): List<ClosedRange<Instant>> {
    val tz = TimeZone.currentSystemDefault()
    val sinceYear = since.toLocalDateTime(tz).year
    val untilYear = until.toLocalDateTime(tz).year
    if (sinceYear == untilYear) return listOf(since..until)

    val firstYear = since..since.eoy(tz)
    val years = (sinceYear + 1 until untilYear).map { year ->
        LocalDateTime(year, 1, 1, 0, 0).toInstant(tz)..LocalDateTime(year + 1, 1, 1, 0, 0).toInstant(tz)
    }

    val lastYear = LocalDateTime(untilYear, 1, 1, 0, 0).toInstant(tz)..until

    return listOf(firstYear) + years + if (lastYear.start < until) listOf(lastYear) else emptyList()

}

/**
 * Splits by shifts. Splits into series of ranges, on 08:00 and 16:00
 */

fun splitByShifts(start: Instant, end: Instant): List<ClosedRange<Instant>> {
    val tz = TimeZone.currentSystemDefault()
    if (start.nextShiftChange(tz) >= end) return listOf(start..end)

    val result = mutableListOf(start..start.nextShiftChange(tz))
    while (result.last().endInclusive.nextShiftChange(tz) < end) result.add(
        result.last().endInclusive..result.last().endInclusive.nextShiftChange(
            tz
        )
    )
    result.add(result.last().endInclusive..end)

    return result.toList()
}

fun Instant.nextShiftChange(tz: TimeZone): Instant {
    val localDateTime = toLocalDateTime(tz)
    val atMorning = localDateTime.date.atTime(SOB, 0)
    val atAfternoon = localDateTime.date.atTime(EOB, 0)
    val nextDay = localDateTime.date + DatePeriod(days = 1)
    return when {
        localDateTime < atMorning -> atMorning
        localDateTime < atAfternoon -> atAfternoon
        else -> nextDay.atTime(SOB, 0)
    }.toInstant(tz)
}

fun Instant.eoy(tz: TimeZone): Instant {
    val year = toLocalDateTime(tz).year
    return LocalDateTime(year + 1, 1, 1, 0, 0).toInstant(tz)
}

//fun Instant.eom(tz: TimeZone): Instant {
//    val year = toLocalDateTime(tz).year
//    return LocalDateTime(year + 1, 1, 1, 0, 0).toInstant(tz)
//}
//
fun Instant.eod(tz: TimeZone): Instant {
    val date = toLocalDateTime(tz).date + DatePeriod(days = 1)
    return date.atStartOfDayIn(tz)
}

fun Instant.bod(tz: TimeZone): Instant {
    val date = toLocalDateTime(tz).date
    return date.atStartOfDayIn(tz)
}

fun Instant.prevYear(tz: TimeZone): ClosedRange<LocalDateTime> {
    val year = toLocalDateTime(tz).date.year
    val since = LocalDateTime(year - 1, 1, 1, 0, 0)
    val until = LocalDateTime(year, 1, 1, 0, 0)
    return since..until
}

fun Instant.thisYearSoFar(tz: TimeZone): ClosedRange<LocalDateTime> {
    val date = toLocalDateTime(tz).date
    val year = date.year
    val since = LocalDateTime(year, 1, 1, 0, 0)
    val until = LocalDateTime(year, date.month, date.dayOfMonth, 0, 0)
    return since..until
}

fun Instant.prevMonth(tz: TimeZone): ClosedRange<LocalDateTime> {
    val date = toLocalDateTime(tz).date
    val year = date.year
    val month = date.month
    val until = LocalDate(year, month, 1)
    val since = until - DatePeriod(months = 1)
    return since.atTime(0, 0)..until.atTime(0, 0)
}

fun Instant.thisMonthSoFar(tz: TimeZone): ClosedRange<LocalDateTime> {
    val date = toLocalDateTime(tz).date
    val year = date.year
    val month = date.month
    val since = LocalDateTime(year, month, 1, 0, 0)
    val until = LocalDateTime(year, month, date.dayOfMonth, 0, 0)
    return since..until
}

