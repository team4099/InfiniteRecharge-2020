package org.usfirst.frc.team4099.lib.util

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import kotlin.Double.Companion.NaN

class LazyTalonSRX(deviceNumber: Int) : TalonSRX(deviceNumber) {
    private var lastSet = NaN
    private var lastControlMode = ControlMode.PercentOutput

    override fun set(controlMode: ControlMode, value: Double) {
        if (value != lastSet || getControlMode() !== lastControlMode) {
            lastSet = value
            lastControlMode = getControlMode()
            super.set(controlMode, value)
        }
    }
}
