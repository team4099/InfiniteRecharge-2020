package com.team4099.robot2020.subsystems

import com.team4099.lib.hardware.SparkMaxServoMotorHardware
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.config.Constants

object Climber : ServoMotorSubsystem(
    Constants.Climber,
    SparkMaxServoMotorHardware(
        Constants.Climber.MASTER_ID,
        listOf(
            Constants.Climber.SLAVE_ID
        )
    )
) {
    var positionSetpoint = Constants.Climber.ClimberPosition.DOWN
        set(value) {
            Climber.positionSetpointMotionProfile = value.position
            field = value
        }

    override fun onStart(timestamp: Double) {
        velocitySetpoint = 0.0
    }

    override fun onLoop(timestamp: Double, dT: Double) {}

    override fun onStop(timestamp: Double) {
        velocitySetpoint = 0.0
    }
}
