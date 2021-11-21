package pdstats

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class MockClient : Client {

    override suspend fun schedules(): List<Schedule> = withContext(Dispatchers.IO) {
        json.decodeFromString<SchedulesResponse>(content("api.pagerduty.com-schedules")).schedules
    }

    override suspend fun schedule(schedule: String, since: Instant, until: Instant): List<Schedule> =
        withContext(Dispatchers.IO) {
            check(schedule == "PICBSZ5")
            listOf(
                json.decodeFromString<ScheduleByIdResponse>(content("api.pagerduty.com-schedules-PICBSZ5-2019")).schedule,
                json.decodeFromString<ScheduleByIdResponse>(content("api.pagerduty.com-schedules-PICBSZ5-2020")).schedule,
            )
        }

    override suspend fun currentUser(): User {
        TODO("Not yet implemented")
    }

    private fun content(request: String):String {
        return Files.readString(Paths.get("local/$request.json"))
    }

}
