package pdstats

import kotlinx.datetime.Clock.System
import kotlinx.datetime.Instant
import kotlin.random.Random

private lateinit var anonymization_me: User
private val anonymization_cache = mutableMapOf<String, String>()

class AnonymizingClient(val realClient: Client) : Client {

    override suspend fun schedules(): List<Schedule> {
        val result = realClient.schedules()
        return result.map { it.anonymize() }
    }

    override suspend fun schedule(schedule: String, since: Instant, until: Instant): List<Schedule> {
        val result = realClient.schedule(schedule, since, until)
        return result.map { it.anonymize() }
    }

    override suspend fun currentUser(): User {
        val result = realClient.currentUser()
        anonymization_cache.clear()
        anonymization_me = result
        return result
    }

    private fun Schedule.anonymize(): Schedule = copy(
        users = users?.map { it.anonymize() },
        finalSchedule = finalSchedule?.copy(entries = finalSchedule.entries.map { it.anonymize() })
    )

    private fun RenderedScheduleEntry.anonymize() = copy(
        user = user.anonymize()
    )

    private fun User.anonymize(): User = when (id) {
        anonymization_me.id -> this
        else -> copy(
            summary = summary.anonymizeWords(),
            name = name?.anonymizeWords(),
        )
    }

    private fun String.anonymizeWords(): String = split(" ").joinToString(" ") { it.anonymize() }

    private val random = Random(System.now().epochSeconds)
    private fun String.anonymize(): String = anonymization_cache.getOrPut(this) {
        map {
            when {
                it.isUpperCase() -> 'A' + random.nextInt(26)
                it.isLowerCase() -> 'a' + random.nextInt(26)
                else -> it
            }
        }.joinToString("")
    }

}
