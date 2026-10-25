package com.goalchallenge.game.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.view.Choreographer
import android.view.MotionEvent
import android.view.View
import com.goalchallenge.game.R
import com.goalchallenge.game.audio.GameAudio
import com.goalchallenge.game.core.BallState
import com.goalchallenge.game.core.GameConfig
import com.goalchallenge.game.core.GameEngine
import com.goalchallenge.game.core.GameEvent
import com.goalchallenge.game.core.GamePhase
import com.goalchallenge.game.core.Player
import com.goalchallenge.game.data.GamePreferences
import kotlin.math.max
import kotlin.math.min

class GameView(
    context: Context,
    private val prefs: GamePreferences,
    private val audio: GameAudio,
    private val onHome: () -> Unit,
) : View(context), Choreographer.FrameCallback {

    private val engine = GameEngine().also {
        it.setScore(prefs.messiScore(), prefs.ronaldoScore())
    }
    private val choreographer = Choreographer.getInstance()
    private var framePosted = false
    private var hostResumed = false
    private var lastFrameNs = 0L
    private var accumulatorMs = 0f
    private var worldScale = 1f
    private var worldOffsetX = 0f
    private var worldOffsetY = 0f

    private val playerPrefs = context.getSharedPreferences("selected_players", Context.MODE_PRIVATE)
    private val bluePlayerName: String =
        playerPrefs.getString("blue_player", "Lionel Messi") ?: "Lionel Messi"
    private val redPlayerName: String =
        playerPrefs.getString("red_player", "Cristiano Ronaldo") ?: "Cristiano Ronaldo"

    private val messiBitmap: Bitmap = loadPlayerBitmap(bluePlayerName, R.drawable.player_messi)
    private val ronaldoBitmap: Bitmap = loadPlayerBitmap(redPlayerName, R.drawable.player_ronaldo)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }

    private val homeRect = RectF(8f, 714f, 82f, 758f)
    private val playAgainRect = RectF(91f, 714f, 211f, 758f)
    private val resetRect = RectF(219f, 714f, 306f, 758f)
    private val startRect = RectF(55f, 445f, 375f, 507f)

    init {
        isFocusable = true
        contentDescription = "$bluePlayerName versus $redPlayerName endless goal counter"
        setBackgroundColor(Color.BLACK)
    }

    fun onHostResume() {
        hostResumed = true
        lastFrameNs = 0L
        accumulatorMs = 0f
        postFrame()
    }

    fun onHostPause() {
        hostResumed = false
        if (framePosted) choreographer.removeFrameCallback(this)
        framePosted = false
        lastFrameNs = 0L
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (hostResumed) postFrame()
    }

    override fun onDetachedFromWindow() {
        if (framePosted) choreographer.removeFrameCallback(this)
        framePosted = false
        super.onDetachedFromWindow()
    }

    private fun postFrame() {
        if (!hostResumed || framePosted) return
        framePosted = true
        choreographer.postFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        framePosted = false
        if (!hostResumed) return

        if (lastFrameNs != 0L) {
            val elapsedMs = ((frameTimeNanos - lastFrameNs) / 1_000_000f).coerceIn(0f, 100f)
            accumulatorMs += elapsedMs
            var steps = 0
            while (accumulatorMs >= GameConfig.FIXED_STEP_MS && steps < 6) {
                engine.tick()
                processEvents()
                accumulatorMs -= GameConfig.FIXED_STEP_MS
                steps++
            }
            if (steps == 6 && accumulatorMs >= GameConfig.FIXED_STEP_MS) accumulatorMs = 0f
        }
        lastFrameNs = frameTimeNanos
        invalidate()
        postFrame()
    }

    private fun processEvents() {
        for (event in engine.drainEvents()) {
            audio.handle(event)
            if (event is GameEvent.RoundWon) {
                prefs.saveScore(engine.state.messiScore, engine.state.ronaldoScore)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val sx = width / GameConfig.WORLD_WIDTH
        val sy = height / GameConfig.WORLD_HEIGHT
        worldScale = min(sx, sy).coerceAtLeast(0.1f)
        worldOffsetX = (width - GameConfig.WORLD_WIDTH * worldScale) / 2f
        worldOffsetY = (height - GameConfig.WORLD_HEIGHT * worldScale) / 2f

        canvas.save()
        canvas.translate(worldOffsetX, worldOffsetY)
        canvas.scale(worldScale, worldScale)
        drawWorld(canvas)
        canvas.restore()
    }

    private fun drawWorld(c: Canvas) {
        drawSky(c)
        drawCloud(c, -30f, 100f); drawCloud(c, 355f, 150f); drawCloud(c, 45f, 205f); drawCloud(c, 270f, 185f)
        drawStadium(c)
        drawField(c)
        drawHud(c)
        drawBalls(c)
        drawControls(c)
        if (engine.state.phase == GamePhase.READY) drawStartOverlay(c)
        engine.state.winner?.let { if (engine.state.phase == GamePhase.ROUND_RESULT) drawWinner(c, it) }
    }

    private fun drawSky(c: Canvas) {
        paint.shader = LinearGradient(0f, 0f, 0f, GameConfig.WORLD_HEIGHT,
            intArrayOf(Color.rgb(67,190,233), Color.rgb(167,231,247), Color.rgb(58,128,173), Color.rgb(34,92,137)),
            floatArrayOf(0f, .25f, .49f, 1f), Shader.TileMode.CLAMP)
        c.drawRect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, paint)
        paint.shader = null
    }

    private fun drawCloud(c: Canvas, x: Float, y: Float) {
        paint.color = Color.argb(145, 255, 255, 255)
        c.drawRoundRect(RectF(x, y, x + 105f, y + 36f), 18f, 18f, paint)
        paint.color = Color.argb(170, 255, 255, 255)
        c.drawCircle(x + 36f, y - 2f, 24f, paint)
        c.drawCircle(x + 76f, y - 7f, 29f, paint)
    }

    private fun drawStadium(c: Canvas) {
        val top = 220f
        paint.color = Color.rgb(238,233,213)
        val path = android.graphics.Path().apply {
            moveTo(0f, top); lineTo(430f, top); lineTo(391f, 485f); lineTo(39f, 485f); close()
        }
        c.drawPath(path, paint)
        var y = top
        var stripe = false
        while (y < 485f) {
            paint.color = if (stripe) Color.rgb(170,167,147) else Color.rgb(238,233,213)
            c.drawRect(0f, y, 430f, min(y + 10f, 485f), paint)
            y += 12f; stripe = !stripe
        }
        paint.color = Color.rgb(44,125,184)
        c.drawRect(0f, 335f, 430f, 485f, paint)
        y = 335f
        stripe = false
        while (y < 485f) {
            paint.color = if (stripe) Color.rgb(25,87,137) else Color.rgb(44,125,184)
            c.drawRect(0f, y, 430f, min(y + 30f, 485f), paint)
            y += 40f; stripe = !stripe
        }
        paint.color = Color.WHITE
        c.drawRect(0f, 335f, 430f, 342f, paint)
        paint.shader = LinearGradient(0f, 0f, 430f, 0f, Color.rgb(232,121,249), Color.rgb(126,34,206), Shader.TileMode.CLAMP)
        c.drawRect(0f, 346f, 430f, 360f, paint)
        paint.shader = null
    }

    private fun drawField(c: Canvas) {
        val fieldTop = 449f
        var y = fieldTop
        var stripe = false
        while (y < 764f) {
            paint.color = if (stripe) Color.rgb(42,164,51) else Color.rgb(50,181,59)
            c.drawRect(0f, y, 430f, min(764f, y + 32f), paint)
            y += 32f; stripe = !stripe
        }
        paint.color = Color.WHITE
        c.drawRect(0f, fieldTop, 430f, fieldTop + 4f, paint)
        c.drawRect(0f, fieldTop + 145f, 430f, fieldTop + 149f, paint)

        stroke.color = Color.WHITE; stroke.strokeWidth = 4f
        c.drawCircle(215f, fieldTop + 144.5f, 52.5f, stroke)
        paint.color = Color.WHITE; c.drawCircle(215f, fieldTop + 144.5f, 4.5f, paint)
        drawGoal(c, fieldTop + 16f)
        drawHole(c)
    }

    private fun drawGoal(c: Canvas, top: Float) {
        val left = 130f; val right = 300f; val bottom = top + 120f
        stroke.color = Color.argb(170,255,255,255); stroke.strokeWidth = 1f
        var x = left + 6f
        while (x <= right - 6f) { c.drawLine(x, top + 5f, x, bottom, stroke); x += 10f }
        var y = top + 5f
        while (y <= bottom) { c.drawLine(left + 6f, y, right - 6f, y, stroke); y += 10f }
        paint.color = Color.WHITE
        c.drawRect(left, top, left + 7f, bottom, paint)
        c.drawRect(right - 7f, top, right, bottom, paint)
        c.drawRect(left, top, right, top + 7f, paint)
    }

    private fun drawHole(c: Canvas) {
        val rect = RectF(GameConfig.HOLE_LEFT, GameConfig.HOLE_TOP, GameConfig.HOLE_LEFT + GameConfig.HOLE_WIDTH, GameConfig.HOLE_TOP + GameConfig.HOLE_HEIGHT)
        paint.shader = RadialGradient(GameConfig.HOLE_CENTER_X, GameConfig.HOLE_CENTER_Y - 2f, 45f,
            intArrayOf(Color.BLACK, Color.BLACK, Color.rgb(2,6,23), Color.rgb(71,85,105)),
            floatArrayOf(0f,.45f,.68f,1f), Shader.TileMode.CLAMP)
        c.drawOval(rect, paint); paint.shader = null
        stroke.color = Color.rgb(23,32,51); stroke.strokeWidth = 3f; c.drawOval(rect, stroke)
        paint.color = Color.BLACK
        c.drawOval(RectF(GameConfig.HOLE_LEFT + 2f, GameConfig.HOLE_TOP + 9f, GameConfig.HOLE_LEFT + 80f, GameConfig.HOLE_TOP + 34f), paint)
    }

    private fun drawHud(c: Canvas) {
        // Red-selected player is displayed on the left; Blue-selected player on the right.
        drawAvatar(c, ronaldoBitmap, 28f, 24f, 88f)
        drawAvatar(c, messiBitmap, 314f, 24f, 88f)
        drawNameBadge(c, redPlayerName.uppercase(), 10f, 115f, 120f)
        drawNameBadge(c, bluePlayerName.uppercase(), 300f, 115f, 120f)
        drawScore(c, engine.state.ronaldoScore, 125f, 47f)
        drawScore(c, engine.state.messiScore, 260f, 47f)
    }

    private fun drawAvatar(c: Canvas, bitmap: Bitmap, x: Float, y: Float, size: Float) {
        val save = c.save()
        val path = android.graphics.Path().apply { addCircle(x + size/2f, y + size/2f, size/2f, android.graphics.Path.Direction.CW) }
        c.clipPath(path)
        c.drawBitmap(bitmap, null, RectF(x,y,x+size,y+size), paint)
        c.restoreToCount(save)
    }

    private fun drawNameBadge(c: Canvas, name: String, x: Float, y: Float, width: Float) {
        paint.color = Color.rgb(255,230,0)
        c.drawRoundRect(RectF(x,y,x+width,y+30f), 7f,7f,paint)
        textPaint.color = Color.rgb(17,17,17)
        textPaint.textSize = when {
            name.length > 15 -> 10f
            name.length > 12 -> 12f
            name.length > 9 -> 14f
            else -> 18f
        }
        c.drawText(name, x + width/2f, y + 21f, textPaint)
    }

    private fun loadPlayerBitmap(name: String, fallbackRes: Int): Bitmap {
        val key = when (name) {
            "Lamine Yamal" -> "lamine_yamal"
            "Mohamed Salah" -> "mohamed_salah"
            "Cristiano Ronaldo" -> "cristiano_ronaldo"
            "Erling Haaland" -> "erling_haaland"
            "Neymar Jr." -> "neymar"
            "Harry Kane" -> "harry_kane"
            "Kylian Mbappé" -> "kylian_mbappe"
            "Vinícius Jr." -> "vinicius_jr"
            "Jude Bellingham" -> "jude_bellingham"
            "Lionel Messi" -> "lionel_messi"
            "Kevin De Bruyne" -> "kevin_de_bruyne"
            "Your Player" -> "your_player"
            else -> ""
        }
        val selectedRes = if (key.isNotEmpty()) {
            resources.getIdentifier(key, "drawable", context.packageName)
        } else 0
        val bitmap = BitmapFactory.decodeResource(
            resources,
            if (selectedRes != 0) selectedRes else fallbackRes
        )
        return bitmap ?: BitmapFactory.decodeResource(resources, fallbackRes)
    }

    private fun drawScore(c: Canvas, score: Int, x: Float, y: Float) {
        paint.color = Color.WHITE
        c.drawRoundRect(RectF(x,y,x+45f,y+45f),12f,12f,paint)
        textPaint.color = Color.rgb(23,52,93); textPaint.textSize = 29f
        c.drawText(score.toString(), x+22.5f, y+32f, textPaint)
    }

    private fun drawBalls(c: Canvas) {
        // Keep the red player/ball on the left and the blue player/ball on the right.
        drawBall(c, engine.state.ronaldoBall, false)
        drawBall(c, engine.state.messiBall, true)
    }

    private fun drawBall(c: Canvas, ball: BallState, blue: Boolean) {
        if (!ball.alive) return
        val scale = if (ball.falling) max(0.06f, 1f - ball.fallProgress * 0.94f) else 1f
        val r = GameConfig.BALL_RADIUS * scale
        val cx = ball.x + GameConfig.BALL_RADIUS
        val cy = ball.y + GameConfig.BALL_RADIUS
        val colors = if (blue) intArrayOf(Color.WHITE, Color.rgb(96,165,250), Color.rgb(37,99,235), Color.rgb(15,23,42))
                     else intArrayOf(Color.WHITE, Color.rgb(251,113,133), Color.rgb(239,68,68), Color.rgb(69,10,10))
        paint.shader = RadialGradient(cx-r*.35f, cy-r*.40f, max(1f,r*1.6f), colors, null, Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r, paint); paint.shader = null
        stroke.color = if (blue) Color.rgb(96,165,250) else Color.rgb(251,113,133)
        stroke.strokeWidth = max(1f,3f*scale)
        c.drawCircle(cx,cy,r,stroke)
    }

    private fun drawControls(c: Canvas) {
        drawButton(c, homeRect, "HOME", Color.argb(195,2,8,18), Color.WHITE, 12f)
        drawButton(c, playAgainRect, "▶ PLAY AGAIN", Color.rgb(21,128,61), Color.WHITE, 11f)
        drawButton(c, resetRect, "↻ RESET", Color.rgb(180,83,9), Color.WHITE, 11f)
    }

    private fun drawStartOverlay(c: Canvas) {
        paint.color = Color.argb(185,2,8,18); c.drawRect(0f,0f,430f,764f,paint)
        val card = RectF(28f,240f,402f,535f)
        paint.color = Color.argb(242,3,16,35); c.drawRoundRect(card,24f,24f,paint)
        stroke.color = Color.argb(120,125,211,252); stroke.strokeWidth=1.5f; c.drawRoundRect(card,24f,24f,stroke)
        textPaint.color = Color.rgb(125,211,252); textPaint.textSize=12f
        c.drawText("ENDLESS GOAL COUNTER",215f,285f,textPaint)
        textPaint.color = Color.WHITE
        textPaint.textSize = if (bluePlayerName.length + redPlayerName.length > 19) 18f else 23f
        c.drawText("${redPlayerName.uppercase()}  VS  ${bluePlayerName.uppercase()}",215f,335f,textPaint)
        textPaint.color=Color.rgb(203,213,225); textPaint.textSize=14f
        c.drawText("Watch the balls. Survive the black hole. Earn +1.",215f,378f,textPaint)
        drawButton(c,startRect,"▶  TAP TO START",Color.rgb(250,204,21),Color.rgb(6,18,29),18f)
        textPaint.color=Color.rgb(148,163,184); textPaint.textSize=11f
        c.drawText("Tap to start with sound",215f,525f,textPaint)
    }

    private fun drawWinner(c: Canvas, winner: Player) {
        val name = if (winner == Player.MESSI) bluePlayerName.uppercase() else redPlayerName.uppercase()
        textPaint.color = if (winner == Player.MESSI) Color.rgb(56,189,248) else Color.rgb(255,100,100)
        textPaint.textSize = 31f
        c.drawText("⚽ $name +1",215f,340f,textPaint)
    }

    private fun drawButton(c: Canvas, rect: RectF, label: String, bg: Int, fg: Int, size: Float) {
        paint.color = bg; c.drawRoundRect(rect,10f,10f,paint)
        textPaint.color=fg; textPaint.textSize=size
        val baseline = rect.centerY() - (textPaint.ascent()+textPaint.descent())/2f
        c.drawText(label,rect.centerX(),baseline,textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val wx = (event.x - worldOffsetX) / worldScale
        val wy = (event.y - worldOffsetY) / worldScale
        if (wx !in 0f..GameConfig.WORLD_WIDTH || wy !in 0f..GameConfig.WORLD_HEIGHT) return true

        when {
            engine.state.phase == GamePhase.READY && startRect.contains(wx,wy) -> engine.startRound()
            homeRect.contains(wx,wy) -> onHome()
            playAgainRect.contains(wx,wy) &&
                engine.state.phase != GamePhase.PLAYING &&
                engine.state.phase != GamePhase.BALL_FALLING -> {
                engine.startRound()
            }
            resetRect.contains(wx,wy) -> {
                engine.resetScoreToReady()
                prefs.saveScore(0, 0)
            }
        }
        invalidate()
        return true
    }
}
