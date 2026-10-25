package com.goalchallenge.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import com.goalchallenge.game.R
import com.goalchallenge.game.core.BounceSurface
import com.goalchallenge.game.core.GameEvent
import com.goalchallenge.game.core.Player
import com.goalchallenge.game.data.GamePreferences
import com.goalchallenge.game.ui.Branding
import java.util.Locale
import kotlin.math.min

class GameAudio(
    private val context: Context,
    private val prefs: GamePreferences,
) : TextToSpeech.OnInitListener {
    private val handler = Handler(Looper.getMainLooper())
    private val soundPool: SoundPool
    private val groundId: Int
    private val wallId: Int
    private val collisionId: Int
    private val victoryId: Int
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var lastBounceAt = 0L
    private var lastCollisionAt = 0L

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(attrs).build()
        groundId = soundPool.load(context, R.raw.bounce_ground, 1)
        wallId = soundPool.load(context, R.raw.bounce_wall, 1)
        collisionId = soundPool.load(context, R.raw.ball_collision, 1)
        victoryId = soundPool.load(context, R.raw.victory, 1)
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val engine = tts ?: return
        val candidates = listOf(Locale("en", "IN"), Locale.UK, Locale.US, Locale.ENGLISH)
        for (locale in candidates) {
            if (engine.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                engine.language = locale
                break
            }
        }
        engine.setSpeechRate(0.88f)
        engine.setPitch(1.02f)
        ttsReady = true
    }

    fun handle(event: GameEvent) {
        val settings = prefs.settings()
        when (event) {
            is GameEvent.Bounce -> {
                if (!settings.sound) return
                val now = System.currentTimeMillis()
                if (now - lastBounceAt < 42L) return
                lastBounceAt = now
                val volume = (0.25f + min(1f, event.impact / 12f) * 0.35f).coerceAtMost(0.65f)
                val id = if (event.surface == BounceSurface.GROUND) groundId else wallId
                val rate = if (event.player == Player.MESSI) 1.05f else 0.95f
                soundPool.play(id, volume, volume, 1, 0, rate)
            }
            is GameEvent.BallCollision -> {
                if (!settings.sound) return
                val now = System.currentTimeMillis()
                if (now - lastCollisionAt < 55L) return
                lastCollisionAt = now
                soundPool.play(collisionId, 0.42f, 0.42f, 1, 0, 1f)
            }
            is GameEvent.RoundWon -> {
                // Do not play the old victory clip: it may contain hard-coded Messi/Ronaldo voice.
                // Winner name is spoken dynamically by TextToSpeech below.
                if (settings.vibration) vibrateWin()
                if (settings.voice) {
                    handler.postDelayed({ speakWinner(event.player) }, 520L)
                }
            }
            GameEvent.RoundStarted -> Unit
        }
    }

    private fun speakWinner(player: Player) {
        if (!ttsReady || !prefs.settings().voice) return
        val selectedPlayers = context.getSharedPreferences("selected_players", Context.MODE_PRIVATE)
        val name = if (player == Player.MESSI) {
            selectedPlayers.getString("blue_player", "Lionel Messi") ?: "Lionel Messi"
        } else {
            selectedPlayers.getString("red_player", "Cristiano Ronaldo") ?: "Cristiano Ronaldo"
        }
        tts?.speak("$name wins!", TextToSpeech.QUEUE_FLUSH, null, "winner")
    }

    private fun vibrateWin() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(55L, 110))
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(55L)
        }
    }

    fun pauseSpeech() {
        handler.removeCallbacksAndMessages(null)
        tts?.stop()
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        tts?.stop()
        tts?.shutdown()
        soundPool.release()
    }
}
