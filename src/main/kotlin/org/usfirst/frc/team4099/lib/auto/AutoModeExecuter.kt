package org.usfirst.frc.team4099.lib.auto

import org.usfirst.frc.team4099.lib.util.CrashTrackingRunnable

/**
 * Created by plato2000 on 2/13/17.
 */
class AutoModeExecuter {
    var autoMode: AutoMode? = null
    private lateinit var thread: Thread

    fun start() {
        thread = Thread(object : CrashTrackingRunnable() {
            override fun runCrashTracked() {
                autoMode?.run()
            }
        })
        thread.start()
    }

    fun stop() {
        autoMode?.stop()
    }
}
