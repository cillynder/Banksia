package moe.lava.banksia.server.gtfsrt

import com.google.transit.realtime.FeedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.lava.banksia.util.log
import java.io.File
import kotlin.time.Instant

private const val BASE_DIR = "./data/gtfsr-archive/"

internal class GtfsrtArchiver {
    private var started = false

    suspend fun start(flow: SharedFlow<Pair<String, ByteArray>>) {
        if (started) {
            log("GtfsrtArchiver", "Tried to start when already started")
            return
        }
        started = true
        coroutineScope {
            launch { compressJob() }

            flow.collect { (type, rawData) ->
                val data = try {
                    FeedMessage.ADAPTER.decode(rawData)
                } catch (e: Throwable) {
                    log("gtfsr $type", "Failed to parse proto: $e")
                    return@collect
                }
                val timestamp = data.header_.timestamp
                    ?: return@collect log("gtfsr $type", "Failed to read proto timestamp")

                val time = Instant.fromEpochSeconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())

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
                        target.writeBytes(rawData)
                    } catch (e: Throwable) {
                        log("gtfsr $type", "Failed to write ${target}: $e")
                    }
                }
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
