package com.team4099.lib.motorcontroller

import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType

class LazySparkMax(deviceNumber: Int) : CANSparkMax(deviceNumber, MotorType.kBrushless) {
    private var lastSet = Double.NaN
    private var lastFF = Double.NaN
    private var lastControlMode: ControlType = ControlType.kVoltage
    private var lastPIDSlot = 0

    fun set(controlMode: ControlType, value: Double) {
//        if (value != lastSet || controlMode != lastControlMode) {
            lastSet = value
            lastControlMode = controlMode
            pidController.setReference(value, controlMode)
//        }
    }

    fun set(controlMode: ControlType, value: Double, pidSlot: Int) {
//        if (value != lastSet || controlMode != lastControlMode) {
            lastSet = value
            lastControlMode = controlMode
            lastPIDSlot = pidSlot
            pidController.setReference(value, controlMode, pidSlot)
//        }
    }
    fun set(controlMode: ControlType, value: Double, pidSlot: Int, ff: Double) {
//        if (value != lastSet || controlMode != lastControlMode || ff != lastFF) {
            lastSet = value
            lastControlMode = controlMode
            lastPIDSlot = pidSlot
            lastFF = ff
            pidController.setReference(value, controlMode, pidSlot, ff)
//        }
    }
}
