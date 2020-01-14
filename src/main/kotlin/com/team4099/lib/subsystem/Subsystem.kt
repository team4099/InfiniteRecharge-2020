package com.team4099.lib.subsystem

import com.team4099.lib.loop.Loop
import com.team4099.lib.config.Configurable

/**
 * Represents a subsystem of the robot.
 */
interface Subsystem : Loop {
    /**
     * A list of objects that can be configured over SmartDashboard.
     */
    val configurableProperties: List<Configurable<Number>>

    /**
     * Output telemetry from this subsystem. Typically will be data sent to SmartDashboard.
     */
    fun outputTelemetry()

    /**
     * Verify the function of the subsystem. Should be called when the robot is in test mode.
     */
    fun checkSystem()

    /**
     * Register logging parameters for this subsystem.
     */
    fun registerLogging()

    /**
     * Reset the subsystem's sensors to a zero state.
     */
    fun zeroSensors()
}
