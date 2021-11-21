package pdstats

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

suspend fun main() {

//    val pdClient = PDClient(Environment.mandatory["API_TOKEN"])
    val pdClient = MockClient()
    val allSchedules = pdClient.schedules()
    val scheduleId = allSchedules.single { it.teams?.any { it.summary == "Privacy" } ?: false }.id
    val schedules = pdClient.schedule(scheduleId, "2019-10-28T14:00:00+01:00".toInstant())

    schedules.forEach { schedule ->
        val stats = analyzeSchedule(schedule)
        println("${schedule.finalSchedule?.entries?.firstOrNull()?.start?.toLocalDateTime(TimeZone.currentSystemDefault())?.year}:")
        stats.usersStats.forEach { userStats ->
            println("${userStats.name.padStart(20)}: ${userStats.durations.joinToString { "${it.first}: ${it.second}" }}")
        }
    }
}
