package com.repforge.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeTest {

    @Test
    fun `round trips every storage label`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(mode, ThemeMode.fromStorageLabel(mode.storageLabel))
        }
    }

    @Test
    fun `unknown label falls back to system`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorageLabel("sepia"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorageLabel(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromStorageLabel(""))
    }
}
