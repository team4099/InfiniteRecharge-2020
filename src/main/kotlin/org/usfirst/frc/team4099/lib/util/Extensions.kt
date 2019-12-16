package org.usfirst.frc.team4099.lib.util

import kotlin.math.abs

fun Double.around(around: Double, tolerance: Double): Boolean {
    return abs(this - around) < tolerance
}
