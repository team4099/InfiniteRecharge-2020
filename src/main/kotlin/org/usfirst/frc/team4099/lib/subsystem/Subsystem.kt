package org.usfirst.frc.team4099.lib.subsystem

import org.usfirst.frc.team4099.lib.loop.Loop

abstract class Subsystem {
    abstract val loop: Loop
    abstract fun outputToSmartDashboard()
    abstract fun stop()
    abstract fun checkSystem()
    open fun zeroSensors() {}
    open fun readPeriodicInputs() {}
    open fun writePeriodicOutputs() {}
}
