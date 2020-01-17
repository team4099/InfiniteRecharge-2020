package com.team4099.lib.config

import kotlin.math.roundToInt

/**
 * Represents the constraints on the motion of a servo
 * motor subsystem during closed loop control. Values are in
 * units of subsystem motion, not native encoder units.
 *
 * @param reverseSoftLimit The maximum distance the subsystem can move in the reverse direction. NaN to disable.
 * @param forwardSoftLimit The maximum distance the subsystem can move in the forward direction. NaN to disable.
 * @param cruiseVel The maximum velocity the subsystem will travel at during motion profile based control.
 * @param maxAccel The maximum acceleration of the subsystem during motion profile based control.
 * @param motionProfileCurveStrength The curvature of the generated motion profile. A value of zero disables
 * S-curve smoothing. Higher values reduce the jerk of the motion.
 */
class ServoMotorSubsystemMotionConstraints(
    override val keyPrefix: String,
    var reverseSoftLimit: Double,
    var forwardSoftLimit: Double,
    var cruiseVel: Double,
    var maxAccel: Double,
    var motionProfileCurveStrength: Int
) : Configurable<Double>({ it }) {
    var motionProfileCurveStrengthDouble = motionProfileCurveStrength.toDouble()
        set(value) {
            motionProfileCurveStrength = value.roundToInt()
            field = value
        }

    override val properties = mapOf(
        "reverseSoftLimit" to ::reverseSoftLimit,
        "forwardSoftLimit" to ::forwardSoftLimit,
        "cruiseVel" to ::cruiseVel,
        "maxAccel" to ::maxAccel,
        "motionProfileCurveStrength" to ::motionProfileCurveStrengthDouble
    )

    override var updateHook = {}
}
