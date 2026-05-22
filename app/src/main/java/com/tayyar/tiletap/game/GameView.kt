package com.tayyar.tiletap.game

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import com.tayyar.tiletap.R
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val thread: GameThread

    private var tiles = LinkedList<Tile>()
    private var tempTiles = CopyOnWriteArrayList<Tile>()

    private var vibrator: Vibrator? = null

    private var blackPaint = Paint()
    private var grayPaint = Paint()
    private var redPaint = Paint()
    private var scorePaint = Paint()
    private var progressBasePaint = Paint()
    private var progressFillPaint = Paint()
    private var milestonePaint = Paint()
    private var messagePaint = Paint()
    private var messageBoxPaint = Paint()
    private var messageBoxStrokePaint = Paint()
    private var pauseButtonPaint = Paint()

    private var row = -1
    private var lastRow = -1

    private var gameOver = false
    private var gameOverOver = false // true after game over sound is played
    private var tappedWrongTile = -1
    private var startY = -1
    private var endY = -1

    private var touchedX = 0f
    private var touchedY = 0f

    private var scoreSize = 120f
    private var started = false
    var isPaused = false
        private set

    private var soundPool: SoundPool? = null
    private var failSound: Int? = null
    private var playingSound: Int? = null
    private var mediaPlayer: MediaPlayer? = null

    private var frameNo = 0
    
    private val backgroundBitmap: Bitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.background).let {
            Bitmap.createScaledBitmap(it, screenWidth, screenHeight, true)
        }
    }

    private var progressMilestone = 100
    private var lastTileSpeed = 0.0
    
    // Message timer
    private var messageStartTime: Long = 0
    private var currentMessage: String? = null
    private var lastMilestoneScore = 0

    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
        score = 0
        row = (0..3).random()
        tiles.add(Tile(blackPaint, grayPaint, redPaint, row))
        lastRow = row

        val neonPurple = ContextCompat.getColor(context, R.color.neon_purple)
        val neonBlue = ContextCompat.getColor(context, R.color.neon_blue)
        val gold = ContextCompat.getColor(context, R.color.gold)

        blackPaint.color = Color.BLACK
        grayPaint.color = neonBlue
        redPaint.color = Color.RED
        
        scorePaint.apply {
            color = gold
            textSize = scoreSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        progressBasePaint.apply {
            color = Color.parseColor("#88222222")
            style = Paint.Style.FILL
        }

        milestonePaint.apply {
            color = gold
            textSize = 35f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        messagePaint.apply {
            color = Color.WHITE
            textSize = 45f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        messageBoxPaint.apply {
            color = Color.parseColor("#CC0D0B21")
            style = Paint.Style.FILL
        }

        messageBoxStrokePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        pauseButtonPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        if (music) {
            mediaPlayer = MediaPlayer.create(context, R.raw.a)
            mediaPlayer?.isLooping = true
            soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
            failSound = soundPool?.load(context, R.raw.failsound, 1)
        }

        if (vibration) {
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    companion object {
        var score = 0
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        var music = true
        var vibration = true
        var initialSpeed = 30
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        thread.setRunning(true)
        if (!started) {
            thread.start()
            started = true
            if (music) mediaPlayer?.start()
        }
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        saveIfHighScore(initialSpeed, score)
        thread.setRunning(false)
        mediaPlayer?.pause()
    }

    fun destroy() {
        soundPool?.release()
        soundPool = null
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun restart() {
        if (playingSound != null) {
            soundPool?.stop(playingSound!!)
        }
        (context as GameActivity).hideReplayButton()
        Tile.speed = initialSpeed.toDouble()
        tiles.clear()
        score = 0
        tappedWrongTile = -1
        row = (0..3).random()
        tiles.add(Tile(blackPaint, grayPaint, redPaint, row))
        lastRow = row
        gameOver = false
        gameOverOver = false
        isPaused = false
        lastMilestoneScore = 0
        currentMessage = null
        progressMilestone = 100
        
        mediaPlayer?.seekTo(0)
        if (music) mediaPlayer?.start()
        
        thread.setRunning(true)
    }

    private fun saveIfHighScore(speed: Int, score: Int) {
        val sharedPref = context?.getSharedPreferences(
            context.getString(R.string.shared_preferences_name),
            Context.MODE_PRIVATE
        ) ?: return
        val highScore = sharedPref.getInt(speed.toString(), 0)
        if (highScore < score) {
            with(sharedPref.edit()) {
                putInt(speed.toString(), score)
                apply()
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (!isPaused) frameNo++

        if (gameOver && !gameOverOver) {
            mediaPlayer?.pause()
            playingSound = soundPool?.play(failSound!!, 1f, 1f, 0, 0, 1f)
            Tile.speed = 0.0
            thread.setRunning(false)
            saveIfHighScore(initialSpeed, score)
            (context as GameActivity).showReplayButton()
            gameOverOver = true
        }

        drawBackground(canvas)
        drawLines(canvas)

        if (!isPaused) {
            if (tiles.isNotEmpty() && tiles.first.outOfScreen) {
                tiles.poll()
            }
            if (tiles.isEmpty() || tiles.last.startY >= 0) {
                do {
                    row = (0..3).random()
                } while (row == lastRow)
                tiles.add(Tile(blackPaint, grayPaint, redPaint, row))
                lastRow = row
            }
        }

        for (tile in tiles) {
            if (!isPaused) tile.update(frameNo)
            tile.draw(canvas)
            if (tile.gameOver) {
                gameOver = true
            }
        }
        
        when (tappedWrongTile) {
            0 -> canvas.drawRect(Rect(0, startY, screenWidth / 4, endY), redPaint)
            1 -> canvas.drawRect(Rect(screenWidth / 4, startY, screenWidth / 2, endY), redPaint)
            2 -> canvas.drawRect(Rect(screenWidth / 2, startY, screenWidth * 3 / 4, endY), redPaint)
            3 -> canvas.drawRect(Rect(screenWidth * 3 / 4, startY, screenWidth, endY), redPaint)
        }

        drawUI(canvas)
        
        if (isPaused) {
            drawPauseOverlay(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawBitmap(backgroundBitmap, 0f, 0f, null)
    }

    fun drawLines(canvas: Canvas) {
        val linePaint = Paint().apply {
            color = Color.parseColor("#33FFFFFF")
            strokeWidth = 2f
        }
        for (i in 1..3) {
            val x = i * screenWidth.toFloat() / 4
            canvas.drawLine(x, 0f, x, screenHeight.toFloat(), linePaint)
        }
    }

    fun drawScore(canvas: Canvas) {
        drawUI(canvas)
    }

    private fun drawUI(canvas: Canvas) {
        // Pause Button
        val pauseSize = 60f
        val pauseMargin = 40f
        if (isPaused) {
            val path = Path()
            path.moveTo(pauseMargin, pauseMargin)
            path.lineTo(pauseMargin + pauseSize, pauseMargin + pauseSize / 2)
            path.lineTo(pauseMargin, pauseMargin + pauseSize)
            path.close()
            canvas.drawPath(path, pauseButtonPaint)
        } else {
            canvas.drawRect(pauseMargin, pauseMargin, pauseMargin + 20f, pauseMargin + pauseSize, pauseButtonPaint)
            canvas.drawRect(pauseMargin + 35f, pauseMargin, pauseMargin + 55f, pauseMargin + pauseSize, pauseButtonPaint)
        }

        // Score
        canvas.drawText(score.toString(), screenWidth / 2f, scoreSize + 40f, scorePaint)

        // Progress Bar
        val margin = 60f
        val barHeight = 25f
        val barTop = scoreSize + 80f
        val barWidth = screenWidth - (2 * margin)
        
        canvas.drawRoundRect(margin, barTop, screenWidth - margin, barTop + barHeight, barHeight/2, barHeight/2, progressBasePaint)

        val progress = (score.toFloat() / progressMilestone).coerceAtMost(1f)
        if (progress > 0) {
            val neonPurple = ContextCompat.getColor(context, R.color.neon_purple)
            val neonBlue = ContextCompat.getColor(context, R.color.neon_blue)
            
            progressFillPaint.shader = LinearGradient(
                margin, barTop, margin + (barWidth * progress), barTop,
                neonPurple, neonBlue, Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(margin, barTop, margin + (barWidth * progress), barTop + barHeight, barHeight/2, barHeight/2, progressFillPaint)
        }

        // Milestone number
        canvas.drawText(progressMilestone.toString(), screenWidth - margin - 10f, barTop + barHeight - 5f, milestonePaint)
        
        if (score >= progressMilestone) {
            progressMilestone += 100
        }

        // Achievement Messages every 50
        val currentTime = System.currentTimeMillis()
        
        if (score > 0 && score % 50 == 0 && score != lastMilestoneScore) {
            val messages = listOf("GOOD JOB!", "KEEP IT UP!", "EXCELLENT!")
            val index = (score / 50 - 1) % messages.size
            currentMessage = messages[index]
            messageStartTime = currentTime
            lastMilestoneScore = score
        }

        if (currentMessage != null) {
            if (currentTime - messageStartTime < 1000) {
                drawMessageBox(canvas, currentMessage!!)
            } else {
                currentMessage = null
            }
        }
    }

    private fun drawMessageBox(canvas: Canvas, text: String) {
        val rectWidth = screenWidth * 0.8f
        val rectHeight = 150f
        val left = (screenWidth - rectWidth) / 2
        val top = screenHeight * 0.4f
        val rect = RectF(left, top, left + rectWidth, top + rectHeight)

        canvas.drawRoundRect(rect, 30f, 30f, messageBoxPaint)
        val neonPurple = ContextCompat.getColor(context, R.color.neon_purple)
        val neonBlue = ContextCompat.getColor(context, R.color.neon_blue)
        messageBoxStrokePaint.shader = LinearGradient(left, top, left + rectWidth, top, neonPurple, neonBlue, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(rect, 30f, 30f, messageBoxStrokePaint)
        canvas.drawText(text, screenWidth / 2f, top + (rectHeight / 2) + 15f, messagePaint)
    }

    private fun drawPauseOverlay(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#88000000"))
        val oldSize = messagePaint.textSize
        messagePaint.textSize = 80f
        canvas.drawText("PAUSED", screenWidth / 2f, screenHeight / 2f, messagePaint)
        messagePaint.textSize = oldSize
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.actionMasked.let { action ->
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                event.actionIndex.let { index ->
                    val tx = event.getX(index)
                    val ty = event.getY(index)

                    if (tx < 150 && ty < 150) {
                        togglePause()
                        return true
                    }

                    if (isPaused) return true

                    if (Tile.speed > 0) {
                        touchedX = tx
                        touchedY = ty
                        tempTiles = CopyOnWriteArrayList(tiles)
                        for (tile in tempTiles) {
                            if (tile.checkTouch(touchedX, touchedY)) {
                                if (Build.VERSION.SDK_INT >= 26) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    vibrator?.vibrate(40)
                                }
                                break
                            } else if (!tile.pressed && touchedY < tile.endY && touchedY > tile.startY) {
                                tappedWrongTile = when {
                                    (touchedX < screenWidth / 4) -> 0
                                    (touchedX < screenWidth / 2) -> 1
                                    (touchedX < 3 * screenWidth / 4) -> 2
                                    else -> 3
                                }
                                startY = tile.startY
                                endY = tile.endY
                                gameOver = true
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    fun pauseGame() {
        if (!isPaused) {
            lastTileSpeed = Tile.speed
            Tile.speed = 0.0
            isPaused = true
            mediaPlayer?.pause()
        }
    }

    fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            lastTileSpeed = Tile.speed
            Tile.speed = 0.0
            mediaPlayer?.pause()
        } else {
            Tile.speed = lastTileSpeed
            if (music) mediaPlayer?.start()
        }
    }
}