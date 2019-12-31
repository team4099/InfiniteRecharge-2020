package org.usfirst.frc.team4099.lib.motorcontroller

import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.revrobotics.AlternateEncoderType
import com.revrobotics.CANAnalog
import com.revrobotics.CANDigitalInput
import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType
import com.revrobotics.EncoderType
import kotlin.math.PI

class GenericSparkMax(
    override val id: Int,
    override val timeout: Int,
    private val brush: MotorType
) : GenericSmartMotorController, CANSparkMax(id, brush) {
    private var enc = encoder
    private var analog = getAnalog(CANAnalog.AnalogMode.kAbsolute)
    private var pid = pidController
    private var forwardLimit = getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen)
    private var reverseLimit = getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen)

    init {
        setCANTimeout(timeout)
    }

    override var master: GenericSmartMotorController = this
        set(value) {
            handleFollow(value, false)
            field = value
        }
    override var idleMode: GenericSmartMotorController.IdleMode = GenericSmartMotorController.IdleMode.COAST
        set(value) {
            when (value) {
                GenericSmartMotorController.IdleMode.BRAKE -> setIdleMode(IdleMode.kBrake)
                GenericSmartMotorController.IdleMode.COAST -> setIdleMode(IdleMode.kCoast)
            }
            field = value
        }

    // TODO: figure out how to invert spark max external encoder
    override var sensorAgreesWithInversion: Boolean = true
        set(value) {
            field = value
        }

    override var inverted: GenericSmartMotorController.InvertType = GenericSmartMotorController.InvertType.NONE
        set(value) {
            when (value) {
                GenericSmartMotorController.InvertType.NONE -> setInverted(false)
                GenericSmartMotorController.InvertType.INVERT -> setInverted(true)
                GenericSmartMotorController.InvertType.FOLLOW_MASTER -> handleFollow(master, false)
                GenericSmartMotorController.InvertType.OPPOSE_MASTER -> handleFollow(master, true)
            }
            field = value
        }

    override var openLoopRampSecs: Double = 0.0
        set(value) {
            openLoopRampRate = value
            field = value
        }

    override var closedLoopRampSecs: Double = 0.0
        set(value) {
            closedLoopRampRate = value
            field = value
        }

    override var peakOutputForward: Double = 1.0

    override var peakOutputReverse: Double = -1.0

    override var nominalOutputForward: Double = 0.0

    override var nominalOutputReverse: Double = 0.0

    override var neutralDeadband: Double = 0.0

    override var maxCompensatedVoltage: Double = 0.0

    override var useVoltageCompensation: Boolean = false
        set(value) {
            if (value) {
                enableVoltageCompensation(maxCompensatedVoltage)
            } else {
                disableVoltageCompensation()
            }
            field = value
        }

    override val inputVoltage: Double
        get() = busVoltage

    override val motorOutput: Double
        get() = appliedOutput

    override val motorOutputVolts: Double
        get() = inputVoltage * motorOutput

    override var selectedEncoder: GenericSmartMotorController.EncoderType =
        GenericSmartMotorController.EncoderType.INTERNAL_QUADRATURE
        set(value) {
            when (value) {
                GenericSmartMotorController.EncoderType.NONE -> {
                    enc = getEncoder(EncoderType.kNoSensor, 0)
                    pid.setFeedbackDevice(enc)
                }
                GenericSmartMotorController.EncoderType.INTERNAL_QUADRATURE -> {
                    enc = getEncoder(EncoderType.kHallSensor, 0)
                    pid.setFeedbackDevice(enc)
                }
                GenericSmartMotorController.EncoderType.EXTERNAL_QUADRATURE -> {
                    enc = getAlternateEncoder(AlternateEncoderType.kQuadrature, encoderPPR)
                    pid.setFeedbackDevice(enc)
                }
                GenericSmartMotorController.EncoderType.ABSOLUTE_PWM ->
                    throw IncorrectControllerException("Spark MAX doesn't support absolute PWM encoder")
                GenericSmartMotorController.EncoderType.ABSOLUTE_ANALOG -> {
                    analog = getAnalog(CANAnalog.AnalogMode.kAbsolute)
                    pid.setFeedbackDevice(analog)
                }
                GenericSmartMotorController.EncoderType.CAN_QUADRATURE ->
                    throw IncorrectControllerException("Spark MAX doesn't support CAN encoders")
                GenericSmartMotorController.EncoderType.CAN_ABSOLUTE_PWM ->
                    throw IncorrectControllerException("Spark MAX doesn't support CAN encoders")
                GenericSmartMotorController.EncoderType.CAN_ABSOLUTE_ANALOG ->
                    throw IncorrectControllerException("Spark MAX doesn't support CAN encoders")
            }
            field = value
        }

    override var encoderPPR: Int = 1
        set(value) {
            if (selectedEncoder == GenericSmartMotorController.EncoderType.EXTERNAL_QUADRATURE) {
                enc = getAlternateEncoder(AlternateEncoderType.kQuadrature, value)
                pid.setFeedbackDevice(enc)
            }
            field = value
        }

    override var encoderToUnits: Double = 1.0

    override var encoderPosition: Double
        get() = rawToPosition(enc.position)
        set(value) {
            enc.position = positionToRaw(value)
        }

    override val encoderVelocity: Double
        get() = rawToVelocity(enc.velocity)

    override var rawEncoderPosition: Double
        get() = enc.position
        set(value) {
            enc.position = value
        }

    override val rawEncoderVelocity: Double
        get() = enc.velocity

    override var velocityMeasPeriod: GenericSmartMotorController.VelocityMeasPeriod =
        GenericSmartMotorController.VelocityMeasPeriod.MS_100
        set(value) {
            when (value) {
                GenericSmartMotorController.VelocityMeasPeriod.MS_1 -> enc.measurementPeriod = 1
                GenericSmartMotorController.VelocityMeasPeriod.MS_2 -> enc.measurementPeriod = 2
                GenericSmartMotorController.VelocityMeasPeriod.MS_5 -> enc.measurementPeriod = 5
                GenericSmartMotorController.VelocityMeasPeriod.MS_10 -> enc.measurementPeriod = 10
                GenericSmartMotorController.VelocityMeasPeriod.MS_20 -> enc.measurementPeriod = 20
                GenericSmartMotorController.VelocityMeasPeriod.MS_25 -> enc.measurementPeriod = 25
                GenericSmartMotorController.VelocityMeasPeriod.MS_50 -> enc.measurementPeriod = 50
                GenericSmartMotorController.VelocityMeasPeriod.MS_100 -> enc.measurementPeriod = 100
            }
            field = value
        }

    override var velocityMeasWindow: Int = 64
        set(value) {
            enc.averageDepth = value
            field = value
        }

    override var useLimitSwitches: Boolean = true
        set(value) {}

    override var useSoftLimits: Boolean = true
        set(value) {}

    override var useForwardSoftLimit: Boolean = false
        set(value) {
            enableSoftLimit(SoftLimitDirection.kForward, value)
            field = value
        }

    override var useReverseSoftLimit: Boolean = false
        set(value) {
            enableSoftLimit(SoftLimitDirection.kReverse, value)
            field = value
        }

    override var forwardSoftLimitValue: Double
        get() = rawToPosition(rawForwardSoftLimitValue)
        set(value) {
            rawForwardSoftLimitValue = positionToRaw(value)
        }

    override var rawForwardSoftLimitValue: Double = 0.0
        set(value) {
            setSoftLimit(SoftLimitDirection.kForward, value.toFloat())
            field = value
        }

    override var reverseSoftLimitValue: Double
        get() = rawToPosition(rawReverseSoftLimitValue)
        set(value) {
            rawReverseSoftLimitValue = positionToRaw(value)
        }

    override var rawReverseSoftLimitValue: Double = 0.0
        set(value) {
            setSoftLimit(SoftLimitDirection.kReverse, value.toFloat())
            field = value
        }

    override var allowableVelocityError: Double
        get() = rawToVelocity(rawAllowableVelocityError)
        set(value) {
            rawAllowableVelocityError = velocityToRaw(value)
        }

    override var rawAllowableVelocityError: Double = 0.0
        set(value) {}

    override var allowablePositionError: Double
        get() = rawToPosition(rawAllowablePositionError)
        set(value) {
            rawAllowablePositionError = positionToRaw(value)
        }

    override var rawAllowablePositionError: Double = 0.0
        set(value) {}

    override var maxVelocityIntegralAccumulator: Double
        get() = rawToVelocity(rawMaxVelocityIntegralAccumulator)
        set(value) {
            rawMaxVelocityIntegralAccumulator = velocityToRaw(value)
        }

    override var rawMaxVelocityIntegralAccumulator: Double
        get() = pid.getIMaxAccum(0)
        set(value) {
            pid.setIMaxAccum(value, 0)
        }

    override var positionIntegralAccumulator: Double
        get() = rawToPosition(rawMaxPositionIntegralAccumulator)
        set(value) {
            rawMaxPositionIntegralAccumulator = positionToRaw(value)
        }

    override var rawMaxPositionIntegralAccumulator: Double
        get() = pid.getIMaxAccum(1)
        set(value) {
            pid.setIMaxAccum(value, 1)
        }

    override val currIntegralAccumulator: Double
        get() = rawToVelocity(rawCurrIntegralAccumulator)

    override val rawCurrIntegralAccumulator: Double
        get() = pid.iAccum

    override var velocityPeakOutput: Double = 1.0
        set(value) {
            pid.setOutputRange(0.0, value, 0)
            field = value
        }

    override var positionPeakOutput: Double = 1.0
        set(value) {
            pid.setOutputRange(0.0, value, 1)
            field = value
        }

    override val velocityTarget: Double
        get() = rawToVelocity(rawVelocityTarget)

    override val rawVelocityTarget: Double
        get() = TODO("not implemented")

    override val positionTarget: Double
        get() = rawToPosition(rawPositionTarget)

    override val rawPositionTarget: Double
        get() = TODO("not implemented")

    override var motionCruiseVelocity: Double
        get() = rawToVelocity(rawMotionCruiseVelocity)
        set(value) {
            rawMotionCruiseVelocity = velocityToRaw(value)
        }

    override var rawMotionCruiseVelocity: Double
        get() = pid.getSmartMotionMaxVelocity(0)
        set(value) {
            pid.setSmartMotionMaxVelocity(value, 0)
        }

    override var motionAcceleration: Double
        get() = rawToVelocity(rawMotionAcceleration)
        set(value) {
            rawMotionAcceleration = velocityToRaw(value)
        }

    override var rawMotionAcceleration: Double
        get() = pid.getSmartMotionMaxAccel(0)
        set(value) {
            pid.setSmartMotionMaxAccel(value, 0)
        }

    override var motionSCurveStrength: Int = 8
        get() = if (pid.getSmartMotionAccelStrategy(0) == CANPIDController.AccelStrategy.kSCurve) 8 else 0
        set(value) {
            field = if (value > 0) {
                pid.setSmartMotionAccelStrategy(CANPIDController.AccelStrategy.kSCurve, 0)
                8
            } else {
                pid.setSmartMotionAccelStrategy(CANPIDController.AccelStrategy.kTrapezoidal, 0)
                0
            }
        }

    override var continuousInputCurrentLimit: Int
        get() = TODO("not implemented")
        set(value) {}

    override var continuousStatorCurrentLimit: Int = 80
        set(value) {
            setSmartCurrentLimit(value)
            field = value
        }

    override var peakInputCurrentLimit: Int
        get() = TODO("not implemented")
        set(value) {}

    override var peakStatorCurrentLimit: Int = 120
        set(value) {
            setSecondaryCurrentLimit(value.toDouble())
            field = value
        }

    override var peakCurrentDurationMs: Int
        get() = TODO("not implemented")
        set(value) {}

    override fun set(mode: GenericSmartMotorController.ControlMode, outputValue: Double) {
        when (mode) {
            GenericSmartMotorController.ControlMode.PERCENT_VBUS -> {
                // TODO: check if this is the same thing as just calling set()
                pid.setReference(outputValue, ControlType.kDutyCycle)
            }
            GenericSmartMotorController.ControlMode.VELOCITY -> {
                pid.setReference(velocityToRaw(outputValue), ControlType.kVelocity, 0)
            }
            GenericSmartMotorController.ControlMode.POSITION -> {
                pid.setReference(positionToRaw(outputValue), ControlType.kPosition, 1)
            }
            GenericSmartMotorController.ControlMode.PROFILED -> {
                pid.setReference(positionToRaw(outputValue), ControlType.kSmartMotion, 0)
            }
        }
    }

    override fun setRaw(mode: GenericSmartMotorController.ControlMode, outputValue: Double) {
        when (mode) {
            GenericSmartMotorController.ControlMode.PERCENT_VBUS -> {
                // TODO: check if this is the same thing as just calling set()
                pid.setReference(outputValue, ControlType.kDutyCycle)
            }
            GenericSmartMotorController.ControlMode.VELOCITY -> {
                pid.setReference(outputValue, ControlType.kVelocity, 0)
            }
            GenericSmartMotorController.ControlMode.POSITION -> {
                pid.setReference(outputValue, ControlType.kPosition, 1)
            }
            GenericSmartMotorController.ControlMode.PROFILED -> {
                pid.setReference(outputValue, ControlType.kSmartMotion, 0)
            }
        }
    }

    override fun setVelocityArbFeedforward(velocity: Double, feedForward: Double) {
        setRawVelocityArbFeedforward(velocityToRaw(velocity), feedForward)
    }

    override fun setRawVelocityArbFeedforward(velocity: Double, feedForward: Double) {
        pid.setReference(velocity, ControlType.kVelocity, 0, feedForward, CANPIDController.ArbFFUnits.kPercentOut)
    }

    override fun resetToFactoryDefault() {
        restoreFactoryDefaults(true)
    }

    override fun saveToFlash() {
        burnFlash()
    }

    override fun setControlFramePeriod(frame: GenericSmartMotorController.ControlFrame, periodMs: Int) {
        setControlFramePeriodMs(periodMs)
    }

    override fun setStatusFramePeriod(frame: GenericSmartMotorController.StatusFrame, periodMs: Int) {
        when (frame) {
            GenericSmartMotorController.StatusFrame.GENERAL -> setPeriodicFramePeriod(PeriodicFrame.kStatus0, periodMs)
            GenericSmartMotorController.StatusFrame.PRIMARY_FEEDBACK -> {
                setPeriodicFramePeriod(PeriodicFrame.kStatus1, periodMs)
                setPeriodicFramePeriod(PeriodicFrame.kStatus2, periodMs)
            }
            else -> {}
        }
    }

    override fun setForwardLimitSwitch(
        source: GenericSmartMotorController.LimitSwitchSource,
        normal: GenericSmartMotorController.LimitSwitchNormal
    ) {
        when (source) {
            GenericSmartMotorController.LimitSwitchSource.CONNECTED -> {
                forwardLimit = when (normal) {
                    GenericSmartMotorController.LimitSwitchNormal.NORMALLY_OPEN ->
                        getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen)
                    GenericSmartMotorController.LimitSwitchNormal.NORMALLY_CLOSED ->
                        getForwardLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyClosed)
                }
                forwardLimit.enableLimitSwitch(true)
            }
            GenericSmartMotorController.LimitSwitchSource.REMOTE_CANIFIER ->
                throw IncorrectControllerException("Spark MAX doesn't support remote limits")
            GenericSmartMotorController.LimitSwitchSource.REMOTE_SRX ->
                throw IncorrectControllerException("Spark MAX doesn't support remote limits")
        }
    }

    override fun setReverseLimitSwitch(
        source: GenericSmartMotorController.LimitSwitchSource,
        normal: GenericSmartMotorController.LimitSwitchNormal
    ) {
        when (source) {
            GenericSmartMotorController.LimitSwitchSource.CONNECTED -> {
                reverseLimit = when (normal) {
                    GenericSmartMotorController.LimitSwitchNormal.NORMALLY_OPEN ->
                        getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyOpen)
                    GenericSmartMotorController.LimitSwitchNormal.NORMALLY_CLOSED ->
                        getReverseLimitSwitch(CANDigitalInput.LimitSwitchPolarity.kNormallyClosed)
                }
                reverseLimit.enableLimitSwitch(true)
            }
            GenericSmartMotorController.LimitSwitchSource.REMOTE_CANIFIER ->
                throw IncorrectControllerException("Spark MAX doesn't support remote limits")
            GenericSmartMotorController.LimitSwitchSource.REMOTE_SRX ->
                throw IncorrectControllerException("Spark MAX doesn't support remote limits")
        }
    }

    override fun setVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        // TODO: Figure out real PID units and do conversion here
        setRawVelocityPID(kP, kI, kD, kF, iZone)
    }

    override fun setRawVelocityPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        setRawPID(0, kP, kI, kD, kF, iZone)
    }

    override fun setPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        // TODO: Figure out real PID units and do conversion here
        setRawPositionPID(kP, kI, kD, kF, iZone)
    }

    override fun setRawPositionPID(kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        setRawPID(1, kP, kI, kD, kF, iZone)
    }

    override fun setRawPID(slotId: Int, kP: Double, kI: Double, kD: Double, kF: Double, iZone: Double) {
        pid.setP(kP, slotId)
        pid.setI(kI, slotId)
        pid.setD(kD, slotId)
        pid.setFF(kF, slotId)
        pid.setIZone(iZone, slotId)
    }

    override fun rawToVelocity(raw: Double): Double {
        return rawToPosition(raw) / 60
    }

    override fun velocityToRaw(units: Double): Double {
        return positionToRaw(units) * 60
    }

    override fun rawToPosition(raw: Double): Double {
        return raw / encoderToUnits * (2 * PI)
    }

    override fun positionToRaw(units: Double): Double {
        return (units * encoderToUnits / (2 * PI))
    }

    private fun handleFollow(master: GenericSmartMotorController, inverted: Boolean) {
        if (master is CANSparkMax) {
            follow(master, inverted)
        } else if (master is TalonSRX || master is VictorSPX) {
            follow(ExternalFollower.kFollowerPhoenix, master.id, inverted)
        }
    }
}
