package com.team4099.robot2020.subsystems

import com.team4099.lib.drive.DriveSignal
import com.team4099.lib.loop.Loop
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.config.Constants.Superstructure
import com.team4099.robot2020.statemachines.ShooterStatemachine
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Superstructure : Loop {

    private var hasStateChanged = false

//    private var previousWantedState = Superstructure.WantedState.NA
    private var currentWantedState = Superstructure.WantedState.DEFAULT
        set(value) {
//            previousWantedState = currentWantedState

            if (value != currentWantedState) {
                 // TODO: Do a transition handler

                field = value
            }
        }

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
            Superstructure.WantedState.SHOOT -> {
                ShooterStatemachine.wantedShootState = ShooterStatemachine.ShootState.SHOOT

                while (!Shooter.shooterReady) {
                    currentRobotState = Superstructure.RobotState.SPINNING_FLYWHEEL
                }
                currentRobotState = Superstructure.RobotState.SHOOTING
            }
            Superstructure.WantedState.CLIMBER_CLIMB -> {
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.UP

                currentRobotState = Superstructure.RobotState.CLIMBING
            }
            Superstructure.WantedState.INTAKE -> {
                Feeder.feederState = Feeder.FeederState.INTAKE
                Intake.intakeState = Intake.IntakeState.IN

                currentRobotState = Superstructure.RobotState.INTAKING
            }
            Superstructure.WantedState.EXHAUST -> {
                Shooter.shooterState = Shooter.State.EXHAUST
                Feeder.feederState = Feeder.FeederState.EXHAUST
                Intake.intakeState = Intake.IntakeState.OUT

                currentRobotState = Superstructure.RobotState.EXHAUSTING
            }
        }
    }

    override fun onStop(timestamp: Double) { }
}
