package com.blockpuzzle.dragon

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * SurfaceView-based renderer for Block Puzzle Dragon.
 * Runs its own game-loop thread.
 */
class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback, Runnable {

    val engine = GameEngine()

    private var gameThread: Thread? = null
    private var running = false

    // Paints
    private val bgPaint     = Paint().apply { color = Color.parseColor("#1A0A2E") }
    private val gridPaint   = Paint().apply {
        color = Color.parseColor("#2D1B4E"); style = Paint.Style.STROKE; strokeWidth = 1f
    }
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#9B59B6"); style = Paint.Style.STROKE; strokeWidth = 4f
    }
    private val textPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 40f; typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700"); textSize = 32f
    }
    private val gameOverPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF6B35"); textSize = 64f
        typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    private val blockPaint  = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ghostPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 70; style = Paint.Style.STROKE; strokeWidth = 3f
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { alpha = 120 }

    // Layout (computed in onSizeChanged)
    private var boardLeft = 0f
    private var boardTop  = 0f
    private var cellSize  = 0f
    private val SIDE_W    = 220f  // sidebar width (px)

    var onGameOverListener: ((Long) -> Unit)? = null

    init {
        holder.addCallback(this)
        setZOrderOnTop(false)
    }

    // ── SurfaceHolder.Callback ─────────────────────────────────────────────

    override fun surfaceCreated(h: SurfaceHolder) {
        running = true
        gameThread = Thread(this, "GameLoop").also { it.start() }
    }

    override fun surfaceChanged(h: SurfaceHolder, fmt: Int, w: Int, h2: Int) {
        computeLayout(w, h2)
    }

    override fun surfaceDestroyed(h: SurfaceHolder) {
        running = false
        gameThread?.join()
    }

    // ── Game loop ──────────────────────────────────────────────────────────

    private var lastTick = System.currentTimeMillis()
    private var wasGameOver = false

    override fun run() {
        while (running) {
            val now = System.currentTimeMillis()
            val tickMs = tickInterval()
            if (now - lastTick >= tickMs) {
                if (!engine.isPaused && !engine.isGameOver) {
                    engine.tick()
                    if (engine.isGameOver && !wasGameOver) {
                        wasGameOver = true
                        post { onGameOverListener?.invoke(engine.score) }
                    }
                }
                lastTick = now
            }
            draw()
            Thread.sleep(16) // ~60 fps
        }
    }

    private fun tickInterval(): Long {
        // Faster every level: start 800ms, floor 80ms
        return maxOf(80L, 800L - (engine.level - 1) * 70L)
    }

    // ── Input ──────────────────────────────────────────────────────────────

    fun moveLeft()   { if (!engine.isGameOver) engine.moveLeft() }
    fun moveRight()  { if (!engine.isGameOver) engine.moveRight() }
    fun softDrop()   { if (!engine.isGameOver) engine.softDrop() }
    fun hardDrop()   { if (!engine.isGameOver) engine.hardDrop() }
    fun rotate()     { if (!engine.isGameOver) engine.rotate() }
    fun togglePause(){ engine.isPaused = !engine.isPaused }

    fun restart() {
        wasGameOver = false
        engine.restart()
        lastTick = System.currentTimeMillis()
    }

    // ── Layout ─────────────────────────────────────────────────────────────

    private fun computeLayout(w: Int, h: Int) {
        val availW = w - SIDE_W
        val cellW  = availW / engine.cols
        val cellH  = h.toFloat() / engine.rows
        cellSize   = minOf(cellW, cellH)
        val boardW = cellSize * engine.cols
        val boardH = cellSize * engine.rows
        boardLeft  = (availW - boardW) / 2f
        boardTop   = (h - boardH) / 2f
        textPaint.textSize  = cellSize * 0.55f
        labelPaint.textSize = cellSize * 0.42f
    }

    // ── Drawing ────────────────────────────────────────────────────────────

    private fun draw() {
        val canvas = holder.lockCanvas() ?: return
        try {
            canvas.drawColor(Color.parseColor("#1A0A2E"))
            drawGrid(canvas)
            drawGhost(canvas)
            drawLockedCells(canvas)
            drawPiece(canvas, engine.current)
            drawBorder(canvas)
            drawSidebar(canvas)
            if (engine.isGameOver) drawGameOver(canvas)
            if (engine.isPaused && !engine.isGameOver) drawPaused(canvas)
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawGrid(canvas: Canvas) {
        val right  = boardLeft + cellSize * engine.cols
        val bottom = boardTop  + cellSize * engine.rows
        for (c in 0..engine.cols) {
            val x = boardLeft + c * cellSize
            canvas.drawLine(x, boardTop, x, bottom, gridPaint)
        }
        for (r in 0..engine.rows) {
            val y = boardTop + r * cellSize
            canvas.drawLine(boardLeft, y, right, y, gridPaint)
        }
    }

    private fun drawLockedCells(canvas: Canvas) {
        for (r in 0 until engine.rows) {
            for (c in 0 until engine.cols) {
                val color = engine.getCellColor(r, c) ?: continue
                drawCell(canvas, r, c, color)
            }
        }
    }

    private fun drawPiece(canvas: Canvas, piece: Tetromino) {
        piece.cells().forEach { (r, c) ->
            if (r >= 0) drawCell(canvas, r, c, piece.color)
        }
    }

    private fun drawGhost(canvas: Canvas) {
        val ghost = engine.ghostPiece()
        ghost.cells().forEach { (r, c) ->
            if (r >= 0) {
                ghostPaint.color = engine.current.color
                val rect = cellRect(r, c)
                canvas.drawRect(rect, ghostPaint)
            }
        }
    }

    private fun drawCell(canvas: Canvas, row: Int, col: Int, color: Int) {
        val rect = cellRect(row, col)
        // Shadow
        shadowPaint.color = darken(color, 0.4f)
        canvas.drawRect(rect.left + 4, rect.top + 4, rect.right + 4, rect.bottom + 4, shadowPaint)
        // Main fill
        blockPaint.color = color
        canvas.drawRect(rect, blockPaint)
        // Highlight top-left
        blockPaint.color = lighten(color, 0.35f)
        val hl = cellSize * 0.12f
        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + hl, blockPaint)
        canvas.drawRect(rect.left, rect.top, rect.left + hl, rect.bottom, blockPaint)
    }

    private fun cellRect(row: Int, col: Int) = RectF(
        boardLeft + col * cellSize + 2,
        boardTop  + row * cellSize + 2,
        boardLeft + (col + 1) * cellSize - 2,
        boardTop  + (row + 1) * cellSize - 2
    )

    private fun drawBorder(canvas: Canvas) {
        canvas.drawRect(
            boardLeft - 2, boardTop - 2,
            boardLeft + cellSize * engine.cols + 2,
            boardTop  + cellSize * engine.rows + 2,
            borderPaint
        )
    }

    private fun drawSidebar(canvas: Canvas) {
        val sideX = boardLeft + cellSize * engine.cols + 20f
        val h = height.toFloat()

        // Title
        labelPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("🐉 DRAGON", sideX, h * 0.06f, labelPaint)
        labelPaint.color = Color.parseColor("#FF6B35")
        canvas.drawText("  BLOCKS", sideX, h * 0.10f, labelPaint)

        // Score
        labelPaint.color = Color.parseColor("#AAB8C2")
        canvas.drawText("SCORE", sideX, h * 0.20f, labelPaint)
        textPaint.color = Color.WHITE
        canvas.drawText("${engine.score}", sideX, h * 0.26f, textPaint)

        // Level
        labelPaint.color = Color.parseColor("#AAB8C2")
        canvas.drawText("LEVEL", sideX, h * 0.36f, labelPaint)
        textPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("${engine.level}", sideX, h * 0.42f, textPaint)

        // Lines
        labelPaint.color = Color.parseColor("#AAB8C2")
        canvas.drawText("LINES", sideX, h * 0.50f, labelPaint)
        textPaint.color = Color.parseColor("#2ECC71")
        canvas.drawText("${engine.linesCleared}", sideX, h * 0.56f, textPaint)

        // Next piece preview
        labelPaint.color = Color.parseColor("#AAB8C2")
        canvas.drawText("NEXT", sideX, h * 0.66f, labelPaint)
        drawNextPreview(canvas, sideX, h * 0.70f)
    }

    private fun drawNextPreview(canvas: Canvas, x: Float, y: Float) {
        val previewCell = cellSize * 0.75f
        engine.next.shape.forEach { offset ->
            val px = x + offset[1] * previewCell
            val py = y + offset[0] * previewCell
            blockPaint.color = engine.next.color
            canvas.drawRect(px + 2, py + 2, px + previewCell - 2, py + previewCell - 2, blockPaint)
        }
    }

    private fun drawGameOver(canvas: Canvas) {
        // Dimmer overlay
        val dim = Paint().apply { color = Color.parseColor("#AA000000") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dim)

        gameOverPaint.color = Color.parseColor("#FF6B35")
        canvas.drawText("🐉 GAME OVER", width / 2f, height * 0.42f, gameOverPaint)

        gameOverPaint.textSize = 44f
        gameOverPaint.color = Color.parseColor("#FFD700")
        canvas.drawText("Score: ${engine.score}", width / 2f, height * 0.52f, gameOverPaint)

        gameOverPaint.textSize = 36f
        gameOverPaint.color = Color.WHITE
        canvas.drawText("Tap RESTART to play again", width / 2f, height * 0.60f, gameOverPaint)
    }

    private fun drawPaused(canvas: Canvas) {
        val dim = Paint().apply { color = Color.parseColor("#88000000") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dim)

        gameOverPaint.textSize = 64f
        gameOverPaint.color = Color.parseColor("#FFD700")
        gameOverPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("⏸ PAUSED", width / 2f, height * 0.48f, gameOverPaint)
    }

    // ── Colour helpers ─────────────────────────────────────────────────────

    private fun darken(color: Int, factor: Float): Int {
        val r = (Color.red(color)   * (1 - factor)).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * (1 - factor)).toInt().coerceIn(0, 255)
        val b = (Color.blue(color)  * (1 - factor)).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }

    private fun lighten(color: Int, factor: Float): Int {
        val r = (Color.red(color)   + (255 - Color.red(color))   * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) + (255 - Color.green(color)) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color)  + (255 - Color.blue(color))  * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}
