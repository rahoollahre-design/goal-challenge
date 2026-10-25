package com.goalchallenge.game.core

import kotlin.random.Random

fun interface RandomSource {
    fun nextFloat(): Float
}

class DefaultRandomSource : RandomSource {
    override fun nextFloat(): Float = Random.nextFloat()
}

class SeededRandomSource(seed: Int) : RandomSource {
    private val random = Random(seed)
    override fun nextFloat(): Float = random.nextFloat()
}
