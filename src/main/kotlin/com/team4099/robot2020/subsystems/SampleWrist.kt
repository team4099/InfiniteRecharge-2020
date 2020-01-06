package com.team4099.robot2020.subsystems

import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.config.Constants

object SampleWrist : ServoMotorSubsystem(
    Constants.SampleWrist,
    CTREMotorControllerFactory.createDefaultTalon(Constants.SampleWrist.MASTER_ID),
    listOf(
        CTREMotorControllerFactory.createPermanentSlaveVictor(
            Constants.SampleWrist.SLAVE1_ID,
            Constants.SampleWrist.MASTER_ID
        )
    )
) {
    var positionSetpoint: Constants.SampleWrist.WristPosition = Constants.SampleWrist.WristPosition.HORIZONTAL
        set(value) {
            positionSetpointMotionProfile = value.position
            field = value
        }
}
