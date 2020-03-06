package com.team4099.robot2020.auto.modes

import com.team4099.lib.auto.AutoMode
import com.team4099.lib.auto.ParallelRaceAction
import com.team4099.robot2020.auto.PathStore
import com.team4099.robot2020.auto.actions.FollowPathAction
import com.team4099.robot2020.auto.actions.IntakeAction
import com.team4099.robot2020.auto.actions.ShootAction
import com.team4099.robot2020.config.Constants

class SixBallMode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    override fun routine() {
        runAction(FollowPathAction(PathStore.toNearTrench))
        runAction(ParallelRaceAction(listOf(
            FollowPathAction(PathStore.intakeInNearTrench),
            IntakeAction()
        )))
        runAction(FollowPathAction(PathStore.fromNearTrench))
        runAction(ShootAction())
    }

    override fun done() {}
}
