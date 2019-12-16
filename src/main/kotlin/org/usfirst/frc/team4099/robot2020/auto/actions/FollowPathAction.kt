package org.usfirst.frc.team4099.robot2020.auto.actions

import edu.wpi.first.wpilibj.trajectory.Trajectory
import org.usfirst.frc.team4099.lib.auto.Action
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.robot2020.subsystems.Drive

class FollowPathAction(private val path: Trajectory) : Action {
    override fun isFinished(timestamp: Double): Boolean {
        return Drive.isPathFinished(timestamp)
    }

    override fun onStart(timestamp: Double) {
        Drive.path = path
    }

    override fun onLoop(timestamp: Double) {}

    override fun onStop(timestamp: Double) {
        Drive.setOpenLoop(DriveSignal.NEUTRAL)
    }
}
