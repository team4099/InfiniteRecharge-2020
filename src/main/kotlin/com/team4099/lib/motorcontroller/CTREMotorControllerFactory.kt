package com.team4099.lib.motorcontroller

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.InvertType
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.RemoteLimitSwitchSource
import com.ctre.phoenix.motorcontrol.StatusFrame
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.team4099.robot2020.config.Constants

/**
 * Creates CTRE motor controllers with consistent default configurations.
 */
object CTREMotorControllerFactory {
    private const val SLAVE_FRAME_PERIOD_MS = 1000

    /**
     * Represents the configuration of a Talon FX, Talon SRX or, Victor SPX
     */
    @Suppress("MagicNumber")
    data class Configuration(
        var enableLimitSwitch: Boolean = false,
        var limitSwitchSource: LimitSwitchSource = LimitSwitchSource.Deactivated,
        var remoteLimitSwitchSource: RemoteLimitSwitchSource = RemoteLimitSwitchSource.Deactivated,
        var limitSwitchNormallyOpen: LimitSwitchNormal = LimitSwitchNormal.NormallyOpen,
        var enableSoftLimit: Boolean = false,
        var forwardSoftLimit: Int = 0,
        var reverseSoftLimit: Int = 0,

        var maxOutputVoltage: Double = 12.0,
        var nominalVoltage: Double = 0.0,

        var neutralMode: NeutralMode = NeutralMode.Coast,
        var neutralDeadband: Double = 0.04,

        var enableVoltageCompensation: Boolean = false,
        var voltageCompensationLevel: Double = 12.0,

        var enableCurrentLimit: Boolean = false,
        var currentLimit: Int = 0,

        var inverted: Boolean = false,
        var sensorPhase: Boolean = false,

        var controlFramePeriodMs: Int = 5,
        var motionControlFramePeriodMs: Int = 100,
        var generalStatusFrameRateMs: Int = 5,
        var feedbackStatusFrameRateMs: Int = 100,
        var quadEncoderStatusFrameRateMs: Int = 100,
        var analogTempVbatStatusFrameMs: Int = 100,
        var pulseWidthStatusFrameMs: Int = 100,

        var velocityMeasurementPeriod: VelocityMeasPeriod = VelocityMeasPeriod.Period_100Ms,
        var velocityMeasurementRollingAverageWindow: Int = 64,

        var voltageCompensationRampRate: Double = 0.0,
        var voltageRampRate: Double = 0.0,

        var motionMagicCruiseVelocity: Int = 0,
        var motionMagicAcceleration: Int = 0,

        var timeout: Int = Constants.Universal.CTRE_CONFIG_TIMEOUT
    )

    val defaultConfiguration = Configuration()

    /**
     * Create a Talon SRX with the default [Configuration].
     *
     * @param id The CAN ID of the Talon SRX to create.
     */
    fun createDefaultTalonSRX(id: Int): TalonSRX {
        return createTalonSRX(id, defaultConfiguration)
    }

    /**
     * Create a Victor SPX with the default [Configuration].
     *
     * @param id The CAN ID of the Victor SPX to create.
     */
    fun createDefaultVictorSPX(id: Int): VictorSPX {
        return createVictorSPX(id, defaultConfiguration)
    }

    /**
     * Create a Talon FX with the default [Configuration].
     *
     * @param id The CAN ID of the Talon FX to create
     */
    fun createDefaultTalonFX(id: Int): TalonFX {
        return createTalonFX(id, defaultConfiguration)
    }

    /**
     * Create a Talon SRX that follows another motor controller.
     *
     * @param id The CAN ID of the Talon SRX to create.
     * @param masterId The CAN ID of the motor controller to follow.
     */
    fun createPermanentSlaveTalonSRX(
        id: Int,
        masterId: Int,
        config: Configuration = defaultConfiguration,
        invertToMaster: Boolean = false
    ): TalonSRX {
        val slaveConfiguration = config.copy(
            controlFramePeriodMs = SLAVE_FRAME_PERIOD_MS,
            motionControlFramePeriodMs = SLAVE_FRAME_PERIOD_MS,
            generalStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            feedbackStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            quadEncoderStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            analogTempVbatStatusFrameMs = SLAVE_FRAME_PERIOD_MS,
            pulseWidthStatusFrameMs = SLAVE_FRAME_PERIOD_MS,
            neutralDeadband = 0.0
        )
        val talon = createTalonSRX(id, slaveConfiguration)
        talon.set(ControlMode.Follower, masterId.toDouble())
        talon.setInverted(if (invertToMaster) InvertType.OpposeMaster else InvertType.FollowMaster)
        return talon
    }

    /**
     * Create a Victor SPX that follows another motor controller.
     *
     * @param id The CAN ID of the Victor SPX to create.
     * @param masterId The CAN ID of the motor controller to follow.
     */
    fun createPermanentSlaveVictorSPX(
        id: Int,
        masterId: Int,
        config: Configuration = defaultConfiguration,
        invertToMaster: Boolean = false
    ): VictorSPX {
        val slaveConfiguration = config.copy(
            controlFramePeriodMs = SLAVE_FRAME_PERIOD_MS,
            motionControlFramePeriodMs = SLAVE_FRAME_PERIOD_MS,
            generalStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            feedbackStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            quadEncoderStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            analogTempVbatStatusFrameMs = SLAVE_FRAME_PERIOD_MS,
            pulseWidthStatusFrameMs = SLAVE_FRAME_PERIOD_MS,
            neutralDeadband = 0.0
        )
        val victor = createVictorSPX(id, slaveConfiguration)
        victor.set(ControlMode.Follower, masterId.toDouble())
        victor.setInverted(if (invertToMaster) InvertType.OpposeMaster else InvertType.FollowMaster)
        return victor
    }

    /**
     * Create a Talon FX that follows another motor controller.
     *
     * @param id The CAN ID of the Talon FX to create.
     * @param masterId The CAN ID of the motor controller to follow.
     */
    fun createPermanentSlaveTalonFX(
        id: Int,
        masterId: Int,
        config: Configuration = defaultConfiguration,
        invertToMaster: Boolean = false
    ): TalonFX {
        val slaveConfiguration = config.copy(
            controlFramePeriodMs = SLAVE_FRAME_PERIOD_MS,
            motionControlFramePeriodMs = SLAVE_FRAME_PERIOD_MS,
            generalStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            feedbackStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            quadEncoderStatusFrameRateMs = SLAVE_FRAME_PERIOD_MS,
            analogTempVbatStatusFrameMs = SLAVE_FRAME_PERIOD_MS,
            pulseWidthStatusFrameMs = SLAVE_FRAME_PERIOD_MS,
            neutralDeadband = 0.0
        )
        val talonFX = createTalonFX(id, slaveConfiguration)
        talonFX.set(ControlMode.Follower, masterId.toDouble())
        talonFX.setInverted(if (invertToMaster) InvertType.OpposeMaster else InvertType.FollowMaster)
        return talonFX
    }

    /**
     * Create a Talon SRX.
     *
     * @param id The CAN ID of the Talon SRX to create.
     * @param config The [Configuration] to use for this motor controller.
     */
    @Suppress("LongMethod")
    fun createTalonSRX(id: Int, config: Configuration): TalonSRX {
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
    fun createVictorSPX(id: Int, config: Configuration): VictorSPX {
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

    /**
     * Create a Talon FX.
     *
     * @param id The CAN ID of the Talon FX to create.
     * @param config The [Configuration] to use for this motor controller.
     */
    @Suppress("LongMethod")
    fun createTalonFX(id: Int, config: Configuration): TalonFX {
        return LazyTalonFX(id).apply {
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
}
