package com.team4099.lib.subsystem

import com.team4099.lib.loop.Loop
import com.team4099.lib.config.Configurable

/**
 * Represents a subsystem of the robot.
 */
abstract class Subsystem : Loop {
    /**
     * A list of objects that can be configured over SmartDashboard.
     */
    open val configurableProperties = listOf<Configurable<out Number>>()

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
    abstract fun zeroSensors()
}
