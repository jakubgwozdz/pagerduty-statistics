package pdstats

import kotlinx.coroutines.await
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.xhr.XMLHttpRequest
import kotlin.js.Promise

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

class PDClient(
    val apiToken: String,
    val baseUrl: String = "https://api.pagerduty.com",
) : Client {

    override suspend fun schedules(): List<Schedule> {
        val url = "$baseUrl/schedules?limit=100"
        val text = callPD(url)
        val resp: SchedulesResponse = json.decodeFromString(text)
        return resp.schedules
    }

    override suspend fun schedule(schedule: String, since: Instant, until: Instant): List<Schedule> {

        val periods = splitByYear(since, until)

        return periods.map {
            val url = "$baseUrl/schedules/$schedule?time_zone=UTC&since=${it.start}&until=${it.endInclusive}"
            val text = callPD(url)
            val resp: ScheduleByIdResponse = json.decodeFromString(text)
            resp.schedule
        }
    }

    override suspend fun currentUser(): User {

        val url = "$baseUrl/users/me"
        val text = callPD(url)
        val resp: UsersResponse = json.decodeFromString(text)
        return resp.user
    }


    private suspend fun callPD(url: String): String {
        val p = Promise<XMLHttpRequest> { resolve, reject ->
            val req = XMLHttpRequest()
            req.open("GET", url)
            req.setRequestHeader("Accept", "application/vnd.pagerduty+json;version=2")
            req.setRequestHeader("Authorization", "Token token=$apiToken")
            req.addEventListener("load", { e -> resolve(req) })
            req.addEventListener("error", { e -> reject(RuntimeException(JSON.stringify(e))) })
            req.send("")
        }
        return p.await().responseText
    }


}
