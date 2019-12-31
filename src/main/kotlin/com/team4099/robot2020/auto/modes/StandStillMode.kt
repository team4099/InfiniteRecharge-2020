package com.team4099.robot2020.auto.modes

import com.team4099.lib.auto.AutoMode
import com.team4099.lib.auto.AutoModeEndedException
import com.team4099.robot2020.config.Constants

/**
 * Fallback for when all autonomous modes do not work, resulting in a robot
 * standstill.
 */
class StandStillMode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    @Throws(AutoModeEndedException::class)
    override fun routine() {}

    override fun done() {}
}
