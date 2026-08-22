package com.repforge.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.repforge.core.database.RepForgeDatabase
import com.repforge.core.model.SetLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Todo 10 acceptance: completeSet persists locally AND enqueues outbox atomically;
 * outbox depth increments per operation; same operationId replay is idempotent.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkoutRepositoryTest {

    private lateinit var db: RepForgeDatabase
    private lateinit var repo: WorkoutRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RepForgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = WorkoutRepository(
            sessionDao = db.trainingSessionDao(),
            setLogDao = db.setLogDao(),
            db = db,
            syncDao = db.syncOperationDao(),
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun setLog(id: String, sessionId: String = "s1") = SetLog(
        setId = id, sessionId = sessionId, exerciseId = "bench_bb", exerciseName = "Bench Press",
        setIndex = 1, weightKg = 82.5, targetReps = 8, completedReps = 8, rir = 2, rpe = null,
        restSeconds = 120, durationSeconds = null, isWarmup = false, isFailure = false,
        timestamp = 1_000L, recommendationId = null, recommendationAccepted = null,
    )

    @Test
    fun completeSetUpdatesRoomFlowImmediately() = runBlocking {
        repo.completeSet(setLog("set1"))
        val sets = repo.observeSets("s1").first()
        assertEquals(1, sets.size)
        assertEquals(82.5, sets.single().weightKg, 0.001)
    }

    @Test
    fun outboxDepthIncrementsPerOperation() = runBlocking {
        assertEquals(0, db.syncOperationDao().getAll().size)
        repo.completeSet(setLog("a"))
        assertEquals(1, db.syncOperationDao().getAll().size)
        repo.completeSet(setLog("b"))
        assertEquals(2, db.syncOperationDao().getAll().size)
        assertTrue(db.syncOperationDao().getAll().all { !it.synced })
    }

    @Test
    fun replaySameOperationIdIsIdempotent() = runBlocking {
        val opId = "op-fixed-1"
        repo.completeSet(setLog("dup"), operationId = opId)
        repo.completeSet(setLog("dup"), operationId = opId)

        assertEquals(1, db.syncOperationDao().getAll().size)
        assertEquals(opId, db.syncOperationDao().getAll().single().operationId)
        assertEquals(1, repo.observeSets("s1").first().size)
    }

    @Test
    fun distinctOperationsForSameSetAreBothEnqueued() = runBlocking {
        repo.completeSet(setLog("edit-me"), operationId = "op-1")
        repo.completeSet(setLog("edit-me"), operationId = "op-2")

        assertEquals(2, db.syncOperationDao().getAll().size)
        assertEquals(1, repo.observeSets("s1").first().size)
    }

    @Test
    fun pendingQueueOnlyContainsUnsyncedOperations() = runBlocking {
        repo.completeSet(setLog("x"), operationId = "op-x")
        db.syncOperationDao().markSynced("op-x")
        assertEquals(0, db.syncOperationDao().getPending().size)
        assertEquals(1, db.syncOperationDao().getAll().size)
    }
}
