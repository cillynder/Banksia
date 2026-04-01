package moe.lava.banksia.server.gtfsrt

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import moe.lava.banksia.Constants
import moe.lava.banksia.util.LogScope
import moe.lava.banksia.util.log

private val types = arrayOf(
    "metro/trip-updates",
    "metro/vehicle-positions",
    "metro/service-alerts",
    "tram/trip-updates",
    "tram/vehicle-positions",
    "tram/service-alerts",
    "bus/trip-updates",
    "bus/vehicle-positions",
    "vline/trip-updates",
    "vline/vehicle-positions",
)

class GtfsrtService(
    private val client: HttpClient,
) {
    private val archiver = GtfsrtArchiver()
    private var started = false

    internal val rawMessages: SharedFlow<Pair<String, ByteArray>>
        field = MutableSharedFlow<Pair<String, ByteArray>>()

    fun start(
        scope: CoroutineScope,
        enableArchiving: Boolean = false,
    ) {
        if (started) {
            log("GtfsrtService", "Tried to start when already started")
            return
        }

        if (enableArchiving) {
            scope.launch { archiver.start(rawMessages) }
        }

        scope.launch { fetch() }
    }

    private suspend fun fetch() {
        coroutineScope {
            types.map { type ->
                launch(context = Dispatchers.IO) {
                    val logger = LogScope("gtfsr $type")
                    try {
                        val res = client.get {
                            url("https://api.opendata.transport.vic.gov.au/opendata/public-transport/gtfs/realtime/v1/${type}")
                            header("KeyId", Constants.opendataKey)
                        }
                        if (!res.status.isSuccess()) {
                            logger.log("${res.status} | ${res.bodyAsText()}")
                        } else {
                            val bytes = res.readRawBytes()
                            rawMessages.emit(type to bytes)
                        }
                    } catch (e: Throwable) {
                        logger.log("$e")
                        logger.log(e.stackTraceToString())
                    }
                }
            }.joinAll()
        }

        delay(10000)
        fetch()
    }
}
