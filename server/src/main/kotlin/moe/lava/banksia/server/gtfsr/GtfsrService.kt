package moe.lava.banksia.server.gtfsr

import com.google.transit.realtime.FeedMessage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import moe.lava.banksia.Constants
import moe.lava.banksia.util.LogScope
import moe.lava.banksia.util.log
import java.io.File
import java.time.Instant
import java.time.ZoneId

private const val BASE_DIR = "./data/gtfsr-archive/"

class GtfsrService(private val client: HttpClient) {
    private var started = false
    private val latest = mutableMapOf<String, FeedMessage>()

    fun latestFor(type: String) = latest[type]

    private val iFlow = MutableSharedFlow<Pair<String, FeedMessage>>()
    val flow = iFlow.asSharedFlow()

    companion object {
        val types = arrayOf(
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
    }

    suspend fun start() {
        if (started) {
            log("GtfsrService", "Tried to start when already started")
            return
        }
        started = true
        coroutineScope {
            launch { compressJob() }

            while (true) {
                val results = mutableMapOf<String, ByteArray>()
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
                                results[type] = res.readRawBytes()
                            }
                        } catch (e: Throwable) {
                            logger.log("$e")
                            logger.log(e.stackTraceToString())
                        }
                    }
                }.joinAll()

                results.forEach { (type, data) ->
                    val dec = try {
                        FeedMessage.ADAPTER.decode(data)
                    } catch (e: Throwable) {
                        log("gtfsr $type", "Failed to parse proto: $e")
                        return@forEach
                    }
                    val timestamp = dec.header_.timestamp
                        ?: return@forEach log("gtfsr $type", "Failed to read proto timestamp")

                    val time = Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault())

                    val base = File(BASE_DIR, type)
                    val previousParent = File(base, "${time.year}-${((time.dayOfYear - 1) / 7).toString().padStart(2, '0')}")
                    val currentParent = File(base, "${time.year}-${((time.dayOfYear - 1) / 7 + 1).toString().padStart(2, '0')}")
                    val target = File(currentParent, "${timestamp}.proto")

                    if (previousParent.isDirectory) {
                        enqueueCompression(previousParent)
                    }

                    if (!target.exists()) {
                        try {
                            if (!target.parentFile.isDirectory) {
                                target.parentFile.mkdirs()
                            }
                            target.writeBytes(data)
                        } catch (e: Throwable) {
                            log("gtfsr $type", "Failed to write ${target}: $e")
                        }
                    }
                }
                delay(10000)
            }
        }
    }

    private val cqueue = mutableSetOf<File>()
    private val ignore = mutableSetOf<File>()
    private val cmut = Mutex()
    private suspend fun enqueueCompression(fd: File) {
        cmut.withLock { cqueue.add(fd) }
    }

    private suspend fun compressJob() {
        while(true) {
            while(true) {
                val next = cmut.withLock { cqueue.firstOrNull() }
                    ?: break
                if (!next.isDirectory) {
                    cmut.withLock { cqueue.remove(next) }
                    continue
                }
                if (next in ignore) continue

                withContext(Dispatchers.IO) {
                    val proc = ProcessBuilder(
                        "tar", "-acf",
                        "${next.absolutePath}.tar.zst",
                        next.absolutePath
                    ).start()
                    val exitCode = proc.waitFor()
                    if (exitCode == 0) {
                        if (next.deleteRecursively()) {
                            cmut.withLock { cqueue.remove(next) }
                        } else {
                            log("CompressJob", "Failed to delete $next")
                            ignore.add(next)
                        }
                    } else {
                        val msg = proc.errorStream.readAllBytes().decodeToString()
                        log("CompressJob", "Failed to delete $next (exit code $exitCode")
                        log("CompressJob", msg)
                    }
                }
            }
            delay(30000)
        }
    }
}
