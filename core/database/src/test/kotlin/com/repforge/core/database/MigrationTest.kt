package com.repforge.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Migration 3→4 smoke test (todo 9), WITHOUT MigrationTestHelper.
 *
 * Robolectric's instrumentation context does not reliably expose merged assets to
 * Room's helper on this toolchain, so we build the real v3 database from the EXPORTED
 * schema JSON (./schemas/3.json) using plain SQL — DDL for every entity/index, the v3
 * identityHash into room_master_table, PRAGMA user_version = 3 — insert legacy rows,
 * then open through Room with MIGRATION_3_4 registered. Room runs the migration AND
 * performs its own full schema validation against the compiled @Database (throws
 * "Migration didn't properly handle" on any column/index mismatch), which is stricter
 * than helper-based validation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun createSqlOf(entity: kotlinx.serialization.json.JsonObject): String =
        entity.getValue("createSql").jsonPrimitive.content
            .replace("\${TABLE_NAME}", entity.getValue("tableName").jsonPrimitive.content)

    @Test
    fun migrate3To4PreservesRowsAddsTombstoneColumnsAndRebuildsOutbox() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = File(context.getDatabasePath("migration-test-v3.db").parentFile, "migration-test-v3.db")
        dbFile.parentFile.mkdirs()
        if (dbFile.exists()) dbFile.delete()

        // ── Build the v3 database from the exported schema ──────────────────────
        val schema = json.parseToJsonElement(
            File("schemas/com.repforge.core.database.RepForgeDatabase/3.json").readText()
        ).jsonObject
        val database = schema.getValue("database").jsonObject
        val entities = database.getValue("entities").jsonArray

        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { v3 ->
            entities.forEach { e ->
                val obj = e.jsonObject
                v3.execSQL(createSqlOf(obj))
                if (obj.containsKey("indices")) {
                    obj.getValue("indices").jsonArray.forEach { i ->
                        v3.execSQL(
                            i.jsonObject.getValue("createSql").jsonPrimitive.content
                                .replace("\${TABLE_NAME}", obj.getValue("tableName").jsonPrimitive.content)
                        )
                    }
                }
            }
            val identityHash = database.getValue("identityHash").jsonPrimitive.content
            v3.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            v3.execSQL("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES(42, '$identityHash')")
            v3.version = 3

            // ── Legacy rows (pre-tombstone columns, autogen Int outbox id) ────────
            v3.execSQL(
                "INSERT INTO routines (id, name, description, dayOfWeek, estimatedMin, level, createdAt, updatedAt) " +
                    "VALUES ('r1', 'PPL Push', NULL, 1, 62, 'INTERMEDIATE', 10, 20)"
            )
            v3.execSQL(
                "INSERT INTO training_sessions (id, routineId, routineName, state, startedAt, completedAt) " +
                    "VALUES ('s1', 'r1', 'PPL Push', 'COMPLETED', 100, 200)"
            )
            v3.execSQL(
                "INSERT INTO sync_operations (entityType, entityId, operation, payloadJson, createdAt, synced) " +
                    "VALUES ('set_log', 'log1', 'insert', '{}', 5, 0)"
            )
        }

        // ── Open through Room: runs MIGRATION_3_4 then validates full schema ────
        val db = Room.databaseBuilder(context, RepForgeDatabase::class.java, dbFile.absolutePath)
            .addMigrations(MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()
        try {
            // Any query forces open -> migration -> Room schema validation.
            db.runInTransaction {
                db.openHelper.writableDatabase.query("SELECT revision, deletedAt FROM routines WHERE id = 'r1'").use { c ->
                    assertTrue(c.moveToFirst())
                    assertEquals(0L, c.getLong(0))
                    assertTrue(c.isNull(1))
                }
                db.openHelper.writableDatabase.query("SELECT revision, deletedAt FROM training_sessions WHERE id = 's1'").use { c ->
                    assertTrue(c.moveToFirst())
                    assertEquals(0L, c.getLong(0))
                    assertTrue(c.isNull(1))
                }
                db.openHelper.writableDatabase.query(
                    "SELECT operationId, entityType, entityId, operation, payloadJson, baseRevision, " +
                        "idempotencyKey, createdAt, synced FROM sync_operations"
                ).use { c ->
                    assertTrue(c.moveToFirst())
                    assertEquals("1", c.getString(0)) // legacy Int id carried over as TEXT PK
                    assertEquals("set_log", c.getString(1))
                    assertEquals("log1", c.getString(2))
                    assertEquals("insert", c.getString(3))
                    assertEquals("{}", c.getString(4))
                    assertEquals(0L, c.getLong(5))
                    assertEquals("legacy-1", c.getString(6))
                    assertEquals(5L, c.getLong(7))
                    assertEquals(0, c.getInt(8))
                    assertTrue(c.isLast) // exactly one migrated outbox row — no duplicates
                }
            }
        } finally {
            db.close()
        }
    }
}
