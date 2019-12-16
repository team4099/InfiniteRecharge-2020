package org.usfirst.frc.team4099.lib.auto

import org.usfirst.frc.team4099.lib.loop.Loop

/**
 * Created by plato2000 on 2/13/17.
 */
interface Action : Loop {

    /**
     * Returns whether or not the code has finished execution. When implementing
     * this interface, this method is used by the runAction method every cycle
     * to know when to stop running the action
     *
     * @return boolean
     */
    fun isFinished(timestamp: Double): Boolean
}
