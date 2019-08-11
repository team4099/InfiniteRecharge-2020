package org.usfirst.frc.team4099.lib.joystick

object JoystickUtils {

    /**
     * Transforms the joystick input from linear to cubic.
     * @param signal The raw input from the joystick
     * @param deadbandWidth The maximum output of the joystick that is still considered 0.
     * @return The transformed, potentially smoother control curve.
     */
    fun deadband(signal: Double, deadbandWidth: Double): Double {
        var mSignal = signal
        // TODO: Implement MAX_OUTPUT limiter based on currentVoltage

//        val currentVoltage = VoltageEstimator.instance.averageVoltage
        val MAX_OUTPUT = 0.85
        val alpha = 0.1
        val beta = MAX_OUTPUT - alpha

        mSignal = alpha * mSignal + beta * Math.pow(mSignal, 3.0)
        val sign = if (mSignal > 0) 1 else -1
        mSignal = Math.abs(mSignal)

        return if (mSignal < deadbandWidth) 0.0 else sign * (mSignal - deadbandWidth) / (1.0 - deadbandWidth)

    }

    fun deadbandNoShape(signal: Double, deadbandWidth: Double): Double {
        var mSignal = signal
        val MAX_OUTPUT = 0.85

        val sign = if (mSignal > 0) 1 else -1
        mSignal = Math.abs(mSignal)

        return if (mSignal < deadbandWidth) 0.0 else MAX_OUTPUT * sign.toDouble() * (mSignal - deadbandWidth) / (1.0 - deadbandWidth)

    }
}
