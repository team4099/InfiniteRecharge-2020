package com.team4099.robot2020.subsystems

import com.team4099.lib.drive.DriveSignal
import com.team4099.lib.loop.Loop
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.config.Constants.Superstructure
import com.team4099.robot2020.statemachines.ShootingStatemachine

object Superstructure : Loop {
    // TODO: Do this
    private var hasStateChanged = false

    private var currentWantedState = Superstructure.WantedState.DEFAULT
    private var currentRobotState = Superstructure.RobotState.DEFAULT

    override fun onStart(timestamp: Double) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        when (currentWantedState) {
            Superstructure.WantedState.IDLE -> {
                Drive.setOpenLoop(DriveSignal.NEUTRAL)
                Intake.intakeState = Intake.IntakeState.IDLE
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.DOWN

                currentRobotState = Superstructure.RobotState.IDLING
            }
            Superstructure.WantedState.SHOOTER_SHOOT -> {
                ShootingStatemachine.wantedShootState = ShootingStatemachine.ShootState.SHOOT

                currentRobotState = Superstructure.RobotState.SHOOTING
            }
            Superstructure.WantedState.CLIMBER_CLIMB -> {
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.UP
            }
            Superstructure.WantedState.INTAKE_INTAKE -> {
                Intake.intakeState = Intake.IntakeState.IN
            }
            Superstructure.WantedState.INTAKE_UNJAM -> {
                Intake.intakeState = Intake.IntakeState.OUT
            }
            Superstructure.WantedState.FEEDER_FEED -> {
//                Feeder.feederState = Feeder.State.FEEDING
            }
            Superstructure.WantedState.FEEDER_UNJAM -> { }
        }
    }

    override fun onStop(timestamp: Double) { }
}
