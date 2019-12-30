package org.usfirst.frc.team4099.lib.config

import org.usfirst.frc.team4099.lib.util.limit

/**
 * Represents the constraints on the motion of a servo
 * motor subsystem during closed loop control. Values are in
 * units of subsystem motion, not native encoder units.
 *
 * @param updateHook A function that will be called when these constraints change.
 * @param reverseSoftLimit The maximum distance the subsystem can move in the reverse direction. NaN to disable.
 * @param forwardSoftLimit The maximum distance the subsystem can move in the forward direction. NaN to disable.
 * @param cruiseVelocity The maximum velocity the subsystem will travel at during motion profile based control.
 * @param maximumAcceleration The maximum acceleration of the subsystem during motion profile based control.
 * @param motionProfileCurveStrength The curvature of the generated motion profile. A value of zero disables
 * S-curve smoothing. Higher values reduce the jerk of the motion.
 */
class ServoMotorSubsystemMotionConstraints(
    private val updateHook: () -> Unit,
    reverseSoftLimit: Double,
    forwardSoftLimit: Double,
    cruiseVelocity: Double,
    maximumAcceleration: Double,
    motionProfileCurveStrength: Int
) {
    /**
     * The maximum distance the subsystem can move in the reverse direction.
     */
    var reverseSoftLimit = reverseSoftLimit
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The maximum distance the subsystem can move in the forward direction. NaN to disable.
     */
    var forwardSoftLimit = forwardSoftLimit
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The maximum velocity the subsystem will travel at during motion profile based control. NaN to disable.
     */
    var cruiseVelocity = cruiseVelocity
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The maximum acceleration of the subsystem during motion profile based control.
     */
    var maximumAcceleration = maximumAcceleration
        set(value) {
            if (field != value) {
                field = value
                updateHook()
            }
        }

    /**
     * The curvature of the generated motion profile. A value of zero disables
     * S-curve smoothing. Higher values reduce the jerk of the motion.
     */
    var motionProfileCurveStrength = motionProfileCurveStrength
        set(value) {
            value.limit(0, 8)
            if (field != value) {
                field = value
                updateHook()
            }
        }
}