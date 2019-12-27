package org.usfirst.frc.team4099.lib.util

import com.ctre.phoenix.motorcontrol.ControlFrame
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.IMotorController
import com.ctre.phoenix.motorcontrol.InvertType
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.StatusFrame
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod
import com.ctre.phoenix.motorcontrol.can.TalonSRX

class GenericTalonSRX(override val id: Int, override val timeout: Int) : GenericSmartMotorController, TalonSRX(id) {
    override var master: GenericSmartMotorController = this
        set(value) {
            if (value is IMotorController) {
                this.follow(value)
                field = value
            } else {
                throw IncorrectControllerException("SRX can't follow this controller")
            }
        }

    override var idleMode: GenericSmartMotorController.IdleMode = GenericSmartMotorController.IdleMode.COAST
        set(value) {
            when (value) {
                GenericSmartMotorController.IdleMode.BRAKE -> setNeutralMode(NeutralMode.Brake)
                GenericSmartMotorController.IdleMode.COAST -> setNeutralMode(NeutralMode.Coast)
            }
            field = value
        }
    override var sensorAgreesWithInversion: Boolean = true
        set(value) {
            super.setSensorPhase(value)
            field = value
        }

    override var inverted: GenericSmartMotorController.InvertType = GenericSmartMotorController.InvertType.NONE
        set(value) {
            when (value) {
                GenericSmartMotorController.InvertType.NONE -> super.setInverted(InvertType.None)
                GenericSmartMotorController.InvertType.FOLLOW_MASTER -> super.setInverted(InvertType.FollowMaster)
                GenericSmartMotorController.InvertType.OPPOSE_MASTER -> super.setInverted(InvertType.OpposeMaster)
                GenericSmartMotorController.InvertType.INVERT -> super.setInverted(InvertType.InvertMotorOutput)
            }
            field = value
        }

    override var openLoopRampSecs: Double = 0.0
        set(value) {
            super.configOpenloopRamp(value, timeout)
            field = value
        }

    override var closedLoopRampSecs: Double = 0.0
        set(value) {
            super.configClosedloopRamp(value, timeout)
            field = value
        }

    override var peakOutputForward: Double = 1.0
        set(value) {
            super.configPeakOutputForward(value, timeout)
            field = value
        }

    override var peakOutputReverse: Double = -1.0
        set(value) {
            super.configPeakOutputReverse(value, timeout)
            field = value
        }

    override var nominalOutputForward: Double = 0.0
        set(value) {
            super.configNominalOutputForward(value, timeout)
            field = value
        }

    override var nominalOutputReverse: Double = 0.0
        set(value) {
            super.configNominalOutputReverse(value, timeout)
            field = value
        }

    override var neutralDeadband: Double = 0.0
        set(value) {
            super.configNeutralDeadband(value, timeout)
            field = value
        }

    override var maxCompensatedVoltage: Double = 12.0
        set(value) {
            super.configVoltageCompSaturation(value)
            field = value
        }

    override var useVoltageCompensation: Boolean = false
        set(value) {
            super.enableVoltageCompensation(value)
            field = value
        }

    override val inputVoltage: Double
        get() = super.getBusVoltage()

    override val motorOutput: Double
        get() = super.getMotorOutputPercent()

    override val motorOutputVolts: Double
        get() = super.getMotorOutputVoltage()

    override var selectedEncoder: GenericSmartMotorController.EncoderType = GenericSmartMotorController.EncoderType.NONE
        set(value) {
            when (value) {
                GenericSmartMotorController.EncoderType.ABSOLUTE_ANALOG ->
                    super.configSelectedFeedbackSensor(FeedbackDevice.Analog)
                GenericSmartMotorController.EncoderType.ABSOLUTE_PWM ->
                    super.configSelectedFeedbackSensor(FeedbackDevice.PulseWidthEncodedPosition)
                GenericSmartMotorController.EncoderType.QUADRATURE ->
                    super.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder)
                GenericSmartMotorController.EncoderType.EXTERNAL_ABSOLUTE_ANALOG -> {
                    // TODO: make this actually pick the right absolute sensor
                    super.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0)
                }
                GenericSmartMotorController.EncoderType.EXTERNAL_QUADRATURE -> {
                    super.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0)
                }
                GenericSmartMotorController.EncoderType.EXTERNAL_ABSOLUTE_PWM -> {
                    super.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0)
                }
                GenericSmartMotorController.EncoderType.NONE -> {
                }
            }
            field = value
        }

    override var encoderPPR: Double = 1.0

    override var encoderToUnits: Double = 1.0

    override var encoderPosition: Double = 0.0
        get() = rawToPosition(selectedSensorPosition)
        set(value) {
            selectedSensorPosition = positionToRaw(value)
            field = positionToRaw(value).toDouble()
        }

    override val encoderVelocity: Double
        get() = rawToVelocity(selectedSensorVelocity)

    override var rawEncoderPosition: Double = 0.0
        get() = selectedSensorPosition.toDouble()
        set(value) {
            selectedSensorPosition = value.toInt()
            field = value
        }

    override val rawEncoderVelocity: Double
        get() = selectedSensorVelocity.toDouble()

    override var velocityMeasPeriod: GenericSmartMotorController.VelocityMeasPeriod =
        GenericSmartMotorController.VelocityMeasPeriod.MS_50
        set(value) {
            when (value) {
                GenericSmartMotorController.VelocityMeasPeriod.MS_1 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_1Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_2 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_2Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_5 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_5Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_10 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_10Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_20 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_20Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_25 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_25Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_50 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_50Ms, timeout)
                GenericSmartMotorController.VelocityMeasPeriod.MS_100 ->
                    super.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_100Ms, timeout)
            }
            field = value
        }

    // TODO: check this value for accuracy on default
    override var velocityMeasWindow: Int = 4
        set(value) {
            configVelocityMeasurementWindow(value, timeout)
            field = value
        }

    override var useLimitSwitches: Boolean = false
        set(value) {
            overrideLimitSwitchesEnable(value)
            field = value
        }

    override var useSoftLimits: Boolean = false
        set(value) {
            overrideSoftLimitsEnable(value)
            field = value
        }

    override var useForwardSoftLimit: Boolean = false
        set(value) {
            super.configForwardSoftLimitEnable(value, timeout)
            field = value
        }

    override var useReverseSoftLimit: Boolean = false
        set(value) {
            super.configReverseSoftLimitEnable(value, timeout)
            field = value
        }

    override var allowableVelocityError: Double = 0.0
        get() = rawToVelocity(rawAllowableVelocityError)
        set(value) {
            rawAllowableVelocityError = velocityToRaw(value)
            field = value
        }

    override var rawAllowableVelocityError: Int = 0
        set(value) {
            configAllowableClosedloopError(0, value, timeout)
            field = value
        }

    override var allowablePositionError: Double = 0.0
        get() = rawToPosition(rawAllowablePositionError)
        set(value) {
            rawAllowablePositionError = positionToRaw(value)
            field = value
        }

    override var rawAllowablePositionError: Int = 0
        set(value) {
            configAllowableClosedloopError(1, value, timeout)
            field = value
        }

    override var maxVelocityIntegralAccumulator: Double = 0.0
        get() = rawToVelocity(rawMaxVelocityIntegralAccumulator)
        set(value) {
            rawMaxVelocityIntegralAccumulator = velocityToRaw(value)
            field = value
        }

    override var rawMaxVelocityIntegralAccumulator: Int = 0
        set(value) {
            configMaxIntegralAccumulator(0, value.toDouble(), timeout)
            field = value
        }
    override var positionIntegralAccumulator: Double = 0.0
        get() = rawToPosition(rawMaxPositionIntegralAccumulator)
        set(value) {
            rawMaxPositionIntegralAccumulator = positionToRaw(value)
            field = value
        }

    override var rawMaxPositionIntegralAccumulator: Int = 0
        set(value) {
            configMaxIntegralAccumulator(1, value.toDouble(), timeout)
            field = value
        }

    override val currIntegralAccumulator: Double
        get() = rawToPosition(getIntegralAccumulator(0).toInt())

    override val rawCurrIntegralAccumulator: Int
        get() = getIntegralAccumulator(0).toInt()

    override var velocityPeakOutput: Double = 1.0
        set(value) {
            configClosedLoopPeakOutput(0, value, timeout)
            field = value
        }

    override var positionPeakOutput: Double = 1.0
        set(value) {
            configClosedLoopPeakOutput(1, value, timeout)
            field = value
        }

    override val velocityTarget: Double
        get() = rawToVelocity(getClosedLoopTarget(0).toInt())

    override val rawVelocityTarget: Int
        get() = getClosedLoopTarget(0).toInt()

    override val positionTarget: Double
        get() = rawToPosition(getClosedLoopTarget(1).toInt())

    override val rawPositionTarget: Int
        get() = getClosedLoopTarget(1).toInt()

    override var motionCruiseVelocity: Double = 0.0
        get() = rawToVelocity(rawMotionCruiseVelocity)
        set(value) {
            rawMotionCruiseVelocity = velocityToRaw(value)
            field = value
        }

    override var rawMotionCruiseVelocity: Int = 0
        set(value) {
            configMotionCruiseVelocity(value, timeout)
            field = value
        }

    override var motionAcceleration: Double = 0.0
        get() = rawToVelocity(rawMotionAcceleration)
        set(value) {
            rawMotionAcceleration = velocityToRaw(value)
            field = value
        }

    override var rawMotionAcceleration: Int = 0
        set(value) {
            configMotionAcceleration(value, timeout)
            field = value
        }

    override var motionSCurveStrength: Int = 0
        set(value) {
            configMotionSCurveStrength(value, timeout)
            field = value
        }

    override var continuousInputCurrentLimit: Int = 60
        set(value) {
            configContinuousCurrentLimit(value, timeout)
            field = value
        }

    override var continuousStatorCurrentLimit: Int = 0
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set stator current limit with SRX")
        }

    override var peakInputCurrentLimit: Int = 100
        set(value) {
            configPeakCurrentLimit(value, timeout)
            field = value
        }

    override var peakStatorCurrentLimit: Int = 0
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set stator current limit with SRX")
        }

    override var peakCurrentDurationMs: Int = 0
        set(value) {
            configPeakCurrentDuration(value, timeout)
            field = value
        }

    override fun set(mode: GenericSmartMotorController.ControlMode, outputValue: Double) {
        when (mode) {
            GenericSmartMotorController.ControlMode.PERCENT_VBUS ->
                set(ControlMode.PercentOutput, outputValue)
            GenericSmartMotorController.ControlMode.VELOCITY ->
                set(ControlMode.Velocity, velocityToRaw(outputValue).toDouble())
            GenericSmartMotorController.ControlMode.POSITION ->
                set(ControlMode.Position, positionToRaw(outputValue).toDouble())
            GenericSmartMotorController.ControlMode.PROFILED ->
                set(ControlMode.MotionMagic, positionToRaw(outputValue).toDouble())
        }
    }

    override fun setRaw(mode: GenericSmartMotorController.ControlMode, outputValue: Double) {
        when (mode) {
            GenericSmartMotorController.ControlMode.PERCENT_VBUS -> set(ControlMode.PercentOutput, outputValue)
            GenericSmartMotorController.ControlMode.VELOCITY -> set(ControlMode.Velocity, outputValue)
            GenericSmartMotorController.ControlMode.POSITION -> set(ControlMode.Position, outputValue)
            GenericSmartMotorController.ControlMode.PROFILED -> set(ControlMode.MotionMagic, outputValue)
        }
    }

    override fun setVelocityArbFeedforward(velocity: Double, feedForward: Double) {
        set(ControlMode.Velocity, velocityToRaw(velocity).toDouble(), DemandType.ArbitraryFeedForward, feedForward)
    }

    override fun setRawVelocityArbFeedforward(velocity: Double, feedForward: Double) {
        set(ControlMode.Velocity, velocity, DemandType.ArbitraryFeedForward, feedForward)
    }

    override fun resetToFactoryDefault() {
        configFactoryDefault(timeout)
    }

    override fun burnFlash() {}

    override fun setControlFramePeriod(frame: GenericSmartMotorController.ControlFrame, periodMs: Int) {
        when (frame) {
            GenericSmartMotorController.ControlFrame.GENERAL ->
                setControlFramePeriod(ControlFrame.Control_3_General, periodMs)
            GenericSmartMotorController.ControlFrame.ADVANCED ->
                setControlFramePeriod(ControlFrame.Control_4_Advanced, periodMs)
        }
    }

    override fun setStatusFramePeriod(frame: GenericSmartMotorController.StatusFrame, periodMs: Int) {
        when (frame) {
            GenericSmartMotorController.StatusFrame.GENERAL ->
                setStatusFramePeriod(StatusFrame.Status_1_General, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.PRIMARY_FEEDBACK ->
                setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.MISC ->
                setStatusFramePeriod(StatusFrame.Status_6_Misc, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.COMM_STATUS ->
                setStatusFramePeriod(StatusFrame.Status_7_CommStatus, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.TARGETS ->
                setStatusFramePeriod(StatusFrame.Status_10_Targets, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.SECONDARY_FEEDBACK ->
                setStatusFramePeriod(StatusFrame.Status_12_Feedback1, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.PRIMARY_PID ->
                setStatusFramePeriod(StatusFrameEnhanced.Status_13_Base_PIDF0, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.SECONDARY_PID ->
                setStatusFramePeriod(StatusFrameEnhanced.Status_14_Turn_PIDF1, periodMs, timeout)
        }
    }

    override fun setForwardLimitSwitch(
        source: GenericSmartMotorController.LimitSwitchSource,
        normal: GenericSmartMotorController.LimitSwitchNormal
    ) {
        val limitSwitchNormal = when (normal) {
            GenericSmartMotorController.LimitSwitchNormal.NORMALLY_OPEN -> LimitSwitchNormal.NormallyOpen
            GenericSmartMotorController.LimitSwitchNormal.NORMALLY_CLOSED -> LimitSwitchNormal.NormallyClosed
        }
        when (source) {
            GenericSmartMotorController.LimitSwitchSource.CONNECTED ->
                configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, limitSwitchNormal, timeout)
            GenericSmartMotorController.LimitSwitchSource.REMOTE_CANIFIER ->
                configForwardLimitSwitchSource(LimitSwitchSource.RemoteCANifier, limitSwitchNormal, timeout)
            GenericSmartMotorController.LimitSwitchSource.REMOTE_SRX ->
                configForwardLimitSwitchSource(LimitSwitchSource.RemoteTalonSRX, limitSwitchNormal, timeout)
        }
    }

    override fun setReverseLimitSwitch(
        source: GenericSmartMotorController.LimitSwitchSource,
        normal: GenericSmartMotorController.LimitSwitchNormal
    ) {
        val limitSwitchNormal = when (normal) {
            GenericSmartMotorController.LimitSwitchNormal.NORMALLY_OPEN -> LimitSwitchNormal.NormallyOpen
            GenericSmartMotorController.LimitSwitchNormal.NORMALLY_CLOSED -> LimitSwitchNormal.NormallyClosed
        }
        when (source) {
            GenericSmartMotorController.LimitSwitchSource.CONNECTED ->
                configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, limitSwitchNormal, timeout)
            GenericSmartMotorController.LimitSwitchSource.REMOTE_CANIFIER ->
                configReverseLimitSwitchSource(LimitSwitchSource.RemoteCANifier, limitSwitchNormal, timeout)
            GenericSmartMotorController.LimitSwitchSource.REMOTE_SRX ->
                configReverseLimitSwitchSource(LimitSwitchSource.RemoteTalonSRX, limitSwitchNormal, timeout)
        }
    }

    override fun setVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        TODO("not implemented")
    }

    override fun setRawVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        config_kP(0, kP, timeout)
        config_kI(0, kI, timeout)
        config_kD(0, kD, timeout)
        config_kF(0, kF, timeout)
        config_IntegralZone(0, iZone.toInt(), timeout)
    }

    override fun setPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        TODO("not implemented")
    }

    override fun setRawPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        config_kP(1, kP, timeout)
        config_kI(1, kI, timeout)
        config_kD(1, kD, timeout)
        config_kF(1, kF, timeout)
        config_IntegralZone(1, iZone.toInt(), timeout)
    }

    override fun setRawPID(slotId: Int, kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        config_kP(slotId, kP, timeout)
        config_kI(slotId, kI, timeout)
        config_kD(slotId, kD, timeout)
        config_kF(slotId, kF, timeout)
        config_IntegralZone(slotId, iZone.toInt(), timeout)
    }

    override fun rawToVelocity(raw: Int): Double {
        return rawToPosition(raw) * 10
    }

    override fun velocityToRaw(units: Double): Int {
        return positionToRaw(units) / 10
    }
}
