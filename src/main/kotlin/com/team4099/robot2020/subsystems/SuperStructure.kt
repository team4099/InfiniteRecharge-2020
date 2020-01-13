package com.team4099.robot2020.subsystems

import com.team4099.lib.loop.Loop
import com.team4099.robot2020.config.Constants.SuperStructure

object SuperStructure : Loop {
    // TODO: Do this
    private var hasStateChanged = false

    private lateinit var currentWantedState: SuperStructure.WantedState

    override fun onStart(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        when (currentWantedState) {
            SuperStructure.WantedState.IDLE -> {

            }
            SuperStructure.WantedState.SPIN_UP_FLYWHEEL -> {

            }
            SuperStructure.WantedState.SHOOT -> {}
            SuperStructure.WantedState.UNJAM_SHOOTER -> {}
            SuperStructure.WantedState.CLIMB -> {}
            SuperStructure.WantedState.INTAKE -> {}
            SuperStructure.WantedState.UNJAM_INTAKE -> {}
            SuperStructure.WantedState.FEED -> {}
            SuperStructure.WantedState.UNJAM_FEEDER -> {}

        }
    }

    public fun setState(wantedState: SuperStructure.WantedState) {
        currentWantedState = wantedState
    }

    override fun onStop(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}