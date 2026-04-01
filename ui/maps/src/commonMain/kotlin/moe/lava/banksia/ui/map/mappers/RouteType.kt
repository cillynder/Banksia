package moe.lava.banksia.ui.map.mappers

import androidx.compose.runtime.Composable
import moe.lava.banksia.core.model.RouteType
import moe.lava.banksia.ui.extensions.getUIProperties
import moe.lava.banksia.ui.platform.BanksiaTheme
import org.maplibre.compose.expressions.dsl.case
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.switch

internal val routeColorExpression @Composable get() = switch(
    input = feature["type"].convertToString(),
    cases = RouteType.entries.map {
        case(label = it.name, output = const(it.getUIProperties().colour))
    }.toTypedArray(),
    fallback = const(BanksiaTheme.colors.surface),
)
