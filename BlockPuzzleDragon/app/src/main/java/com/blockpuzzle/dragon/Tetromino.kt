package com.blockpuzzle.dragon

import android.graphics.Color

/**
 * Tetromino piece definitions and rotation logic.
 * Dragon-themed colour palette.
 */

enum class TetrominoType(val color: Int, val emoji: String) {
    I(Color.parseColor("#FF6B35"), "🔥"),   // Dragon fire – orange-red
    O(Color.parseColor("#FFD700"), "✨"),   // Dragon gold
    T(Color.parseColor("#9B59B6"), "🔮"),   // Mystic purple
    S(Color.parseColor("#2ECC71"), "🍃"),   // Forest green
    Z(Color.parseColor("#E74C3C"), "❤️"),   // Dragon red
    J(Color.parseColor("#3498DB"), "💧"),   // Ice blue
    L(Color.parseColor("#F39C12"), "🌟"),   // Amber
}

// Each piece: array of rotations; each rotation: list of (row, col) offsets from pivot
val TETROMINO_SHAPES: Map<TetrominoType, Array<Array<IntArray>>> = mapOf(
    TetrominoType.I to arrayOf(
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(0,2), intArrayOf(0,3)),
        arrayOf(intArrayOf(0,2), intArrayOf(1,2), intArrayOf(2,2), intArrayOf(3,2)),
        arrayOf(intArrayOf(2,0), intArrayOf(2,1), intArrayOf(2,2), intArrayOf(2,3)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,1), intArrayOf(2,1), intArrayOf(3,1))
    ),
    TetrominoType.O to arrayOf(
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1)),
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1)),
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1)),
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1))
    ),
    TetrominoType.T to arrayOf(
        arrayOf(intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(1,2)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,1)),
        arrayOf(intArrayOf(1,0), intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,1)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(2,1))
    ),
    TetrominoType.S to arrayOf(
        arrayOf(intArrayOf(0,1), intArrayOf(0,2), intArrayOf(1,0), intArrayOf(1,1)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,2)),
        arrayOf(intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,0), intArrayOf(2,1)),
        arrayOf(intArrayOf(0,0), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(2,1))
    ),
    TetrominoType.Z to arrayOf(
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(1,1), intArrayOf(1,2)),
        arrayOf(intArrayOf(0,2), intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,1)),
        arrayOf(intArrayOf(1,0), intArrayOf(1,1), intArrayOf(2,1), intArrayOf(2,2)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(2,0))
    ),
    TetrominoType.J to arrayOf(
        arrayOf(intArrayOf(0,0), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(1,2)),
        arrayOf(intArrayOf(0,1), intArrayOf(0,2), intArrayOf(1,1), intArrayOf(2,1)),
        arrayOf(intArrayOf(1,0), intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,2)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,1), intArrayOf(2,0), intArrayOf(2,1))
    ),
    TetrominoType.L to arrayOf(
        arrayOf(intArrayOf(0,2), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(1,2)),
        arrayOf(intArrayOf(0,1), intArrayOf(1,1), intArrayOf(2,1), intArrayOf(2,2)),
        arrayOf(intArrayOf(1,0), intArrayOf(1,1), intArrayOf(1,2), intArrayOf(2,0)),
        arrayOf(intArrayOf(0,0), intArrayOf(0,1), intArrayOf(1,1), intArrayOf(2,1))
    )
)

data class Tetromino(
    val type: TetrominoType,
    var row: Int = 0,
    var col: Int = 3,
    var rotation: Int = 0
) {
    val shape get() = TETROMINO_SHAPES[type]!![rotation]
    val color get() = type.color

    /** Absolute cell positions on the board */
    fun cells(): List<Pair<Int,Int>> =
        shape.map { Pair(row + it[0], col + it[1]) }

    fun rotated(delta: Int = 1): Tetromino =
        copy(rotation = (rotation + delta + 4) % 4)

    fun moved(dRow: Int, dCol: Int): Tetromino =
        copy(row = row + dRow, col = col + dCol)
}
