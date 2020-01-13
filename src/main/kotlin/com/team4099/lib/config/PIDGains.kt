package com.team4099.lib.config

import kotlin.math.roundToInt

/**
 * Represents gains and other configuration for a PID
 * controller.
 *
 * @param keyPrefix A human readable name for adjustment via SmartDashboard.
 * @param slotNumber The slot these gains will use on a smart motor controller.
 * @param kP The proportional gain for the controller.
 * @param kI The integral gain for the controller.
 * @param kD The derivative gain for the controller.
 * @param kF The feed-forward gain for the controller.
 * @param iZone The integral zone for the controller.
 */
class PIDGains(
    override val keyPrefix: String,
    val slotNumber: Int,
    var kP: Double,
    var kI: Double,
    var kD: Double,
    var kF: Double,
    var iZone: Int
) : Configurable<Double>({ it }) {
    private var iZoneDouble = iZone.toDouble()
        set(value) {
            iZone = value.roundToInt()
            field = value
        }

    override val properties = mapOf(
        "kP" to ::kP,
        "kI" to ::kI,
        "kD" to ::kD,
        "kF" to ::kF,
        "iZone" to ::iZoneDouble
    )

    override var updateHook = {}
}
