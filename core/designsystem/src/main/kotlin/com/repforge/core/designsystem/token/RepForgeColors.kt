package com.repforge.core.designsystem.token

/**
 * Compat aliases over [FitnessVisualTokens] — the single token source.
 *
 * Raw hex values live ONLY in FitnessVisualTokens.kt (the token definition file).
 * New code should reference [FitnessVisualTokens] roles directly; these objects
 * remain for existing call sites until features migrate (todos 11-14).
 */
object RepForgeColors {
    // Ember — molten metal primary
    val Ember10 get() = FitnessVisualTokens.FitnessPalette.Ember10
    val Ember20 get() = FitnessVisualTokens.FitnessPalette.Ember20
    val Ember35 get() = FitnessVisualTokens.FitnessPalette.Ember35
    val Ember40 get() = FitnessVisualTokens.FitnessPalette.Ember40
    val Ember80 get() = FitnessVisualTokens.FitnessPalette.Ember80
    val Ember90 get() = FitnessVisualTokens.FitnessPalette.Ember90
    val Ember95 get() = FitnessVisualTokens.FitnessPalette.Ember95

    // Forge — charcoal neutral
    val Forge10 get() = FitnessVisualTokens.FitnessPalette.Forge10
    val Forge20 get() = FitnessVisualTokens.FitnessPalette.Forge20
    val Forge30 get() = FitnessVisualTokens.FitnessPalette.Forge30
    val Forge80 get() = FitnessVisualTokens.FitnessPalette.Forge80
    val Forge90 get() = FitnessVisualTokens.FitnessPalette.Forge90
    val Forge95 get() = FitnessVisualTokens.FitnessPalette.Forge95
    val Forge98 get() = FitnessVisualTokens.FitnessPalette.Forge98
    val Forge99 get() = FitnessVisualTokens.FitnessPalette.Forge99

    // Paper — warm surface neutral
    val Paper98 get() = FitnessVisualTokens.FitnessPalette.Paper98
    val Paper95 get() = FitnessVisualTokens.FitnessPalette.Paper95
    val Paper90 get() = FitnessVisualTokens.FitnessPalette.Paper90

    // Steel — secondary
    val Steel30 get() = FitnessVisualTokens.FitnessPalette.Steel30
    val Steel40 get() = FitnessVisualTokens.FitnessPalette.Steel40
    val Steel80 get() = FitnessVisualTokens.FitnessPalette.Steel80
    val Steel90 get() = FitnessVisualTokens.FitnessPalette.Steel90
    val Steel95 get() = FitnessVisualTokens.FitnessPalette.Steel95

    // Brass — tertiary surprise
    val Brass40 get() = FitnessVisualTokens.FitnessPalette.Brass40
    val Brass80 get() = FitnessVisualTokens.FitnessPalette.Brass80
    val Brass90 get() = FitnessVisualTokens.FitnessPalette.Brass90

    // Semantic — RIR / readiness
    val Success get() = FitnessVisualTokens.FitnessPalette.Success
    val SuccessContainer get() = FitnessVisualTokens.FitnessPalette.SuccessContainer
    val Warning get() = FitnessVisualTokens.FitnessPalette.Warning
    val WarningContainer get() = FitnessVisualTokens.FitnessPalette.WarningContainer
    val Error get() = FitnessVisualTokens.FitnessPalette.Error
    val ErrorContainer get() = FitnessVisualTokens.FitnessPalette.ErrorContainer

    // Expressive surfaces — immersive modes
    val RestSurfaceLight get() = FitnessVisualTokens.FitnessPalette.RestSurfaceLight
    val RestOnSurfaceLight get() = FitnessVisualTokens.FitnessPalette.RestOnSurfaceLight
    val WorkoutHeroLight get() = FitnessVisualTokens.FitnessPalette.WorkoutHeroLight
}

object RepForgeSemantic {
    val ReadyHigh get() = FitnessVisualTokens.ReadySemantic.High
    val ReadyHighContainer get() = FitnessVisualTokens.ReadySemantic.HighContainer
    val ReadyMedium get() = FitnessVisualTokens.ReadySemantic.Medium
    val ReadyMediumContainer get() = FitnessVisualTokens.ReadySemantic.MediumContainer
    val ReadyLow get() = FitnessVisualTokens.ReadySemantic.Low
    val ReadyLowContainer get() = FitnessVisualTokens.ReadySemantic.LowContainer
}
