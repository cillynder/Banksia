package moe.lava.banksia.server.gtfsrt

import com.google.transit.realtime.FeedMessage
import moe.lava.banksia.util.Point

class RealtimeVehiclePositions(data: FeedMessage) : GtfsRealtime(data) {
    private val positions = mutableMapOf<String, Point>()

    init {
        data.entity
            .mapNotNull { ent ->
                if (ent.vehicle?.position == null) return@mapNotNull null
                ent.id to ent.vehicle.position.run {
                    Point(latitude.toDouble(), longitude.toDouble())
                }
            }
            .let { positions.putAll(it) }
    }

    fun getAll() = positions.toMap()
    fun forTrip(tripId: String) = positions[tripId]
}
