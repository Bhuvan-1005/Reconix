package com.example.reconix.utils

import kotlin.math.roundToInt

/**
 * Kotlin Multiplatform compatible number formatting utilities
 */

/**
 * Format a Double to a string with specified decimal places
 */
fun Double.format(decimals: Int): String {
    val multiplier = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        3 -> 1000.0
        else -> 100.0
    }
    val rounded = (this * multiplier).roundToInt() / multiplier
    return when (decimals) {
        0 -> rounded.toInt().toString()
        1 -> {
            val wholePart = rounded.toInt()
            val decimalPart = ((rounded - wholePart) * 10).roundToInt()
            "$wholePart.$decimalPart"
        }
        2 -> {
            val wholePart = rounded.toInt()
            val decimalPart = ((rounded - wholePart) * 100).roundToInt()
            val decimalStr = decimalPart.toString().padStart(2, '0')
            "$wholePart.$decimalStr"
        }
        3 -> {
            val wholePart = rounded.toInt()
            val decimalPart = ((rounded - wholePart) * 1000).roundToInt()
            val decimalStr = decimalPart.toString().padStart(3, '0')
            "$wholePart.$decimalStr"
        }
        else -> rounded.toString()
    }
}

/**
 * Format currency (2 decimal places)
 */
fun Double.formatCurrency(): String = this.format(2)

/**
 * Format percentage (1 decimal place)
 */
fun Double.formatPercentage(): String = this.format(1)

/**
 * Format as integer
 */
fun Double.formatInt(): String = this.format(0)

