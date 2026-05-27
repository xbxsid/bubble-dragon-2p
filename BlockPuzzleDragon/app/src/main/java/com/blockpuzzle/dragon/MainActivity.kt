package com.blockpuzzle.dragon

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blockpuzzle.dragon.databinding.ActivityMainBinding
import kotlin.math.abs

/**
 * Main activity – wires up the GameView, touch controls, and button panel.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var gameView: GameView
    private lateinit var prefs: SharedPreferences
    private lateinit var gestureDetector: GestureDetector

    companion object {
        private const val PREF_HIGH_SCORE = "high_score"
        private const val SWIPE_THRESHOLD = 80f
        private const val SWIPE_VELOCITY  = 100f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs    = getSharedPreferences("BlockPuzzleDragon", Context.MODE_PRIVATE)
        gameView = binding.gameView

        setupGestures()
        setupButtons()

        gameView.onGameOverListener = { score ->
            val best = prefs.getLong(PREF_HIGH_SCORE, 0L)
            if (score > best) {
                prefs.edit().putLong(PREF_HIGH_SCORE, score).apply()
                Toast.makeText(this, "🏆 New High Score: $score!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Game Over! Score: $score  Best: $best", Toast.LENGTH_LONG).show()
            }
        }

        updateHighScore()
    }

    // ── Gesture / touch ────────────────────────────────────────────────────

    private fun setupGestures() {
        val listener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                gameView.rotate()
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val dx = e2.x - (e1?.x ?: e2.x)
                val dy = e2.y - (e1?.y ?: e2.y)

                return when {
                    abs(dx) > abs(dy) && abs(dx) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY -> {
                        if (dx > 0) gameView.moveRight() else gameView.moveLeft()
                        true
                    }
                    dy > SWIPE_THRESHOLD && velocityY > SWIPE_VELOCITY -> {
                        gameView.hardDrop()
                        true
                    }
                    else -> false
                }
            }
        }

        gestureDetector = GestureDetector(this, listener)

        gameView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun setupButtons() {
        binding.btnLeft.setOnClickListener    { gameView.moveLeft()   }
        binding.btnRight.setOnClickListener   { gameView.moveRight()  }
        binding.btnRotate.setOnClickListener  { gameView.rotate()     }
        binding.btnDown.setOnClickListener    { gameView.softDrop()   }
        binding.btnDrop.setOnClickListener    { gameView.hardDrop()   }
        binding.btnPause.setOnClickListener   { gameView.togglePause(); updatePauseButton() }
        binding.btnRestart.setOnClickListener { gameView.restart(); updateHighScore(); updatePauseButton() }
    }

    private fun updatePauseButton() {
        binding.btnPause.text = if (gameView.engine.isPaused) "▶ Resume" else "⏸ Pause"
    }

    private fun updateHighScore() {
        val best = prefs.getLong(PREF_HIGH_SCORE, 0L)
        binding.tvHighScore.text = "🏆 Best: $best"
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onPause() {
        super.onPause()
        if (!gameView.engine.isGameOver && !gameView.engine.isPaused) {
            gameView.togglePause()
            updatePauseButton()
        }
    }
}
