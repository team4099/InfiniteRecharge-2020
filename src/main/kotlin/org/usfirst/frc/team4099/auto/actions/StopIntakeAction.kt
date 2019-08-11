package org.usfirst.frc.team4099.auto.actions

import org.usfirst.frc.team4099.robot.subsystems.Intake

class StopIntakeAction : Action {
    val intake = Intake.instance

    override fun update() { }

    override fun isFinished(): Boolean {
        return true
    }

    override fun done() { }

    override fun start() {
        intake.intakeState = Intake.IntakeState.STOP
        intake.open = false
    }

}