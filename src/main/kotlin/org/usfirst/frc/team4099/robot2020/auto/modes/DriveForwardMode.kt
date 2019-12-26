package org.usfirst.frc.team4099.robot2020.auto.modes

import org.usfirst.frc.team4099.lib.auto.AutoMode
import org.usfirst.frc.team4099.robot2020.auto.PathStore
import org.usfirst.frc.team4099.robot2020.auto.actions.FollowPathAction
import org.usfirst.frc.team4099.robot2020.config.Constants

/**
 * Sample path following mode. Drives forward, following a WPILib Trajectory.
 */
class DriveForwardMode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    override fun routine() {
        runAction(FollowPathAction(PathStore.driveForward))
    }

    override fun done() {}
}
