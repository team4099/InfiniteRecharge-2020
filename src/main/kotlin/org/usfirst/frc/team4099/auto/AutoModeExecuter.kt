package org.usfirst.frc.team4099.auto

import org.usfirst.frc.team4099.auto.modes.AutoModeBase
import org.usfirst.frc.team4099.lib.util.CrashTrackingRunnable

/**
 * Created by plato2000 on 2/13/17.
 */
class AutoModeExecuter {
    private var autoMode: AutoModeBase? = null
    private var thread: Thread? = null

    fun setAutoMode(newAutoMode: AutoModeBase) {
        autoMode = newAutoMode
    }

    fun start() {
        if (thread == null) {
            thread = Thread(object : CrashTrackingRunnable() {
                override fun runCrashTracked() {
                    autoMode?.run()
                }
            })
            thread?.start()
        }

    }

    fun stop() {
        autoMode?.stop()
        thread = null
    }

}
