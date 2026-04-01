package moe.lava.banksia.data.ptv.structures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import moe.lava.banksia.core.model.RouteType

@Serializable
data class PtvRoute(
    @SerialName("route_type") val routeType: PtvRouteType,
    @SerialName("route_id") val routeId: Int,
    @SerialName("route_number") val routeNumber: String,
    @SerialName("route_name") val routeName: String,
    @SerialName("route_gtfs_id") val routeGtfsId: String,
    @SerialName("geopath") val geopath: List<PtvGeopath>,
) {
    fun gtfsSubType(): RouteType =
        RouteType.entries.first { routeGtfsId.startsWith(it.value.toString() + "-") }

    fun getShortFullName(): String {
        var res = ""
        if (this.routeNumber != "")
            res += this.routeNumber + " - "
        res += this.routeName.split(" via")[0]
        return res
    }
}
