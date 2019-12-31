package com.team4099.lib.auto

/**
 * Executes one action at a time. Useful as a member of [ParallelAction].
 */
class SeriesAction(actions: List<Action>) : Action {
    private var currentAction: Action? = null
    private val remainingActions = mutableListOf<Action>()

    init {
        for (action in actions) {
            remainingActions.add(action)
        }
    }

    override fun isFinished(timestamp: Double): Boolean {
        return remainingActions.isEmpty() && currentAction == null
    }

    override fun onStart(timestamp: Double) {}

    override fun onLoop(timestamp: Double, dT: Double) {
        if (currentAction == null) {
            if (remainingActions.isEmpty()) {
                return
            }
            currentAction = remainingActions.removeAt(0)
            currentAction?.onStart(timestamp)
        }
        currentAction?.onLoop(timestamp, dT)
        if (currentAction?.isFinished(timestamp) == true) {
            currentAction?.onStop(timestamp)
            currentAction = null
        }
    }

    override fun onStop(timestamp: Double) {}
}
