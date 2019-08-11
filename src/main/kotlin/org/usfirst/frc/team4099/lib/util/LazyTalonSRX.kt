package org.usfirst.frc.team4099.lib.util

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX

class LazyTalonSRX(deviceNumber: Int) : TalonSRX(deviceNumber)  {
    protected var mLastSet = java.lang.Double.NaN
    protected var mLastControlMode: ControlMode = ControlMode.PercentOutput

    override fun set(controlMode: ControlMode, value: Double) {
        if (value != mLastSet || getControlMode() !== mLastControlMode) {
            mLastSet = value
            mLastControlMode = getControlMode()
            super.set(controlMode, value)
        }
    }
}