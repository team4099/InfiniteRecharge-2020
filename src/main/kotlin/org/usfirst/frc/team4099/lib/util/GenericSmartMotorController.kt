package org.usfirst.frc.team4099.lib.util

import kotlin.math.PI

interface GenericSmartMotorController {
    enum class ControlMode {
        PERCENT_VBUS,
        VELOCITY,
        POSITION,
        PROFILED
    }

    enum class IdleMode {
        BRAKE,
        COAST
    }

    enum class InvertType {
        NONE,
        INVERT,
        FOLLOW_MASTER,
        OPPOSE_MASTER
    }

    enum class EncoderType {
        NONE,
        QUADRATURE,
        EXTERNAL_QUADRATURE,
        ABSOLUTE_PWM,
        ABSOLUTE_ANALOG,
        CAN_QUADRATURE,
        CAN_ABSOLUTE_PWM,
        CAN_ABSOLUTE_ANALOG
    }

    // TODO: Figure out what these actually are :/
    enum class ControlFrame {
        GENERAL,
        ADVANCED
    }

    enum class StatusFrame {
        GENERAL,
        PRIMARY_FEEDBACK,
        MISC,
        COMM_STATUS,
        TARGETS,
        SECONDARY_FEEDBACK,
        PRIMARY_PID,
        SECONDARY_PID
    }

    @SuppressWarnings("MagicNumber")
    enum class VelocityMeasPeriod(periodMs: Int) {
        MS_1(1),
        MS_2(2),
        MS_5(5),
        MS_10(10),
        MS_20(20),
        MS_25(25),
        MS_50(50),
        MS_100(100)
    }

    enum class LimitSwitchNormal {
        NORMALLY_OPEN,
        NORMALLY_CLOSED
    }

    enum class LimitSwitchSource {
        CONNECTED,
        REMOTE_CANIFIER,
        REMOTE_SRX
    }

    val id: Int
    // TODO: add "no controller" here to avoid making this null
    var master: GenericSmartMotorController

    var idleMode: IdleMode

    var sensorAgreesWithInversion: Boolean
    var inverted: InvertType

    val timeout: Int

    var openLoopRampSecs: Double
    var closedLoopRampSecs: Double

    var peakOutputForward: Double
    var peakOutputReverse: Double
    var nominalOutputForward: Double
    var nominalOutputReverse: Double

    var neutralDeadband: Double

    var maxCompensatedVoltage: Double
    var useVoltageCompensation: Boolean

    // TODO: voltage measurement filter

    val inputVoltage: Double
    val motorOutput: Double
    val motorOutputVolts: Double

    // TODO: Determine if we ever want to use different encoders for different PID slots
    var selectedEncoder: EncoderType

    // TODO: figure out absolute versions of these
    var encoderPPR: Int
    var encoderToUnits: Double

    var encoderPosition: Double
    val encoderVelocity: Double

    var rawEncoderPosition: Double
    val rawEncoderVelocity: Double

    var velocityMeasPeriod: VelocityMeasPeriod
    var velocityMeasWindow: Int

    var useLimitSwitches: Boolean
    var useSoftLimits: Boolean

    var useForwardSoftLimit: Boolean
    var useReverseSoftLimit: Boolean

    var forwardSoftLimitValue: Double
    var rawForwardSoftLimitValue: Double

    var reverseSoftLimitValue: Double
    var rawReverseSoftLimitValue: Double

    var allowableVelocityError: Double
    var rawAllowableVelocityError: Double

    var allowablePositionError: Double
    var rawAllowablePositionError: Double

    var maxVelocityIntegralAccumulator: Double
    var rawMaxVelocityIntegralAccumulator: Double

    var positionIntegralAccumulator: Double
    var rawMaxPositionIntegralAccumulator: Double

    val currIntegralAccumulator: Double
    val rawCurrIntegralAccumulator: Double

    var velocityPeakOutput: Double
    var positionPeakOutput: Double

    val velocityTarget: Double
    val rawVelocityTarget: Double

    val positionTarget: Double
    val rawPositionTarget: Double

    var motionCruiseVelocity: Double
    var rawMotionCruiseVelocity: Double

    var motionAcceleration: Double
    var rawMotionAcceleration: Double

    var motionSCurveStrength: Int

    var continuousInputCurrentLimit: Int
    var continuousStatorCurrentLimit: Int

    var peakInputCurrentLimit: Int
    var peakStatorCurrentLimit: Int

    var peakCurrentDurationMs: Int

    fun set(mode: ControlMode, outputValue: Double)
    fun setRaw(mode: ControlMode, outputValue: Double)

    fun setVelocityArbFeedforward(velocity: Double, feedForward: Double)
    fun setRawVelocityArbFeedforward(velocity: Double, feedForward: Double)

    fun resetToFactoryDefault()
    fun saveToFlash()

    fun setControlFramePeriod(frame: ControlFrame, periodMs: Int)
    fun setStatusFramePeriod(frame: StatusFrame, periodMs: Int)

    fun setForwardLimitSwitch(source: LimitSwitchSource, normal: LimitSwitchNormal)
    fun setReverseLimitSwitch(source: LimitSwitchSource, normal: LimitSwitchNormal)

    @SuppressWarnings("LongParameterList")
    fun setVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)
    @SuppressWarnings("LongParameterList")
    fun setRawVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)

    @SuppressWarnings("LongParameterList")
    fun setPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)
    @SuppressWarnings("LongParameterList")
    fun setRawPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)

    @SuppressWarnings("LongParameterList")
    fun setRawPID(slotId: Int, kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)

    fun rawToPosition(raw: Double): Double {
        return raw / encoderPPR / encoderToUnits * (2 * PI)
    }

    fun positionToRaw(units: Double): Double {
        return units * encoderPPR * encoderToUnits / (2 * PI)
    }

    fun rawToVelocity(raw: Double): Double

    fun velocityToRaw(units: Double): Double
}
