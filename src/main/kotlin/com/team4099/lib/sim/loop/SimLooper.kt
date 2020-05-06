package com.team4099.lib.sim.loop

import com.team4099.lib.loop.Loop
import com.team4099.lib.loop.Looper
import com.team4099.lib.subsystem.SimulatableSubsystem

class SimLooper(name: String, simDt: Double, val physicsStep: Double, val electronicsStep: Double = 0.01): Looper(name, simDt) {
    val loops = mutableListOf<Loop>()
    val subsystems = mutableListOf<SimulatableSubsystem>()

    var timestamp = 0.0

    var lastElectronicsStep = 0.0
    var lastPhysicsStep = 0.0
    var lastLoopStep = 0.0

    override fun register(loop: Loop) {
        loops.add(loop)
    }

    fun registerSimSubsystem(simSubsystem: SimulatableSubsystem) {
        subsystems.add(simSubsystem)
    }

    override fun start() {
        loops.forEach { it.onStart(timestamp) }
    }

    override fun stop() {
        loops.forEach { it.onStop(timestamp) }
    }

    private fun runPhysicsStep() {
        subsystems.forEach {
            it.physicsStep(timestamp - lastPhysicsStep)
        }
        lastPhysicsStep = timestamp
    }

    private fun runElectronicsStep() {
        subsystems.forEach {
            it.electronicsStep(timestamp - lastElectronicsStep)
        }
        lastElectronicsStep = timestamp
    }

    private fun runLoopStep() {
        loops.forEach {
            it.onLoop(timestamp, timestamp - lastLoopStep)
        }
    }

    fun runForTime(timeSecs: Double) {
        val endTimestamp = timestamp + timeSecs
        while (timestamp < endTimestamp) {
            if (timestamp - lastLoopStep >= targetDt) {
                runLoopStep()
            }
            if (timestamp - lastElectronicsStep >= electronicsStep) {
                runElectronicsStep()
            }
            runPhysicsStep()

            timestamp += physicsStep
        }
    }

    fun reset() {
        timestamp = 0.0
        subsystems.forEach {
            it.resetSim()
        }
    }
}