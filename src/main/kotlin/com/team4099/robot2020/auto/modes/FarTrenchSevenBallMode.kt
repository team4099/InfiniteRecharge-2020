package com.team4099.robot2020.auto.modes

import com.team4099.lib.auto.AutoMode
import com.team4099.lib.auto.ParallelRaceAction
import com.team4099.lib.auto.SeriesAction
import com.team4099.robot2020.auto.PathStore
import com.team4099.robot2020.auto.actions.FollowPathAction
import com.team4099.robot2020.auto.actions.IntakeAction
import com.team4099.robot2020.auto.actions.ShootAction
import com.team4099.robot2020.auto.actions.WaitAction
import com.team4099.robot2020.config.Constants

class FarTrenchSevenBallMode(delay: Double) : AutoMode(delay, Constants.Autonomous.AUTON_DT) {
    override fun routine() {
        runAction(ParallelRaceAction(listOf(
            SeriesAction(listOf(
                FollowPathAction(PathStore.toFarTrench),
                WaitAction(0.5)
            )),
            IntakeAction()
        )))
        runAction(FollowPathAction(PathStore.fromFarTrench))
        runAction(ShootAction())
        runAction(ParallelRaceAction(listOf(
            FollowPathAction(PathStore.toRendezvousPoint2Balls),
            IntakeAction()
        )))
        runAction(FollowPathAction(PathStore.fromRendezvousPoint2Balls))
        runAction(ShootAction())
    }

    override fun done() {}
}
