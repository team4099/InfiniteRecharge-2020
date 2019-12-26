package org.usfirst.frc.team4099.lib.util

import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType

class LazySparkMax(deviceNumber: Int) : CANSparkMax(deviceNumber, MotorType.kBrushless) {
    private var lastSet = Double.NaN
    private var lastControlMode: ControlType = ControlType.kVoltage
    val pidController: CANPIDController = super.getPIDController()

    fun set(controlMode: ControlType, value: Double) {
        if (value != lastSet || controlMode != lastControlMode) {
            lastSet = value
            lastControlMode = controlMode
            pidController.setReference(value, controlMode)
        }
    }
}