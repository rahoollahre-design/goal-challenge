package com.goalchallenge.game.data

import android.content.Context

data class AudioSettings(
    val sound: Boolean,
    val voice: Boolean,
    val vibration: Boolean,
)

class GamePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("goalchallenge", Context.MODE_PRIVATE)

    fun messiScore(): Int = prefs.getInt("messi_score", 0)
    fun ronaldoScore(): Int = prefs.getInt("ronaldo_score", 0)
    fun saveScore(messi: Int, ronaldo: Int) {
        prefs.edit().putInt("messi_score", messi).putInt("ronaldo_score", ronaldo).apply()
    }

    fun settings(): AudioSettings = AudioSettings(
        sound = prefs.getBoolean("sound", true),
        voice = prefs.getBoolean("voice", true),
        vibration = prefs.getBoolean("vibration", true),
    )

    fun setSound(value: Boolean) = prefs.edit().putBoolean("sound", value).apply()
    fun setVoice(value: Boolean) = prefs.edit().putBoolean("voice", value).apply()
    fun setVibration(value: Boolean) = prefs.edit().putBoolean("vibration", value).apply()
}
