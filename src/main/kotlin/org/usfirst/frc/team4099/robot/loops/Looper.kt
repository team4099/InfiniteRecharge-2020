package org.usfirst.frc.team4099.robot.loops

import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.util.CrashTrackingRunnable
import org.usfirst.frc.team4099.robot.Constants
import java.util.*

class Looper(private val name: String) {

    private var running: Boolean = false

    private val notifier: Notifier
    private val taskRunningLock = Any()

    private val loops: MutableList<Loop> = ArrayList()

    private var timestamp = 0.0
    private var dt = 0.0

    // define the thread (runnable) that will be repeated continuously
    private val runnable = object : CrashTrackingRunnable() {
        override fun runCrashTracked() {
            synchronized(taskRunningLock) {
                if (running) {
                    val now = Timer.getFPGATimestamp()
                    for (loop in loops) {
                        loop.onLoop()
                    }
                    dt = now - timestamp
                    timestamp = now
                }
            }
        }
    }

    init {
        notifier = Notifier(runnable)
        running = false
    }

    @Synchronized
    fun register(loop: Loop) {
        synchronized(taskRunningLock) {
            loops.add(loop)
        }
    }

    @Synchronized
    fun start() {
        if (!running) {
            println("Starting looper: " + name)
            synchronized(taskRunningLock) {
                timestamp = Timer.getFPGATimestamp()
                for (loop in loops) {
                    loop.onStart()
                }

                running = true
            }
            notifier.startPeriodic(kPeriod)
        }
    }

    @Synchronized
    fun stop() {
        if (running) {
            println("Stopping looper: " + name)
            notifier.stop()

            synchronized(taskRunningLock) {
                running = false

                for (loop in loops) {
                    println("Stopping " + loop) // give the loops a name
                    loop.onStop()
                }
            }
        }
    }

    fun outputToSmartDashboard() {
        SmartDashboard.putNumber("looper_dt ($name)", dt)
    }

    companion object {

        private val kPeriod = Constants.Loopers.LOOPER_DT
    }

}
