package com.team4099.robot2020.subsystems

import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlin.math.pow

object Superstructure : Subsystem {
    enum class SuperstructureState {
        IDLE,
        SHOOTING,
        INTAKE_IN,
        INTAKE_OUT,
        INTAKE_AND_SHOOT
    }

    var state = SuperstructureState.IDLE

    private fun shoot() {
        Vision.state = Vision.VisionState.AIMING
        Shooter.shooterState = Shooter.State.SHOOTING

        if (Vision.onTarget) {
            val dist = Vision.distance
            Shooter.velocityTarget = Constants.Shooter.DIST_POLY_A * dist.pow(2.0) +
                Constants.Shooter.DIST_POLY_B * dist +
                Constants.Shooter.DIST_POLY_C

            Feeder.feederState = if (Shooter.shooterReady) {
                Feeder.FeederState.SHOOT
            } else {
                Feeder.FeederState.IDLE
            }
        }
    }

    override fun onStart(timestamp: Double) {
        state = SuperstructureState.IDLE
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        when (state) {
            SuperstructureState.IDLE -> {
                Shooter.shooterState = Shooter.State.IDLE
                Feeder.feederState = Feeder.FeederState.IDLE
            }
            SuperstructureState.SHOOTING -> {
                shoot()
            }
            SuperstructureState.INTAKE_IN -> {
                Intake.intakeState = Intake.IntakeState.IN
                if (Intake.hasPowerCell) {
                    Feeder.feederState = Feeder.FeederState.AUTO_INTAKE
                }
            }
            SuperstructureState.INTAKE_OUT -> {
                Intake.intakeState = Intake.IntakeState.OUT
                Feeder.feederState = Feeder.FeederState.EXHAUST
            }
            SuperstructureState.INTAKE_AND_SHOOT -> {
                shoot()
                Intake.intakeState = Intake.IntakeState.IN
            }
        }

        Wrist.positionSetpoint = when (state) {
            SuperstructureState.INTAKE_IN, SuperstructureState.INTAKE_OUT, SuperstructureState.INTAKE_AND_SHOOT ->
                Constants.Wrist.WristPosition.HORIZONTAL
            else -> Constants.Wrist.WristPosition.VERTICAL
        }
    }

    override fun onStop(timestamp: Double) {
        state = SuperstructureState.IDLE
    }

    override fun outputTelemetry() {}

    override fun checkSystem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLogging() {
        HelixLogger.addSource("SUPERSTRUCTURE State") { state.toString() }
        val tab = Shuffleboard.getTab("Superstructure")
        tab.addString("State") { state.toString() }
    }

    override fun zeroSensors() {}
}
