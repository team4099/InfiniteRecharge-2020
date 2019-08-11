package org.usfirst.frc.team4099.lib.drive

class DriveSignal constructor(var leftMotor: Double, var rightMotor: Double, var brakeMode: Boolean = false) {

    override fun toString(): String {
        return "L: $leftMotor, R: $rightMotor, Brake: $brakeMode"
    }

    companion object {

        var NEUTRAL = DriveSignal(0.0, 0.0)
        var BRAKE = DriveSignal(0.0, 0.0, true)
    }
}
