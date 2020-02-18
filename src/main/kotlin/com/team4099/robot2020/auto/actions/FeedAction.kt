package com.team4099.robot2020.auto.actions;

import com.team4099.lib.auto.Action
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.subsystems.Feeder
import com.team4099.robot2020.subsystems.Intake
import com.team4099.robot2020.subsystems.Wrist

class FeedAction : Action {
    override fun isFinished(timestamp: Double): Boolean {
//        return Feeder.inBeamBroken && Feeder.outBeamBroken
        return true
    }

    override fun onStart(timestamp: Double) {
        Wrist.positionSetpoint = Constants.Wrist.WristPosition.HORIZONTAL
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        Intake.intakeState = Intake.IntakeState.IN
        Feeder.feederState = Feeder.FeederState.HOLD
    }

    override fun onStop(timestamp: Double) {

    }
}
