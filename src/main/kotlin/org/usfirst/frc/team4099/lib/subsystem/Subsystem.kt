package org.usfirst.frc.team4099.lib.subsystem

import org.usfirst.frc.team4099.lib.loop.Loop

/**
 * Represents a subsystem of the robot.
 */
abstract class Subsystem : Loop {
    /**
     * Output telemetry from this subsystem. Typically will be data sent to SmartDashboard.
     */
    abstract fun outputTelemetry()

    /**
     * Verify the function of the subsystem. Should be called when the robot is in test mode.
     */
    abstract fun checkSystem()

    /**
     * Register logging parameters for this subsystem.
     */
    abstract fun registerLogging()

    /**
     * Reset the subsystem's sensors to a zero state.
     */
    open fun zeroSensors() {}
    open fun readPeriodicInputs() {}
    open fun writePeriodicOutputs() {}
}
