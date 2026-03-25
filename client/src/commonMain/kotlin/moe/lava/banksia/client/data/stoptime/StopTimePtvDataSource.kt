package moe.lava.banksia.client.data.stoptime

import moe.lava.banksia.data.ptv.PtvService
import moe.lava.banksia.model.RouteType
import moe.lava.banksia.model.StopTime

class StopTimePtvDataSource(
    private val ptvService: PtvService,
) {
    suspend fun getForStop(type: RouteType, stopId: String): List<StopTime> {
        return listOf()
//        val res = ptvService.departures(type, stopId)
//        // Map<
//        //     Pair<DirectionId, RouteId>,
//        //     Pair<DirectionName, List<DepartureTimes>>
//        // >
//        val timetable = HashMap<Pair<Int, Int>, Pair<String, MutableList<String>>>()
//        res.departures.forEach { dep ->
//            val key = Pair(dep.directionId, dep.routeId)
//            val direction = ptvService.direction(dep.directionId, dep.routeId)
//            val route = res.routes[dep.routeId.toString()]
//            val prefix = route?.let { if (it.routeNumber == "") "" else "${it.routeNumber} - " } ?: ""
//            val element = timetable.getOrPut(key) { Pair(prefix + direction.directionName, mutableListOf()) }.second
//            if (element.size >= 5)
//                return@forEach
//
//            val date = Instant.parse(dep.estimatedDepartureUtc ?: dep.scheduledDepartureUtc)
//            val min = (date - Clock.System.now()).inWholeMinutes
//            if (min <= -5)
//                return@forEach
//            if (min >= 65)
//                element.add("${((min + 30.0) / 60.0).toInt()}hr")
//            else
//                element.add("${min}mn")
//        }
//
//        val departures = timetable.values.sortedBy { it.first }.map { (name, list) ->
//            if (list.isEmpty())
//                InfoPanelState.Stop.Departure(name, "No departures")
//            else
//                InfoPanelState.Stop.Departure(name, list.joinToString(" | "))
//        }
    }
}
