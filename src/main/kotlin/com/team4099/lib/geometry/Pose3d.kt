package com.team4099.lib.geometry

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d

data class Pose3d(val x: Double, val y: Double, val z: Double, val yaw: Rotation2d, val pitch: Rotation2d) {
    constructor(translation: Translation3d, yaw: Rotation2d, pitch: Rotation2d) :
        this(translation.x, translation.y, translation.z, yaw, pitch)

    val translation = Translation3d(x, y, z)

    val pose2d
        get() = Pose2d(translation.x, translation.y, yaw)

    operator fun plus(other: Pose3d) : Pose3d {
        return Pose3d(translation + other.translation, yaw + other.yaw, pitch + other.pitch)
    }

    operator fun minus(other: Pose3d) : Pose3d {
        return Pose3d(translation - other.translation, yaw - other.yaw, pitch - other.pitch)
    }

    operator fun times(scalar: Double) : Pose3d {
        return Pose3d(translation * scalar, yaw, pitch)
    }

    operator fun div(scalar: Double) : Pose3d {
        return Pose3d(translation / scalar, yaw, pitch)
    }

    operator fun unaryMinus() : Pose3d {
        return Pose3d(-translation, yaw + Rotation2d.fromDegrees(180.0), pitch + Rotation2d.fromDegrees(180.0))
    }
}