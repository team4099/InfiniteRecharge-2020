package org.usfirst.frc.team4099.lib.util

import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import edu.wpi.first.wpilibj.MotorSafety


object CANMotorControllerFactory {

    class Configuration {
        var LIMIT_SWITCH_SOURCE = LimitSwitchSource.Deactivated
        var LIMIT_SWITCH_NORMALLY_OPEN = LimitSwitchNormal.NormallyOpen
        var MAX_OUTPUT_VOLTAGE = 12.0
        var NOMINAL_VOLTAGE = 0.0
        var PEAK_VOLTAGE = 12.0
        var ENABLE_BRAKE = NeutralMode.Coast
        var ENABLE_CURRENT_LIMIT = false
        var ENABLE_SOFT_LIMIT = false
        var ENABLE_LIMIT_SWITCH = false
        var CURRENT_LIMIT = 0
        var EXPIRATION_TIMEOUT_SECONDS = MotorSafety.DEFAULT_SAFETY_EXPIRATION
        var FORWARD_SOFT_LIMIT = 0
        var INVERTED = false
        var NOMINAL_CLOSED_LOOP_VOLTAGE = 12.0
        var REVERSE_SOFT_LIMIT = 0
        var SAFETY_ENABLED = false

        var CONTROL_FRAME_PERIOD_MS = 5
        var MOTION_CONTROL_FRAME_PERIOD_MS = 100
        var GENERAL_STATUS_FRAME_RATE_MS = 5
        var FEEDBACK_STATUS_FRAME_RATE_MS = 100
        var QUAD_ENCODER_STATUS_FRAME_RATE_MS = 100
        var ANALOG_TEMP_VBAT_STATUS_FRAME_RATE_MS = 100
        var PULSE_WIDTH_STATUS_FRAME_RATE_MS = 100

        var VELOCITY_MEASUREMENT_PERIOD = VelocityMeasPeriod.Period_100Ms
        var VELOCITY_MEASUREMENT_ROLLING_AVERAGE_WINDOW = 64

        var VOLTAGE_COMPENSATION_RAMP_RATE = 0.0
        var VOLTAGE_RAMP_RATE = 0.0

        var TIMEOUT = 0
    }

    private val kDefaultConfiguration = Configuration()
    private val kSlaveConfiguration = Configuration()

    init {
        kSlaveConfiguration.CONTROL_FRAME_PERIOD_MS = 1000;
        kSlaveConfiguration.MOTION_CONTROL_FRAME_PERIOD_MS = 1000;
        kSlaveConfiguration.GENERAL_STATUS_FRAME_RATE_MS = 1000;
        kSlaveConfiguration.FEEDBACK_STATUS_FRAME_RATE_MS = 1000;
        kSlaveConfiguration.QUAD_ENCODER_STATUS_FRAME_RATE_MS = 1000;
        kSlaveConfiguration.ANALOG_TEMP_VBAT_STATUS_FRAME_RATE_MS = 1000;
        kSlaveConfiguration.PULSE_WIDTH_STATUS_FRAME_RATE_MS = 1000;
    }

    fun createDefaultTalon(id: Int): TalonSRX {
        return createTalon(id, kDefaultConfiguration)
    }

    fun createPermanentSlaveTalon(id: Int, masterId: Int): TalonSRX {
        val talon = createTalon(id, kSlaveConfiguration)
        talon.set(ControlMode.Follower, masterId.toDouble())
        return talon
    }

    fun createPermanentSlaveVictor(id: Int, master: IMotorController): VictorSPX {
        val victor = VictorSPX(id)
        victor.follow(master)
        return victor
    }

    fun createTalon(id: Int, config: Configuration): TalonSRX {

        val talon = LazyTalonSRX(id).apply {
            set(ControlMode.PercentOutput, 0.0)
            changeMotionControlFramePeriod(config.MOTION_CONTROL_FRAME_PERIOD_MS)
            for (i in 0 .. 10) {
                setIntegralAccumulator(0.0, i, config.TIMEOUT)
            }
            clearMotionProfileHasUnderrun(config.TIMEOUT)
            clearMotionProfileTrajectories()
            clearStickyFaults(config.TIMEOUT)
            configForwardLimitSwitchSource(config.LIMIT_SWITCH_SOURCE, config.LIMIT_SWITCH_NORMALLY_OPEN, config.TIMEOUT)
            configPeakOutputForward(config.MAX_OUTPUT_VOLTAGE, config.TIMEOUT)
            configPeakOutputReverse(-config.MAX_OUTPUT_VOLTAGE, config.TIMEOUT)
            configNominalOutputForward(config.NOMINAL_VOLTAGE, config.TIMEOUT)
            configNominalOutputReverse(-config.NOMINAL_VOLTAGE, config.TIMEOUT)

            configReverseLimitSwitchSource(config.LIMIT_SWITCH_SOURCE, config.LIMIT_SWITCH_NORMALLY_OPEN, config.TIMEOUT)
            setNeutralMode(config.ENABLE_BRAKE)
            configForwardSoftLimitEnable(config.ENABLE_SOFT_LIMIT, config.TIMEOUT)
            configReverseSoftLimitEnable(config.ENABLE_SOFT_LIMIT, config.TIMEOUT)
            overrideSoftLimitsEnable(config.ENABLE_SOFT_LIMIT)
            overrideLimitSwitchesEnable(config.ENABLE_LIMIT_SWITCH)

            inverted = config.INVERTED
            setSensorPhase(false)


            sensorCollection.setAnalogPosition(0, config.TIMEOUT)
            enableCurrentLimit(config.ENABLE_CURRENT_LIMIT)
            configContinuousCurrentLimit(config.CURRENT_LIMIT, config.TIMEOUT)

            configForwardSoftLimitThreshold(config.FORWARD_SOFT_LIMIT, config.TIMEOUT)

            selectProfileSlot(0, 0)
            sensorCollection.setQuadraturePosition(0, config.TIMEOUT)
            sensorCollection.setAnalogPosition(0, config.TIMEOUT)
            sensorCollection.setPulseWidthPosition(0, config.TIMEOUT)
            configReverseSoftLimitThreshold(config.REVERSE_SOFT_LIMIT, config.TIMEOUT)
            configVelocityMeasurementPeriod(config.VELOCITY_MEASUREMENT_PERIOD, config.TIMEOUT)
            configVelocityMeasurementWindow(config.VELOCITY_MEASUREMENT_ROLLING_AVERAGE_WINDOW, config.TIMEOUT)
            configClosedloopRamp(config.VOLTAGE_COMPENSATION_RAMP_RATE, config.TIMEOUT)
            configOpenloopRamp(config.VOLTAGE_RAMP_RATE, config.TIMEOUT)

            setStatusFramePeriod(StatusFrame.Status_1_General, config.GENERAL_STATUS_FRAME_RATE_MS, config.TIMEOUT)
            setStatusFramePeriod(StatusFrame.Status_2_Feedback0, config.FEEDBACK_STATUS_FRAME_RATE_MS, config.TIMEOUT)
            setStatusFramePeriod(StatusFrameEnhanced.Status_3_Quadrature, config.QUAD_ENCODER_STATUS_FRAME_RATE_MS, config.TIMEOUT)
            setStatusFramePeriod(StatusFrame.Status_4_AinTempVbat, config.ANALOG_TEMP_VBAT_STATUS_FRAME_RATE_MS, config.TIMEOUT)
            setStatusFramePeriod(StatusFrameEnhanced.Status_8_PulseWidth, config.PULSE_WIDTH_STATUS_FRAME_RATE_MS, config.TIMEOUT)
        }

        return talon
    }

}