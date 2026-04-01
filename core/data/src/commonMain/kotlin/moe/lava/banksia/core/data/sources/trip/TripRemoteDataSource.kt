package moe.lava.banksia.core.data.sources.trip

import io.ktor.client.HttpClient
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import moe.lava.banksia.core.model.Trip
import kotlin.time.Clock

internal class TripRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun get(
        day: DayOfWeek? = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek,
    ): List<Trip> {
        return listOf()
    }
}
