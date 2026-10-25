package com.goalchallenge.game.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Space
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.goalchallenge.game.data.GamePreferences

class MainActivity : Activity() {
    private lateinit var prefs: GamePreferences

    private val navy = Color.rgb(27, 52, 79)
    private val orange = Color.rgb(255, 113, 27)
    private val cyan = Color.rgb(0, 184, 218)
    private val red = Color.rgb(255, 35, 57)
    private var isPlayersScreen = false
    private var redPlayerName = "Cristiano Ronaldo"
    private var bluePlayerName = "Lionel Messi"
    private var selectingRedPlayer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = GamePreferences(this)
        val playerPrefs = getSharedPreferences("selected_players", MODE_PRIVATE)
        redPlayerName = playerPrefs.getString("red_player", "Cristiano Ronaldo") ?: "Cristiano Ronaldo"
        bluePlayerName = playerPrefs.getString("blue_player", "Lionel Messi") ?: "Lionel Messi"
        window.statusBarColor = navy
        window.navigationBarColor = navy
        window.decorView.systemUiVisibility = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            window.setDecorFitsSystemWindows(false)
        }
        isPlayersScreen = false
        setContentView(buildHome())
    }

    private fun buildHome(): View {
        val root = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(2, 9, 35), Color.rgb(5, 37, 91), Color.rgb(1, 11, 31))
            )
            applySystemBarInsets(this)
        }

        // Blue stadium-style glow behind the interactive home controls.
        val glow = View(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(10, 88, 180), Color.rgb(5, 27, 68), Color.rgb(2, 10, 27))
            ).apply { cornerRadius = dp(28).toFloat() }
            alpha = 0.62f
        }
        root.addView(glow, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(300), Gravity.TOP
        ))

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(16), dp(12), dp(16), dp(22))
        }
        scroll.addView(column, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ))
        root.addView(scroll, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ))

        column.addView(text("👑", 31f, Color.rgb(255, 210, 44), true).apply {
            gravity = Gravity.CENTER
            setShadowLayer(dp(12).toFloat(), 0f, dp(2).toFloat(), Color.rgb(0, 184, 255))
        })
        val banner = text("GOAL", 39f, Color.rgb(255, 207, 32), true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.05f
            setShadowLayer(dp(3).toFloat(), dp(2).toFloat(), dp(3).toFloat(), Color.rgb(180, 50, 0))
        }
        column.addView(banner, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(46)
        ))
        column.addView(text("C H A L L E N G E", 19f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.06f
            background = neonPill(Color.rgb(11, 47, 103), Color.rgb(0, 196, 255))
            setPadding(dp(16), dp(4), dp(16), dp(4))
            elevation = dp(5).toFloat()
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(36)))
        column.addView(space(15))

        val cards = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
            clipToPadding = false
        }
        cards.addView(
            playerCard(bluePlayerName, "PLAYER 1", cyan, false),
            LinearLayout.LayoutParams(0, dp(220), 1f).apply { marginEnd = dp(9) }
        )
        cards.addView(
            playerCard(redPlayerName, "PLAYER 2", red, true),
            LinearLayout.LayoutParams(0, dp(220), 1f).apply { marginStart = dp(9) }
        )
        column.addView(cards, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(220)
        ))
        column.addView(text("⚡  V S  ⚡", 18f, Color.rgb(255, 199, 32), true).apply {
            gravity = Gravity.CENTER
            setShadowLayer(dp(6).toFloat(), 0f, 0f, Color.rgb(255, 80, 0))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(27)))

        column.addView(space(13))
        column.addView(menuButton("▶   PLAY GAME") {
            startActivity(Intent(this, GameActivity::class.java))
        }.apply {
            textSize = 25f
            background = neonPill(Color.rgb(21, 224, 45), Color.rgb(181, 255, 94))
            setTextColor(Color.WHITE)
            setShadowLayer(dp(7).toFloat(), 0f, dp(2).toFloat(), Color.rgb(0, 255, 55))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)))
        column.addView(space(12))

        val secondary = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        secondary.addView(menuButton("♟  PLAYERS") {
            selectingRedPlayer = true
            showPlayers()
        }.apply {
            textSize = 17f
            background = neonPill(Color.rgb(0, 111, 255), Color.rgb(91, 224, 255))
        }, LinearLayout.LayoutParams(0, dp(58), 1f).apply { marginEnd = dp(6) })
        secondary.addView(menuButton("⚙  SETTINGS") {
            showSettings()
        }.apply {
            textSize = 17f
            background = neonPill(Color.rgb(15, 37, 76), Color.rgb(0, 196, 255))
        }, LinearLayout.LayoutParams(0, dp(58), 1f).apply { marginStart = dp(6) })
        column.addView(secondary, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(58)
        ))

        val info = TextView(this).apply {
            text = "?"
            textSize = 20f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = neonPill(Color.rgb(0, 79, 143), Color.rgb(0, 220, 255))
            elevation = dp(6).toFloat()
            setOnClickListener { showHowToPlay() }
            contentDescription = "How to play"
        }
        root.addView(info, FrameLayout.LayoutParams(dp(36), dp(36), Gravity.BOTTOM or Gravity.END).apply {
            setMargins(0, 0, dp(10), dp(10))
        })
        return root
    }

    /** Compact, layered player cards for the home screen. */
    private fun playerCard(name: String, subtitle: String, accent: Int, isRonaldo: Boolean): View {
        val darkAccent = if (isRonaldo) Color.rgb(9, 51, 48) else Color.rgb(5, 52, 83)
        val brightAccent = if (isRonaldo) Color.rgb(13, 126, 78) else Color.rgb(0, 145, 191)
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(7), dp(7), dp(7), dp(7))
            background = esportsCard(darkAccent, if (isRonaldo) Color.rgb(255, 55, 91) else Color.rgb(0, 224, 255))
            elevation = dp(8).toFloat()
            isClickable = true
            isFocusable = true
            contentDescription = "Choose player for ${if (isRonaldo) "Red" else "Blue"}: $name"
            setOnClickListener {
                selectingRedPlayer = isRonaldo
                showPlayers()
            }
        }
        val imageFrame = FrameLayout(this).apply {
            background = imageStageGradient(isRonaldo)
            elevation = dp(2).toFloat()
            clipToPadding = true
            clipChildren = true
            val imageId = playerImageResource(name)
            if (imageId != 0) {
                addView(ImageView(this@MainActivity).apply {
                    setImageResource(imageId)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    adjustViewBounds = false
                    contentDescription = "$name portrait"
                }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
        }
        card.addView(imageFrame, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(126)))
        card.addView(space(5))
        card.addView(text(name, 13f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setShadowLayer(dp(3).toFloat(), 0f, dp(1).toFloat(), Color.BLACK)
        })
        card.addView(text(subtitle, 10f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.12f
            setPadding(dp(8), dp(3), dp(8), dp(3))
            background = pillGradient(accent, darken(accent, 0.55f), lighten(accent, 0.25f))
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(22)).apply { topMargin = dp(4) })
        return card
    }

    private fun playerImageResource(name: String): Int {
        val imageKey = when (name) {
            "Lamine Yamal" -> "lamine_yamal"
            "Mohamed Salah" -> "mohamed_salah"
            "Cristiano Ronaldo", "RONALDO" -> "cristiano_ronaldo"
            "Erling Haaland" -> "erling_haaland"
            "Neymar Jr." -> "neymar"
            "Harry Kane" -> "harry_kane"
            "Kylian Mbappé" -> "kylian_mbappe"
            "Vinícius Jr." -> "vinicius_jr"
            "Jude Bellingham" -> "jude_bellingham"
            "Lionel Messi", "MESSI" -> "lionel_messi"
            "Kevin De Bruyne" -> "kevin_de_bruyne"
            "Your Player" -> "your_player"
            else -> ""
        }
        if (imageKey.isBlank()) return 0
        return resources.getIdentifier(imageKey, "drawable", packageName)
    }

    private data class PlayerEntry(val name: String, val shortName: String, val accent: Int)

    private fun showPlayers() {
        isPlayersScreen = true
        setContentView(buildPlayersScreen())
    }

    private fun buildPlayersScreen(): View {
        val root = FrameLayout(this).apply {
            setBackgroundColor(navy)
            applySystemBarInsets(this)
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; clipToPadding = false }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(20))
        }
        scroll.addView(content, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        root.addView(scroll, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(5), dp(12), dp(5))
            background = rounded(Color.WHITE, Color.TRANSPARENT, 10f, 0)
            setOnClickListener { returnToHome() }
            contentDescription = "Back to home"
        }
        header.addView(text("⚽ PLAYERS", 20f, navy, true), LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(text("↩", 24f, navy, true))
        content.addView(header, LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(38)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(12)
        })

        content.addView(text("MEET THE PLAYERS", 17f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.06f
        })
        content.addView(text("Choose a player for ${if (selectingRedPlayer) "Red" else "Blue"}.", 12f, Color.WHITE, false).apply {
            gravity = Gravity.CENTER
            alpha = 0.85f
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(4); bottomMargin = dp(12)
        })

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(7), dp(8), dp(7), dp(10))
            background = rounded(Color.rgb(31, 158, 190), Color.TRANSPARENT, 10f, 0)
        }
        content.addView(panel, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val players = listOf(
            PlayerEntry("Lamine Yamal", "LY", cyan),
            PlayerEntry("Mohamed Salah", "MS", orange),
            PlayerEntry("Cristiano Ronaldo", "CR", red),
            PlayerEntry("Erling Haaland", "EH", cyan),
            PlayerEntry("Neymar Jr.", "NJ", orange),
            PlayerEntry("Harry Kane", "HK", red),
            PlayerEntry("Kylian Mbappé", "KM", red),
            PlayerEntry("Vinícius Jr.", "VJ", orange),
            PlayerEntry("Jude Bellingham", "JB", cyan),
            PlayerEntry("Lionel Messi", "LM", cyan),
            PlayerEntry("Kevin De Bruyne", "KD", red),
            PlayerEntry("Your Player", "+", Color.WHITE)
        )
        val rows = players.chunked(3)
        rows.forEachIndexed { rowIndex, rowPlayers ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.TOP }
            rowPlayers.forEachIndexed { colIndex, player ->
                val tile = playerTile(player)
                row.addView(tile, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    if (colIndex > 0) marginStart = dp(7)
                    if (colIndex < 2) marginEnd = dp(2)
                })
            }
            if (rowPlayers.size < 3) {
                repeat(3 - rowPlayers.size) {
                    row.addView(Space(this), LinearLayout.LayoutParams(0, dp(1), 1f))
                }
            }
            panel.addView(row, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                if (rowIndex > 0) topMargin = dp(8)
            })
        }

        content.addView(text("TAP A PLAYER TO SELECT", 11f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            alpha = 0.8f
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) })
        return root
    }


    private fun playerTile(player: PlayerEntry): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(2), dp(2), dp(2), dp(2))
        }
        val image = FrameLayout(this).apply {
            background = rounded(Color.rgb(255, 113, 27), Color.rgb(15, 37, 52), 8f, 2)
            contentDescription = "${player.name} portrait"
            val imageId = playerImageResource(player.name)
            if (imageId != 0) {
                addView(ImageView(this@MainActivity).apply {
                    setImageResource(imageId)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    contentDescription = player.name
                }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            } else {
                addView(TextView(this@MainActivity).apply {
                    text = "＋\nADD IMAGE"
                    gravity = Gravity.CENTER
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                }, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
            if ((selectingRedPlayer && redPlayerName == player.name) || (!selectingRedPlayer && bluePlayerName == player.name)) {
                addView(TextView(this@MainActivity).apply {
                    text = "✓"
                    textSize = 22f
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    background = solid(Color.rgb(20, 160, 75), 100)
                }, FrameLayout.LayoutParams(dp(32), dp(32), Gravity.TOP or Gravity.END))
            }
            setOnClickListener {
                if (selectingRedPlayer && bluePlayerName == player.name) {
                    Toast.makeText(this@MainActivity, "This player is already selected for Blue!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!selectingRedPlayer && redPlayerName == player.name) {
                    Toast.makeText(this@MainActivity, "This player is already selected for Red!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (selectingRedPlayer) redPlayerName = player.name else bluePlayerName = player.name
                getSharedPreferences("selected_players", MODE_PRIVATE).edit()
                    .putString("red_player", redPlayerName)
                    .putString("blue_player", bluePlayerName)
                    .apply()
                returnToHome()
            }
        }
        tile.addView(image, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(88)))
        tile.addView(text(player.shortName, 14f, Color.BLACK, true).apply {
            gravity = Gravity.CENTER
            background = solid(player.accent, 100)
        }, LinearLayout.LayoutParams(dp(30), dp(30)).apply { topMargin = dp(4) })
        tile.addView(text(player.name, 11f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            maxLines = 2
        }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(32)).apply { topMargin = dp(3) })
        return tile
    }


    private fun returnToHome() {
        isPlayersScreen = false
        setContentView(buildHome())
    }

    @Deprecated("Deprecated by Android in favor of predictive back; retained for this simple single-activity screen flow")
    override fun onBackPressed() {
        if (isPlayersScreen) returnToHome() else super.onBackPressed()
    }

    private fun showHowToPlay() {
        AlertDialog.Builder(this)
            .setTitle("How to Play")
            .setMessage("1. Tap PLAY GAME, then TAP TO START.\n\n2. ${Branding.BLUE_NAME}'s blue ball and ${Branding.RED_NAME}'s red ball bounce and collide naturally.\n\n3. When one ball falls into the black hole, the surviving player earns +1.\n\n4. The next round starts automatically.\n\n5. RESET SCORE returns the score to 0–0.")
            .setPositiveButton("CLOSE", null)
            .show()
    }

    private fun showSettings() {
        val current = prefs.settings()
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), dp(8)) }
        val sound = Switch(this).apply { text = "Sound effects"; isChecked = current.sound }
        val voice = Switch(this).apply { text = "Winner voice"; isChecked = current.voice }
        val vibration = Switch(this).apply { text = "Winner vibration"; isChecked = current.vibration }
        box.addView(sound); box.addView(voice); box.addView(vibration)
        AlertDialog.Builder(this)
            .setTitle("Game Settings")
            .setView(box)
            .setPositiveButton("SAVE") { _, _ ->
                prefs.setSound(sound.isChecked)
                prefs.setVoice(voice.isChecked)
                prefs.setVibration(vibration.isChecked)
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun menuButton(label: String, onClick: () -> Unit): View = Button(this).apply {
        text = label
        textSize = 20f
        setTextColor(Color.WHITE)
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        isAllCaps = false
        gravity = Gravity.CENTER
        letterSpacing = 0.035f
        background = neonPill(Color.rgb(0, 125, 174), Color.rgb(0, 224, 255))
        elevation = dp(7).toFloat()
        setShadowLayer(dp(5).toFloat(), 0f, dp(3).toFloat(), Color.rgb(0, 0, 0))
        setPadding(dp(12), 0, dp(12), 0)
        setOnClickListener { onClick() }
        minHeight = dp(50)
        stateListAnimator = null
    }.also { it.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(50)) }

    private fun verticalGradient(top: Int, bottom: Int) = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom)
    ).apply { cornerRadius = dp(18).toFloat() }

    private fun pillGradient(top: Int, bottom: Int, edge: Int) = GradientDrawable(
        GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(top, bottom)
    ).apply {
        cornerRadius = dp(14).toFloat()
        setStroke(dp(1), edge)
    }

    private fun layeredCard(dark: Int, bright: Int, edge: Int) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR, intArrayOf(bright, dark, Color.rgb(5, 18, 34))
    ).apply {
        cornerRadius = dp(18).toFloat()
        setStroke(dp(2), edge)
    }

    private fun imageStageGradient(isRedPlayer: Boolean) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        if (isRedPlayer) intArrayOf(Color.rgb(24, 178, 105), Color.rgb(6, 50, 50), Color.rgb(3, 20, 36))
        else intArrayOf(Color.rgb(0, 204, 245), Color.rgb(0, 84, 132), Color.rgb(3, 20, 36))
    ).apply {
        cornerRadius = dp(13).toFloat()
        setStroke(dp(1), if (isRedPlayer) Color.rgb(104, 255, 177) else Color.rgb(112, 238, 255))
    }

    private fun neonPill(fill: Int, glow: Int) = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(fill, Color.rgb((Color.red(fill) * 0.72f).toInt(), (Color.green(fill) * 0.72f).toInt(), (Color.blue(fill) * 0.72f).toInt()))).apply { cornerRadius = dp(13).toFloat(); setStroke(dp(1), glow) }

    private fun esportsCard(fill: Int, glow: Int) = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.rgb(19, 43, 67), fill, Color.rgb(5, 12, 25))).apply { cornerRadius = dp(15).toFloat(); setStroke(dp(1), glow) }

    private fun darken(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) * factor).toInt().coerceIn(0, 255),
        (Color.green(color) * factor).toInt().coerceIn(0, 255),
        (Color.blue(color) * factor).toInt().coerceIn(0, 255)
    )

    private fun lighten(color: Int, factor: Float): Int = Color.rgb(
        (Color.red(color) + (255 - Color.red(color)) * factor).toInt().coerceIn(0, 255),
        (Color.green(color) + (255 - Color.green(color)) * factor).toInt().coerceIn(0, 255),
        (Color.blue(color) + (255 - Color.blue(color)) * factor).toInt().coerceIn(0, 255)
    )

    private fun text(value: String, size: Float, color: Int, bold: Boolean) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(color)
        if (bold) typeface = Typeface.DEFAULT_BOLD
    }

    private fun rounded(fill: Int, stroke: Int, radiusDp: Float, strokeDp: Int) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radiusDp.toInt()).toFloat()
        if (stroke != Color.TRANSPARENT && strokeDp > 0) setStroke(dp(strokeDp), stroke)
    }

    private fun solid(fill: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun applySystemBarInsets(view: View) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return

        view.setOnApplyWindowInsetsListener { target, insets ->
            val systemBars = insets.getInsets(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            target.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun space(h: Int) = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp(h)) }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density + 0.5f).toInt()
}