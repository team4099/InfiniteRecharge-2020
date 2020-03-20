package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.team4099.lib.hardware.CTREServoMotorHardware
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.config.Constants

object Wrist : ServoMotorSubsystem(
    Constants.Wrist,
    CTREServoMotorHardware(
        Constants.Wrist.MASTER_ID,
        listOf(),
        feedbackDevice = FeedbackDevice.CTRE_MagEncoder_Absolute
    )
) {

    var positionSetpoint: Constants.Wrist.WristPosition = Constants.Wrist.WristPosition.HORIZONTAL
        set(value) {
            positionSetpointMotionProfile = value.position
            field = value
        }
}
