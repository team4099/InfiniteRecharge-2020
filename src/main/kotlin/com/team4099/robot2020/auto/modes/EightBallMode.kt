package com.team4099.robot2020.auto.modes

import com.team4099.lib.auto.AutoMode
import com.team4099.lib.auto.ParallelRaceAction
import com.team4099.lib.auto.SeriesAction
import com.team4099.robot2020.auto.PathStore
import com.team4099.robot2020.auto.actions.FollowPathAction
import com.team4099.robot2020.auto.actions.IntakeAction
import com.team4099.robot2020.auto.actions.ShootAction
import com.team4099.robot2020.config.Constants

class EightBallMode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    override fun routine() {
        runAction(ParallelRaceAction(listOf(
            FollowPathAction(PathStore.toRendezvousPoint2Balls),
            IntakeAction()
        )))
        runAction(FollowPathAction(PathStore.fromRendezvousPoint2Balls))
        runAction(ShootAction())
        runAction(ParallelRaceAction(listOf(
            SeriesAction(listOf(
                FollowPathAction(PathStore.toNearTrench),
                FollowPathAction(PathStore.intakeInNearTrench)
            )),
            IntakeAction()
        )))
        runAction(FollowPathAction(PathStore.fromNearTrench))
        runAction(ShootAction())
    }

    override fun done() {}
}
