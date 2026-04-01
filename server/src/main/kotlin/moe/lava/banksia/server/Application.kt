package moe.lava.banksia.server

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import moe.lava.banksia.core.Constants
import moe.lava.banksia.core.model.atDate
import moe.lava.banksia.core.room.dao.RouteDao
import moe.lava.banksia.core.room.dao.StopDao
import moe.lava.banksia.core.room.dao.StopTimeDao
import moe.lava.banksia.core.room.dao.VersionMetadataDao
import moe.lava.banksia.core.util.serialise
import moe.lava.banksia.server.di.ServerModules
import moe.lava.banksia.server.gtfsrt.GtfsrtService
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import kotlin.time.Clock

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }
    install(Koin) {
        modules(module { single { log } })
        modules(ServerModules)
    }

    val gtfsr by inject<GtfsrtService>()
    @Suppress("KotlinConstantConditions")
    launch { gtfsr.start(this, !Constants.devMode) }

    routing {
        if (Constants.devMode) {
            get("/fixup") {
                call.respondText("received")
                val fixer by inject<GtfsDataFixer>()
                fixer.addParentsToStops()
            }
        }
        get("/update") {
            val key = call.parameters["key"]
            if (key != Constants.updateKey) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            val datasetUuid = call.parameters["uuid"] ?: "fb152201-859f-4882-9206-b768060b50ad"
            val datasetUrl = call.parameters["url"]
                ?: "https://opendata.transport.vic.gov.au/dataset/3f4e292e-7f8a-4ffe-831f-1953be0fe448/resource/${datasetUuid}/download/gtfs.zip"
            call.respondText("received")
            launch(context = Dispatchers.IO) {
                val fixer by inject<GtfsDataFixer>()
                val importer by inject<GtfsImporter>()
                importer.import(datasetUrl)

                fixer.addParentsToStops()
            }
        }

        get("/metadata/{type?}") {
            val dao by inject<VersionMetadataDao>()
            val type = call.parameters["type"]
            if (type == null) {
                call.respond(dao.getAll().map { it.asModel() })
                return@get
            }

            val data = dao.get(type)?.asModel()
            if (data == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(data)
            }
        }

        get("/routes") {
            val routes = withContext(context = Dispatchers.IO) {
                inject<RouteDao>().value.getAll()
            }
            val res = routes.map { it.asModel() }
            call.respond(res)
        }
        get("/routes/{route_id}") {
            val routeId = call.parameters["route_id"]!!
            val route = withContext(context = Dispatchers.IO) {
                inject<RouteDao>().value.get(routeId)
            }
            if (route != null)
                call.respond(route.asModel())
            else
                call.respond(HttpStatusCode.NotFound)
        }
        get("/stops") {
            val routes = withContext(context = Dispatchers.IO) {
                inject<StopDao>().value.getAll()
            }
            val res = routes.map { it.asModel() }
            call.respond(res)
        }
        get("/stops/{stop_id}") {
            val stopId = call.parameters["stop_id"]!!
            val stop = withContext(context = Dispatchers.IO) {
                inject<StopDao>().value.get(stopId)
            }
            if (stop != null)
                call.respond(stop.asModel())
            else
                call.respond(HttpStatusCode.NotFound)
        }
        get("/route_stops/{route_id}") {
            val routeId = call.parameters["route_id"]!!
            val useParent = call.queryParameters["parent"] !in listOf("false", "0")
            val stops = withContext(Dispatchers.IO) {
                val routeDao by inject<RouteDao>()
                if (useParent)
                    routeDao.stopsParent(routeId)
                else
                    routeDao.stops(routeId)
            }
            call.respond(stops.map { it.asModel() })
        }
        get("/stoptimes/by_stop/{stop_id}") {
            val stopId = call.parameters["stop_id"]!!
            val date = call.queryParameters["date"]
                ?.let { LocalDate.parse(it, LocalDate.Formats.ISO) }
                ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
            val times = withContext(context = Dispatchers.IO) {
                inject<StopTimeDao>().value
                    .getForStopDated(
                        stopId,
                        listOf(date.dayOfWeek).serialise(),
                        date.toEpochDays().toInt(),
                    )
                    .map { it.asModel().atDate(date) }
                    .sortedBy { it.departureTime }
            }
            call.respond(times)
        }
    }
}
