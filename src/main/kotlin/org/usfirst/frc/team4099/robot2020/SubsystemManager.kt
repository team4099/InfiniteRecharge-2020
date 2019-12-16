package org.usfirst.frc.team4099.robot2020

import com.team2363.logger.HelixLogger
import org.usfirst.frc.team4099.lib.loop.Loop
import org.usfirst.frc.team4099.lib.subsystem.Subsystem

object SubsystemManager {
    val subsystems = mutableListOf<Subsystem>()
    val enabledLoop = object : Loop {
        override fun onStart(timestamp: Double) {
            subsystems.forEach { it.loop.onStart(timestamp) }
        }

        override fun onLoop(timestamp: Double) {
            subsystems.forEach { it.loop.onLoop(timestamp) }
            outputTelemetry()
        }

        override fun onStop(timestamp: Double) {
            subsystems.forEach { it.loop.onStop(timestamp) }
        }
    }

    val disabledLoop = object : Loop {
        override fun onStart(timestamp: Double) {}

        override fun onLoop(timestamp: Double) {
            outputTelemetry()
        }

        override fun onStop(timestamp: Double) {}
    }

    fun register(subsystem: Subsystem) {
        subsystems.add(subsystem)
    }

    fun register(subsystemColl: Collection<Subsystem>) {
        subsystems.addAll(subsystemColl)
    }

    fun outputTelemetry() {
        subsystems.forEach { it.outputToSmartDashboard() }
        HelixLogger.saveLogs()
    }

    fun stop() {
        subsystems.forEach { it.stop() }
    }
}
