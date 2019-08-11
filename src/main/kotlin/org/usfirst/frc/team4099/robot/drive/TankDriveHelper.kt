package org.usfirst.frc.team4099.robot.drive

import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.lib.joystick.JoystickUtils

/**
 * Tank Drive
 * Left joystick controls the left motor speed.
 * Right joystick controls the right motor speed.
 */

class TankDriveHelper {

    private val mSignal = DriveSignal(0.0, 0.0)

    fun tankDrive(left: Double, right: Double): DriveSignal {
        var left = left
        var right = right
        left = JoystickUtils.deadband(left, kLeftDeadband)
        right = JoystickUtils.deadband(right, kRightDeadband)

        mSignal.leftMotor = left
        mSignal.rightMotor = right

        return mSignal
    }

    companion object {

        val instance = TankDriveHelper()
        private val kLeftDeadband = 0.05
        private val kRightDeadband = 0.05
    }

}
