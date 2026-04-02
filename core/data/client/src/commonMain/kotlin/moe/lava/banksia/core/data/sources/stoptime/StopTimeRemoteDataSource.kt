package moe.lava.banksia.core.data.sources.stoptime

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import moe.lava.banksia.core.model.StopTimeDated
import kotlin.time.Clock

internal class StopTimeRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun getAtStop(
        stopId: String,
        date: LocalDate? = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    ): List<StopTimeDated> {
        return client.get("stoptimes/by_stop/${stopId}") {
            parameter("date", date)
        }.body<List<StopTimeDated>>()
    }

    /*suspend fun get(
        stop: String? = null,
        trip: String? = null,
        day: DayOfWeek? = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfWeek,
    ): List<StopTime> {
        return client.get("stoptimes") {
            stop?.let { parameter("stop", it) }
            trip?.let { parameter("trip", it) }
            day?.let { parameter("day", it) }
        }.body<List<StopTime>>()
    }*/
}
