package com.team4099.robot2020.subsystems

import com.team4099.lib.hardware.SparkMaxServoMotorHardware
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.config.Constants

object Wrist : ServoMotorSubsystem(
    Constants.Wrist,
    SparkMaxServoMotorHardware(Constants.Wrist.MASTER_ID, listOf(Constants.Wrist.SLAVE1_ID))
) {
    var positionSetpoint: Constants.Wrist.WristPosition = Constants.Wrist.WristPosition.HORIZONTAL
        set(value) {
            positionSetpointMotionProfile = value.position
            field = value
        }
}
