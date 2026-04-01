package moe.lava.banksia.server.gtfs

import com.lightningkite.kotlinx.serialization.csv.CsvFormat
import com.lightningkite.kotlinx.serialization.csv.StringDeferringConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.util.logging.Logger
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import moe.lava.banksia.Constants
import moe.lava.banksia.model.Route
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.model.Service
import moe.lava.banksia.model.ServiceException
import moe.lava.banksia.model.Shape
import moe.lava.banksia.model.Stop
import moe.lava.banksia.model.StopTime
import moe.lava.banksia.model.Trip
import moe.lava.banksia.server.gtfs.structures.GtfsRoute
import moe.lava.banksia.server.gtfs.structures.GtfsService
import moe.lava.banksia.server.gtfs.structures.GtfsServiceException
import moe.lava.banksia.server.gtfs.structures.GtfsShape
import moe.lava.banksia.server.gtfs.structures.GtfsStop
import moe.lava.banksia.server.gtfs.structures.GtfsStopTime
import moe.lava.banksia.server.gtfs.structures.GtfsTrip
import moe.lava.banksia.util.Point
import java.io.File
import java.util.zip.ZipFile
import kotlin.time.ExperimentalTime

sealed class GtfsData {
    data class RouteChunk(val routes: List<Route>) : GtfsData()
    data class ServiceChunk(val services: List<Service>) : GtfsData()
    data class ServiceExceptionChunk(val exceptions: List<ServiceException>) : GtfsData()
    data class ShapeChunk(val shapes: List<Shape>) : GtfsData()
    data class StopChunk(val stops: List<Stop>) : GtfsData()
    data class StopTimeChunk(val stopTimes: List<StopTime>) : GtfsData()
    data class TripChunk(val trips: List<Trip>) : GtfsData()
}

class GtfsParser(
    private val log: Logger,
    private val client: HttpClient,
) {
    private val csv = CsvFormat(StringDeferringConfig(EmptySerializersModule()))
    private val datasetPath = File("/tmp/banksia", "dataset.zip")

    @OptIn(ExperimentalTime::class)
    suspend fun update(datasetUrl: String): Flow<GtfsData> {
        val parentDir = datasetPath.parentFile
        @Suppress("SimplifyBooleanWithConstants", "KotlinConstantConditions")
        if (parentDir.exists() && !Constants.devMode)
            parentDir.deleteRecursively()

        parentDir.mkdirs()

        log.info("fetching..")
        client.prepareRequest {
            url(datasetUrl)
        }.execute { resp ->
            if (!datasetPath.exists())
                resp.bodyAsChannel().copyAndClose(datasetPath.writeChannel())
            log.info("fetched!")
        }

        log.info("extracting...")
        @Suppress("KotlinConstantConditions")
        val files = if (Constants.devMode) {
            datasetPath.parentFile
                .listFiles { it.isDirectory }
                .flatMap { d -> d.listFiles { f -> f.extension == "txt" }.toList() }
                .ifEmpty { extractAll(datasetPath) }
//                .filter { it.parentFile.name == "2" }
        } else {
            extractAll(datasetPath)
        }

        log.info("parsing...")
        return parse(files)
            .onCompletion {
                @Suppress("KotlinConstantConditions")
                if (!Constants.devMode) {
                    parentDir.deleteRecursively()
                }

                log.info("done!")
            }
    }

    private fun parse(files: List<File>) = flow {
        files
            .filter { it.name == "routes.txt" }
            .forEach { emit(GtfsData.RouteChunk(parseRoutes(it))) }

        files
            .filter { it.name == "stops.txt" }
            .forEach { emit(GtfsData.StopChunk(parseStops(it))) }

        files
            .filter { it.name == "shapes.txt" }
            .forEach { emit(GtfsData.ShapeChunk(parseShapes(it))) }

        val services = files
            .filter { it.name == "calendar.txt" }
            .flatMap { fd ->
                parseServices(fd)
                    .also { emit(GtfsData.ServiceChunk(it)) }
            }
            .associateBy { it.id }

        files
            .filter { it.name == "calendar_dates.txt" }
            .forEach { emit(GtfsData.ServiceExceptionChunk(parseServiceExceptions(it))) }

        val trips = files
            .filter { it.name == "trips.txt" }
            .flatMap { fd ->
                parseTrips(fd, services)
                    .also { emit(GtfsData.TripChunk(it)) }
            }
            .associateBy { it.id }

        files
            .filter { it.name == "stop_times.txt" }
            .forEach { fd ->
                log.info("parsing stop times for ${fd.parent}...")
                parseStopTimes(fd, trips) { seq ->
                    seq.chunked(10000)
                        .forEach { emit(GtfsData.StopTimeChunk(it)) }
                }
            }
    }

    private fun parseRoutes(fd: File) =
        fd.parseCsv<GtfsRoute>()
            .map { with(it) {
                Route(
                    id = route_id,
                    type = RouteType.from(fd.parentFile.name.toInt()),
                    number = route_short_name,
                    name = route_long_name,
                )
            } }

    private fun parseShapes(fd: File) =
        fd.parseCsv<GtfsShape>()
            .groupBy { it.shape_id }
            .map { (id, group) ->
                val points = group
                    .sortedBy { it.shape_pt_sequence }
                    .map { Point(it.shape_pt_lat, it.shape_pt_lon) }

                Shape(id, points)
            }

    private fun parseStops(fd: File) =
        fd.parseCsv<GtfsStop>()
            .map { with(it) {
                Stop(
                    id = stop_id,
                    name = stop_name,
                    pos = Point(stop_lat, stop_lon),
                    parent = parent_station,
                    hasWheelChairBoarding = wheelchair_boarding == "1",
                    level = level_id,
                    platformCode = platform_code,
                )
            } }

    private inline fun parseStopTimes(fd: File, trips: Map<String, Trip>, block: (Sequence<StopTime>) -> Unit) =
        fd.parseCsvSequence<GtfsStopTime> { seq ->
            seq
                .map { with(it) {
                    StopTime(
                        tripId = trip_id,
                        stopId = stop_id,
                        arrivalTime = GtfsStopTime.parseGtfsTime(arrival_time),
                        departureTime = GtfsStopTime.parseGtfsTime(departure_time),
                        headsign = stop_headsign.ifEmpty { trips[trip_id]!!.tripHeadsign },
                        pickupType = pickup_type,
                        dropOffType = drop_off_type,
                    )
                } }
                .let { block(it) }
        }

    private fun parseServices(fd: File) =
        fd.parseCsv<GtfsService>()
            .map { with(it) {
                val days = buildList {
                    if (monday == 1) add(DayOfWeek.MONDAY)
                    if (tuesday == 1) add(DayOfWeek.TUESDAY)
                    if (wednesday == 1) add(DayOfWeek.WEDNESDAY)
                    if (thursday == 1) add(DayOfWeek.THURSDAY)
                    if (friday == 1) add(DayOfWeek.FRIDAY)
                    if (saturday == 1) add(DayOfWeek.SATURDAY)
                    if (sunday == 1) add(DayOfWeek.SUNDAY)
                }
                Service(
                    id = service_id,
                    days = days,
                    start = LocalDate.parse(start_date, LocalDate.Formats.ISO_BASIC),
                    end = LocalDate.parse(end_date, LocalDate.Formats.ISO_BASIC),
                )
            } }

    private fun parseServiceExceptions(fd: File) =
        fd.parseCsv<GtfsServiceException>()
            .map { with(it) {
                ServiceException(
                    serviceId = service_id,
                    date = LocalDate.parse(date, LocalDate.Formats.ISO_BASIC),
                    type = exception_type,
                )
            } }

    private fun parseTrips(fd: File, services: Map<String, Service>) =
        fd.parseCsv<GtfsTrip>()
            .map { with(it) {
                Trip(
                    id = trip_id,
                    routeId = route_id,
                    service = services[service_id]!!,
                    shapeId = shape_id.ifEmpty { null },
                    tripHeadsign = trip_headsign,
                    directionId = direction_id,
                    blockId = block_id,
                    wheelchairAccessible = wheelchair_accessible,
                )
            } }

    private fun extract(fd: File): List<File> {
        val outputs = mutableListOf<File>()
        ZipFile(fd).use { zip ->
            for (entry in zip.entries()) {
                zip.getInputStream(entry).use { input ->
                    val out = File(fd.parentFile, entry.name)
                    out.parentFile.mkdirs()
                    out.outputStream().use { output ->
                        input.copyTo(output)
                    }
                    outputs.add(out)
                }
            }
        }
        return outputs
    }

    private fun extractAll(fd: File) = extract(fd).flatMap(::extract)

    private inline fun <reified T> File.parseCsv(): List<T> = this
        .readText()
        .replace("\uFEFF", "") // remove bom
        .replace("\r\n", "\n") // crlf -> lf
        .let { csv.decodeFromString(it) }

    private inline fun <reified T> File.parseCsvSequence(block: (Sequence<T>) -> Unit) = this
        .bufferedReader()
        .use { reader ->
            val iter = object : CharIterator() {
                var next: Char? = null
                override fun nextChar(): Char {
                    if (!hasNext()) {
                        throw NoSuchElementException()
                    }
                    val ret = next!!
                    next = null
                    return ret
                }
                override fun hasNext(): Boolean {
                    if (next == null) {
                        do {
                            next = null
                            val new = reader.read()
                            if (new != -1) {
                                next = new.toChar()
                            }
                        } while (next == '\uFEFF' || next == '\r')
                    }
                    return next != null
                }
            }
            block(csv.decodeToSequence(iter, csv.serializersModule.serializer()))
        }
}
