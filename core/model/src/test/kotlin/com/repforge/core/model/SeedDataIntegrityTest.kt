package com.repforge.core.model

import com.repforge.core.database.seed.ExerciseSeed
import org.junit.Assert.*
import org.junit.Test

class SeedDataIntegrityTest {
    @Test fun `exercises at least 120`() {
        assertTrue(ExerciseSeed.all.size >= 120)
    }

    @Test fun `ids unique`() {
        val ids = ExerciseSeed.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun `all have glb`() {
        assertTrue(ExerciseSeed.all.all { it.glbAsset != null && it.glbAsset!!.endsWith(".glb") })
    }

    @Test fun `extended has instructions`() {
        assertTrue(ExerciseSeed.extended.all { it.instructions != null && it.instructions!!.isNotBlank() })
    }

    @Test fun `location valid`() {
        val valid = setOf("HOME","GYM","BOTH")
        assertTrue(ExerciseSeed.all.all { it.location in valid })
    }

    @Test fun `equipment valid`() {
        val valid = setOf("BARBELL","DUMBBELL","MACHINE","CABLE","BODYWEIGHT","KETTLEBELL","BAND","SMITH","EZ_BAR")
        assertTrue(ExerciseSeed.all.all { it.equipment in valid })
    }

    @Test fun `difficulty varied`() {
        val diffs = ExerciseSeed.all.map { it.difficulty }.toSet()
        assertTrue(diffs.size >= 3)
    }

    @Test fun `muscle groups cover`() {
        val groups = ExerciseSeed.all.map { it.muscleGroup }.toSet()
        assertTrue(groups.containsAll(listOf("CHEST","BACK","LEGS","SHOULDERS","CORE")))
    }
}
