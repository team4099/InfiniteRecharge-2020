package com.team4099.lib.config

import com.team4099.lib.motorcontroller.CTREMotorControllerFactory

/**
 * The configuration of a [com.team4099.lib.subsystem.ServoMotorSubsystem]
 */
open class ServoMotorSubsystemConfig(
    val masterMotorControllerConfiguration: CTREMotorControllerFactory.Configuration,
    val name: String,
    val unitsName: String,
    val positionPIDGains: PIDGains,
    val velocityPIDGains: PIDGains,
    val homePosition: Double,
    val motionConstraints: ServoMotorSubsystemMotionConstraints,
    unitsPerEncoderRev: Double,
    encoderResolution: Int
) {
    val ticksPerUnitDistance = encoderResolution / unitsPerEncoderRev
}
