package moe.lava.banksia.data.ptv

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import moe.lava.banksia.core.Constants
import moe.lava.banksia.core.model.RouteType
import moe.lava.banksia.core.util.LoopFlow.Companion.initWith
import moe.lava.banksia.core.util.error
import moe.lava.banksia.core.util.log
import moe.lava.banksia.core.util.loopFlow
import moe.lava.banksia.data.ptv.structures.PtvDeparture
import moe.lava.banksia.data.ptv.structures.PtvDirection
import moe.lava.banksia.data.ptv.structures.PtvRoute
import moe.lava.banksia.data.ptv.structures.PtvRouteType
import moe.lava.banksia.data.ptv.structures.PtvRouteType.Companion.asPtvType
import moe.lava.banksia.data.ptv.structures.PtvRun
import moe.lava.banksia.data.ptv.structures.PtvStop
import okio.ByteString.Companion.encodeUtf8
import kotlin.random.Random

object Responses {
    @Serializable
    data class PtvRouteResponse(val route: PtvRoute)
    @Serializable
    data class PtvRoutesResponse(val routes: List<PtvRoute>)

    @Serializable
    data class PtvRunsResponse(val runs: List<PtvRun>)

    @Serializable
    data class PtvStopResponse(val stop: PtvStop)
    @Serializable
    data class PtvStopsResponse(val stops: List<PtvStop>)

    @Serializable
    data class PtvDeparturesResponse(val departures: List<PtvDeparture>, val routes: Map<String, PtvRoute>, val directions: Map<String, PtvDirection>)

    @Serializable
    data class PtvDirectionsResponse(val directions: List<PtvDirection>)
}

suspend inline fun <K, V> MutableMap<K, V>.getOrPutSuspend(key: K, defaultValue: suspend () -> V): V {
    if (!containsKey(key))
        this[key] = defaultValue()
    return this[key]!!
}

class PtvService() {
    class PtvCache(
        val directions: MutableMap<Pair<Int, Int>, PtvDirection> = mutableMapOf(),
        val routes: MutableMap<Int, PtvRoute> = mutableMapOf(),
        val runs: MutableMap<String, PtvRun> = mutableMapOf(),
        val stops: MutableMap<Int, PtvStop> = mutableMapOf(),
    )

    val cache = PtvCache()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        defaultRequest {
            url("https://timetableapi.ptv.vic.gov.au/v3/")
        }
    }

    init {
        client.plugin(HttpSend).intercept { req ->
            req.parameter("devid", Constants.devid)
            @OptIn(ExperimentalStdlibApi::class)
            req.parameter("nonce", Random.nextBytes(6).toHexString())
            val fullPath = req.url.build().encodedPathAndQuery
            val hash = fullPath.encodeUtf8().hmacSha1(Constants.key.encodeUtf8()).hex()
            req.parameter("signature", hash)
            log("ktor.intercept", req.url.build().encodedPathAndQuery)
            execute(req)
        }
    }

    suspend fun HttpClient.safeGet(
        urlString: String? = null,
        retries: Int = 1,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpResponse =
        runCatching {
            get {
                urlString?.let { url(it) }
                block?.invoke(this)
            }
        }.getOrElse { e ->
            error("PtvService", "Fetch error occurred (attempt $retries / 3), retrying in 5000ms...", e)
            if (retries >= 3)
                throw e
            delay(5000)
            safeGet(urlString, retries + 1, block)
        }

    suspend fun route(id: Int, includeGeopath: Boolean = false): PtvRoute {
        val cached = cache.routes[id]
        // TODO: im braindead so clean this up later
        if (cached != null && (!includeGeopath || (includeGeopath && cached.geopath.isNotEmpty())))
            return cached

        return client
            .safeGet("routes") {
                url {
                    appendPathSegments(id.toString())
                    parameters.append("include_geopath", if (includeGeopath) "true" else "false")
                }
            }
            .body<Responses.PtvRouteResponse>()
            .route
            .also { cache.routes[it.routeId] = it }
    }

    suspend fun routes(): List<PtvRoute> {
        val cached = cache.routes
        if (cached.isEmpty()) {
            client
                .safeGet("routes")
                .body<Responses.PtvRoutesResponse>()
                .routes
                .forEach { route ->
                    cached[route.routeId] = route
                }
        }
        return cached.values.toList()
    }

    fun runFlow(ref: String, firstWithCache: Boolean = false) =
        loopFlow {
            client
                .safeGet {
                    url {
                        appendPathSegments("runs", ref)
                    }
                }
                .body<Responses.PtvRunsResponse>()
                .runs
                .also { it.forEach { run -> cache.runs[run.runRef] = run } }
                .let { emit(it[0]) }
        }.initWith {
            cache.runs[ref]?.let {
                if (firstWithCache)
                    emit(it)
            }
        }

    fun runsFlow(routeId: Int) =
        loopFlow {
            client
                .safeGet {
                    url {
                        appendPathSegments("runs", "route", routeId.toString())
                        parameter("expand", "VehiclePosition")
                    }
                }
                .body<Responses.PtvRunsResponse>()
                .runs
                .also { it.forEach { run -> cache.runs[run.runRef] = run } }
                .let { emit(it) }
        }

    suspend fun stopsByRoute(routeId: Int, routeType: PtvRouteType): List<PtvStop> =
        client
            .safeGet("stops") {
                url {
                    appendPathSegments(
                        "route", routeId.toString(),
                        "route_type", routeType.ordinal.toString(),
                    )
                }
            }
            .body<Responses.PtvStopsResponse>()
            .stops
            .also { it.forEach { stop -> cache.stops[stop.stopId] = stop } }

    suspend fun stop(routeType: PtvRouteType, stopId: Int): PtvStop =
        cache.stops.getOrPutSuspend(stopId) {
            client
                .safeGet {
                    url {
                        appendPathSegments(
                            "stops", stopId.toString(),
                            "route_type", routeType.ordinal.toString(),
                        )
                    }
                }
                .body<Responses.PtvStopResponse>()
                .stop
        }

    suspend fun directionsByRoute(routeId: Int): List<PtvDirection> =
        client
            .safeGet("directions") {
                url {
                    appendPathSegments("route", routeId.toString())
                }
            }
            .body<Responses.PtvDirectionsResponse>()
            .directions

    suspend fun direction(directionId: Int, routeId: Int): PtvDirection {
        if (!cache.directions.containsKey(directionId to routeId)) {
            val directions = directionsByRoute(routeId)
            for (direction in directions)
                cache.directions[direction.directionId to direction.routeId] = direction
        }

        return cache.directions[directionId to routeId]!!
    }

    suspend fun departures(routeType: RouteType, stopId: String): Responses.PtvDeparturesResponse =
        client
            .safeGet ("departures") {
                url {
                    appendPathSegments(
                        "route_type", routeType.asPtvType().ordinal.toString(),
                        "stop", stopId,
                    )
                    parameter("expand", "Route")
                    parameter("expand", "Direction")
                    parameter("gtfs", "true")
                }
            }.body()

    suspend fun departures(routeType: PtvRouteType, stopId: Int): Responses.PtvDeparturesResponse =
        client
            .safeGet ("departures") {
                url {
                    appendPathSegments(
                        "route_type", routeType.ordinal.toString(),
                        "stop", stopId.toString(),
                    )
                    parameter("expand", "Route")
                    parameter("expand", "Direction")
                }
            }.body()
}
