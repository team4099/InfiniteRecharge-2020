package org.usfirst.frc.team4099.lib.util.conversions

import com.fasterxml.jackson.core.sym.NameN
import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType
import com.revrobotics.SparkMax

class LazySparkMax(deviceNumber: Int) : CANSparkMax(deviceNumber, MotorType.kBrushless) {
    private var lastSet = Double.NaN
    private var lastControlMode: ControlType = ControlType.kVoltage

    fun set(controlMode: ControlType, value: Double) {
        if (value != lastSet || controlMode != lastControlMode) {
            lastSet = value
            lastControlMode = controlMode
            super.getPIDController().setReference(value, controlMode)

        }
    }
}