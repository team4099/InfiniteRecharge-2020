package org.usfirst.frc.team4099.lib.auto

import com.team2363.logger.HelixEvents
import edu.wpi.first.wpilibj.Timer
import org.usfirst.frc.team4099.robot2020.auto.actions.WaitAction

/**
 * An autonomous mode. Represents a routine that runs various actions.
 * @param delay The amount of time (in seconds) to wait before running the [routine].
 * @param targetDt The amount of time (in seconds) to target between
 * each loop of actions within this mode.
 */
abstract class AutoMode(private val delay: Double, private val targetDt: Double) {
    /**
     * Represents if the autonomous mode is currently running.
     */
    var running = false
        private set

    private val runningWithThrow: Boolean
        /**
         * Returns the value of running. Throws if the autonomous mode has
         * stopped in the middle of execution.
         */
        @Throws(AutoModeEndedException::class)
        get() {
            if (!running) {
                throw AutoModeEndedException()
            }
            return running
        }

    /**
     * The routine run by this autonomous mode. Typically calls [runAction] one
     * or more times with actions for the robot to perform.
     */
    @Throws(AutoModeEndedException::class)
    abstract fun routine()

    /**
     * Run this autonomous mode. Waits [delay] seconds and then calls [routine].
     */
    fun run() {
        running = true
        try {
            if (delay >= 0) runAction(WaitAction(delay))
            routine()
        } catch (e: AutoModeEndedException) {
            HelixEvents.addEvent("AUTONOMOUS", "Auto mode done, ended early")
            return
        }

        stop()
        HelixEvents.addEvent("AUTONOMOUS", "Auto mode done")
    }

    /**
     * Called when the routine is finished or the mode is stopped early by [stop].
     */
    abstract fun done()

    /**
     * Stops the autonomous mode.
     */
    fun stop() {
        running = false
        done()
    }

    /**
     * Run an action for this autonomous mode. Typically called by [runAction].
     * @param action The action to be run.
     */
    @Throws(AutoModeEndedException::class)
    internal fun runAction(action: Action) {
        runningWithThrow
        action.onStart(Timer.getFPGATimestamp())
        HelixEvents.addEvent("AUTONOMOUS", "Beginning autonomous action $action")
        var now = Timer.getFPGATimestamp()
        while (runningWithThrow && !action.isFinished(now)) {
            now = Timer.getFPGATimestamp()
            action.onLoop(now, targetDt)
            val waitTime = (targetDt * 1000.0).toLong()
            try {
                Thread.sleep(waitTime)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        action.onStop(Timer.getFPGATimestamp())
    }
}
