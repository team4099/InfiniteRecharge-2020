package com.team4099.lib.config

/**
 * Represents gains and other configuration for a PID
 * controller.
 *
 * @param name A human readable name for adjustment via SmartDashboard.
 * @param slotNumber The slot these gains will use on a smart motor controller.
 * @param kP The proportional gain for the controller.
 * @param kI The integral gain for the controller.
 * @param kD The derivative gain for the controller.
 * @param iZone The integral zone for the controller.
 */
class PIDGains(
    val slotNumber: Int,
    kP: Double,
    kI: Double,
    kD: Double,
    iZone: Int
) {
    /**
     * A function that will be called when these gains change.
     */
    var updateHook = {}

    /**
     * The proportional gain for the controller.
     */
    var kP = kP
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The integral gain for the controller.
     */
    var kI = kI
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The derivative gain for the controller.
     */
    var kD = kD
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The integral zone for the controller. When nonzero,
     * Integral Accumulator is automatically cleared when
     * the absolute value of Closed-Loop Error exceeds it.
     */
    var iZone = iZone
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }
}
