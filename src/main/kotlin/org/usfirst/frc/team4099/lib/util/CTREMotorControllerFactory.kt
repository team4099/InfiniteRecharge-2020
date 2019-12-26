package org.usfirst.frc.team4099.lib.util

import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX

/**
 * Creates CTRE motor controllers with consistent default configurations.
 */
object CTREMotorControllerFactory {
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
        var nominalClosedLoopVoltage = 12.0

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

        var timeout = 0
    }

    private val defaultConfiguration = Configuration()
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

    fun createDefaultTalon(id: Int): TalonSRX {
        return createTalon(id, defaultConfiguration)
    }

    fun createDefaultVictor(id: Int): VictorSPX {
        return createVictor(id, defaultConfiguration)
    }

    fun createPermanentSlaveTalon(id: Int, masterId: Int): TalonSRX {
        val talon = createTalon(id, slaveConfiguration)
        talon.set(ControlMode.Follower, masterId.toDouble())
        return talon
    }

    fun createPermanentSlaveVictor(id: Int, masterId: Int): VictorSPX {
        val victor = createVictor(id, slaveConfiguration)
        victor.set(ControlMode.Follower, masterId.toDouble())
        return victor
    }

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
}
