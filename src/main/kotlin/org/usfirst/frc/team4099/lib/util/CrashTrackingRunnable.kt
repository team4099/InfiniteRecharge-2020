package org.usfirst.frc.team4099.lib.util

abstract class CrashTrackingRunnable : Runnable {

    override fun run() {
        try {
            runCrashTracked()
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("CrTrRu.run", t)
            throw t
        }

    }

    abstract fun runCrashTracked()
}
