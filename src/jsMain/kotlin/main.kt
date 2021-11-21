import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import pdstats.Schedule
import pdstats.User
import pdstats.bod
import pdstats.eod

val timeZone = TimeZone.currentSystemDefault()

var since = try {
    (localStorage.getItem("defaultSince") ?: "2020-01-01T00:00:00").toLocalDateTime().toInstant(timeZone)
} catch (_: Throwable) {
    "2020-01-01T00:00:00".toLocalDateTime().toInstant(timeZone)
}

var until = Clock.System.now().bod(timeZone)

fun main() {
    document.addEventListener("DOMContentLoaded", { createUI() })
}


val schedules = mutableListOf<Schedule>()
var currentUser: User? = null

