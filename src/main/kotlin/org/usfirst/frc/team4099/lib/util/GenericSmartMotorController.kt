package org.usfirst.frc.team4099.lib.util

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
        ABSOLUTE_PWM,
        ABSOLUTE_ANALOG,
        EXTERNAL_QUADRATURE,
        EXTERNAL_ABSOLUTE_PWM,
        EXTERNAL_ABSOLUTE_ANALOG
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
    val master: GenericSmartMotorController

    var idleMode: IdleMode

    val sensorPhase: Boolean
    val inverted: InvertType

    val timeout: Double

    val openLoopRampSecs: Double
    val closedLoopRampSecs: Double

    val peakOutputForward: Double
    val peakOutputReverse: Double
    val nominalOutputForward: Double
    val nominalOutputReverse: Double

    val neutralDeadband: Double

    val maxCompensatedVoltage: Double
    val useVoltageCompensation: Boolean

    // TODO: voltage measurement filter

    val busVoltage: Double
    val motorOutputPercent: Double
    val motorOutputVoltage: Double

    // TODO: Determine if we ever want to use different encoders for different PID slots
    val selectedEncoder: EncoderType

    // TODO: figure out absolute versions of these
    val encoderPPR: Double
    val encoderToUnits: Double

    var encoderPosition: Double
    val encoderVelocity: Double

    var rawEncoderPosition: Double
    val rawEncoderVelocity: Double

    val velocityMeasPeriod: VelocityMeasPeriod
    val velocityMeasWindow: Int

    val useLimitSwitches: Boolean
    val useSoftLimits: Boolean

    val useForwardSoftLimit: Boolean
    val useReverseSoftLimit: Boolean

    val allowableClosedLoopError: Double
    val rawAllowableClosedLoopError: Int

    val maxIntegralAccumulator: Double
    var currIntegralAccumulator: Double

    val closedLoopPeakOutput: Double

    val closedLoopTarget: Double

    var motionCruiseVelocity: Double
    var rawMotionCruiseVelocity: Int

    var motionAcceleration: Double
    var rawMotionAcceleration: Int

    var motionSCurveStrength: Int

    fun set(mode: ControlMode, outputValue: Double)
    fun set(mode: ControlMode, outputValue: Double, feedForward: Double)

    fun configFactoryDefault()
    fun burnFlash()

    fun setControlFramePeriod(frame: ControlFrame, periodMs: Int)
    fun setStatusFramePeriod(frame: StatusFrame, periodMs: Int)

    fun getControlFramePeriod(frame: ControlFrame): Int
    fun getStatusFramePeriod(frame: StatusFrame): Int

    fun setForwardLimitSwitch(source: LimitSwitchSource, normal: LimitSwitchNormal)
    fun setReverseLimitSwitch(source: LimitSwitchSource, normal: LimitSwitchNormal)

    fun setPID(slotId: Int, kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)
    fun setRawPID(slotId: Int, kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double)

}