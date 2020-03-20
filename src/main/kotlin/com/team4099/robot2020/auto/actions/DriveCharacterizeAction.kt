package com.team4099.robot2020.auto.actions

import com.team4099.lib.auto.Action
import com.team4099.lib.drive.DriveSignal
import com.team4099.robot2020.subsystems.Drive
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.RobotController
import java.lang.Math.toRadians

@Suppress("MagicNumber")
class DriveCharacterizeAction : Action {
    private val autoSpeedEntry: NetworkTableEntry = NetworkTableInstance.getDefault().getEntry("/robot/autospeed")
    private val telemetryEntry: NetworkTableEntry = NetworkTableInstance.getDefault().getEntry("/robot/telemetry")
    private val rotateEntry: NetworkTableEntry = NetworkTableInstance.getDefault().getEntry("/robot/rotate")
    private var numberArray = arrayOfNulls<Number>(10)

    override fun isFinished(timestamp: Double): Boolean {
        return false
    }

    override fun onStart(timestamp: Double) {
        // Set the update rate instead of using flush because of a ntcore bug
        // -> probably don't want to do this on a robot in competition
        NetworkTableInstance.getDefault().setUpdateRate(0.010)
        Drive.zeroSensors()
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        val autoSpeed = autoSpeedEntry.getDouble(0.0)
        Drive.setOpenLoop(DriveSignal(
            (if (rotateEntry.getBoolean(false)) -1 else 1) * autoSpeed,
            autoSpeed
        ))

        numberArray[0] = timestamp
        numberArray[1] = RobotController.getBatteryVoltage()
        numberArray[2] = autoSpeed
        numberArray[3] = Drive.leftMasterOutputVoltage
        numberArray[4] = Drive.rightMasterOutputVoltage
        numberArray[5] = Drive.leftDistanceMeters
        numberArray[6] = Drive.rightDistanceMeters
        numberArray[7] = Drive.leftVelocityMetersPerSec
        numberArray[8] = Drive.rightVelocityMetersPerSec
        numberArray[9] = toRadians(Drive.angle)

        telemetryEntry.setNumberArray(numberArray)
    }

    override fun onStop(timestamp: Double) {
        Drive.setOpenLoop(DriveSignal.NEUTRAL)
        NetworkTableInstance.getDefault().setUpdateRate(0.1)
    }
}
