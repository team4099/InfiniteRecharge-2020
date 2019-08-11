package org.usfirst.frc.team4099.lib.util

class SignalTable {
    @JvmField var sensorPosition: Int = 0
    @JvmField var sensorVelocity: Int = 0
    @JvmField var closedLoopError: Int = 0
    @JvmField var activePosition: Int = 0
    @JvmField var activeVelocity: Int = 0
    @JvmField var elevatorVoltage: Double = 0.0
    @JvmField var elevatorCurrent: Double = 0.0
}