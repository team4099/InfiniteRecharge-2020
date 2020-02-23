package com.team4099.robot2020.config

import com.team4099.lib.joystick.Gamepad
import com.team4099.lib.joystick.XboxOneGamepad

/**
 * Maps buttons on the driver and operator controllers to specific actions
 * with meaningful variable names.
 */
object ControlBoard {
    private val driver: Gamepad = XboxOneGamepad(Constants.Joysticks.DRIVER_PORT)
    private val operator: Gamepad = XboxOneGamepad(Constants.Joysticks.SHOTGUN_PORT)

    val throttle: Double
        get() = driver.rightTriggerAxis - driver.leftTriggerAxis

    val turn: Double
        get() = -driver.leftXAxis

    val sampleClimberVelocity: Double
        get() = operator.leftTriggerAxis - operator.rightTriggerAxis

    val wristVertical: Boolean
        get() = operator.leftShoulderButton

    val wristHorizontal: Boolean
        get() = operator.rightShoulderButton

    val enableVisionAlignment: Boolean
        get() = driver.aButton

    val startShooter: Boolean
        get() = operator.xButton

    val stopShooter: Boolean
        get() = operator.yButton

    val climberUp: Boolean
        get() = driver.dPadUp

    val climberDown: Boolean
        get() = driver.dPadDown

    val runIntakeIn: Boolean
        get() = operator.aButton

    val runIntakeOut: Boolean
        get() = operator.bButton

    val runFeederIn: Boolean
        get() = operator.dPadDown

    val runFeederOut: Boolean
        get() = operator.dPadUp

    val slowMode: Boolean
        get() = driver.dPadDown
}
