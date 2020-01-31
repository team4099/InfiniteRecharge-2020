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

    val sampleWristVelocity: Double
        get() = operator.leftYAxis

    val wristVertical: Boolean
        get() = operator.leftShoulderButton

    val wristHorizontal: Boolean
        get() = operator.rightShoulderButton

    val startShooter: Boolean
        get() = operator.xButton

    val stopShooter: Boolean
        get() = operator.yButton

    val climberUp: Boolean
        get() = operator.dPadUp

    val climberDown: Boolean
        get() = operator.dPadDown

    val runIntakeIn: Boolean
        get() = operator.bButton
<<<<<<< HEAD


    val startShooter: Boolean
        get() = operator.aButton

    val stopShooter: Boolean
        get() = operator.bButton
=======
    val runIntakeOut: Boolean
        get() = operator.aButton
>>>>>>> 7dcfd11f7a7a668e4f0e2b5f423a5d1accdbc61d
}
