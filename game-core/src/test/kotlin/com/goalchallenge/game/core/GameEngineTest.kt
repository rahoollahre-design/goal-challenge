package com.goalchallenge.game.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameEngineTest {
    @Test
    fun resetScoreStartsFreshRound() {
        val engine = GameEngine(SeededRandomSource(7))
        engine.setScore(5, 4)
        engine.resetScoreAndStart()
        assertEquals(0, engine.state.messiScore)
        assertEquals(0, engine.state.ronaldoScore)
        assertEquals(GamePhase.PLAYING, engine.state.phase)
    }

    @Test
    fun holeCannotActivateImmediately() {
        val engine = GameEngine(SeededRandomSource(1))
        engine.startRound()
        repeat(30) { engine.tick() }
        assertTrue(engine.state.phase == GamePhase.PLAYING)
        assertFalse(engine.state.messiBall.falling)
        assertFalse(engine.state.ronaldoBall.falling)
    }

    @Test
    fun fixedSeedIsReproducible() {
        val a = GameEngine(SeededRandomSource(42))
        val b = GameEngine(SeededRandomSource(42))
        a.startRound(); b.startRound()
        repeat(180) { a.tick(); b.tick() }
        assertEquals(a.state.messiBall.x, b.state.messiBall.x)
        assertEquals(a.state.messiBall.y, b.state.messiBall.y)
        assertEquals(a.state.ronaldoBall.x, b.state.ronaldoBall.x)
        assertEquals(a.state.ronaldoBall.y, b.state.ronaldoBall.y)
    }

    @Test
    fun simulationEventuallyScores() {
        val engine = GameEngine(SeededRandomSource(11))
        engine.startRound()
        repeat(60 * 120) {
            engine.tick()
            if (engine.state.messiScore + engine.state.ronaldoScore > 0) return@repeat
        }
        assertTrue(engine.state.messiScore + engine.state.ronaldoScore > 0)
    }
}
