package com.team4099.robot2020.auto.actions

import com.team4099.lib.auto.Action
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.subsystems.Feeder
import com.team4099.robot2020.subsystems.Shooter
import com.team4099.robot2020.subsystems.Vision
import edu.wpi.first.wpilibj.Timer
import kotlin.concurrent.timerTask

private var startTime = 0.0

class ShootAction : Action {
    override fun isFinished(timestamp: Double): Boolean {
        return timestamp - startTime > Constants.Autonomous.SHOOT_MAX_TIME_SECONDS
    }

    override fun onStart(timestamp: Double) {
        startTime = timestamp
        Shooter.shooterState = Shooter.State.SHOOTING
        Vision.state = Vision.VisionState.AIMING
        Feeder.feederState = Feeder.FeederState.AUTO_SHOOT
    }

    override fun onLoop(timestamp: Double, dT: Double) {}

    override fun onStop(timestamp: Double) {
        Shooter.shooterState = Shooter.State.IDLE
        Vision.state = Vision.VisionState.IDLE
        Feeder.feederState = Feeder.FeederState.IDLE
    }
}