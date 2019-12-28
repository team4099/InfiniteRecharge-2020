package org.usfirst.frc.team4099.lib.util

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Determines if another value is near this Double.
 *
 * @param around The value to compare this to.
 * @param tolerance The range within values will be considered near.
 * @return If [around] is within [tolerance] of this Double.
 */
fun Double.around(around: Double, tolerance: Double): Boolean {
    return abs(this - around) < tolerance
}

/**
 * Constrains this Double between the given bounds.
 *
 * @param lowerBound The lower bound of this Double's range.
 * @param upperBound The upper bound of this Double's range.
 * @return Return this Double if it is in the range otherwise return [lowerBound] or [upperBound].
 */
fun Double.limit(lowerBound: Double, upperBound: Double): Double {
    return min(upperBound, max(lowerBound, this))
}
