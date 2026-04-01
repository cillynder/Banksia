package moe.lava.banksia.server

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import io.ktor.util.logging.Logger
import moe.lava.banksia.core.model.Route
import moe.lava.banksia.core.model.Service
import moe.lava.banksia.core.model.ServiceException
import moe.lava.banksia.core.model.Shape
import moe.lava.banksia.core.model.Stop
import moe.lava.banksia.core.model.StopTime
import moe.lava.banksia.core.model.Trip
import moe.lava.banksia.core.room.Database
import moe.lava.banksia.core.room.entity.asEntity
import moe.lava.banksia.server.gtfs.GtfsData
import moe.lava.banksia.server.gtfs.GtfsParser
import kotlin.time.Clock

class GtfsImporter(
    private val parser: GtfsParser,
    private val database: Database,
    private val log: Logger,
) {
    suspend fun import(url: String, date: Long = Clock.System.now().epochSeconds) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                database.routeDao.deleteAll()
                database.serviceDao.deleteAll()
                database.serviceExceptionDao.deleteAll()
                database.shapeDao.deleteAll()
                database.stopDao.deleteAll()
                database.stopTimeDao.deleteAll()
                database.tripDao.deleteAll()

                parser.update(url).collect { chunk ->
                    when (chunk) {
                        is GtfsData.RouteChunk -> addRoutes(chunk.routes)
                        is GtfsData.ServiceChunk -> addServices(chunk.services)
                        is GtfsData.ServiceExceptionChunk -> addServiceExceptions(chunk.exceptions)
                        is GtfsData.ShapeChunk -> addShapes(chunk.shapes)
                        is GtfsData.StopChunk -> addStops(chunk.stops)
                        is GtfsData.StopTimeChunk -> addStopTimes(chunk.stopTimes)
                        is GtfsData.TripChunk -> addTrips(chunk.trips)
                    }
                }

                updateMetadata(date)
            }
        }
    }

    private suspend fun updateMetadata(date: Long) {
        val dao = database.versionMetadataDao
        log.info("updating metadata...")
        dao.update(date, listOf("routes", "stops", "shapes", "trips", "stop_times"))
        log.info("done")
    }

    private suspend fun addRoutes(routes: List<Route>) {
        val dao = database.routeDao
        log.info("inserting routes...")
        dao.insertOrReplaceAll(*routes.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun addServices(services: List<Service>) {
        val dao = database.serviceDao
        log.info("inserting services...")
        dao.insertOrReplaceAll(*services.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun addServiceExceptions(exceptions: List<ServiceException>) {
        val dao = database.serviceExceptionDao
        log.info("inserting exceptions...")
        dao.insertOrReplaceAll(*exceptions.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun addShapes(shapes: List<Shape>) {
        val dao = database.shapeDao
        log.info("inserting shapes...")
        dao.insertOrReplaceAll(*shapes.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun addStops(stops: List<Stop>) {
        val dao = database.stopDao
        log.info("inserting stops...")
        stops
            .groupBy { it.id }
            .forEach { (id, gstops) ->
                if (gstops.size > 1) {
                    if (gstops.withIndex().any { (i, stop) -> i != 0 && stop != gstops[i - 1] }) {
                        gstops.forEach {
                            log.warn("duplicate $id: $it")
                        }
                    }
                }
            }
        dao.insertOrReplaceAll(*stops.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun addStopTimes(stopTimes: List<StopTime>) {
        val dao = database.stopTimeDao
        log.info("inserting ${stopTimes.size} stoptimes...")
        dao.insertOrReplaceAll(*stopTimes.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun addTrips(trips: List<Trip>) {
        val dao = database.tripDao
        log.info("inserting ${trips.size} trips...")
        dao.insertOrReplaceAll(*trips.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }
}
