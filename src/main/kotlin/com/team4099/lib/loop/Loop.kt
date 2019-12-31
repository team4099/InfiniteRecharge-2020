package com.team4099.lib.loop

/**
 * Represents a function that runs in a loop.
 */
interface Loop {
    /**
     * Called at the start of the loop.
     *
     * @param timestamp The time at the start of the loop. Value originates from Timer.getFPGATimestamp.
     */
    fun onStart(timestamp: Double)

    /**
     * Called at a each iteration of the loop.
     *
     * @param timestamp The time at this iteration of the loop. Value originates from Timer.getFPGATimestamp.
     * @param dT The interval since the last time that this function was called.
     */
    fun onLoop(timestamp: Double, dT: Double)

    /**
     * Called at the end of the loop.
     *
     * @param timestamp The time at the start of the loop. Value originates from Timer.getFPGATimestamp.
     */
    fun onStop(timestamp: Double)
}
