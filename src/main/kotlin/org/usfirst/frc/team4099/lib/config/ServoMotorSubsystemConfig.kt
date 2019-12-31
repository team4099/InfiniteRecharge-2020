package org.usfirst.frc.team4099.lib.config

import org.usfirst.frc.team4099.lib.motorcontroller.CTREMotorControllerFactory

/**
 * The configuration of a [org.usfirst.frc.team4099.lib.subsystem.ServoMotorSubsystem]
 */
class ServoMotorSubsystemConfig(
    val masterMotorControllerConfiguration: CTREMotorControllerFactory.Configuration,
    val name: String,
    val unitsName: String,
    val positionPIDGains: PIDGains,
    val velocityPIDGains: PIDGains,
    val homePosition: Double,
    val motionConstraints: ServoMotorSubsystemMotionConstraints,
    reductionFromEncoder: Double,
    encoderResolution: Int
) {
    val ticksPerUnitDistance = encoderResolution / reductionFromEncoder
}
