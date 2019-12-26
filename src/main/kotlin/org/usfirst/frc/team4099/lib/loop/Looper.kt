package org.usfirst.frc.team4099.lib.loop

import com.team2363.logger.HelixEvents
import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.Timer
import org.usfirst.frc.team4099.lib.util.CrashTrackingRunnable

/**
 * Calls multiple [Loop] objects at a target interval.
 *
 * @param name A human readable name for this looper.
 * @param targetDt The target time (in seconds) between iterations of the [Loop]s.
 */
open class Looper(private val name: String, val targetDt: Double) {
    // define the thread (runnable) that will be repeated continuously
    private val runnable = object : CrashTrackingRunnable("Looper $name") {
        override fun runCrashTracked() {
            synchronized(taskRunningLock) {
                if (running) {
                    val now = Timer.getFPGATimestamp()
                    dt = now - timestamp
                    loops.forEach { it.onLoop(timestamp, dt) }
                    timestamp = now
                }
            }
        }
    }

    private var running = false

    private val notifier = Notifier(runnable)
    private val taskRunningLock = Any()

    private val loops = mutableListOf<Loop>()

    private var timestamp = 0.0
    var dt = 0.0
        private set

    /**
     * Add a loop to the collection of [Loop]s
     *
     * @param loop The loop to add.
     */
    @Synchronized
    fun register(loop: Loop) {
        synchronized(taskRunningLock) {
            loops.add(loop)
        }
    }

    /**
     * Start the looper.
     */
    @Synchronized
    fun start() {
        if (!running) {
            HelixEvents.addEvent("LOOPER $name", "Starting looper")
            synchronized(taskRunningLock) {
                timestamp = Timer.getFPGATimestamp()
                loops.forEach { it.onStart(timestamp) }

                running = true
            }
            notifier.startPeriodic(targetDt)
        }
    }

    /**
     * Stop the looper.
     */
    @Synchronized
    open fun stop() {
        if (running) {
            HelixEvents.addEvent("LOOPER $name", "Stopping looper")
            notifier.stop()

            synchronized(taskRunningLock) {
                running = false
                timestamp = Timer.getFPGATimestamp()

                loops.forEach { it.onStop(timestamp) }
            }
        }
    }
}
