package org.usfirst.frc.team4099.lib.loop

interface Loop {
    fun onStart(timestamp: Double)
    fun onLoop(timestamp: Double, dT: Double)
    fun onStop(timestamp: Double)
}
