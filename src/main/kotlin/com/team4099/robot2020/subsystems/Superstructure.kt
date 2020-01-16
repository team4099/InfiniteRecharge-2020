package com.team4099.robot2020.subsystems

import com.team4099.lib.drive.DriveSignal
import com.team4099.lib.loop.Loop
<<<<<<< HEAD
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.config.Constants.SuperStructure
import com.team4099.robot2020.config.ControlBoard
=======
import com.team4099.robot2020.config.Constants.SuperStructure
>>>>>>> Set drivetrain to off in IDLE state

object SuperStructure : Loop {
    // TODO: Do this
    private var hasStateChanged = false

    private var currentWantedState = SuperStructure.States.DEFAULT
    private var currentRobotState = SuperStructure.States.DEFAULT

    override fun onStart(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        when (currentWantedState) {
            SuperStructure.States.IDLE -> {
                Drive.setOpenLoop(DriveSignal.NEUTRAL)
<<<<<<< HEAD
                Intake.intakeState = Intake.IntakeState.IDLE
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.DOWN
            }
            SuperStructure.States.SPIN_UP_FLYWHEEL -> {
                Shooter.shooterState = Shooter.State.ACCELERATING
            }
            SuperStructure.States.SHOOT -> {
                Shooter.shooterState = Shooter.State.SHOOTING
            }
            SuperStructure.States.CLIMB -> {
                Climber.positionSetpoint = Constants.Climber.ClimberPosition.UP
            }
            SuperStructure.States.INTAKE -> {
                Intake.intakeState = Intake.IntakeState.IN
            }
            SuperStructure.States.UNJAM_INTAKE -> {
                Intake.intakeState = Intake.IntakeState.OUT
            }
            SuperStructure.States.FEED -> {
                Feeder.feederState = Feeder.State.FEEDING
            }
=======
            }
            SuperStructure.States.SPIN_UP_FLYWHEEL -> {}
            SuperStructure.States.SHOOT -> {}
            SuperStructure.States.UNJAM_SHOOTER -> {}
            SuperStructure.States.CLIMB -> {}
            SuperStructure.States.INTAKE -> {}
            SuperStructure.States.UNJAM_INTAKE -> {}
            SuperStructure.States.FEED -> {}
>>>>>>> Set drivetrain to off in IDLE state
            SuperStructure.States.UNJAM_FEEDER -> {}
        }
    }

    public fun setState(wantedState: SuperStructure.States) {
        currentWantedState = wantedState
    }

    override fun onStop(timestamp: Double) {
<<<<<<< HEAD
        // Hello i dont have a life
    }
=======
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


>>>>>>> Set drivetrain to off in IDLE state
}
