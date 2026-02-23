package com.tayyar.tiletap.game

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tayyar.tiletap.R

class GameActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var gameOverLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)

        val speed = intent.getStringExtra("speed")
        val music = intent.getBooleanExtra("music", true)
        val vibration = intent.getBooleanExtra("vibration", true)
        val speedIncrease = intent.getBooleanExtra("speedIncrease", false)

        GameView.music = music
        GameView.vibration = vibration
        Tile.speedIncrease = speedIncrease

        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val height = displayMetrics.heightPixels
        Tile.speed = speed!!.toDouble() * height / 1280
        GameView.initialSpeed = speed.toInt()

        val screen = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        gameView = GameView(this)
        gameView.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        screen.addView(gameView)

        gameOverLayout = layoutInflater.inflate(R.layout.game_over_layout, screen, false)
        gameOverLayout.visibility = View.GONE
        
        gameOverLayout.findViewById<ImageButton>(R.id.replayButton).setOnClickListener {
            gameView.restart()
        }
        
        gameOverLayout.findViewById<ImageButton>(R.id.homeButton).setOnClickListener {
            finish()
        }
        
        screen.addView(gameOverLayout)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    fun showReplayButton() {
        this@GameActivity.runOnUiThread {
            val score = GameView.score
            gameOverLayout.findViewById<TextView>(R.id.scoreResult).text = "Your Score: $score"
            
            val appreciation = when {
                score >= 150 -> "Legendary! 🏆🤩"
                score >= 100 -> "Excellent! 🔥🌟"
                score >= 50 -> "Good Job! 👍✨"
                else -> "Try Again! 💪"
            }
            gameOverLayout.findViewById<TextView>(R.id.appreciationText).text = appreciation

            gameOverLayout.visibility = View.VISIBLE
        }
    }

    fun hideReplayButton() {
        this@GameActivity.runOnUiThread {
            gameOverLayout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        gameView.destroy()
        super.onDestroy()
    }
}
