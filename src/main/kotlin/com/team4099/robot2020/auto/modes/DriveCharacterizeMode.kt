package com.team4099.robot2020.auto.modes

import com.team4099.lib.auto.AutoMode
import com.team4099.robot2020.auto.actions.DriveCharacterizeAction
import com.team4099.robot2020.config.Constants

/**
 * Sample path following mode. Drives forward, following a WPILib Trajectory.
 */
class DriveCharacterizeMode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    override fun routine() {
        runAction(DriveCharacterizeAction())
    }

    override fun done() {}
}
