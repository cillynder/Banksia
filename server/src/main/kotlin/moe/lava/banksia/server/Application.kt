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
import moe.lava.banksia.core.sqld.RouteQueries
import moe.lava.banksia.core.sqld.StopQueries
import moe.lava.banksia.core.sqld.StopTimeQueries
import moe.lava.banksia.core.sqld.mappers.asModel
import moe.lava.banksia.core.util.serialise
import moe.lava.banksia.server.di.ServerModules
import moe.lava.banksia.server.gtfsrt.GtfsrtService
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import kotlin.time.Clock

fun main() {
    if (System.getenv("BANKSIA_PRODUCTION") == "1") Constants.devMode = false

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    log.info("devMode: ${Constants.devMode}")
    install(ContentNegotiation) {
        json()
    }
    install(Koin) {
        modules(module { single { log } })
        modules(ServerModules)
    }

    @Suppress("KotlinConstantConditions")
    launch { get<GtfsrtService>().start(this, !Constants.devMode) }

    routing {
        if (Constants.devMode) {
            get("/fixup") {
                call.respondText("received")
                get<GtfsDataFixer>().addParentsToStops()
            }
        }
        get("/manage/fixup") {
            val key = call.parameters["key"]
            if (key != Constants.updateKey) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }

            call.respondText("fixing")
            launch(context = Dispatchers.IO) {
                get<GtfsDataFixer>().addParentsToStops()
            }
        }
        get("/manage/update") {
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
                get<GtfsImporter>().import(datasetUrl)
                get<GtfsDataFixer>().addParentsToStops()
            }
        }

        get("/routes") {
            val routes = withContext(context = Dispatchers.IO) {
                get<RouteQueries>().getAll().executeAsList()
            }
            val res = routes.map { it.asModel() }
            call.respond(res)
        }
        get("/routes/{route_id}") {
            val routeId = call.parameters["route_id"]!!
            val route = withContext(context = Dispatchers.IO) {
                get<RouteQueries>().get(routeId).executeAsOneOrNull()
            }
            if (route != null) {
                call.respond(route.asModel())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/stops") {
            val routes = withContext(context = Dispatchers.IO) {
                get<StopQueries>().getAll().executeAsList()
            }
            val res = routes.map { it.asModel() }
            call.respond(res)
        }
        get("/stops/{stop_id}") {
            val stopId = call.parameters["stop_id"]!!
            val stop = withContext(context = Dispatchers.IO) {
                get<StopQueries>().get(stopId).executeAsOneOrNull()
            }
            if (stop != null) {
                call.respond(stop.asModel())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/route_stops/{route_id}") {
            val routeId = call.parameters["route_id"]!!
            val useParent = call.queryParameters["parent"] !in listOf("false", "0")
            val stops = withContext(Dispatchers.IO) {
                val queries = get<StopQueries>()
                if (useParent) {
                    queries.getParentsByRoute(routeId).executeAsList()
                } else {
                    queries.getByRoute(routeId).executeAsList()
                }
            }
            call.respond(stops.map { it.asModel() })
        }
        get("/stoptimes/by_stop/{stop_id}") {
            val stopId = call.parameters["stop_id"]!!
            val date = call.queryParameters["date"]
                ?.let { LocalDate.parse(it, LocalDate.Formats.ISO) }
                ?: Clock.System.todayIn(TimeZone.currentSystemDefault())
            val times = withContext(context = Dispatchers.IO) {
                get<StopTimeQueries>()
                    .getForStopDated(
                        listOf(date.dayOfWeek).serialise().toLong(),
                        date.toEpochDays(),
                        stopId,
                    )
                    .executeAsList()
                    .map { it.asModel().atDate(date) }
                    .sortedBy { it.time.departure }
            }
            call.respond(times)
        }
    }
}
