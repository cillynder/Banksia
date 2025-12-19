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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.serializer
import moe.lava.banksia.Constants
import moe.lava.banksia.model.Route
import moe.lava.banksia.model.Shape
import moe.lava.banksia.model.Stop
import moe.lava.banksia.model.StopTime
import moe.lava.banksia.model.Trip
import moe.lava.banksia.room.Database
import moe.lava.banksia.room.converter.RouteTypeConverter
import moe.lava.banksia.room.entity.asEntity
import moe.lava.banksia.server.gtfs.structures.GtfsRoute
import moe.lava.banksia.server.gtfs.structures.GtfsShape
import moe.lava.banksia.server.gtfs.structures.GtfsStop
import moe.lava.banksia.server.gtfs.structures.GtfsStopTime
import moe.lava.banksia.server.gtfs.structures.GtfsTrip
import moe.lava.banksia.util.Point
import java.io.File
import java.util.zip.ZipFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class GtfsHandler(
    private val log: Logger,
    private val client: HttpClient,
    private val db: Database,
) {
    private val csv = CsvFormat(StringDeferringConfig(EmptySerializersModule()))
    private val datasetPath = File("/tmp/banksia", "dataset.zip")

    @OptIn(ExperimentalTime::class)
    suspend fun update(datasetUrl: String, date: Long? = null) {
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
        } else {
            extractAll(datasetPath)
        }

        addRoutes(files)
        addStops(files)
        addShapes(files)
        addTrips(files)
        addStopTimes(files)

        updateMetadata(date ?: Clock.System.now().epochSeconds)

        @Suppress("KotlinConstantConditions")
        if (!Constants.devMode) {
            parentDir.deleteRecursively()
        }

        log.info("done!")
    }

    private suspend fun updateMetadata(date: Long) {
        val dao = db.versionMetadataDao
        log.info("updating metadata...")
        dao.update(date, listOf("routes", "stops", "shapes", "trips", "stop_times"))
    }

    private suspend fun addRoutes(files: List<File>) {
        val dao = db.routeDao
        log.info("parsing routes...")
        val routes = files
            .filter { it.name == "routes.txt" }
            .flatMap { fd -> parseRoutes(fd) }

        log.info("inserting routes...")
        dao.deleteAll()
        dao.insertAll(*routes.map { it.asEntity() }.toTypedArray())
    }

    private fun parseRoutes(fd: File) =
        fd.parseCsv<GtfsRoute>()
            .map { with(it) {
                Route(
                    id = route_id,
                    type = RouteTypeConverter.from(fd.parentFile.name.toInt()),
                    number = route_short_name,
                    name = route_long_name,
                )
            } }

    private suspend fun addShapes(files: List<File>) {
        val dao = db.shapeDao
        log.info("parsing shapes...")
        val shapes = files
            .filter { it.name == "shapes.txt" }
            .flatMap { fd -> parseShapes(fd) }

        log.info("inserting shapes...")
        dao.deleteAll()
        dao.insertAll(*shapes.map { it.asEntity() }.toTypedArray())
    }

    private fun parseShapes(fd: File) =
        fd.parseCsv<GtfsShape>()
            .groupBy { it.shape_id }
            .map { (id, group) ->
                val points = group
                    .sortedBy { it.shape_pt_sequence }
                    .map { Point(it.shape_pt_lat, it.shape_pt_lon) }

                Shape(id, points)
            }

    private suspend fun addStops(files: List<File>) {
        val dao = db.stopDao
        log.info("parsing stops...")
        val stops = files
            .filter { it.name == "stops.txt" }
            .flatMap { fd -> parseStops(fd) }

        log.info("inserting stops...")
        dao.deleteAll()
        stops
            .groupBy { it.id }
            .forEach { (id, gstops) ->
                if (gstops.size > 1) {
                    if (gstops.withIndex().any { (i, stop) -> i != 0 && stop != gstops[i - 1] }) {
                        gstops.forEach {
                            log.info("duplicate $id: $it")
                        }
                    }
                }
            }
        dao.insertOrReplaceAll(*stops.map { it.asEntity() }.toTypedArray())
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

    private suspend fun addStopTimes(files: List<File>) {
        val dao = db.stopTimeDao
        dao.deleteAll()
        log.info("parsing stop times...")
        files
            .filter { it.name == "stop_times.txt" }
            .forEach { fd ->
                log.info("parsing stop times for ${fd.parent}...")
                parseStopTimes(fd) { seq ->
                    seq.chunked(1000000)
                        .forEach { queue ->
                            log.info("converting stop times (${queue.size}) for ${fd.parent}...")
                            val conv = queue.map { it.asEntity() }.toTypedArray()
                            log.info("inserting stop times (${conv.size}) for ${fd.parent}...")
                            dao.insertOrReplaceAll(*conv)
                        }
                }
            }
    }

    private inline fun parseStopTimes(fd: File, block: (Sequence<StopTime>) -> Unit) =
        fd.parseCsvSequence<GtfsStopTime> { seq ->
            seq
                .map { with(it) {
                    StopTime(
                        tripId = trip_id,
                        stopId = stop_id,
                        arrivalTime = GtfsStopTime.parseGtfsTime(arrival_time),
                        departureTime = GtfsStopTime.parseGtfsTime(departure_time),
                        headsign = stop_headsign,
                        pickupType = pickup_type,
                        dropOffType = drop_off_type,
                    )
                } }
                .let { block(it) }
        }

    private suspend fun addTrips(files: List<File>) {
        val dao = db.tripDao
        log.info("parsing trips...")
        val trips = files
            .filter { it.name == "trips.txt" }
            .flatMap { fd -> parseTrips(fd) }

        log.info("inserting trips...")
        dao.deleteAll()
        dao.insertOrReplaceAll(*trips.map { it.asEntity() }.toTypedArray())
    }

    private fun parseTrips(fd: File) =
        fd.parseCsv<GtfsTrip>()
            .map { with(it) {
                Trip(
                    id = trip_id,
                    routeId = route_id,
                    serviceId = service_id,
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
