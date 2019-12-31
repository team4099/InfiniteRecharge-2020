package com.team4099.lib.auto

import com.team4099.lib.logging.CrashTrackingRunnable

/**
 * Executes an autonomous mode within a [CrashTrackingRunnable].
 * @param autoMode The mode to execute
 */
class AutoModeExecuter(private val autoMode: AutoMode) {
    private var thread: Thread = Thread(object : CrashTrackingRunnable("AutoModeExecuter") {
        override fun runCrashTracked() {
            autoMode.run()
        }
    })

    /**
     * Starts the autonomous mode.
     */
    fun start() {
        thread.start()
    }

    /**
     * Stops the autonomous mode.
     */
    fun stop() {
        autoMode.stop()
    }
}
