package org.usfirst.frc.team4099.lib.drive

/**
 * Represents a pair of motor powers as well as the brake mode for a differential drivetrain.
 *
 * @param leftMotor The power applied to the left side of the drivetrain.
 * @param rightMotor The power applied to the right side of the drivetrain.
 * @param brakeMode True if the motor controllers should brake with zero power,
 * false if they should coast.
 */
class DriveSignal constructor(var leftMotor: Double, var rightMotor: Double, var brakeMode: Boolean = false) {
    override fun toString(): String {
        return "L: $leftMotor, R: $rightMotor, Brake: $brakeMode"
    }

    companion object {
        var NEUTRAL = DriveSignal(0.0, 0.0)
        var BRAKE = DriveSignal(0.0, 0.0, true)
    }
}
