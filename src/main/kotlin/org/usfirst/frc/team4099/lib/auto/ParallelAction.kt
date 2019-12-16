package org.usfirst.frc.team4099.lib.auto

/**
 * Composite action, running all sub-actions at the same time All actions are
 * started then updated until all actions report being done.
 *
 * @param actions List of Action objects
 */
class ParallelAction(actions: List<Action>) : Action {
    private val actions = mutableListOf<Action>()

    init {
        for (action in actions) {
            this.actions.add(action)
        }
    }

    override fun isFinished(timestamp: Double): Boolean {
        var allFinished = true
        for (action in actions) {
            if (!action.isFinished(timestamp)) {
                allFinished = false
            }
        }
        return allFinished
    }

    override fun onLoop(timestamp: Double) {
        for (action in actions) {
            action.onLoop(timestamp)
        }
    }

    override fun onStop(timestamp: Double) {
        for (action in actions) {
            action.onStop(timestamp)
        }
    }

    override fun onStart(timestamp: Double) {
        for (action in actions) {
            action.onStart(timestamp)
        }
    }
}
