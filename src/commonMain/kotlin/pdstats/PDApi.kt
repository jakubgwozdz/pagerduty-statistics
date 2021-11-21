@file:UseSerializers(InstantSerializer::class)

package pdstats

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
data class SchedulesResponse(
    val schedules: List<Schedule>
)

@Serializable
data class ScheduleByIdResponse(
    val schedule: Schedule
)

@Serializable
data class UsersResponse(
    val user: User
)

@Serializable
data class Schedule(
    val id: String,
    val summary: String,
    val name: String? = null,
    val users: List<User>? = null,
    val teams: List<Team>? = null,
    @SerialName("final_schedule") val finalSchedule: RenderedSchedule? = null
)

fun Schedule.containsUser(user: User) = users?.any { it.summary == user.summary || it.id == user.id }?:false

@Serializable
data class User(
    val id: String,
    val summary: String,
    val name: String? = null,
    @SerialName("job_title") val jobTitle: String? = null
)

@Serializable
data class Team(
    val id: String,
    val summary: String
)

@Serializable
data class RenderedSchedule(
    val name: String,
    @SerialName("rendered_schedule_entries") val entries: List<RenderedScheduleEntry> = emptyList(),
    @SerialName("rendered_coverage_percentage") val coverage: Double? = null,
)

@Serializable
data class RenderedScheduleEntry(
    val user: User,
    val start: Instant? = null,
    val end: Instant? = null,
)

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}
