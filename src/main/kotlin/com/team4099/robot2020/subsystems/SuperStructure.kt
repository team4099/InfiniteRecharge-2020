package com.team4099.robot2020.subsystems

import com.team4099.lib.drive.DriveSignal
import com.team4099.lib.loop.Loop
import com.team4099.robot2020.config.Constants.SuperStructure

object SuperStructure : Loop {
    // TODO: Do this
    private var hasStateChanged = false

    private var currentWantedState = SuperStructure.States.DEFAULT
    private var currentRobotState = SuperStructure.States.DEFAULT

    override fun onStart(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        when (currentWantedState) {
            SuperStructure.States.IDLE -> {
                Drive.setOpenLoop(DriveSignal.NEUTRAL)
            }
            SuperStructure.States.SPIN_UP_FLYWHEEL -> {}
            SuperStructure.States.SHOOT -> {}
            SuperStructure.States.UNJAM_SHOOTER -> {}
            SuperStructure.States.CLIMB -> {}
            SuperStructure.States.INTAKE -> {}
            SuperStructure.States.UNJAM_INTAKE -> {}
            SuperStructure.States.FEED -> {}
            SuperStructure.States.UNJAM_FEEDER -> {}
        }
    }

    public fun setState(wantedState: SuperStructure.States) {
        currentWantedState = wantedState
    }

    override fun onStop(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}