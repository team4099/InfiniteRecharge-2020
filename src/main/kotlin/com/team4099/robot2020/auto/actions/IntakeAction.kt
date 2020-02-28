package com.team4099.robot2020.auto.actions

import com.team4099.lib.auto.Action
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.subsystems.Intake
import com.team4099.robot2020.subsystems.Wrist

class IntakeAction : Action {
    override fun isFinished(timestamp: Double): Boolean {
        return false
    }

    override fun onStart(timestamp: Double) {
        Intake.intakeState = Intake.IntakeState.IN
        Wrist.positionSetpoint = Constants.Wrist.WristPosition.HORIZONTAL
    }

    override fun onLoop(timestamp: Double, dT: Double) {}

    override fun onStop(timestamp: Double) {
        Intake.intakeState = Intake.IntakeState.IDLE
        Wrist.positionSetpoint = Constants.Wrist.WristPosition.VERTICAL
    }
}