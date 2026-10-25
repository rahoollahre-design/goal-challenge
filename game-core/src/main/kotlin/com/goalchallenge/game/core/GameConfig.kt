package com.goalchallenge.game.core

object GameConfig {
    const val WORLD_WIDTH = 430f
    const val WORLD_HEIGHT = 764f
    const val BALL_SIZE = 55f
    const val BALL_RADIUS = BALL_SIZE / 2f

    // Values ported from the original browser game.
    const val GRAVITY = 0.34f
    const val BOUNCE = 0.84f
    const val MAX_HORIZONTAL_SPEED = 4.4f
    const val MIN_GROUND_REBOUND = 7.4f

    const val MIN_BOUNCES_FOR_HOLE = 4
    const val MIN_TIME_FOR_HOLE_MS = 4200f
    const val FALL_DURATION_MS = 520f
    const val NEXT_ROUND_DELAY_MS = 1450f
    const val FIXED_STEP_MS = 1000f / 60f

    const val GROUND_MARGIN_BOTTOM = 75f
    const val GROUND_Y = WORLD_HEIGHT - GROUND_MARGIN_BOTTOM - BALL_SIZE

    // Original DOM geometry: field starts at y=449, hole is top=180 within field.
    const val HOLE_WIDTH = 82f
    const val HOLE_HEIGHT = 38f
    const val HOLE_LEFT = (WORLD_WIDTH - HOLE_WIDTH) / 2f
    const val HOLE_TOP = (WORLD_HEIGHT - 315f) + 180f
    const val HOLE_CENTER_X = HOLE_LEFT + HOLE_WIDTH / 2f
    const val HOLE_CENTER_Y = HOLE_TOP + HOLE_HEIGHT / 2f
    const val HOLE_RX = HOLE_WIDTH / 2f
    const val HOLE_RY = HOLE_HEIGHT / 2f
}
