package com.team4099.lib.sim.motors

import kotlin.math.PI
import kotlin.math.pow

object SimDCMotorFactory {

    /**
     * Convenience method for creating 775Pro SimDCMotor with correct motor constants
     * @param gearing Gear ratio between motor and mechanism (<1 means reduction)
     * @param encoderRatio Gear ratio between motor and encoder (<1 means reduction)
     */
    fun create775Pro(gearing: Double, efficiency: Double, encoderRatio: Double): SimDCMotor {
        return createMotor(0.71, 134.0, 18730.0, 12.0, 0.362874, 0.0423, gearing, efficiency, encoderRatio)
    }

    /**
     * Convenience method for creating Neo550 SimDCMotor with correct motor constants
     * @param gearing Gear ratio between motor and mechanism (<1 means reduction)
     * @param encoderRatio Gear ratio between motor and encoder (<1 means reduction)
     */
    fun createNeo550(gearing: Double, efficiency: Double, encoderRatio: Double): SimDCMotor {
        // TODO: correct Neo 550 mass and diameter
        return createMotor(0.97, 100.0, 11000.0, 12.0, 0.362874, 0.0423, gearing, efficiency, encoderRatio)
    }

    /**
     * Convenience method for creating BAG SimDCMotor with correct motor constants
     * @param gearing Gear ratio between motor and mechanism (<1 means reduction)
     * @param encoderRatio Gear ratio between motor and encoder (<1 means reduction)
     */
    fun createBAG(gearing: Double, efficiency: Double, encoderRatio: Double): SimDCMotor {
        // TODO: correct BAG mass and diameter
        return createMotor(0.43, 53.0, 13180.0, 12.0, 0.362874, 0.0423, gearing, efficiency, encoderRatio)
    }

    /**
     * Convenience method for creating NEO SimDCMotor with correct motor constants
     * @param gearing Gear ratio between motor and mechanism (<1 means reduction)
     * @param encoderRatio Gear ratio between motor and encoder (<1 means reduction)
     */
    fun createNeo(gearing: Double, efficiency: Double, encoderRatio: Double): SimDCMotor {
        // TODO: figure out actual diameter and mass (currently copied from https://github.com/cpostbitbuckets/FRCSim/blob/master/simulator/src/main/java/frc/robot/simulator/sim/motors/MotorStore.java)
        return createMotor(3.36, 166.0, 5880.0, 12.0, 0.425, 0.06, gearing, efficiency, encoderRatio)
    }

    /**
     * Convenience method for creating Falcon 500 SimDCMotor with correct motor constants
     * @param gearing Gear ratio between motor and mechanism (<1 means reduction)
     * @param encoderRatio Gear ratio between motor and encoder (<1 means reduction)
     */
    fun createFalcon(gearing: Double, efficiency: Double, encoderRatio: Double): SimDCMotor {
        // TODO: figure out actual diameter and mass (currently copied from https://github.com/cpostbitbuckets/FRCSim/blob/master/simulator/src/main/java/frc/robot/simulator/sim/motors/MotorStore.java)
        return createMotor(4.69, 257.0, 6380.0, 12.0, 0.425, 0.06, gearing, efficiency, encoderRatio)
    }

    /**
     * Convenience method for creating SimDCMotor with standard Vex PRO DC Motor Testing specs (in correct units)
     * @param stallTorque Stall torque of motor in N*m
     * @param stallCurrent Stall current of motor in A
     * @param freeSpeed Free speed of motor in RPM
     * @param voltage Voltage for given stall torque, current, and free speed in V
     * @param mass Mass of rotor in kg
     * @param diameter Diameter of rotor in m
     * @param gearing Gear ratio between motor and mechanism (<1 means reduction)
     * @param encoderRatio Gear ratio between motor and encoder (<1 means reduction)
     *
     * @return SimDCMotor with given specs
     */
    fun createMotor(stallTorque: Double, stallCurrent: Double, freeSpeed: Double, voltage: Double, mass: Double, diameter: Double, gearing: Double, efficiency: Double, encoderRatio: Double): SimDCMotor {
        val kT = stallTorque / stallCurrent * gearing * efficiency
        val kV = freeSpeed * 2 * PI / 60 / voltage / gearing
        val resistance = voltage / stallCurrent
        // TODO: figure out why you multiply inertia by gearing^2
        val inertia = 0.5 * mass * (diameter / 2).pow(2) * gearing.pow(2)
        return SimDCMotor(kT, kV, resistance, inertia, encoderRatio)
    }
}