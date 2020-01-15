package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.team4099.lib.hardware.CTREServoMotorHardware
import com.team4099.lib.hardware.ServoMotorHardware
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.config.Constants

object Climber : ServoMotorSubsystem(
    Constants.Climber,
    CTREServoMotorHardware(
        Constants.Climber.MASTER_ID,
        listOf(
            Constants.Climber.SLAVE_ID
        )
    )
) {

    var positionSetpoint: Constants.Climber.ClimberPosition = Constants.Climber.ClimberPosition.DOWN
        set(value) {
            Climber.positionSetpointMotionProfile = value.position
            field = value
        }

    override fun onStart(timestamp: Double) {
        velocitySetpoint = 0.0
    }

    override fun onStop(timestamp: Double) {
        velocitySetpoint = 0.0
    }
}
