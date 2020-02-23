package com.team4099.robot2020.auto.modes

import com.team4099.lib.auto.AutoMode
import com.team4099.robot2020.auto.actions.ShootAction
import com.team4099.robot2020.config.Constants

class Shoot3Mode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    override fun routine() {
        runAction(ShootAction())
    }

    override fun done() {}
}
