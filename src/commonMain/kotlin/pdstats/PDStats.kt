package pdstats

import kotlinx.datetime.DayOfWeek.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import pdstats.DutyCategory.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

data class Stats(val usersStats: List<UserStats>)

data class UserStats(val name: String, val durations: List<Pair<DutyCategory, Duration>>)

enum class DutyCategory {
    WEEKEND, AFTERDUTY, WORKDAY
}

fun analyzeSchedule(schedule: Schedule): Stats {

    val groupedByUsers = schedule.finalSchedule?.entries
        ?.groupBy { it.user.summary }
        ?.mapValues { (_, v) -> v.map { it.start!! .. it.end!! } }
        ?: emptyMap()

    val usersStats = groupedByUsers.map { (name, periods) ->
        val totalDuration = periods
            .flatMap { splitByShifts(it.start, it.endInclusive) }
            .groupBy { category(it.start) }
            .mapValues { (_, period) ->
                period
                    .map { it.endInclusive - it.start }
                    .fold(ZERO, Duration::plus)
            }
            .toList()
            .sortedBy { it.first }
        UserStats(name, totalDuration)
    }

    return Stats(usersStats.sortedBy { it.name })
}

fun category(start: Instant): DutyCategory {
    val tz = TimeZone.currentSystemDefault()
    val localDateTime = start.toLocalDateTime(tz)
    val dayOfWeek = localDateTime.dayOfWeek
    val hour = localDateTime.hour

    return when {
        dayOfWeek == SUNDAY -> WEEKEND
        dayOfWeek == SATURDAY -> WEEKEND
//        dayOfWeek == FRIDAY && hour >= 16 -> WEEKEND
//        dayOfWeek == MONDAY && hour < 8 -> WEEKEND
        hour < 8 -> AFTERDUTY
        hour >= 16 -> AFTERDUTY
        else -> WORKDAY
    }

}
