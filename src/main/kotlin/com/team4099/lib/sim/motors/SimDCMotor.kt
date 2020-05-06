package com.team4099.lib.sim.motors

import kotlin.math.pow
import kotlin.math.sign

/**
 * Simulates motor physics of DC motor + simple gearbox
 * Based on https://github.com/Team254/Sim-FRC-2015/blob/master/src/com/team254/frc2015/sim/DCMotor.java
 *
 * @param kT The torque constant of the motor (N*m/A)
 * @param kV The free speed of the motor (rad/s/V)
 * @param resistance Internal resistance of motor (Ohms)
 * @param inertia Moment of inertia of rotor of motor (kg*m^2)
 * @param encoderRatio Gear ratio between motor and encoder (unitless, <1 means gearing down)
 */
class SimDCMotor(val kT: Double, val kV: Double, val resistance: Double, val inertia: Double, val encoderRatio: Double) {
    var current: Double = 0.0
        private set

    var velocity: Double = 0.0
        private set
    val encoderVelocity: Double
        get() {
            return velocity * encoderRatio
        }

    var position: Double = 0.0
    val encoderPosition: Double
        get() {
            return position * encoderRatio
        }

    fun step(appliedVoltage: Double, extMOI: Double, extTorque: Double, timestep: Double) {
        val systemMomentOfInertia = inertia + extMOI
        val acceleration = (appliedVoltage - velocity / kV) * kT / (resistance * systemMomentOfInertia) + extTorque / systemMomentOfInertia
        velocity += acceleration * timestep
        position += velocity * timestep + 0.5 * acceleration * timestep.pow(2)
        current = systemMomentOfInertia * acceleration * sign(appliedVoltage) / kT
    }

    fun reset() {
        current = 0.0
        velocity = 0.0
        position = 0.0
    }
}