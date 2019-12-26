package org.usfirst.frc.team4099.lib.util

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.VictorSPX

class LazyVictorSPX(deviceNumber: Int) : VictorSPX(deviceNumber) {
    private var lastSet = Double.NaN
    private var lastControlMode: ControlMode = ControlMode.Disabled

    override fun set(controlMode: ControlMode, value: Double) {
        if (value != lastSet || controlMode != lastControlMode) {
            lastSet = value
            lastControlMode = controlMode
            super.set(controlMode, value)
        }
    }
}