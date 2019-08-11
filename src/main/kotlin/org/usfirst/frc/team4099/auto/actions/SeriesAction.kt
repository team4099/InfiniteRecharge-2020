package org.usfirst.frc.team4099.auto.actions

import java.util.*

/**
 * Executes one action at a time. Useful as a member of [ParallelAction]
 */
class SeriesAction(actions: List<Action>) : Action {

    private var currentAction: Action? = null
    private val remainingActions: ArrayList<Action> = ArrayList(actions.size)

    init {
        for (action in actions) {
            remainingActions.add(action)
        }
        currentAction = null
    }

    override fun isFinished(): Boolean {
        return remainingActions.isEmpty() && currentAction == null
    }

    override fun start() {}

    override fun update() {
        if (currentAction == null) {
            if (remainingActions.isEmpty()) {
                return
            }
            currentAction = remainingActions.removeAt(0)
            currentAction?.start()
        }
        currentAction?.update()
        if (currentAction?.isFinished() == true) { // have to write that explicitly because its a nullable boolean
            currentAction?.done()
            currentAction = null
        }
    }

    override fun done() {}
}
