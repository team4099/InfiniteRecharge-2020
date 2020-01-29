package com.team4099.robot2020.statemachines

object FeederStatemachine {
    enum class FeederState {
        OFF,
        HOLD,
        FEED_IN
    }

    var wantedFeederState: FeederState = FeederState.OFF
        set (value) {
            when (wantedFeederState) {
                FeederState.OFF -> {

                }
                FeederState.HOLD -> {

                }
                FeederState.FEED_IN -> {

                }
            }
            field = value
        }
}