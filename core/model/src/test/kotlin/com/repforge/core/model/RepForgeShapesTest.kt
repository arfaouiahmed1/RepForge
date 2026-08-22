package com.repforge.core.model

import com.repforge.core.designsystem.token.RepForgeShapes
import org.junit.Assert.*
import org.junit.Test

class RepForgeShapesTest {
    @Test fun `roles are distinct objects`() {
        assertNotSame(RepForgeShapes.Hero, RepForgeShapes.PrimaryActionCircle)
        assertNotSame(RepForgeShapes.Metric, RepForgeShapes.Media)
        assertNotSame(RepForgeShapes.NavPill, RepForgeShapes.Timer)
    }

    @Test fun `all shapes non-null`() {
        listOf(
            RepForgeShapes.Hero, RepForgeShapes.HeroInverse, RepForgeShapes.PrimaryAction,
            RepForgeShapes.Metric, RepForgeShapes.Media, RepForgeShapes.Sheet,
            RepForgeShapes.NavPill, RepForgeShapes.Timer, RepForgeShapes.CardLarge
        ).forEach { assertNotNull(it) }
    }
}
