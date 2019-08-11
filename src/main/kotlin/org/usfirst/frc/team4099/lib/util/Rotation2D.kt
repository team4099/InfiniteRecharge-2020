package org.usfirst.frc.team4099.lib.util

import java.text.DecimalFormat

/**
 * A rotation in a 2d coordinate frame represented a point on the unit circle
 * (cosine and sine).
 *
 * Inspired by Sophus (https://github.com/strasdat/Sophus/tree/master/sophus)
 */
class Rotation2D : Interpolable<Rotation2D> {

    protected var cos_angle_: Double = 0.toDouble()
    protected var sin_angle_: Double = 0.toDouble()

    @JvmOverloads constructor(x: Double = 1.0, y: Double = 0.0, normalize: Boolean = false) {
        cos_angle_ = x
        sin_angle_ = y
        if (normalize) {
            normalize()
        }
    }

    constructor(other: Rotation2D) {
        cos_angle_ = other.cos_angle_
        sin_angle_ = other.sin_angle_
    }

    /**
     * From trig, we know that sin^2 + cos^2 == 1, but as we do math on this
     * object we might accumulate rounding errors. Normalizing forces us to
     * re-scale the sin and cos to reset rounding errors.
     */
    fun normalize() {
        val magnitude = Math.hypot(cos_angle_, sin_angle_)
        if (magnitude > kEpsilon) {
            sin_angle_ /= magnitude
            cos_angle_ /= magnitude
        } else {
            sin_angle_ = 0.0
            cos_angle_ = 1.0
        }
    }

    fun cos(): Double {
        return cos_angle_
    }

    fun sin(): Double {
        return sin_angle_
    }

    fun tan(): Double {
        return if (cos_angle_ < kEpsilon) {
            if (sin_angle_ >= 0.0) {
                java.lang.Double.POSITIVE_INFINITY
            } else {
                java.lang.Double.NEGATIVE_INFINITY
            }
        } else sin_angle_ / cos_angle_
    }

    val radians: Double
        get() = Math.atan2(sin_angle_, cos_angle_)

    val degrees: Double
        get() = Math.toDegrees(radians)

    /**
     * We can rotate this Rotation2D by adding together the effects of it and
     * another rotation.
     *
     * @param other
     * The other rotation. See:
     * https://en.wikipedia.org/wiki/Rotation_matrix
     * @return This rotation rotated by other.
     */
    fun rotateBy(other: Rotation2D): Rotation2D {
        return Rotation2D(cos_angle_ * other.cos_angle_ - sin_angle_ * other.sin_angle_,
                cos_angle_ * other.sin_angle_ + sin_angle_ * other.cos_angle_, true)
    }

    /**
     * The inverse of a Rotation2D "undoes" the effect of this rotation.
     *
     * @return The opposite of this rotation.
     */
    fun inverse(): Rotation2D {
        return Rotation2D(cos_angle_, -sin_angle_, false)
    }

    override fun interpolate(other: Rotation2D, x: Double): Rotation2D {
        if (x <= 0) {
            return Rotation2D(this)
        } else if (x >= 1) {
            return Rotation2D(other)
        }
        val angle_diff = inverse().rotateBy(other).radians
        return this.rotateBy(fromRadians(angle_diff * x))
    }

    override fun toString(): String {
        val fmt = DecimalFormat("#0.000")
        return "(" + fmt.format(degrees) + " deg)"
    }

    companion object {
        protected val kEpsilon = 1E-9

        var FORWARDS = fromDegrees(0.0)
        var BACKWARDS = fromDegrees(179.9)

        fun fromRadians(angle_radians: Double): Rotation2D {
            return Rotation2D(Math.cos(angle_radians), Math.sin(angle_radians), false)
        }

        fun fromDegrees(angle_degrees: Double): Rotation2D {
            return fromRadians(Math.toRadians(angle_degrees))
        }
    }
}