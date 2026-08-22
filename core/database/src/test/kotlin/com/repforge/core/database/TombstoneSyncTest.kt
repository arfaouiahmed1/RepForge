package com.repforge.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.repforge.core.database.entity.SetLogEntity
import com.repforge.core.database.entity.SyncOperationEntity
import com.repforge.core.database.entity.TrainingSessionEntity
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tombstone + outbox contract tests (todo 9) against an in-memory Room database (Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TombstoneSyncTest {

    private lateinit var db: RepForgeDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RepForgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun session(id: String, startedAt: Long = 100L) = TrainingSessionEntity(
        id = id,
        routineId = "ppl_push",
        routineName = "PPL Push",
        state = "COMPLETED",
        startedAt = startedAt,
        completedAt = startedAt + 60_000,
    )

    @Test
    fun softDeleteTombstonesRowAndDefaultQueryExcludesItWhileSyncQueryIncludesIt() = runTest {
        val dao = db.trainingSessionDao()
        dao.upsert(session("s1"))
        assertEquals(1, dao.observeAll().first().size)
        assertEquals(0L, dao.getById("s1")!!.revision)
        assertNull(dao.getById("s1")!!.deletedAt)

        dao.softDelete("s1", now = 200L)

        assertTrue(dao.observeAll().first().isEmpty())
        assertNull(dao.getById("s1"))

        val tombstoned = dao.getAllForSync().single()
        assertEquals(200L, tombstoned.deletedAt)
        assertEquals(200L, tombstoned.updatedAt)
        assertEquals(1L, tombstoned.revision)
    }

    @Test
    fun softDeleteIsIdempotentAndDoesNotDoubleBumpRevision() = runTest {
        val dao = db.trainingSessionDao()
        dao.upsert(session("s2"))
        dao.softDelete("s2", now = 200L)
        dao.softDelete("s2", now = 300L)

        val row = dao.getAllForSync().single()
        assertEquals(200L, row.deletedAt)
        assertEquals(1L, row.revision)
    }

    @Test
    fun mergeByIdUpsertsRowsTransactionallyIncludingTombstones() = runTest {
        val dao = db.trainingSessionDao()
        dao.upsert(session("s3"))
        val cloudCopy = session("s3", startedAt = 500L).copy(
            state = "COMPLETED",
            deletedAt = 900L,
            revision = 7,
        )
        dao.mergeById(listOf(cloudCopy))

        val merged = dao.getAllForSync().single()
        assertEquals(500L, merged.startedAt)
        assertEquals(900L, merged.deletedAt)
        assertEquals(7L, merged.revision)
    }

    @Test
    fun setLogSoftDeleteHiddenFromObserveQueriesButPresentInSyncPull() = runTest {
        val dao = db.setLogDao()
        val log = SetLogEntity(
            setId = "log1", sessionId = "s1", exerciseId = "bench_bb", exerciseName = "Bench Press",
            setIndex = 1, weightKg = 80.0, targetReps = 8, completedReps = 8, rir = 2, rpe = null,
            restSeconds = 90, durationSeconds = null, isWarmup = false, isFailure = false,
            timestamp = 123L, recommendationId = null, recommendationAccepted = null,
        )
        dao.upsert(log)
        assertEquals(1, dao.observeRecent(10).first().size)

        dao.softDelete("log1", now = 999L)

        assertTrue(dao.observeRecent(10).first().isEmpty())
        assertTrue(dao.observeForSession("s1").first().isEmpty())
        assertEquals(1, dao.getAllForSync().size)
    }

    @Test
    fun duplicateOperationIdReplayIsRejectedWithNoDuplicateRow() = runTest {
        val dao = db.syncOperationDao()
        val op = SyncOperationEntity(
            operationId = UUID.randomUUID().toString(),
            entityType = "set_log",
            entityId = "log1",
            operation = "insert",
            payloadJson = """{"weightKg":80.0}""",
            baseRevision = 0,
            idempotencyKey = "idem-key-1",
            createdAt = 1_000L,
        )

        val firstInsert = dao.insert(op)
        val replayInsert = dao.insert(op.copy(createdAt = 2_000L))

        assertNotEquals(-1L, firstInsert)
        assertEquals(-1L, replayInsert)
        assertEquals(1, dao.getAll().size)
        assertEquals(1, dao.getPending().size)
        assertTrue(dao.existsByOperationId(op.operationId))
    }

    @Test
    fun duplicateIdempotencyKeyIsRejectedByUniqueIndex() = runTest {
        val dao = db.syncOperationDao()
        val first = SyncOperationEntity(
            operationId = UUID.randomUUID().toString(),
            entityType = "routine",
            entityId = "r1",
            operation = "update",
            payloadJson = "{}",
            baseRevision = 3,
            idempotencyKey = "same-key",
            createdAt = 1L,
        )
        val second = first.copy(operationId = UUID.randomUUID().toString())

        dao.insert(first)
        val rejected = dao.insert(second)

        assertEquals(-1L, rejected)
        assertEquals(1, dao.getAll().size)
    }

    @Test
    fun markSyncedMovesRowOutOfPendingAndPruneDropsAcknowledgedRows() = runTest {
        val dao = db.syncOperationDao()
        val op = SyncOperationEntity(
            operationId = UUID.randomUUID().toString(),
            entityType = "body_metric",
            entityId = "bm1",
            operation = "insert",
            payloadJson = "{}",
            baseRevision = 0,
            idempotencyKey = "idem-key-2",
            createdAt = 5_000L,
        )
        dao.insert(op)

        dao.markSynced(op.operationId)

        assertTrue(dao.getPending().isEmpty())
        assertEquals(1, dao.getAll().size)

        dao.pruneSyncedBefore(beforeEpochMs = 6_000L)

        assertTrue(dao.getAll().isEmpty())
    }
}
