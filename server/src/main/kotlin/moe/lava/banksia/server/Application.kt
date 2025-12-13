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
import moe.lava.banksia.Constants
import moe.lava.banksia.di.CommonModules
import moe.lava.banksia.room.dao.RouteDao
import moe.lava.banksia.room.dao.StopDao
import moe.lava.banksia.server.di.ServerModules
import moe.lava.banksia.server.gtfs.GtfsHandler
import moe.lava.banksia.server.gtfsr.GtfsrService
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

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
        modules(CommonModules, ServerModules)
    }

    val gtfsr by inject<GtfsrService>()
    launch { gtfsr.start() }

    routing {
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
                val handler by inject<GtfsHandler>()
                handler.update(datasetUrl)
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
            val useParent = call.queryParameters["parent"] in listOf("true", "1")
            val stops = withContext(Dispatchers.IO) {
                val routeDao by inject<RouteDao>()
                if (useParent)
                    routeDao.stopsParent(routeId)
                else
                    routeDao.stops(routeId)
            }
            call.respond(stops.map { it.asModel() })
//            val stops = withContext(Dispatchers.IO) {
//                val stopDao by inject<StopDao>()
//                val stopTimeDao by inject<StopTimeDao>()
//                val tripDao by inject<TripDao>()
//
//                tripDao.getByRoute(routeId)
//                    .map { it.id }
//                    .let { stopTimeDao.get(it) }
//                    .flatMap { it.asModel().stopInfos }
//                    .map { it.stopId }
//                    .let { stopDao.get(it) }
//                    .map { it.asModel() }
//            }
//            call.respond(stops)
        }
    }
}
