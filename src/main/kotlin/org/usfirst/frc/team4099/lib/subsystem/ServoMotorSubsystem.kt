package org.usfirst.frc.team4099.lib.subsystem

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.team2363.logger.HelixEvents
import com.team2363.logger.HelixLogger
import org.usfirst.frc.team4099.lib.config.PIDGains
import org.usfirst.frc.team4099.lib.config.ServoMotorSubsystemConfig
import org.usfirst.frc.team4099.lib.util.limit
import kotlin.math.roundToInt

abstract class ServoMotorSubsystem(val configuration: ServoMotorSubsystemConfig): Subsystem {
    init {
        updateMotionConstraints()
        updatePIDGains()
    }

    enum class ControlState(val usesPositionControl: Boolean, val usesVelocityControl: Boolean) {
        OPEN_LOOP(false, false),
        MOTION_MAGIC(false, true),
        VELOCITY_PID(false, true),
        POSITION_PID(true, false)
    }

    private var state: ControlState = ControlState.OPEN_LOOP

    abstract val masterMotorController: TalonSRX
    abstract val slaveMotorControllers: List<BaseMotorController>

    private val positionTicks: Int
        get() = masterMotorController.selectedSensorPosition

    /**
     * The current position of the mechanism in units.
     */
    @get:Synchronized
    val position: Double
        get() = ticksToHomedUnits(positionTicks)

    /**
     * The goal position of the mechanism in units. Targeted
     * using motion profiling.
     */
    @set:Synchronized
    var positionSetpointMotionProfile: Double = Double.NaN
        set(value) {
            if (!state.usesVelocityControl) {
                state = ControlState.MOTION_MAGIC
                enterVelocityClosedLoop()
            }
            field = constrainPositionUnits(value)
            masterMotorController.set(ControlMode.MotionMagic, field)
        }

    /**
     * The goal position of the mechanism in units. Targeted
     * using position PID.
     */
    @set:Synchronized
    var positionSetpointPositionPID: Double = Double.NaN
        set(value) {
            if (!state.usesPositionControl) {
                state = ControlState.POSITION_PID
                enterPositionClosedLoop()
            }
            field = constrainPositionUnits(value)
            masterMotorController.set(ControlMode.Position, field)
        }

    private val velocityTicksPer100Ms: Int
        get() = masterMotorController.selectedSensorVelocity

    /**
     * The current velocity of the mechanism in units per second.
     */
    @get:Synchronized
    val velocity: Double
        get() = ticksPer100msToUnitsPerSecond(velocityTicksPer100Ms)

    /**
     * The goal velocity of the mechanism in units per second. Targeted
     * using velocity PID.
     */
    @set:Synchronized
    var velocitySetpoint: Double = Double.NaN
        set(value) {
            if (!state.usesVelocityControl) {
                state = ControlState.VELOCITY_PID
                enterVelocityClosedLoop()
            }
            field = constrainVelocityUnitsPerSecond(value)
            masterMotorController.set(ControlMode.Velocity, field)
        }

    /**
     * Open loop power in percentage output.
     */
    @set:Synchronized
    var openLoopPower: Double = 0.0
        set(value) {
            if (state != ControlState.OPEN_LOOP) state = ControlState.OPEN_LOOP
            masterMotorController.set(ControlMode.PercentOutput, value)
            field = value
        }

    override fun checkSystem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLogging() {
        HelixLogger.addSource("${configuration.name} Position (${configuration.unitsName})") { position }
        HelixLogger.addSource("${configuration.name} Velocity (${configuration.unitsName}/s)") { velocity }
    }

    override fun onStart(timestamp: Double) {
        openLoopPower = 0.0
    }

    override fun onLoop(timestamp: Double, dT: Double) {}

    override fun onStop(timestamp: Double) {
        openLoopPower = 0.0
    }

    override fun outputTelemetry() {}

    private fun applyPIDGains(gains: PIDGains) {
        val timeout = configuration.masterMotorControllerConfiguration.timeout

        masterMotorController.config_kP(gains.slotNumber, gains.kP, timeout)
        masterMotorController.config_kI(gains.slotNumber, gains.kI, timeout)
        masterMotorController.config_kD(gains.slotNumber, gains.kD, timeout)
        masterMotorController.config_IntegralZone(gains.slotNumber, gains.iZone, timeout)
    }

    fun updatePIDGains() {
        applyPIDGains(configuration.velocityPIDGains)
        applyPIDGains(configuration.positionPIDGains)

        HelixEvents.addEvent(configuration.name, "Updated PID gains")
    }

    fun updateMotionConstraints() {
        masterMotorController.configReverseSoftLimitEnable(
            !configuration.motionConstraints.reverseSoftLimit.isNaN(),
            configuration.masterMotorControllerConfiguration.timeout
        )
        masterMotorController.configReverseSoftLimitThreshold(
            unitsToTicks(configuration.motionConstraints.reverseSoftLimit),
            configuration.masterMotorControllerConfiguration.timeout
        )
        masterMotorController.configForwardSoftLimitEnable(
            !configuration.motionConstraints.forwardSoftLimit.isNaN(),
            configuration.masterMotorControllerConfiguration.timeout
        )
        masterMotorController.configForwardSoftLimitThreshold(
            unitsToTicks(configuration.motionConstraints.forwardSoftLimit),
            configuration.masterMotorControllerConfiguration.timeout
        )

        masterMotorController.configMotionCruiseVelocity(
            unitsPerSecondToTicksPer100ms(configuration.motionConstraints.cruiseVelocity),
            configuration.masterMotorControllerConfiguration.timeout
        )
        masterMotorController.configMotionAcceleration(
            unitsPerSecondToTicksPer100ms(configuration.motionConstraints.maximumAcceleration),
            configuration.masterMotorControllerConfiguration.timeout
        )
        masterMotorController.configMotionSCurveStrength(
            configuration.motionConstraints.motionProfileCurveStrength,
            configuration.masterMotorControllerConfiguration.timeout
        )

        HelixEvents.addEvent(configuration.name, "Updated motion constraints")
    }

    /**
     * Sets PID slot for velocity control modes (velocity setpoint,
     * motion magic).
     */
    @Synchronized
    private fun enterVelocityClosedLoop() {
        masterMotorController.selectProfileSlot(configuration.velocityPIDGains.slotNumber, 0)

        HelixEvents.addEvent(configuration.name, "Entered velocity closed loop")
    }

    /**
     * Sets PID slot for position PID.
     */
    @Synchronized
    private fun enterPositionClosedLoop() {
        masterMotorController.selectProfileSlot(configuration.positionPIDGains.slotNumber, 0)

        HelixEvents.addEvent(configuration.name, "Entered position closed loop")
    }

    protected fun ticksToUnits(ticks: Int): Double {
        return ticks / configuration.ticksPerUnitDistance
    }

    protected fun ticksToHomedUnits(ticks: Int): Double {
        return ticksToUnits(ticks) + configuration.homePosition
    }

    protected fun unitsToTicks(units: Double): Int {
        return (units * configuration.ticksPerUnitDistance).roundToInt()
    }

    protected fun homeAwareUnitsToTicks(units: Double): Int {
        return unitsToTicks(units - configuration.homePosition)
    }

    protected fun constrainPositionUnits(units: Double): Double {
        return units.limit(
            configuration.motionConstraints.reverseSoftLimit,
            configuration.motionConstraints.forwardSoftLimit
        )
    }

    protected fun ticksPer100msToUnitsPerSecond(ticksPer100ms: Int): Double {
        return ticksToUnits(ticksPer100ms) * 10.0
    }

    protected fun unitsPerSecondToTicksPer100ms(unitsPerSeconds: Double): Int {
        return (unitsToTicks(unitsPerSeconds) / 10.0).roundToInt()
    }

    protected fun constrainVelocityUnitsPerSecond(units: Double): Double {
        return units.limit(
            -configuration.motionConstraints.cruiseVelocity,
            configuration.motionConstraints.cruiseVelocity
        )
    }
}
