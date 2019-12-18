package org.usfirst.frc.team4099.lib.auto

import com.team2363.logger.HelixEvents
import edu.wpi.first.wpilibj.Timer
import org.usfirst.frc.team4099.robot2020.auto.actions.WaitAction

abstract class AutoMode(private val delay: Double, private val targetDt: Double) {
    var running = false
    private val runningWithThrow: Boolean
        @Throws(AutoModeEndedException::class)
        get() {
            if (!running) {
                throw AutoModeEndedException()
            }
            return running
        }

    @Throws(AutoModeEndedException::class)
    abstract fun routine()

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

    abstract fun done()

    fun stop() {
        running = false
        done()
    }

    @Throws(AutoModeEndedException::class)
    fun runAction(action: Action) {
        runningWithThrow
        action.onStart(Timer.getFPGATimestamp())
        HelixEvents.addEvent("AUTONOMOUS", "Beginning autonomous action")
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
