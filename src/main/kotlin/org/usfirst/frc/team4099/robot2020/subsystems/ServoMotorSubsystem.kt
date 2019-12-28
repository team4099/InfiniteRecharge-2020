import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.loop.Loop
import org.usfirst.frc.team4099.lib.util.CTREMotorControllerFactory
import org.usfirst.frc.team4099.lib.subsystem.Subsystem
import org.usfirst.frc.team4099.lib.util.around
import org.usfirst.frc.team4099.lib.util.limit
import org.usfirst.frc.team4099.robot2020.config.Constants
import kotlin.math.sign


abstract class ServoMotorSubsystem protected constructor(protected val mConstants: ServoMotorSubsystemConstants) : Subsystem() {
    // Recommend initializing in a static block!
    class TalonSRXConstants() {
        var id: Int = -1
        var invert_motor: Boolean = false
        var invert_sensor_phase: Boolean = false
    }

    // Recommend initializing in a static block!
    class ServoMotorSubsystemConstants() {
        var kName: String = "ERROR_ASSIGN_A_NAME"
        var kMasterConstants: TalonSRXConstants = TalonSRXConstants()
        var kSlaveConstants: Array<TalonSRXConstants?> = arrayOfNulls(0)
        var kHomePosition: Double = 0.0 // Units
        var kTicksPerUnitDistance: Double = 1.0
        var kKp: Double = 0.0 // Raw output / raw error
        var kKi: Double = 0.0 // Raw output / sum of raw error
        var kKd: Double = 0.0 // Raw output / (err - prevErr)
        var kKf: Double = 0.0 // Raw output / velocity in ticks/100ms
        var kKa: Double = 0.0 // Raw output / accel in (ticks/100ms) / s
        var kMaxIntegralAccumulator: Double = 0.0
        var kIZone: Int = 0 // Ticks
        var kDeadband: Int = 0 // Ticks
        var kPositionKp: Double = 0.0
        var kPositionKi: Double = 0.0
        var kPositionKd: Double = 0.0
        var kPositionKf: Double = 0.0
        var kPositionMaxIntegralAccumulator: Double = 0.0
        var kPositionIZone: Int = 0 // Ticks
        var kPositionDeadband: Int = 0 // Ticks
        var kCruiseVelocity: Int = 0 // Ticks / 100ms
        var kAcceleration: Int = 0 // Ticks / 100ms / s
        var kRampRate: Double = 0.0 // s
        var kContinuousCurrentLimit: Int = 20 // amps
        var kPeakCurrentLimit: Int = 60 // amps
        var kPeakCurrentDuration: Int = 200 // milliseconds
        var kMaxVoltage: Double = 12.0
        var kMaxUnitsLimit: Double = Double.POSITIVE_INFINITY
        var kMinUnitsLimit: Double = Double.NEGATIVE_INFINITY
        var kStastusFrame8UpdateRate: Int = 1000
        var kRecoverPositionOnReset: Boolean = false
    }

    protected val mMaster: TalonSRX
    protected val mSlaves: Array<TalonSRX?>
    protected val mForwardSoftLimitTicks: Int
    protected val mReverseSoftLimitTicks: Int

    class PeriodicIO() {
        // INPUTS
        var timestamp: Double = 0.0
        var position_ticks: Int = 0
        var position_units: Double = 0.0
        var velocity_ticks_per_100ms: Int = 0
        var active_trajectory_position // ticks
                : Int = 0
        var active_trajectory_velocity // ticks/100ms
                : Int = 0
        var active_trajectory_acceleration // ticks/100ms/s
                : Double = 0.0
        var output_percent: Double = 0.0
        var output_voltage: Double = 0.0
        var master_current: Double = 0.0
        var error_ticks: Double = 0.0
        var encoder_wraps: Int = 0
        var absolute_pulse_offset: Int = 0
        var absolute_pulse_position: Int = 0
        var absolute_pulse_position_modded: Int = 0
        var reset_occured: Boolean = false
        // OUTPUTS
        var demand: Double = 0.0
        var feedforward: Double = 0.0
    }

    protected enum class ControlState {
        OPEN_LOOP, MOTION_MAGIC, POSITION_PID, MOTION_PROFILING
    }

    protected var mPeriodicIO: PeriodicIO = PeriodicIO()
    protected var mControlState: ControlState = ControlState.OPEN_LOOP
    //protected var mCSVWriter: ReflectingCSVWriter<PeriodicIO>? = null
    protected var mHasBeenZeroed: Boolean = false
    protected var mFaults: StickyFaults = StickyFaults()
    protected var mSetpointGenerator: SetpointGenerator = SetpointGenerator()
    protected var mMotionProfileConstraints: MotionProfileConstraints
    @Synchronized
    override fun readPeriodicInputs() {
        mPeriodicIO.timestamp = Timer.getFPGATimestamp()
        if (mMaster.hasResetOccurred()) {
            DriverStation.reportError(mConstants.kName + ": Talon Reset! ", false)
            mPeriodicIO.reset_occured = true
            return
        } else {
            mPeriodicIO.reset_occured = false
        }
        mMaster.getStickyFaults(mFaults)
        if (mFaults.hasAnyFault()) {
            DriverStation.reportError(mConstants.kName + ": Talon Fault! " + mFaults.toString(), false)
            mMaster.clearStickyFaults(0)
        }
        if (mMaster.controlMode == ControlMode.MotionMagic) {
            mPeriodicIO.active_trajectory_position = mMaster.activeTrajectoryPosition
            if (mPeriodicIO.active_trajectory_position < mReverseSoftLimitTicks) {
                DriverStation.reportError(mConstants.kName + ": Active trajectory past reverse soft limit!", false)
            } else if (mPeriodicIO.active_trajectory_position > mForwardSoftLimitTicks) {
                DriverStation.reportError(mConstants.kName + ": Active trajectory past forward soft limit!", false)
            }
            val newVel: Int = mMaster.activeTrajectoryVelocity
            if (newVel.toDouble().around(mConstants.kCruiseVelocity.toDouble(), Math.max(1, mConstants.kDeadband).toDouble())
                    || newVel.toDouble().around(mPeriodicIO.active_trajectory_velocity.toDouble(), Math.max(1, mConstants.kDeadband).toDouble())) { // Mechanism is ~constant velocity.
                mPeriodicIO.active_trajectory_acceleration = 0.0
            } else { // Mechanism is accelerating.
                mPeriodicIO.active_trajectory_acceleration = sign(newVel - mPeriodicIO.active_trajectory_velocity.toFloat()) * mConstants.kAcceleration.toDouble()
            }
            mPeriodicIO.active_trajectory_velocity = newVel
        } else {
            mPeriodicIO.active_trajectory_position = Int.MIN_VALUE
            mPeriodicIO.active_trajectory_velocity = 0
            mPeriodicIO.active_trajectory_acceleration = 0.0
        }
        if (mMaster.controlMode == ControlMode.Position) {
            mPeriodicIO.error_ticks = mMaster.getClosedLoopError(0).toDouble()
        } else {
            mPeriodicIO.error_ticks = 0.0
        }
        mPeriodicIO.master_current = mMaster.outputCurrent
        mPeriodicIO.output_voltage = mMaster.motorOutputVoltage
        mPeriodicIO.output_percent = mMaster.motorOutputPercent
        mPeriodicIO.position_ticks = mMaster.getSelectedSensorPosition(0)
        mPeriodicIO.position_units = ticksToHomedUnits(mPeriodicIO.position_ticks.toDouble())
        mPeriodicIO.velocity_ticks_per_100ms = mMaster.getSelectedSensorVelocity(0)
        if (mConstants.kRecoverPositionOnReset) {
            mPeriodicIO.absolute_pulse_position = mMaster.sensorCollection.pulseWidthPosition
            mPeriodicIO.absolute_pulse_position_modded = mPeriodicIO.absolute_pulse_position % 4096
            if (mPeriodicIO.absolute_pulse_position_modded < 0) {
                mPeriodicIO.absolute_pulse_position_modded += 4096
            }
            val estimated_pulse_pos: Int = ((if (mConstants.kMasterConstants.invert_sensor_phase) -1 else 1) * mPeriodicIO.position_ticks) + mPeriodicIO.absolute_pulse_offset
            val new_wraps: Int = Math.floor(estimated_pulse_pos / 4096.0).toInt()
            // Only set this when we are really sure its a valid change
            if (Math.abs(mPeriodicIO.encoder_wraps - new_wraps) <= 1) {
                mPeriodicIO.encoder_wraps = new_wraps
            }
        }
        if (mCSVWriter != null) {
            mCSVWriter.add(mPeriodicIO)
        }
    }

    protected fun getAbsoluteEncoderRawPosition(pulseWidthPosition: Int): Int {
        val abs_raw_no_rollover: Int = mMaster.sensorCollection.pulseWidthPosition
        val abs_raw_with_rollover: Int = abs_raw_no_rollover % 4096
        return abs_raw_with_rollover + (if (abs_raw_with_rollover < 0) abs_raw_with_rollover + 4096 else 0)
    }

    override fun writePeriodicOutputs() {
        if (mControlState == ControlState.MOTION_MAGIC) {
            mMaster.set(ControlMode.MotionMagic, mPeriodicIO.demand, DemandType.ArbitraryFeedForward,
                    mPeriodicIO.feedforward)
        } else if (mControlState == ControlState.POSITION_PID || mControlState == ControlState.MOTION_PROFILING) {
            mMaster.set(ControlMode.Position, mPeriodicIO.demand, DemandType.ArbitraryFeedForward,
                    mPeriodicIO.feedforward)
        } else {
            mMaster.set(ControlMode.PercentOutput, mPeriodicIO.demand, DemandType.ArbitraryFeedForward,
                    mPeriodicIO.feedforward)
        }
    }

    @Synchronized
    fun handleMasterReset(reset: Boolean) {
    }

        override fun onStart(timestamp: Double) { // if (mCSVWriter == null) {
//     mCSVWriter = new ReflectingCSVWriter<>("/home/lvuser/"
//             + mConstants.kName.replaceAll("[^A-Za-z0-9]+", "").toUpperCase() + "-LOGS.csv",
//             PeriodicIO.class);
// }
        }

        fun onLoop(timestamp: Double) {
            if (mPeriodicIO.reset_occured) {
                println(mConstants.kName + ": Master Talon reset occurred; resetting frame rates.")
                mMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 10, 20)
                mMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 20)
                mMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_8_PulseWidth, mConstants.kStastusFrame8UpdateRate, 20)
                // Reset encoder position to estimated position from absolute encoder
                if (mConstants.kRecoverPositionOnReset) {
                    mMaster.setSelectedSensorPosition(estimateSensorPositionFromAbsolute(), 0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
                }
            }
            handleMasterReset(mPeriodicIO.reset_occured)
            for (slave: TalonSRX? in mSlaves) {
                if (slave!!.hasResetOccurred()) {
                    println(mConstants.kName + ": Slave Talon reset occurred")
                }
            }
        }

        override fun onStop(timestamp: Double) {
            if (mCSVWriter != null) {
                mCSVWriter.flush()
                mCSVWriter = null
            }
            stop()
        }

    @get:Synchronized
    val positionTicks: Double
        get() = mPeriodicIO.position_ticks.toDouble()

    // In "Units"
    @get:Synchronized
    val position: Double
        get() = ticksToHomedUnits(mPeriodicIO.position_ticks.toDouble())

    // In "Units per second"
    @get:Synchronized
    val velocity: Double
        get() = ticksToUnits(mPeriodicIO.velocity_ticks_per_100ms.toDouble()) * 10.0

    @Synchronized
    fun hasFinishedTrajectory(): Boolean {
        if (mPeriodicIO.active_trajectory_position.toDouble().around(ticksToUnits(setpoint),
                        Math.max(1, mConstants.kDeadband).toDouble())) {
            return true
        }
        return false
    }

    @get:Synchronized
    val setpoint: Double
        get() = if ((mControlState == ControlState.MOTION_MAGIC ||
                        mControlState == ControlState.POSITION_PID)) ticksToUnits(mPeriodicIO.demand) else Double.NaN

    @Synchronized
    fun setSetpointMotionMagic(units: Double, feedforward_v: Double) {
        mPeriodicIO.demand = constrainTicks(homeAwareUnitsToTicks(units))
        mPeriodicIO.feedforward = unitsPerSecondToTicksPer100ms(feedforward_v) * (mConstants.kKf + mConstants.kKd / 100.0) / 1023.0
        if (mControlState != ControlState.MOTION_MAGIC) {
            mMaster.selectProfileSlot(kMotionProfileSlot, 0)
            mControlState = ControlState.MOTION_MAGIC
        }
    }

    @Synchronized
    fun setSetpointMotionMagic(units: Double) {
        setSetpointMotionMagic(units, 0.0)
    }

    @Synchronized
    fun setSetpointPositionPID(units: Double, feedforward_v: Double) {
        mPeriodicIO.demand = constrainTicks(homeAwareUnitsToTicks(units))
        val feedforward_ticks_per_100ms: Double = unitsPerSecondToTicksPer100ms(feedforward_v)
        mPeriodicIO.feedforward = feedforward_ticks_per_100ms * (mConstants.kKf + mConstants.kKd / 100.0) / 1023.0
        if (mControlState != ControlState.POSITION_PID) {
            mMaster.selectProfileSlot(kPositionPIDSlot, 0)
            mControlState = ControlState.POSITION_PID
        }
    }

    @Synchronized
    fun setGoalMotionProfiling(goal: MotionProfileGoal?, feedforward_v: Double) {
        if (mControlState != ControlState.MOTION_PROFILING) {
            mMaster.selectProfileSlot(kPositionPIDSlot, 0)
            mControlState = ControlState.MOTION_PROFILING
            mSetpointGenerator.reset()
        }
        val cur_state: MotionState = MotionState(mPeriodicIO.timestamp, mPeriodicIO.position_units, ticksPer100msToUnitsPerSecond(mPeriodicIO.velocity_ticks_per_100ms.toDouble()), 0.0)
        val setpoint: Setpoint = mSetpointGenerator.getSetpoint(mMotionProfileConstraints, goal, cur_state, mPeriodicIO.timestamp + Constants.kLooperDt)
        mPeriodicIO.demand = constrainTicks(homeAwareUnitsToTicks(setpoint.motion_state.pos()))
        mPeriodicIO.feedforward = ((feedforward_v + setpoint.motion_state.vel()) * mConstants.kKf + setpoint.motion_state.acc() * mConstants.kKa) / 1023.0
    }

    protected fun ticksToUnits(ticks: Double): Double {
        return ticks / mConstants.kTicksPerUnitDistance
    }

    protected fun ticksToHomedUnits(ticks: Double): Double {
        val `val`: Double = ticksToUnits(ticks)
        return `val` + mConstants.kHomePosition
    }

    protected fun unitsToTicks(units: Double): Double {
        return units * mConstants.kTicksPerUnitDistance
    }

    protected fun homeAwareUnitsToTicks(units: Double): Double {
        return unitsToTicks(units - mConstants.kHomePosition)
    }

    protected fun constrainTicks(ticks: Double): Double {
        return ticks.limit( mReverseSoftLimitTicks.toDouble(), mForwardSoftLimitTicks.toDouble())
    }

    protected fun ticksPer100msToUnitsPerSecond(ticks_per_100ms: Double): Double {
        return ticksToUnits(ticks_per_100ms) * 10.0
    }

    protected fun unitsPerSecondToTicksPer100ms(units_per_second: Double): Double {
        return unitsToTicks(units_per_second) / 10.0
    }

    @Synchronized
    fun setOpenLoop(percentage: Double) {
        mPeriodicIO.demand = percentage
        if (mControlState != ControlState.OPEN_LOOP) {
            mControlState = ControlState.OPEN_LOOP
        }
    }

    @get:Synchronized
    val activeTrajectoryUnits: Double
        get() {
            return ticksToHomedUnits(mPeriodicIO.active_trajectory_position.toDouble())
        }

    @get:Synchronized
    val activeTrajectoryVelocityUnitsPerSec: Double
        get() {
            return ticksPer100msToUnitsPerSecond(mPeriodicIO.active_trajectory_velocity.toDouble())
        }

    @Synchronized
    fun getPredictedPositionUnits(lookahead_secs: Double): Double {
        if (mMaster.controlMode != ControlMode.MotionMagic) {
            return position
        }
        val predicted_units: Double = ticksToHomedUnits(mPeriodicIO.active_trajectory_position + (
                lookahead_secs * mPeriodicIO.active_trajectory_velocity) + (
                0.5 * mPeriodicIO.active_trajectory_acceleration * lookahead_secs * lookahead_secs))
        if (mPeriodicIO.demand >= mPeriodicIO.active_trajectory_position) {
            return Math.min(predicted_units, ticksToHomedUnits(mPeriodicIO.demand))
        } else {
            return Math.max(predicted_units, ticksToHomedUnits(mPeriodicIO.demand))
        }
    }

    fun atHomingLocation(): Boolean {
        return false
    }

    @Synchronized
    fun resetIfAtLimit() {
        if (atHomingLocation()) {
            zeroSensors()
        }
    }

    @Synchronized
    fun zeroSensors() {
        mMaster.setSelectedSensorPosition(0, 0, Constants.kCANTimeoutMs)
        mPeriodicIO.absolute_pulse_offset = getAbsoluteEncoderRawPosition(mMaster.sensorCollection.pulseWidthPosition)
        mHasBeenZeroed = true
    }

    @Synchronized
    fun hasBeenZeroed(): Boolean {
        return mHasBeenZeroed
    }

    fun stop() {
        setOpenLoop(0.0)
    }

    fun estimateSensorPositionFromAbsolute(): Int {
        val estimated_pulse_pos: Int = (mPeriodicIO.encoder_wraps * 4096) + mPeriodicIO.absolute_pulse_position_modded
        val estimate_position_ticks: Int = (if (mConstants.kMasterConstants.invert_sensor_phase) -1 else 1) * (estimated_pulse_pos - mPeriodicIO.absolute_pulse_offset)
        return estimate_position_ticks
    }

    override fun outputTelemetry() {
        SmartDashboard.putNumber(mConstants.kName + ": Position (units)", mPeriodicIO.position_units)
        SmartDashboard.putBoolean(mConstants.kName + ": Homing Location", atHomingLocation())
        // synchronized (this) {
//     if (mCSVWriter != null) {
//         mCSVWriter.write();
//     }
// }
    }

    companion object {
        private val kMotionProfileSlot: Int = 0
        private val kPositionPIDSlot: Int = 1
    }

    init {
        mMaster = CTREMotorControllerFactory.createDefaultTalon(mConstants.kMasterConstants.id)
        mSlaves = arrayOfNulls(mConstants.kSlaveConstants.size)
        mMaster.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mForwardSoftLimitTicks = ((mConstants.kMaxUnitsLimit - mConstants.kHomePosition) * mConstants.kTicksPerUnitDistance).toInt()
        mMaster.configForwardSoftLimitThreshold(mForwardSoftLimitTicks, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configForwardSoftLimitEnable(true, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mReverseSoftLimitTicks = ((mConstants.kMinUnitsLimit - mConstants.kHomePosition) * mConstants.kTicksPerUnitDistance).toInt()
        mMaster.configReverseSoftLimitThreshold(mReverseSoftLimitTicks, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configReverseSoftLimitEnable(true, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configVoltageCompSaturation(12.0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kP(kMotionProfileSlot, mConstants.kKp, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kI(kMotionProfileSlot, mConstants.kKi, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kD(kMotionProfileSlot, mConstants.kKd, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kF(kMotionProfileSlot, mConstants.kKf, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configMaxIntegralAccumulator(kMotionProfileSlot, mConstants.kMaxIntegralAccumulator, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_IntegralZone(kMotionProfileSlot, mConstants.kIZone, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configAllowableClosedloopError(kMotionProfileSlot, mConstants.kDeadband, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kP(kPositionPIDSlot, mConstants.kPositionKp, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kI(kPositionPIDSlot, mConstants.kPositionKi, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kD(kPositionPIDSlot, mConstants.kPositionKd, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_kF(kPositionPIDSlot, mConstants.kPositionKf, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configMaxIntegralAccumulator(kPositionPIDSlot, mConstants.kPositionMaxIntegralAccumulator, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.config_IntegralZone(kPositionPIDSlot, mConstants.kPositionIZone, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configAllowableClosedloopError(kPositionPIDSlot, mConstants.kPositionDeadband, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configMotionCruiseVelocity(mConstants.kCruiseVelocity, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configMotionAcceleration(mConstants.kAcceleration, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configOpenloopRamp(mConstants.kRampRate, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configClosedloopRamp(mConstants.kRampRate, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configContinuousCurrentLimit(mConstants.kContinuousCurrentLimit, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configPeakCurrentLimit(mConstants.kPeakCurrentLimit, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.configPeakCurrentDuration(mConstants.kPeakCurrentDuration, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.enableCurrentLimit(true)
        mMaster.configVoltageMeasurementFilter(8)
        mMaster.configVoltageCompSaturation(mConstants.kMaxVoltage, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        mMaster.enableVoltageCompensation(true)
        mMaster.inverted = mConstants.kMasterConstants.invert_motor
        mMaster.setSensorPhase(mConstants.kMasterConstants.invert_sensor_phase)
        mMaster.setNeutralMode(NeutralMode.Brake)
        mMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_2_Feedback0, 10, 20)
        mMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_10_MotionMagic, 10, 20)
        mMaster.setStatusFramePeriod(StatusFrameEnhanced.Status_8_PulseWidth, mConstants.kStastusFrame8UpdateRate, 20)
        // Start with kMotionProfileSlot.
        mMaster.selectProfileSlot(kMotionProfileSlot, 0)
        for (i in mSlaves.indices) {
            mSlaves[i] = CTREMotorControllerFactory.createPermanentSlaveTalon(mConstants.kSlaveConstants[i]!!.id,
                    mConstants.kMasterConstants.id)
            mSlaves[i]!!.inverted = mConstants.kSlaveConstants.get(i)!!.invert_motor
            mSlaves[i]!!.setNeutralMode(NeutralMode.Brake)
            mSlaves[i]!!.follow(mMaster)
        }
        // The accel term can re-use the velocity unit conversion because both input and output units are per second.
        mMotionProfileConstraints = MotionProfileConstraints(ticksPer100msToUnitsPerSecond(mConstants.kCruiseVelocity.toDouble()), ticksPer100msToUnitsPerSecond(mConstants.kAcceleration.toDouble()))
        // Send a neutral command.
        stop()
    }
}