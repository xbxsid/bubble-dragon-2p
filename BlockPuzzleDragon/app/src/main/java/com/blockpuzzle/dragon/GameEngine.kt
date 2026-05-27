package com.blockpuzzle.dragon

import kotlin.random.Random

/**
 * Pure game-logic engine – no Android dependencies.
 * Implements a classic Tetris-like block puzzle with dragon theme.
 */
class GameEngine(
    val cols: Int = 10,
    val rows: Int = 20
) {
    // ── State ──────────────────────────────────────────────────────────────
    /** Each cell: null = empty, non-null = color int of locked piece */
    val board: Array<IntArray?> = Array(rows) { null }  // null row = empty

    private val grid: Array<Array<Int?>> = Array(rows) { Array(cols) { null } }

    var current: Tetromino = spawnPiece()
        private set
    var next: Tetromino = spawnPiece()
        private set

    var score: Long = 0L
        private set
    var level: Int = 1
        private set
    var linesCleared: Int = 0
        private set
    var isGameOver: Boolean = false
        private set
    var isPaused: Boolean = false

    // Points for 1/2/3/4 lines at once
    private val LINE_POINTS = intArrayOf(0, 100, 300, 500, 800)

    // Bag-random piece generator (7-bag system)
    private val bag = mutableListOf<TetrominoType>()

    // ── Public API ─────────────────────────────────────────────────────────

    fun moveLeft()  { tryMove(current.moved(0, -1)) }
    fun moveRight() { tryMove(current.moved(0,  1)) }
    fun softDrop()  { if (!tryMove(current.moved(1, 0))) lockAndAdvance() }

    fun rotate() {
        // Try basic rotation, then wall-kick +1/-1 column
        val rotated = current.rotated()
        when {
            isValid(rotated)             -> current = rotated
            isValid(rotated.moved(0,  1)) -> current = rotated.moved(0,  1)
            isValid(rotated.moved(0, -1)) -> current = rotated.moved(0, -1)
            isValid(rotated.moved(0,  2)) -> current = rotated.moved(0,  2)
            isValid(rotated.moved(0, -2)) -> current = rotated.moved(0, -2)
        }
    }

    fun hardDrop() {
        while (tryMove(current.moved(1, 0))) { /* drop */ }
        lockAndAdvance()
    }

    /** Call this on each game tick; returns true if a line was cleared. */
    fun tick(): Boolean {
        if (isGameOver || isPaused) return false
        return if (!tryMove(current.moved(1, 0))) {
            lockAndAdvance()
            true
        } else false
    }

    /** Ghost piece – lowest valid position for current piece */
    fun ghostPiece(): Tetromino {
        var ghost = current
        while (isValid(ghost.moved(1, 0))) ghost = ghost.moved(1, 0)
        return ghost
    }

    fun restart() {
        for (r in 0 until rows) for (c in 0 until cols) grid[r][c] = null
        bag.clear()
        score = 0L
        level = 1
        linesCleared = 0
        isGameOver = false
        isPaused = false
        current = spawnPiece()
        next = spawnPiece()
    }

    fun getCellColor(row: Int, col: Int): Int? = grid[row][col]

    // ── Private helpers ────────────────────────────────────────────────────

    private fun isValid(piece: Tetromino): Boolean =
        piece.cells().all { (r, c) ->
            r in 0 until rows && c in 0 until cols && grid[r][c] == null
        }

    private fun tryMove(piece: Tetromino): Boolean {
        return if (isValid(piece)) { current = piece; true } else false
    }

    private fun lockAndAdvance() {
        // Lock current piece into grid
        current.cells().forEach { (r, c) -> grid[r][c] = current.color }

        // Clear full lines
        val cleared = clearLines()
        if (cleared > 0) {
            linesCleared += cleared
            score += LINE_POINTS[cleared] * level
            level = 1 + linesCleared / 10
        }

        // Advance to next piece
        current = next
        next = spawnPiece()

        // Check game over
        if (!isValid(current)) {
            isGameOver = true
        }
    }

    private fun clearLines(): Int {
        var cleared = 0
        var writeRow = rows - 1
        for (readRow in rows - 1 downTo 0) {
            if (grid[readRow].all { it != null }) {
                cleared++
            } else {
                if (writeRow != readRow) {
                    for (c in 0 until cols) grid[writeRow][c] = grid[readRow][c]
                }
                writeRow--
            }
        }
        for (r in 0..writeRow) for (c in 0 until cols) grid[r][c] = null
        return cleared
    }

    private fun spawnPiece(): Tetromino {
        if (bag.isEmpty()) refillBag()
        val type = bag.removeAt(0)
        return Tetromino(type, row = 0, col = cols / 2 - 2)
    }

    private fun refillBag() {
        bag.addAll(TetrominoType.values().toList().shuffled())
    }
}
