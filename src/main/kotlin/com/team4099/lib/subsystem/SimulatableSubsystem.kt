package com.team4099.lib.subsystem

interface SimulatableSubsystem: Subsystem {
    fun physicsStep(timestep: Double)
    fun electronicsStep(timestep: Double)
    fun resetSim()
}