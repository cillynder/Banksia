package moe.lava.banksia.ui.state

import moe.lava.banksia.model.RouteType

data class SearchState(
    val entries: List<SearchEntry> = listOf(),
    val text: String = "",
) {
    data class SearchEntry(
        val mainText: String,
        val subText: String?,
        val routeId: String,
        val routeType: RouteType,
    )
}
