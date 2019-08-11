package org.usfirst.frc.team4099.robot.drive

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.lib.joystick.JoystickUtils
import org.usfirst.frc.team4099.lib.util.Utils

/**
 * Curvature Drive -
 * The left joystick controls speed (throttle)
 * The right joystick controls the curvature of the path.
 *
 * Credits: Team 254
 */

class CheesyDriveHelper {

    private var quickStopAccumulator: Double = 0.toDouble()
    private var negativeInertia: Double = 0.toDouble()

    private var lastThrottle: Double = 0.toDouble()

    private val signal = DriveSignal(0.0, 0.0)

    fun curvatureDrive(throttle: Double, wheel: Double, isQuickTurn: Boolean): DriveSignal {
        var throttle = throttle
        var wheel = wheel
        throttle = JoystickUtils.deadband(throttle, kThrottleDeadband)
        // TODO: see if moving wheel in the beginning makes a difference in throttle stop (because it used to come after the negativeInertia code)
        wheel = -JoystickUtils.deadbandNoShape(wheel, kWheelDeadband)

        if (isQuickTurn) {
            wheel /= 1.55
        }
        // TODO: test this, does it really make controls feel better?
        val wheelNonLinearity = 0.5
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity)
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity)
        wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity)

        /* ramps up the joystick throttle when magnitude increases
         * if magnitude decreases (1.0 to 0.0, or -1.0 to 0.0), allow anything
         * kMaxThrottleDelta is the maximum allowed change in throttle
         *   per iteration (~50 Hz)
         * attempt to limit the current draw when accelerating
         *
         * The acceleration time is quite apparent, but not unresponsive.
         */

        if (!Utils.sameSign(throttle, lastThrottle)) {
            throttle = 0.0
        } else {
            val throttleMagnitude = Math.abs(throttle)
            val lastThrottleMagnitude = Math.abs(lastThrottle)

            // only if an increase in magnitude
            if (throttleMagnitude > lastThrottleMagnitude) {
                // for + to more + increases
                if (throttle > lastThrottle + kMaxThrottleDelta)
                    throttle = lastThrottle + kMaxThrottleDelta
                else if (throttle < lastThrottle - kMaxThrottleDelta)
                    throttle = lastThrottle - kMaxThrottleDelta // for - to more - decreases
            }
        }
        SmartDashboard.putNumber("joystickThrottle", throttle)
        lastThrottle = throttle

        //        if (Utils.around(wheel, 0.0, 0.15)) { // if moving straight
        //            double beta = 0.1;
        //
        //            negativeInertia = (1 - beta) * negativeInertia +
        //                    beta * Utils.limit(throttle, 1.0) * 2;
        //        }

        val beta = 0.1

        negativeInertia = (1 - beta) * negativeInertia + beta * Utils.limit(throttle, 1.0) * 2.0

        if (Utils.around(throttle, 0.0, 0.075)) { // if wanting to brake (low throttle)
            if (!Utils.around(negativeInertia, 0.0, 0.0001))
                println("negativeInertia: " + negativeInertia)

            throttle -= negativeInertia

            //TODO: find the optimal value for negativeInertia decrease per iteration
            //TODO: testing 0.1, 0.15, 0.2, 0.25, 0.3, 0.5, 1.0, etc.
            if (negativeInertia > 1) {
                negativeInertia -= 0.1
            } else if (negativeInertia < -1) {
                negativeInertia += 0.1
            } else {
                negativeInertia = 0.0
            }
        }

        // wheel deadband used to be here

        val overPower: Double
        val angularPower: Double

        if (isQuickTurn) {

            wheel /= 1.75
            if (Math.abs(throttle) < 0.2) {
                val alpha = 0.1
                quickStopAccumulator = (1 - alpha) * quickStopAccumulator + // used for "negative inertia"
                                       alpha * Utils.limit(wheel, 1.0) * 2.0
            }
            overPower = 0.5
            angularPower = wheel
        } else {
            overPower = 0.0
            angularPower = Math.abs(throttle) * wheel * kTurnSensitivity - quickStopAccumulator

            if (quickStopAccumulator > 1) {
                quickStopAccumulator -= 0.5
            } else if (quickStopAccumulator < -1) {
                quickStopAccumulator += 0.5
            } else {
                quickStopAccumulator = 0.0
            }
        }

        var rightPower = throttle - angularPower
        var leftPower = throttle + angularPower
        if (leftPower > 1.0) {
            rightPower -= overPower * (leftPower - 1.0)
            leftPower = 1.0
        } else if (rightPower > 1.0) {
            leftPower -= overPower * (rightPower - 1.0)
            rightPower = 1.0
        } else if (leftPower < -1.0) {
            rightPower += overPower * (-1.0 - leftPower)
            leftPower = -1.0
        } else if (rightPower < -1.0) {
            leftPower += overPower * (-1.0 - rightPower)
            rightPower = -1.0
        }

        signal.rightMotor = rightPower
        signal.leftMotor = leftPower

        return signal
    }

    companion object {

        val instance = CheesyDriveHelper()

        private val kThrottleDeadband = 0.02
        private val kWheelDeadband = 0.02
        private val kTurnSensitivity = 0.6
        private val kMaxThrottleDelta = 1.0 / 40.0
    }
}
