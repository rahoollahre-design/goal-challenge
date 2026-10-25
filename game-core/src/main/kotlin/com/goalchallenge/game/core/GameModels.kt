package com.goalchallenge.game.core

enum class Player { MESSI, RONALDO }
enum class GamePhase { READY, PLAYING, BALL_FALLING, ROUND_RESULT, PAUSED }
enum class BounceSurface { WALL, GROUND }

data class BallState(
    val player: Player,
    var x: Float = 0f,
    var y: Float = 0f,
    var previousX: Float = 0f,
    var previousY: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var alive: Boolean = true,
    var falling: Boolean = false,
    var bounces: Int = 0,
    var fallProgress: Float = 0f,
    var fallStartX: Float = 0f,
    var fallStartY: Float = 0f,
)

data class GameState(
    var messiScore: Int = 0,
    var ronaldoScore: Int = 0,
    var phase: GamePhase = GamePhase.READY,
    var winner: Player? = null,
    var roundElapsedMs: Float = 0f,
    var resultElapsedMs: Float = 0f,
    var roundNumber: Long = 0,
    val messiBall: BallState = BallState(Player.MESSI),
    val ronaldoBall: BallState = BallState(Player.RONALDO),
)

sealed interface GameEvent {
    data class Bounce(val player: Player, val surface: BounceSurface, val impact: Float) : GameEvent
    data class BallCollision(val impact: Float) : GameEvent
    data class RoundWon(val player: Player) : GameEvent
    data object RoundStarted : GameEvent
}
