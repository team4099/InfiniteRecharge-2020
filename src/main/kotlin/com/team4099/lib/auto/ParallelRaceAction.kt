package com.team4099.lib.auto

/**
 * Composite action, running all sub-actions at the same time. All actions are
 * started then updated until all actions report being done.
 *
 * @param actions List of Action objects
 */
class ParallelRaceAction(actions: List<Action>) : Action {
    private val actions = ArrayList(actions)

    override fun isFinished(timestamp: Double): Boolean {
        var anyFinished = false
        for (action in actions) {
            if (action.isFinished(timestamp)) {
                anyFinished = true
            }
        }
        return anyFinished
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        actions.forEach { it.onLoop(timestamp, dT) }
    }

    override fun onStop(timestamp: Double) {
        actions.forEach { it.onStop(timestamp) }
    }

    override fun onStart(timestamp: Double) {
        actions.forEach { it.onStart(timestamp) }
    }
}
