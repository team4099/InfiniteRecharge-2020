package org.usfirst.frc.team4099.auto.actions

/**
 * Created by plato2000 on 2/13/17.
 */
interface Action {

    /**
     * Returns whether or not the code has finished execution. When implementing
     * this interface, this method is used by the runAction method every cycle
     * to know when to stop running the action
     *
     * @return boolean
     */
    fun isFinished() = false

    /**
     * Called by runAction in AutoModeBase iteratively until isFinished returns
     * true. Iterative logic lives in this method
     */
    fun update()

    /**
     * Run code once when the action finishes, usually for clean up
     */
    fun done()

    /**
     * Run code once when the action is started, for set up
     */
    fun start()
}
