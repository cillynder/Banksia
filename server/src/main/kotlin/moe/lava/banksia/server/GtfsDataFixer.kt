package moe.lava.banksia.server

import moe.lava.banksia.core.room.Database
import moe.lava.banksia.core.room.entity.StopEntity
import moe.lava.banksia.core.util.log
import java.security.MessageDigest

class GtfsDataFixer(
    private val database: Database,
) {
    suspend fun addParentsToStops() {
        val dao = database.stopDao
        val stops = dao.getAllParentless()
        stops
            .groupBy { it.name.split("/")[0] }
            .filter { (_, stops) -> stops.size > 1 }
            .forEach { (name, stops) ->
                val avgLat = stops.map { it.lat }.average()
                val avgLng = stops.map { it.lng }.average()
                val hash = name.sha256().substring(0, 7)
                val parentId = "bsia:df1:$hash"
                val parent = StopEntity(
                    id = parentId,
                    name = name,
                    lat = avgLat,
                    lng = avgLng,
                    parent = null,
                    hasWheelChairBoarding = stops.all { it.hasWheelChairBoarding },
                    level = "",
                    platformCode = "",
                )
                log("datafixer", "inserting ${parentId} for ${stops.size} children")
                dao.insertAll(parent)
                database.stopDao.updateParents(stops.map { it.id }, parentId)
            }
    }
}

private fun String.sha256() =
    MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
