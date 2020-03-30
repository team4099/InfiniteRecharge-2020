package com.team4099.lib.geometry

import edu.wpi.first.wpilibj.geometry.Translation2d
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Represents a translation in 3d space.
 * This object can be used to represent a point or a vector.
 *
 * <p>This assumes that you are using conventional mathematical axes.
 * When the robot is placed on the origin, facing toward the X direction,
 * moving forward increases the X, moving to the left increases the Y,
 * and moving upwards increases the Z.
 */
data class Translation3d(val x: Double, val y: Double, val z: Double) {
    val translation2d
        get() = Translation2d(x, y)

    val magnitude
        get() = sqrt(x.pow(2) + y.pow(2) + z.pow(2))

    operator fun plus(other: Translation3d): Translation3d {
        return Translation3d(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Translation3d): Translation3d {
        return Translation3d(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(scalar: Double) : Translation3d {
        return Translation3d(x * scalar, y * scalar, z * scalar)
    }

    operator fun div(scalar: Double): Translation3d {
        return Translation3d(x / scalar, y / scalar, z / scalar)
    }

    operator fun unaryMinus(): Translation3d {
        return Translation3d(-x, -y, -z)
    }

    fun normalize(): Translation3d {
        return this / magnitude
    }
}