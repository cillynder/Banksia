package moe.lava.banksia.server

import io.ktor.util.logging.Logger
import moe.lava.banksia.core.model.Route
import moe.lava.banksia.core.model.Service
import moe.lava.banksia.core.model.ServiceException
import moe.lava.banksia.core.model.Shape
import moe.lava.banksia.core.model.Stop
import moe.lava.banksia.core.model.Trip
import moe.lava.banksia.core.sqld.DatabaseManager
import moe.lava.banksia.core.sqld.mappers.asDb
import moe.lava.banksia.server.gtfs.GtfsData
import moe.lava.banksia.server.gtfs.GtfsParser
import kotlin.time.Clock
import moe.lava.banksia.core.sqld.BanksiaDatabase as Database

class GtfsImporter(
    private val parser: GtfsParser,
    private val dbm: DatabaseManager,
    private val log: Logger,
) {
    suspend fun import(url: String, date: Long = Clock.System.now().epochSeconds) {
        val (database, close) = dbm.makeAlt()

        parser.update(url).collect { chunk ->
            when (chunk) {
                is GtfsData.RouteChunk -> database.addRoutes(chunk.routes)
                is GtfsData.ServiceChunk -> database.addServices(chunk.services)
                is GtfsData.ServiceExceptionChunk -> database.addServiceExceptions(chunk.exceptions)
                is GtfsData.ShapeChunk -> database.addShapes(chunk.shapes)
                is GtfsData.StopChunk -> database.addStops(chunk.stops)
                is GtfsData.TripChunk -> database.addTrips(chunk.trips)
            }
        }

        close()
        dbm.swap()
    }

    private fun Database.addRoutes(routes: List<Route>) {
        log.info("inserting routes...")
        routeQueries.transaction {
            routes.forEach {
                routeQueries.insert(it.asDb())
            }
        }
        log.info("done")
    }

    private fun Database.addServices(services: List<Service>) {
        log.info("inserting services...")
        serviceQueries.transaction {
            services.forEach {
                serviceQueries.insert(it.asDb())
            }
        }
        log.info("done")
    }

    private fun Database.addServiceExceptions(exceptions: List<ServiceException>) {
        log.info("inserting exceptions...")
        serviceExceptionQueries.transaction {
            exceptions.forEach {
                serviceExceptionQueries.insert(it.asDb())
            }
        }
        log.info("done")
    }

    private fun Database.addShapes(shapes: List<Shape>) {
        log.info("inserting shapes...")
        shapeQueries.transaction {
            shapes.forEach {
                shapeQueries.insert(it.asDb())
            }
        }
        log.info("done")
    }

    private fun Database.addStops(stops: List<Stop>) {
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

        stopQueries.transaction {
            stops.forEach {
                stopQueries.insert(it.asDb())
            }
        }
        log.info("done")
    }

    private fun Database.addTrips(trips: List<Trip.Undated>) {
        log.info("inserting ${trips.size} trips...")
        transaction {
            trips.forEach { trip ->
                stoppingPatternQueries.insert(trip.pattern.asDb())
                trip.pattern.stoptimes.forEach { stoptime ->
                    stopTimeQueries.insert(stoptime.asDb())
                }
                tripQueries.insert(trip.asDb())
            }
        }
        log.info("done")
    }
}
