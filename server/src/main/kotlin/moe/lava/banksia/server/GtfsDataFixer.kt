package moe.lava.banksia.server

import moe.lava.banksia.core.sqld.BanksiaDatabase
import moe.lava.banksia.core.util.log
import java.security.MessageDigest
import moe.lava.banksia.core.sqld.Stop as DbStop

class GtfsDataFixer(
    private val database: BanksiaDatabase,
) {
    fun addParentsToStops() {
        val queries = database.stopQueries
        val stops = queries.getAllParentless().executeAsList()
        stops
            .groupBy { it.name.split("/")[0] }
            .filter { (_, stops) -> stops.size > 1 }
            .forEach { (name, stops) ->
                val avgLat = stops.map { it.lat }.average()
                val avgLng = stops.map { it.lng }.average()
                val hash = name.sha256().substring(0, 7)
                val parentId = "bsia:df1:$hash"
                val parent = DbStop(
                    id = parentId,
                    name = name,
                    lat = avgLat,
                    lng = avgLng,
                    parent = null,
                    hasWheelChairBoarding = if (stops.all { it.hasWheelChairBoarding == 1L }) 1L else 0L,
                    level = "",
                    platformCode = "",
                )
                log("datafixer", "inserting ${parentId} for ${stops.size} children")
                queries.transaction {
                    queries.insert(parent)
                    queries.updateParents(parentId, stops.map { it.id })
                }
            }
    }
}

private fun String.sha256() =
    MessageDigest
        .getInstance("SHA-256")
        .digest(this.toByteArray())
        .joinToString("") { "%02x".format(it) }
