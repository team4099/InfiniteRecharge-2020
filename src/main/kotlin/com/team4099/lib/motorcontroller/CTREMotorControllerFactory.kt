package com.team4099.lib.motorcontroller

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.RemoteLimitSwitchSource
import com.ctre.phoenix.motorcontrol.StatusFrame
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.ctre.phoenix.motorcontrol.can.TalonFx
import com.team4099.robot2020.config.Constants

/**
 * Creates CTRE motor controllers with consistent default configurations.
 */
object CTREMotorControllerFactory {
    /**
     * Represents the configuration of a Talon SRX or Victor SPX
     */
    @Suppress("MagicNumber")
    class Configuration {
        var enableLimitSwitch = false
        var limitSwitchSource = LimitSwitchSource.Deactivated
        var remoteLimitSwitchSource = RemoteLimitSwitchSource.Deactivated
        var limitSwitchNormallyOpen = LimitSwitchNormal.NormallyOpen

        var enableSoftLimit = false
        var forwardSoftLimit = 0
        var reverseSoftLimit = 0

        var maxOutputVoltage = 12.0
        var nominalVoltage = 0.0

        var neutralMode = NeutralMode.Coast
        var neutralDeadband = 0.04

        var enableVoltageCompensation = false
        var voltageCompensationLevel = 12.0

        var enableCurrentLimit = false
        var currentLimit = 0

        var inverted = false
        var sensorPhase = false

        var controlFramePeriodMs = 5
        var motionControlFramePeriodMs = 100
        var generalStatusFrameRateMs = 5
        var feedbackStatusFrameRateMs = 100
        var quadEncoderStatusFrameRateMs = 100
        var analogTempVbatStatusFrameMs = 100
        var pulseWidthStatusFrameMs = 100

        var velocityMeasurementPeriod = VelocityMeasPeriod.Period_100Ms
        var velocityMeasurementRollingAverageWindow = 64

        var voltageCompensationRampRate = 0.0
        var voltageRampRate = 0.0

        var motionMagicCruiseVelocity = 0
        var motionMagicAcceleration = 0

        var timeout = Constants.Universal.CTRE_CONFIG_TIMEOUT
    }

    val defaultConfiguration = Configuration()
    private val slaveConfiguration = Configuration()

    init {
        slaveConfiguration.controlFramePeriodMs = 1000
        slaveConfiguration.motionControlFramePeriodMs = 1000
        slaveConfiguration.generalStatusFrameRateMs = 1000
        slaveConfiguration.feedbackStatusFrameRateMs = 1000
        slaveConfiguration.quadEncoderStatusFrameRateMs = 1000
        slaveConfiguration.analogTempVbatStatusFrameMs = 1000
        slaveConfiguration.pulseWidthStatusFrameMs = 1000
    }

    /**
     * Create a Talon SRX with the default [Configuration].
     *
     * @param id The CAN ID of the Talon SRX to create.
     */
    fun createDefaultTalon(id: Int): TalonSRX {
        return createTalon(id, defaultConfiguration)
    }

    /**
     * Create a Victor SPX with the default [Configuration].
     *
     * @param id The CAN ID of the Victor SPX to create.
     */
    fun createDefaultVictor(id: Int): VictorSPX {
        return createVictor(id, defaultConfiguration)
    }

    /**
     * Create a Talon FX with the default [Configuration].
     *
     * @param id The CAN ID of the Talon FX to create
     */
    // fun createDefaultTalonFx(id: Int): TalonFx {
    //     return createTalonFx(id, defaultConfiguration)
    // }

    /**
     * Create a Talon SRX that follows another motor controller.
     *
     * @param id The CAN ID of the Talon SRX to create.
     * @param masterId The CAN ID of the motor controller to follow.
     */
    fun createPermanentSlaveTalon(id: Int, masterId: Int): TalonSRX {
        val talon = createTalon(id, slaveConfiguration)
        talon.set(ControlMode.Follower, masterId.toDouble())
        return talon
    }

    /**
     * Create a Victor SPX that follows another motor controller.
     *
     * @param id The CAN ID of the Victor SPX to create.
     * @param masterId The CAN ID of the motor controller to follow.
     */
    fun createPermanentSlaveVictor(id: Int, masterId: Int): VictorSPX {
        val victor = createVictor(id, slaveConfiguration)
        victor.set(ControlMode.Follower, masterId.toDouble())
        return victor
    }

    /**
     * Create a Talon SRX.
     *
     * @param id The CAN ID of the Talon SRX to create.
     * @param config The [Configuration] to use for this motor controller.
     */
    @Suppress("LongMethod")
    fun createTalon(id: Int, config: Configuration): TalonSRX {
        return LazyTalonSRX(id).apply {
            configFactoryDefault(config.timeout)
            set(ControlMode.PercentOutput, 0.0)
            changeMotionControlFramePeriod(config.motionControlFramePeriodMs)
            for (i in 0..10) {
                setIntegralAccumulator(0.0, i, config.timeout)
            }
            clearMotionProfileHasUnderrun(config.timeout)
            clearMotionProfileTrajectories()
            clearStickyFaults(config.timeout)
            configForwardLimitSwitchSource(
                    config.limitSwitchSource,
                    config.limitSwitchNormallyOpen,
                    config.timeout
            )
            configPeakOutputForward(config.maxOutputVoltage, config.timeout)
            configPeakOutputReverse(-config.maxOutputVoltage, config.timeout)
            configNominalOutputForward(config.nominalVoltage, config.timeout)
            configNominalOutputReverse(-config.nominalVoltage, config.timeout)

            configReverseLimitSwitchSource(
                    config.limitSwitchSource,
                    config.limitSwitchNormallyOpen,
                    config.timeout
            )
            setNeutralMode(config.neutralMode)
            configForwardSoftLimitEnable(config.enableSoftLimit, config.timeout)
            configReverseSoftLimitEnable(config.enableSoftLimit, config.timeout)
            overrideSoftLimitsEnable(config.enableSoftLimit)
            overrideLimitSwitchesEnable(config.enableLimitSwitch)

            inverted = config.inverted
            setSensorPhase(config.sensorPhase)

            sensorCollection.setAnalogPosition(0, config.timeout)
            enableCurrentLimit(config.enableCurrentLimit)
            configContinuousCurrentLimit(config.currentLimit, config.timeout)

            configForwardSoftLimitThreshold(config.forwardSoftLimit, config.timeout)

            selectProfileSlot(0, 0)
            sensorCollection.setQuadraturePosition(0, config.timeout)
            sensorCollection.setAnalogPosition(0, config.timeout)
            sensorCollection.setPulseWidthPosition(0, config.timeout)
            configReverseSoftLimitThreshold(config.reverseSoftLimit, config.timeout)
            configVelocityMeasurementPeriod(config.velocityMeasurementPeriod, config.timeout)
            configVelocityMeasurementWindow(config.velocityMeasurementRollingAverageWindow, config.timeout)
            configClosedloopRamp(config.voltageCompensationRampRate, config.timeout)
            configOpenloopRamp(config.voltageRampRate, config.timeout)

            enableVoltageCompensation(config.enableVoltageCompensation)
            configVoltageCompSaturation(config.voltageCompensationLevel, config.timeout)

            configNeutralDeadband(config.neutralDeadband, config.timeout)

            configMotionCruiseVelocity(config.motionMagicCruiseVelocity, config.timeout)
            configMotionAcceleration(config.motionMagicAcceleration, config.timeout)

            setStatusFramePeriod(
                    StatusFrameEnhanced.Status_1_General,
                    config.generalStatusFrameRateMs,
                    config.timeout
            )
            setStatusFramePeriod(
                    StatusFrameEnhanced.Status_2_Feedback0,
                    config.feedbackStatusFrameRateMs,
                    config.timeout
            )
            setStatusFramePeriod(
                    StatusFrameEnhanced.Status_3_Quadrature,
                    config.quadEncoderStatusFrameRateMs,
                    config.timeout
            )
            setStatusFramePeriod(
                    StatusFrameEnhanced.Status_4_AinTempVbat,
                    config.analogTempVbatStatusFrameMs,
                    config.timeout
            )
            setStatusFramePeriod(
                    StatusFrameEnhanced.Status_8_PulseWidth,
                    config.pulseWidthStatusFrameMs,
                    config.timeout
            )
        }
    }

    /**
     * Create a Victor SPX.
     *
     * @param id The CAN ID of the Victor SPX to create.
     * @param config The [Configuration] to use for this motor controller.
     */
    @Suppress("LongMethod")
    fun createVictor(id: Int, config: Configuration): VictorSPX {
        return LazyVictorSPX(id).apply {
            configFactoryDefault(config.timeout)
            set(ControlMode.PercentOutput, 0.0)
            changeMotionControlFramePeriod(config.motionControlFramePeriodMs)
            for (i in 0..10) {
                setIntegralAccumulator(0.0, i, config.timeout)
            }
            clearMotionProfileHasUnderrun(config.timeout)
            clearMotionProfileTrajectories()
            clearStickyFaults(config.timeout)
            configForwardLimitSwitchSource(
                    config.remoteLimitSwitchSource,
                    config.limitSwitchNormallyOpen,
                    config.timeout
            )
            configPeakOutputForward(config.maxOutputVoltage, config.timeout)
            configPeakOutputReverse(-config.maxOutputVoltage, config.timeout)
            configNominalOutputForward(config.nominalVoltage, config.timeout)
            configNominalOutputReverse(-config.nominalVoltage, config.timeout)

            configReverseLimitSwitchSource(
                    config.remoteLimitSwitchSource,
                    config.limitSwitchNormallyOpen,
                    config.timeout
            )
            setNeutralMode(config.neutralMode)
            configForwardSoftLimitEnable(config.enableSoftLimit, config.timeout)
            configReverseSoftLimitEnable(config.enableSoftLimit, config.timeout)
            overrideSoftLimitsEnable(config.enableSoftLimit)
            overrideLimitSwitchesEnable(config.enableLimitSwitch)

            inverted = config.inverted
            setSensorPhase(config.sensorPhase)

            configForwardSoftLimitThreshold(config.forwardSoftLimit, config.timeout)

            selectProfileSlot(0, 0)
            configReverseSoftLimitThreshold(config.reverseSoftLimit, config.timeout)
            configVelocityMeasurementPeriod(config.velocityMeasurementPeriod, config.timeout)
            configVelocityMeasurementWindow(config.velocityMeasurementRollingAverageWindow, config.timeout)
            configClosedloopRamp(config.voltageCompensationRampRate, config.timeout)
            configOpenloopRamp(config.voltageRampRate, config.timeout)

            enableVoltageCompensation(config.enableVoltageCompensation)
            configVoltageCompSaturation(config.voltageCompensationLevel, config.timeout)

            configNeutralDeadband(config.neutralDeadband, config.timeout)

            configMotionCruiseVelocity(config.motionMagicCruiseVelocity, config.timeout)
            configMotionAcceleration(config.motionMagicAcceleration, config.timeout)

            setStatusFramePeriod(
                    StatusFrame.Status_1_General,
                    config.generalStatusFrameRateMs,
                    config.timeout
            )
            setStatusFramePeriod(
                    StatusFrame.Status_2_Feedback0,
                    config.feedbackStatusFrameRateMs,
                    config.timeout
            )
            setStatusFramePeriod(
                    StatusFrame.Status_4_AinTempVbat,
                    config.analogTempVbatStatusFrameMs,
                    config.timeout
            )
        }
    }

//    @Suppress("LongMethod")
//    fun createTalonFx(id: Int, config: Configuration): TalonFx {
//        return TalonFx(id).apply {
//            configFactoryDefault(config.timeout)
//            set(ControlMode.PercentOutput, 0.0)
//            changeMotionControlFramePeriod(config.motionControlFramePeriodMs)
//
//        }
//    }
}


