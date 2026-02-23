package com.tayyar.tiletap.game

import android.graphics.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.tayyar.tiletap.R
import com.tayyar.tiletap.game.GameView.Companion.screenWidth
import com.tayyar.tiletap.game.GameView.Companion.screenHeight
import kotlin.math.roundToInt

/**
 * Tile Class.
 */
class Tile(blackPaint : Paint, private var pressedTileColor: Paint, private var redPaint: Paint, row : Int) {

    companion object {
        var speed = 30.0
        var speedIncrease = false
    }

    private var startX: Int = 0
    var startY: Int = 0
    private var endX: Int = 0
    var endY: Int = 0

    var pressed: Boolean = false

    var outOfScreen = false
    private var outOfBounds = false
    var gameOver = false

    private var tilePaint = Paint()

    init {
        startX = row * (screenWidth/4)
        startY = -screenHeight/4
        endX = screenWidth/4 + startX
        endY = screenHeight/4 + startY
        
        tilePaint.color = Color.BLACK
        tilePaint.style = Paint.Style.FILL
    }

    /**
     * Draws the object on to the canvas.
     */
    fun draw(canvas: Canvas) {
        if (pressed) {
            // Use the same gradient as the start button for pressed tiles
            val neonPurple = Color.parseColor("#B026FF")
            val neonBlue = Color.parseColor("#00D2FF")
            tilePaint.shader = LinearGradient(
                startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(),
                neonPurple, neonBlue, Shader.TileMode.CLAMP
            )
        } else if (outOfBounds) {
            tilePaint.shader = null
            tilePaint.color = Color.RED
        } else {
            tilePaint.shader = null
            tilePaint.color = Color.BLACK
        }
        
        canvas.drawRect(Rect(startX, startY, endX, endY), tilePaint)
    }

    /**
     * update properties for the game object
     */
    fun update(frameNo: Int) {
        if (startY >= screenHeight && !pressed) {
            outOfBounds = true
            speed = -40.0
        }
        if (outOfBounds && endY <= screenHeight) {
            gameOver = true
        }
        if (startY >= screenHeight && pressed) {
            outOfScreen = true
        }
        
        if (speedIncrease && speed != 0.0 && frameNo % 60 == 0 && speed < 50) {
            speed += 1 / (speed * 20)
        }
        startY += (speed.roundToInt())
        endY += (speed.roundToInt())
    }

    fun checkTouch (x: Float, y: Float) : Boolean {
        if (x > startX - screenWidth/30 && x < endX + screenWidth/30 && y < endY && y > startY && !pressed) {
            GameView.score++
            pressed = true
            return pressed
        }
        return false
    }
}