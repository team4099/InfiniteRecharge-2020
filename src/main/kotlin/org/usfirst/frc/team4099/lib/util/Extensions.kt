package org.usfirst.frc.team4099.lib.util

import kotlin.math.abs

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