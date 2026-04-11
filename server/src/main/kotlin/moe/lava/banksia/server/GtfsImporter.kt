package moe.lava.banksia.server

import io.ktor.util.logging.Logger
import moe.lava.banksia.core.model.Route
import moe.lava.banksia.core.model.Service
import moe.lava.banksia.core.model.ServiceException
import moe.lava.banksia.core.model.Shape
import moe.lava.banksia.core.model.Stop
import moe.lava.banksia.core.model.StopTime
import moe.lava.banksia.core.model.Trip
import moe.lava.banksia.core.room.Database
import moe.lava.banksia.core.room.DatabaseManager
import moe.lava.banksia.core.room.entity.asEntity
import moe.lava.banksia.server.gtfs.GtfsData
import moe.lava.banksia.server.gtfs.GtfsParser
import kotlin.time.Clock

class GtfsImporter(
    private val parser: GtfsParser,
    private val dbm: DatabaseManager,
    private val log: Logger,
) {
    suspend fun import(url: String, date: Long = Clock.System.now().epochSeconds) {
        val database = dbm.makeAlt()

        parser.update(url).collect { chunk ->
            when (chunk) {
                is GtfsData.RouteChunk -> database.addRoutes(chunk.routes)
                is GtfsData.ServiceChunk -> database.addServices(chunk.services)
                is GtfsData.ServiceExceptionChunk -> database.addServiceExceptions(chunk.exceptions)
                is GtfsData.ShapeChunk -> database.addShapes(chunk.shapes)
                is GtfsData.StopChunk -> database.addStops(chunk.stops)
                is GtfsData.StopTimeChunk -> database.addStopTimes(chunk.stopTimes)
                is GtfsData.TripChunk -> database.addTrips(chunk.trips)
            }
        }

        database.updateMetadata(date)
        database.close()
        dbm.swap()
    }

    private suspend fun Database.updateMetadata(date: Long) {
        val dao = versionMetadataDao
        log.info("updating metadata...")
        dao.update(date, listOf("routes", "stops", "shapes", "trips", "stop_times"))
        log.info("done")
    }

    private suspend fun Database.addRoutes(routes: List<Route>) {
        val dao = routeDao
        log.info("inserting routes...")
        dao.insertOrReplaceAll(*routes.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun Database.addServices(services: List<Service>) {
        val dao = serviceDao
        log.info("inserting services...")
        dao.insertOrReplaceAll(*services.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun Database.addServiceExceptions(exceptions: List<ServiceException>) {
        val dao = serviceExceptionDao
        log.info("inserting exceptions...")
        dao.insertOrReplaceAll(*exceptions.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun Database.addShapes(shapes: List<Shape>) {
        val dao = shapeDao
        log.info("inserting shapes...")
        dao.insertOrReplaceAll(*shapes.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun Database.addStops(stops: List<Stop>) {
        val dao = stopDao
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

    private suspend fun Database.addStopTimes(stopTimes: List<StopTime>) {
        val dao = stopTimeDao
        log.info("inserting ${stopTimes.size} stoptimes...")
        dao.insertOrReplaceAll(*stopTimes.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }

    private suspend fun Database.addTrips(trips: List<Trip>) {
        val dao = tripDao
        log.info("inserting ${trips.size} trips...")
        dao.insertOrReplaceAll(*trips.map { it.asEntity() }.toTypedArray())
        log.info("done")
    }
}
