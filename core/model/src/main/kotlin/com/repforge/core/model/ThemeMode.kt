package com.repforge.core.model

/**
 * User-selected theme mode (todo 14 Profile/Settings).
 *
 * Persisted as [storageLabel]; [fromStorageLabel] tolerates unknown values by
 * falling back to SYSTEM so a corrupt preference can never break app launch.
 * Pure Kotlin — unit-testable on the JVM without Android.
 */
enum class ThemeMode(val storageLabel: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromStorageLabel(label: String?): ThemeMode =
            entries.firstOrNull { it.storageLabel == label } ?: SYSTEM
    }
}
