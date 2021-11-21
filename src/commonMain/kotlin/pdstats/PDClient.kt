package pdstats

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface Client {

    /**
     * Returns list of all schedules, but without overrides and final renders
     */
    suspend fun schedules(): List<Schedule>

    /**
     * Returns single schedule, but split year by year
     */
    suspend fun schedule(schedule: String, since: Instant, until: Instant = Clock.System.now()): List<Schedule>

    /**
     * Returns current user
     */
    suspend fun currentUser(): User

}
