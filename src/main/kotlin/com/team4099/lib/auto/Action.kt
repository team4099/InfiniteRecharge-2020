package com.team4099.lib.auto

import com.team4099.lib.loop.Loop

/**
 * Represents an action that can be run by an autonomous mode.
 */
interface Action : Loop {
    /**
     * Returns whether or not this action has finished execution. When implementing
     * this interface, this method is used by the runAction method every cycle
     * to know when to stop running the action.
     *
     * @param timestamp The time at which this action is being called. Value originates from Timer.getFPGATimestamp.
     * @return True if the action has finished execution.
     */
    fun isFinished(timestamp: Double): Boolean
}
