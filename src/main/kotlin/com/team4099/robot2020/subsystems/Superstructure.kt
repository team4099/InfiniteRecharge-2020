package com.team4099.robot2020.subsystems

import Shooter
import com.team4099.lib.drive.DriveSignal
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.loop.Loop
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants.Superstructure
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.statemachines.ShooterStatemachine
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard


object Superstructure : Subsystem {

    private var currentWantedState = Superstructure.WantedState.DEFAULT
    private var currentRobotState = Superstructure.RobotState.DEFAULT

    override fun onLoop(timestamp: Double, dT: Double) {
        when (currentWantedState) {
            Superstructure.WantedState.IDLE -> {
                Drive.setOpenLoop(DriveSignal.NEUTRAL)
                Intake.intakeState = Intake.IntakeState.IDLE
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.DOWN
                Shooter.shooterState = Shooter.State.IDLE

                currentRobotState = Superstructure.RobotState.IDLING
            }
            Superstructure.WantedState.SHOOT -> {
                ShooterStatemachine.wantedShootState = ShooterStatemachine.ShootState.SHOOT

                while (!Shooter.shooterReady) {
                    currentRobotState = Superstructure.RobotState.SPINNING_FLYWHEEL
                }
                currentRobotState = Superstructure.RobotState.SHOOTING
            }
            Superstructure.WantedState.INTAKE -> {
                Feeder.feederState = Feeder.FeederState.INTAKE
                Intake.intakeState = Intake.IntakeState.IN

                currentRobotState = Superstructure.RobotState.INTAKING
            }
            Superstructure.WantedState.CLIMBER_CLIMB -> {
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.UP

                currentRobotState = Superstructure.RobotState.CLIMBING
            }

            Superstructure.WantedState.EXHAUST -> {
                Shooter.shooterState = Shooter.State.EXHAUST
                Feeder.feederState = Feeder.FeederState.EXHAUST
                Intake.intakeState = Intake.IntakeState.OUT

                currentRobotState = Superstructure.RobotState.EXHAUSTING
            }
        }
    }

    override fun onStart(timestamp: Double) {
        Drive.setOpenLoop(DriveSignal.NEUTRAL)
        Intake.intakeState = Intake.IntakeState.IDLE
        Climber.positionSetpoint = Constants.Climber.ClimberPosition.DOWN
        Shooter.shooterState = Shooter.State.IDLE
        Feeder.feederState = Feeder.FeederState.IDLE

        currentRobotState = Superstructure.RobotState.IDLING
    }

    override fun onStop(timestamp: Double) {}

    override fun outputTelemetry() {
        SmartDashboard.putString("RobotState", currentRobotState.toString())
    }

    override fun checkSystem() { }

    override fun registerLogging() { }

    override fun zeroSensors() { }
}
