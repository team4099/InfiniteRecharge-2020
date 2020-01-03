package com.team4099.lib.motorcontroller

import com.ctre.phoenix.motorcontrol.ControlFrame
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.IMotorController
import com.ctre.phoenix.motorcontrol.InvertType
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.RemoteLimitSwitchSource
import com.ctre.phoenix.motorcontrol.StatusFrame
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.team4099.lib.config.PIDGains

class GenericVictorSPX(override val id: Int, override val timeout: Int) : GenericSmartMotorController, VictorSPX(id) {
    override var master: GenericSmartMotorController = this
        set(value) {
            if (value is IMotorController) {
                this.follow(value)
                field = value
            } else {
                throw IncorrectControllerException("SPX can't follow this controller")
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
                    throw IncorrectControllerException("Victor SPX has no external encoder support.")
                GenericSmartMotorController.EncoderType.ABSOLUTE_PWM ->
                    throw IncorrectControllerException("Victor SPX has no external encoder support.")
                GenericSmartMotorController.EncoderType.INTERNAL_QUADRATURE ->
                    throw IncorrectControllerException("Victor SPX has no internal encoder support.")
                GenericSmartMotorController.EncoderType.EXTERNAL_QUADRATURE ->
                    throw IncorrectControllerException("Victor SPX has no external encoder support.")
                GenericSmartMotorController.EncoderType.CAN_ABSOLUTE_ANALOG -> {
                    // TODO: make this actually pick the right absolute sensor
                    super.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0)
                }
                GenericSmartMotorController.EncoderType.CAN_QUADRATURE -> {
                    super.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0)
                }
                GenericSmartMotorController.EncoderType.CAN_ABSOLUTE_PWM -> {
                    super.configSelectedFeedbackSensor(FeedbackDevice.RemoteSensor0)
                }
                GenericSmartMotorController.EncoderType.NONE -> {
                }
            }
            field = value
        }

    override var encoderPPR: Int = 1

    override var encoderRevsPerUnit: Double = 1.0

    override var encoderPosition: Double = 0.0
        get() = rawToPosition(selectedSensorPosition.toDouble())
        set(value) {
            selectedSensorPosition = positionToRaw(value).toInt()
            field = positionToRaw(value).toDouble()
        }

    override val encoderVelocity: Double
        get() = rawToVelocity(selectedSensorVelocity.toDouble())

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

    override var forwardSoftLimitValue: Double
        get() = rawToPosition(rawForwardSoftLimitValue)
        set(value) {
            rawForwardSoftLimitValue = positionToRaw(value)
        }

    override var rawForwardSoftLimitValue: Double = 0.0
        set(value) {
            configForwardSoftLimitThreshold(value.toInt(), timeout)
            field = value
        }

    override var reverseSoftLimitValue: Double
        get() = rawToPosition(rawReverseSoftLimitValue)
        set(value) {
            rawReverseSoftLimitValue = positionToRaw(value)
        }

    override var rawReverseSoftLimitValue: Double = 0.0
        set(value) {
            configReverseSoftLimitThreshold(value.toInt(), timeout)
            field = value
        }

    override var allowableVelocityError: Double
        get() = rawToVelocity(rawAllowableVelocityError)
        set(value) {
            rawAllowableVelocityError = velocityToRaw(value)
        }

    override var rawAllowableVelocityError: Double = 0.0
        set(value) {
            configAllowableClosedloopError(0, value.toInt(), timeout)
            field = value
        }

    override var allowablePositionError: Double
        get() = rawToPosition(rawAllowablePositionError)
        set(value) {
            rawAllowablePositionError = positionToRaw(value)
        }

    override var rawAllowablePositionError: Double = 0.0
        set(value) {
            configAllowableClosedloopError(1, value.toInt(), timeout)
            field = value
        }

    override var maxVelocityIntegralAccumulator: Double
        get() = rawToVelocity(rawMaxVelocityIntegralAccumulator)
        set(value) {
            rawMaxVelocityIntegralAccumulator = velocityToRaw(value)
        }

    override var rawMaxVelocityIntegralAccumulator: Double = 0.0
        set(value) {
            configMaxIntegralAccumulator(0, value, timeout)
            field = value
        }

    override var maxPositionIntegralAccumulator: Double
        get() = rawToPosition(rawMaxPositionIntegralAccumulator)
        set(value) {
            rawMaxPositionIntegralAccumulator = positionToRaw(value)
        }

    override var rawMaxPositionIntegralAccumulator: Double = 0.0
        set(value) {
            configMaxIntegralAccumulator(1, value, timeout)
            field = value
        }

    override val currIntegralAccumulator: Double
        get() = rawToVelocity(getIntegralAccumulator(0))

    override val rawCurrIntegralAccumulator: Double
        get() = getIntegralAccumulator(0)

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
        get() = rawToVelocity(getClosedLoopTarget(0))

    override val rawVelocityTarget: Double
        get() = getClosedLoopTarget(0)

    override val positionTarget: Double
        get() = rawToPosition(getClosedLoopTarget(1))

    override val rawPositionTarget: Double
        get() = getClosedLoopTarget(1)

    override var motionCruiseVelocity: Double = 0.0
        get() = rawToVelocity(rawMotionCruiseVelocity)
        set(value) {
            rawMotionCruiseVelocity = velocityToRaw(value)
            field = value
        }

    override var rawMotionCruiseVelocity: Double = 0.0
        set(value) {
            configMotionCruiseVelocity(value.toInt(), timeout)
            field = value
        }

    override var motionAcceleration: Double = 0.0
        get() = rawToVelocity(rawMotionAcceleration)
        set(value) {
            rawMotionAcceleration = velocityToRaw(value)
            field = value
        }

    override var rawMotionAcceleration: Double = 0.0
        set(value) {
            configMotionAcceleration(value.toInt(), timeout)
            field = value
        }

    override var motionSCurveStrength: Int = 0
        set(value) {
            configMotionSCurveStrength(value, timeout)
            field = value
        }

    override var continuousInputCurrentLimit: Int = 0
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set input current limit with SPX")
        }

    override var continuousStatorCurrentLimit: Int = 0
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set stator current limit with SPX")
        }

    override var peakInputCurrentLimit: Int = 100
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set input current limit with SPX")
        }

    override var peakStatorCurrentLimit: Int = 0
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set stator current limit with SPX")
        }

    override var peakCurrentDurationMs: Int = 0
        set(value) {
            field = value
            throw IncorrectControllerException("Can't set input current limit with SPX")
        }

    override val positionPID: PIDGains = PIDGains(1, 0.0, 0.0, 0.0, 0.0, 0.0)
    override val velocityPID: PIDGains = PIDGains(0, 0.0, 0.0, 0.0, 0.0, 0.0)

    init {
        positionPID.updateHook = {
            setRawPositionPID(positionPID.kP, positionPID.kI, positionPID.kD, positionPID.kF, positionPID.iZone)
        }
        velocityPID.updateHook = {
            setRawVelocityPID(velocityPID.kP, velocityPID.kI, velocityPID.kD, velocityPID.kF, velocityPID.iZone)
        }
    }

    override fun set(mode: GenericSmartMotorController.ControlMode, outputValue: Double) {
        when (mode) {
            GenericSmartMotorController.ControlMode.PERCENT_VBUS ->
                set(ControlMode.PercentOutput, outputValue)
            GenericSmartMotorController.ControlMode.VELOCITY ->
                set(ControlMode.Velocity, velocityToRaw(outputValue))
            GenericSmartMotorController.ControlMode.POSITION ->
                set(ControlMode.Position, positionToRaw(outputValue))
            GenericSmartMotorController.ControlMode.PROFILED ->
                set(ControlMode.MotionMagic, positionToRaw(outputValue))
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
        set(ControlMode.Velocity, velocityToRaw(velocity), DemandType.ArbitraryFeedForward, feedForward)
    }

    override fun setRawVelocityArbFeedforward(velocity: Double, feedForward: Double) {
        set(ControlMode.Velocity, velocity, DemandType.ArbitraryFeedForward, feedForward)
    }

    override fun resetToFactoryDefault() {
        configFactoryDefault(timeout)
    }

    override fun saveToFlash() {}

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
                throw IncorrectControllerException("SPX doesn't return primary feedback")
            GenericSmartMotorController.StatusFrame.MISC ->
                setStatusFramePeriod(StatusFrame.Status_6_Misc, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.COMM_STATUS ->
                setStatusFramePeriod(StatusFrame.Status_7_CommStatus, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.TARGETS ->
                setStatusFramePeriod(StatusFrame.Status_10_Targets, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.SECONDARY_FEEDBACK ->
                setStatusFramePeriod(StatusFrame.Status_12_Feedback1, periodMs, timeout)
            GenericSmartMotorController.StatusFrame.PRIMARY_PID ->
                throw IncorrectControllerException("SPX doesn't return primary PID")
            GenericSmartMotorController.StatusFrame.SECONDARY_PID ->
                throw IncorrectControllerException("SPX doesn't return secondary PID")
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
            GenericSmartMotorController.LimitSwitchSource.NONE ->
                configForwardLimitSwitchSource(LimitSwitchSource.Deactivated, limitSwitchNormal, timeout)
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
            GenericSmartMotorController.LimitSwitchSource.NONE ->
                configReverseLimitSwitchSource(RemoteLimitSwitchSource.Deactivated, limitSwitchNormal, timeout)
            GenericSmartMotorController.LimitSwitchSource.CONNECTED ->
                throw IncorrectControllerException("SPX does not support direct connected limit switch")
            GenericSmartMotorController.LimitSwitchSource.REMOTE_CANIFIER ->
                configReverseLimitSwitchSource(RemoteLimitSwitchSource.RemoteCANifier, limitSwitchNormal, timeout)
            GenericSmartMotorController.LimitSwitchSource.REMOTE_SRX ->
                configReverseLimitSwitchSource(RemoteLimitSwitchSource.RemoteTalonSRX, limitSwitchNormal, timeout)
        }
    }

    @SuppressWarnings("LongParameterList")
    private fun setRawVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        setRawPID(0, kP, kI, kD, kF, iZone)
    }

    @SuppressWarnings("LongParameterList")
    private fun setRawPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        setRawPID(1, kP, kI, kD, kF, iZone)
    }

    @SuppressWarnings("LongParameterList")
    private fun setRawPID(slotId: Int, kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        config_kP(slotId, kP, timeout)
        config_kI(slotId, kI, timeout)
        config_kD(slotId, kD, timeout)
        config_kF(slotId, kF, timeout)
        config_IntegralZone(slotId, iZone.toInt(), timeout)
    }

    override fun rawToVelocity(raw: Double): Double {
        return rawToPosition(raw) * 10
    }

    override fun velocityToRaw(units: Double): Double {
        return positionToRaw(units) / 10
    }
}
