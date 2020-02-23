package com.team4099.lib.hardware

import com.team4099.lib.config.PIDGains

interface ServoMotorHardware {
    var timeout: Int
    var pidSlot: Int

    val positionTicks: Int
    val velocityTicksPer100Ms: Int

    fun setMotionProfile(setpoint: Double)
    fun setPosition(setpoint: Double)
    fun setVelocity(setpoint: Double)
    fun setOpenLoop(percentOutput: Double)

    fun zeroSensors()

    fun applyPIDGains(gains: PIDGains)

    @Suppress("LongParameterList")
    fun applyMotionConstraints(
        enableReverseSoftLimit: Boolean,
        reverseSoftLimit: Int,
        enableForwardSoftLimit: Boolean,
        forwardSoftLimit: Int,
        cruiseVel: Int,
        maxAccel: Int,
        motionProfileCurveStrength: Int,
        velocityPIDSlot: Int,
        brakeMode: Boolean = false
    )
}
