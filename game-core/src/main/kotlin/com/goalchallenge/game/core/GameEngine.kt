package com.goalchallenge.game.core

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class GameEngine(
    private val random: RandomSource = DefaultRandomSource(),
    val state: GameState = GameState(),
) {
    private val events = ArrayDeque<GameEvent>()
    private var fallBall: BallState? = null
    private var fallTargetX = 0f
    private var fallTargetY = 0f

    fun drainEvents(): List<GameEvent> = buildList {
        while (events.isNotEmpty()) add(events.removeFirst())
    }

    fun setScore(messi: Int, ronaldo: Int) {
        state.messiScore = max(0, messi)
        state.ronaldoScore = max(0, ronaldo)
    }

    /**
     * Reset the score and return to the READY screen.
     * The user must tap the start button to begin the next round.
     */
    fun resetScoreToReady() {
        state.messiScore = 0
        state.ronaldoScore = 0
        state.phase = GamePhase.READY
        state.winner = null
        state.roundElapsedMs = 0f
        state.resultElapsedMs = 0f
        fallBall = null

        resetBall(state.messiBall, 55f, randomSignedSpeed())
        resetBall(
            state.ronaldoBall,
            GameConfig.WORLD_WIDTH - GameConfig.BALL_SIZE - 55f,
            randomSignedSpeed()
        )
    }

    /** Kept for compatibility with any existing call sites. */
    fun resetScoreAndStart() {
        state.messiScore = 0
        state.ronaldoScore = 0
        startRound()
    }

    fun startRound() {
        state.phase = GamePhase.PLAYING
        state.winner = null
        state.roundElapsedMs = 0f
        state.resultElapsedMs = 0f
        state.roundNumber++
        fallBall = null

        val blueVx = randomSignedSpeed()
        val redVx = randomSignedSpeed()
        resetBall(state.ronaldoBall, 55f, redVx)
        resetBall(state.messiBall, GameConfig.WORLD_WIDTH - GameConfig.BALL_SIZE - 55f, blueVx)
        events += GameEvent.RoundStarted
    }

    fun pause() {
        if (state.phase != GamePhase.PAUSED) state.phase = GamePhase.PAUSED
    }

    fun resume() {
        if (state.phase == GamePhase.PAUSED) state.phase = GamePhase.PLAYING
    }

    /** Advances exactly one fixed 60 Hz simulation step. */
    fun tick() {
        when (state.phase) {
            GamePhase.READY, GamePhase.PAUSED -> Unit
            GamePhase.PLAYING -> tickPlaying()
            GamePhase.BALL_FALLING -> tickFalling()
            GamePhase.ROUND_RESULT -> tickResult()
        }
    }

    private fun tickPlaying() {
        state.roundElapsedMs += GameConfig.FIXED_STEP_MS

        val a = state.messiBall
        val b = state.ronaldoBall
        snapshotPosition(a)
        snapshotPosition(b)
        integrateAndWalls(a)
        integrateAndWalls(b)
        collideBalls(a, b)

        // Evaluate both balls before ground bounce to remove player-order bias.
        val aHole = isHoleCandidate(a)
        val bHole = isHoleCandidate(b)
        val chosen = chooseHoleCandidate(aHole, bHole, a, b)
        if (chosen != null) {
            beginFall(chosen)
            return
        }

        groundBounce(a)
        groundBounce(b)
    }

    private fun tickFalling() {
        state.roundElapsedMs += GameConfig.FIXED_STEP_MS
        val falling = fallBall ?: return
        val survivor = if (falling === state.messiBall) state.ronaldoBall else state.messiBall

        snapshotPosition(survivor)
        integrateAndWalls(survivor)
        groundBounce(survivor)

        falling.fallProgress = min(1f, falling.fallProgress + GameConfig.FIXED_STEP_MS / GameConfig.FALL_DURATION_MS)
        val ease = falling.fallProgress * falling.fallProgress
        falling.x = lerp(falling.fallStartX, fallTargetX, ease)
        falling.y = lerp(falling.fallStartY, fallTargetY, ease)

        if (falling.fallProgress >= 1f) finishRound(falling, survivor)
    }

    private fun tickResult() {
        // Stay on the result screen until the user taps PLAY AGAIN.
        // Do not automatically start another round.
        state.resultElapsedMs += GameConfig.FIXED_STEP_MS
    }

    private fun snapshotPosition(ball: BallState) {
        ball.previousX = ball.x
        ball.previousY = ball.y
    }

    private fun integrateAndWalls(ball: BallState) {
        if (!ball.alive || ball.falling) return
        ball.vy += GameConfig.GRAVITY
        ball.x += ball.vx
        ball.y += ball.vy

        val maxX = GameConfig.WORLD_WIDTH - GameConfig.BALL_SIZE
        if (ball.x <= 0f) {
            val impact = abs(ball.vx)
            ball.x = 0f
            ball.vx = abs(ball.vx)
            events += GameEvent.Bounce(ball.player, BounceSurface.WALL, impact)
        } else if (ball.x >= maxX) {
            val impact = abs(ball.vx)
            ball.x = maxX
            ball.vx = -abs(ball.vx)
            events += GameEvent.Bounce(ball.player, BounceSurface.WALL, impact)
        }
    }

    private fun groundBounce(ball: BallState) {
        if (!ball.alive || ball.falling) return
        if (ball.y >= GameConfig.GROUND_Y) {
            ball.y = GameConfig.GROUND_Y
            val impact = abs(ball.vy)
            ball.vy = -max(GameConfig.MIN_GROUND_REBOUND, impact * GameConfig.BOUNCE)
            ball.bounces++
            ball.vx += (sin(ball.bounces * 1.73f) * 0.18f)
            ball.vx = ball.vx.coerceIn(-GameConfig.MAX_HORIZONTAL_SPEED, GameConfig.MAX_HORIZONTAL_SPEED)
            events += GameEvent.Bounce(ball.player, BounceSurface.GROUND, impact)
        }
    }

    private fun collideBalls(a: BallState, b: BallState) {
        if (!a.alive || !b.alive || a.falling || b.falling) return
        val ax = a.x + GameConfig.BALL_RADIUS
        val ay = a.y + GameConfig.BALL_RADIUS
        val bx = b.x + GameConfig.BALL_RADIUS
        val by = b.y + GameConfig.BALL_RADIUS
        val dx = bx - ax
        val dy = by - ay
        val dist = hypot(dx, dy)
        if (dist <= 0f || dist >= GameConfig.BALL_SIZE) return

        val nx = dx / dist
        val ny = dy / dist
        val rv = (a.vx - b.vx) * nx + (a.vy - b.vy) * ny
        if (rv <= 0f) return

        events += GameEvent.BallCollision(abs(rv))
        a.vx -= rv * nx
        a.vy -= rv * ny
        b.vx += rv * nx
        b.vy += rv * ny

        val push = (GameConfig.BALL_SIZE - dist) / 2f
        a.x -= nx * push
        a.y -= ny * push
        b.x += nx * push
        b.y += ny * push
    }

    private fun holeIsActive(): Boolean =
        state.roundElapsedMs >= GameConfig.MIN_TIME_FOR_HOLE_MS &&
            state.messiBall.bounces >= GameConfig.MIN_BOUNCES_FOR_HOLE &&
            state.ronaldoBall.bounces >= GameConfig.MIN_BOUNCES_FOR_HOLE

    private fun isHoleCandidate(ball: BallState): Boolean {
        if (!holeIsActive() || !ball.alive || ball.falling || ball.vy <= 0f) return false
        val cx = ball.x + GameConfig.BALL_RADIUS
        val cy = ball.y + GameConfig.BALL_RADIUS
        val nx = (cx - GameConfig.HOLE_CENTER_X) / (GameConfig.HOLE_RX + GameConfig.BALL_RADIUS * 0.30f)
        val ny = (cy - GameConfig.HOLE_CENTER_Y) / (GameConfig.HOLE_RY + GameConfig.BALL_RADIUS * 0.12f)
        return nx * nx + ny * ny <= 1f
    }

    private fun chooseHoleCandidate(aHit: Boolean, bHit: Boolean, a: BallState, b: BallState): BallState? {
        if (!aHit && !bHit) return null
        if (aHit && !bHit) return a
        if (!aHit && bHit) return b

        // Extremely rare simultaneous entry: pick the ball deeper inside the opening.
        val aMetric = holeMetric(a)
        val bMetric = holeMetric(b)
        return when {
            aMetric < bMetric -> a
            bMetric < aMetric -> b
            random.nextFloat() < 0.5f -> a
            else -> b
        }
    }

    private fun holeMetric(ball: BallState): Float {
        val cx = ball.x + GameConfig.BALL_RADIUS
        val cy = ball.y + GameConfig.BALL_RADIUS
        val nx = (cx - GameConfig.HOLE_CENTER_X) / (GameConfig.HOLE_RX + GameConfig.BALL_RADIUS * 0.30f)
        val ny = (cy - GameConfig.HOLE_CENTER_Y) / (GameConfig.HOLE_RY + GameConfig.BALL_RADIUS * 0.12f)
        return nx * nx + ny * ny
    }

    private fun beginFall(ball: BallState) {
        state.phase = GamePhase.BALL_FALLING
        ball.falling = true
        ball.fallProgress = 0f
        ball.fallStartX = ball.x
        ball.fallStartY = ball.y
        fallBall = ball
        fallTargetX = GameConfig.HOLE_CENTER_X - GameConfig.BALL_RADIUS
        fallTargetY = GameConfig.HOLE_CENTER_Y - GameConfig.BALL_RADIUS
    }

    private fun finishRound(holeBall: BallState, survivor: BallState) {
        holeBall.alive = false
        survivor.vx = 0f
        survivor.vy = 0f
        val winner = survivor.player
        if (winner == Player.MESSI) state.messiScore++ else state.ronaldoScore++
        state.winner = winner
        state.phase = GamePhase.ROUND_RESULT
        state.resultElapsedMs = 0f
        events += GameEvent.RoundWon(winner)
    }

    private fun resetBall(ball: BallState, x: Float, vx: Float) {
        ball.x = x
        ball.y = GameConfig.GROUND_Y
        ball.previousX = x
        ball.previousY = ball.y
        ball.vx = vx
        ball.vy = -(9.8f + random.nextFloat() * 2.5f)
        ball.alive = true
        ball.falling = false
        ball.bounces = 0
        ball.fallProgress = 0f
    }

    private fun randomSignedSpeed(): Float {
        val sign = if (random.nextFloat() < 0.5f) -1f else 1f
        return sign * (3f + random.nextFloat() * 1.4f)
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
