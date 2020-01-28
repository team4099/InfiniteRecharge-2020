package com.team4099.lib.subsystem

import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import kotlin.math.roundToInt
import com.team4099.lib.config.ServoMotorSubsystemConfig
import com.team4099.lib.hardware.ServoMotorHardware
import com.team4099.lib.limit

abstract class ServoMotorSubsystem(
    val config: ServoMotorSubsystemConfig,
    private val hardware: ServoMotorHardware
) : Subsystem {
    enum class ControlState(val usesPositionControl: Boolean, val usesVelocityControl: Boolean) {
        OPEN_LOOP(false, false),
        MOTION_MAGIC(false, true),
        VELOCITY_PID(false, true),
        POSITION_PID(true, false)
    }

    private var state: ControlState = ControlState.OPEN_LOOP

    private val positionTicks: Int
        get() = hardware.positionTicks

    init {
        updateMotionConstraints()
        updatePIDGains()
    }

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
            hardware.setMotionProfile(field)
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
            hardware.setPosition(field)
        }

    private val velocityTicksPer100Ms: Int
        get() = hardware.velocityTicksPer100Ms

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
            hardware.setVelocity(field)
        }

    /**
     * Open loop power in percentage output.
     */
    @set:Synchronized
    var openLoopPower: Double = 0.0
        set(value) {
            if (state != ControlState.OPEN_LOOP) state = ControlState.OPEN_LOOP
            hardware.setOpenLoop(value)
            field = value
        }

    override fun checkSystem() {
        TODO("not implemented")
    }

    override fun registerLogging() {
        HelixLogger.addSource("${config.name} Position (${config.unitsName})") { position }
        HelixLogger.addSource("${config.name} Velocity (${config.unitsName}/s)") { velocity }
    }

    override fun onStart(timestamp: Double) {
        openLoopPower = 0.0
    }

    override fun onLoop(timestamp: Double, dT: Double) {}

    override fun onStop(timestamp: Double) {
        openLoopPower = 0.0
    }

    override fun outputTelemetry() {}

    override fun zeroSensors() {
        hardware.zeroSensors()
    }

    fun updatePIDGains() {
        hardware.applyPIDGains(config.velocityPIDGains)
        hardware.applyPIDGains(config.positionPIDGains)

        HelixEvents.addEvent(config.name, "Updated PID gains")
    }

    fun updateMotionConstraints() {
        hardware.applyMotionConstraints(
            config.motionConstraints.reverseSoftLimit.isNaN(),
            homeAwareUnitsToTicks(config.motionConstraints.reverseSoftLimit),
            config.motionConstraints.forwardSoftLimit.isNaN(),
            homeAwareUnitsToTicks(config.motionConstraints.forwardSoftLimit),
            unitsPerSecondToTicksPer100ms(config.motionConstraints.cruiseVelocity),
            unitsPerSecondToTicksPer100ms(config.motionConstraints.maxAccel),
            config.motionConstraints.motionProfileCurveStrength,
            config.velocityPIDGains.slotNumber
        )

        HelixEvents.addEvent(config.name, "Updated motion constraints")
    }

    /**
     * Sets PID slot for velocity control modes (velocity setpoint,
     * motion magic).
     */
    @Synchronized
    private fun enterVelocityClosedLoop() {
        hardware.pidSlot = config.velocityPIDGains.slotNumber
        HelixEvents.addEvent(config.name, "Entered velocity closed loop")
    }

    /**
     * Sets PID slot for position PID.
     */
    @Synchronized
    private fun enterPositionClosedLoop() {
        hardware.pidSlot = config.positionPIDGains.slotNumber
        HelixEvents.addEvent(config.name, "Entered position closed loop")
    }

    protected fun ticksToUnits(ticks: Int): Double {
        return ticks / config.ticksPerUnitDistance
    }

    protected fun ticksToHomedUnits(ticks: Int): Double {
        return ticksToUnits(ticks) + config.homePosition
    }

    protected fun unitsToTicks(units: Double): Int {
        return (units * config.ticksPerUnitDistance).roundToInt()
    }

    protected fun homeAwareUnitsToTicks(units: Double): Int {
        return unitsToTicks(units - config.homePosition)
    }

    protected fun constrainPositionUnits(units: Double): Double {
        return units.limit(
            config.motionConstraints.reverseSoftLimit,
            config.motionConstraints.forwardSoftLimit
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
            -config.motionConstraints.cruiseVelocity,
            config.motionConstraints.cruiseVelocity
        )
    }
}
