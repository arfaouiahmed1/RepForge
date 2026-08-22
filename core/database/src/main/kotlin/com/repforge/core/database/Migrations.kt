package com.repforge.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v3 → v4 (todo 9 canonical sync model):
 *  - Tombstone columns on user-data tables. NOT NULL additions carry DEFAULT 0 because
 *    SQLite requires a default when adding a NOT NULL column to a non-empty table; the
 *    entities declare matching @ColumnInfo(defaultValue = "0") so schema validation passes.
 *  - sync_operations rebuilt: PK changes from autogen Int id to String operationId, which
 *    ALTER TABLE cannot express. Legacy rows keep their queue position: id → operationId,
 *    idempotencyKey = "legacy-<id>" (unique), synced flag preserved.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf("training_sessions", "set_logs", "body_metrics", "routine_exercises").forEach { table ->
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `revision` INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `deletedAt` INTEGER")
        }
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `revision` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `deletedAt` INTEGER")
        db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `revision` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN `deletedAt` INTEGER")

        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `_new_sync_operations` (" +
                "`operationId` TEXT NOT NULL, " +
                "`entityType` TEXT NOT NULL, " +
                "`entityId` TEXT NOT NULL, " +
                "`operation` TEXT NOT NULL, " +
                "`payloadJson` TEXT NOT NULL, " +
                "`baseRevision` INTEGER NOT NULL DEFAULT 0, " +
                "`idempotencyKey` TEXT NOT NULL, " +
                "`createdAt` INTEGER NOT NULL, " +
                "`synced` INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(`operationId`))"
        )
        db.execSQL(
            "INSERT INTO `_new_sync_operations` " +
                "(`operationId`,`entityType`,`entityId`,`operation`,`payloadJson`,`baseRevision`,`idempotencyKey`,`createdAt`,`synced`) " +
                "SELECT CAST(`id` AS TEXT), `entityType`, `entityId`, `operation`, `payloadJson`, 0, 'legacy-' || CAST(`id` AS TEXT), `createdAt`, `synced` " +
                "FROM `sync_operations`"
        )
        db.execSQL("DROP TABLE `sync_operations`")
        db.execSQL("ALTER TABLE `_new_sync_operations` RENAME TO `sync_operations`")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sync_operations_idempotencyKey` ON `sync_operations` (`idempotencyKey`)")
    }
}
