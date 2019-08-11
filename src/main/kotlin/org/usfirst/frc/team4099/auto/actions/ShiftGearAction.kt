package org.usfirst.frc.team4099.auto.actions

import org.usfirst.frc.team4099.robot.subsystems.Drive

class ShiftGearAction(private val highGear: Boolean) : Action {
    private val drive = Drive.instance

    override fun update() {}

    override fun isFinished(): Boolean {
        return true
    }

    override fun done() {}

    override fun start() {
        drive.highGear = highGear
    }

}
