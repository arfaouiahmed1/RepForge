package com.repforge.core.designsystem.icon

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Fitness_center as RoundedFitnessCenter
import com.composables.icons.materialsymbols.rounded.Monitoring as RoundedMonitoring
import com.composables.icons.materialsymbols.rounded.Repeat as RoundedRepeat
import com.composables.icons.materialsymbols.rounded.Weight as RoundedWeight
import com.composables.icons.materialsymbols.roundedfilled.Fitness_center as RoundedFilledFitnessCenter
import com.composables.icons.materialsymbols.roundedfilled.Monitoring as RoundedFilledMonitoring
import com.composables.icons.materialsymbols.roundedfilled.Repeat as RoundedFilledRepeat
import com.composables.icons.materialsymbols.roundedfilled.Weight as RoundedFilledWeight

data class RepForgeSymbolRole(
    val default: ImageVector,
    val emphasized: ImageVector,
) {
    fun imageVector(emphasized: Boolean): ImageVector =
        if (emphasized) this.emphasized else default
}

object RepForgeSymbols {
    val ProgressStrength = RepForgeSymbolRole(
        default = MaterialSymbols.Rounded.RoundedFitnessCenter,
        emphasized = MaterialSymbols.RoundedFilled.RoundedFilledFitnessCenter,
    )
    val ProgressVolume = RepForgeSymbolRole(
        default = MaterialSymbols.Rounded.RoundedMonitoring,
        emphasized = MaterialSymbols.RoundedFilled.RoundedFilledMonitoring,
    )
    val ProgressReps = RepForgeSymbolRole(
        default = MaterialSymbols.Rounded.RoundedRepeat,
        emphasized = MaterialSymbols.RoundedFilled.RoundedFilledRepeat,
    )
    val ProgressLoad = RepForgeSymbolRole(
        default = MaterialSymbols.Rounded.RoundedWeight,
        emphasized = MaterialSymbols.RoundedFilled.RoundedFilledWeight,
    )
}
