package com.team4099.robot2020.auto.actions

import com.team4099.lib.auto.Action
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.subsystems.Superstructure

class ShootAction : Action {
    private var startTime = 0.0

    override fun isFinished(timestamp: Double): Boolean {
        return timestamp - startTime > Constants.Autonomous.SHOOT_MAX_TIME_SECONDS
    }

    override fun onStart(timestamp: Double) {
        startTime = timestamp
        Superstructure.alignWithVision = true
        Superstructure.state = Superstructure.SuperstructureState.SHOOTING
    }

    override fun onLoop(timestamp: Double, dT: Double) {}

    override fun onStop(timestamp: Double) {
        Superstructure.state = Superstructure.SuperstructureState.IDLE
    }
}
