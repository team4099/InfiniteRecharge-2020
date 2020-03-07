package com.team4099.robot2020.subsystems

import com.team4099.lib.logging.HelixLogger
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.controller.PIDController
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.tan

object Vision : Subsystem {
    enum class VisionState {
        IDLE, AIMING
    }


    var state = VisionState.IDLE
        set(value) {
            if (value != field) {
                pipeline = when (value) {
                    VisionState.IDLE -> {
                        steeringAdjust = 0.0
                        distanceAdjust = 0.0
                        Constants.Vision.DRIVER_PIPELINE_ID
                    }
                    VisionState.AIMING -> {
                        zeroSensors()
                        Constants.Vision.TARGETING_PIPELINE_ID
                    }
                }
                field = value
            }
        }

    enum class DistanceState {
        LINE, NEAR, MID, FAR
    }

    val currentDistance : DistanceState
        get() {
            return when (distance) {
                in 0.0..100.0 -> DistanceState.LINE
                in 101.0..129.0 -> DistanceState.NEAR
                in 129.0..241.0 -> DistanceState.MID
                else -> DistanceState.FAR
            }
        }

    private var distance = 0.0
    private var distanceError = 0.0
    var steeringAdjust = 0.0
    var distanceAdjust = 0.0
    var onTarget = false

    private val table: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight")
    private val tx get() = table.getEntry("tx").getDouble(0.0)
    private val ty get() = table.getEntry("ty").getDouble(0.0)
    private val tv get() = table.getEntry("tv").getDouble(0.0)
    private val ta get() = table.getEntry("ta").getDouble(0.0)

    private val pipelineEntry: NetworkTableEntry = table.getEntry("pipeline")
    private val turnController = PIDController(
        Constants.Vision.TURN_GAINS.kP,
        Constants.Vision.TURN_GAINS.kI,
        Constants.Vision.TURN_GAINS.kD,
        Constants.Vision.TURN_GAINS.kF
    )

    private var pipeline = Constants.Vision.DRIVER_PIPELINE_ID
        set(value) {
            pipelineEntry.setNumber(value)
            field = value
        }

    @Synchronized
    override fun onStart(timestamp: Double) {
        state = VisionState.IDLE
        turnController.setpoint = 0.0
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        when (state) {
            VisionState.IDLE -> {
                onTarget = false
                distance = 120.0
            }
            VisionState.AIMING -> {
                if (tv != 0.0) {
                    if (abs(tx) < Constants.Vision.MAX_ANGLE_ERROR) {
                        onTarget = true
                    }
                    steeringAdjust = turnController.calculate(tx, 0.0)

                    steeringAdjust += sign(tx) * Constants.Vision.MIN_TURN_COMMAND
                    distance = (Constants.Vision.TARGET_HEIGHT - Constants.Vision.CAMERA_HEIGHT) /
                        tan(Constants.Vision.CAMERA_ANGLE + Math.toRadians(ty))
                    distance = ((distance / 10.0).roundToInt()) * 10.0
                } else {
                    steeringAdjust = 0.0
                }
            }
        }
    }

    @Synchronized
    override fun onStop(timestamp: Double) {
        state = VisionState.IDLE
    }

    override fun outputTelemetry() {}

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("VISION State") { state.toString() }
        HelixLogger.addSource("VISION Pipeline") { pipeline }

        HelixLogger.addSource("VISION Steering Adjust") { steeringAdjust }
        HelixLogger.addSource("VISION On Target") { onTarget }

        HelixLogger.addSource("VISION Distance") { distance }

        val shuffleboardTab = Shuffleboard.getTab("Vision")
        shuffleboardTab.addString("State") { state.toString() }
        shuffleboardTab.addNumber("Pipeline") { pipeline.toDouble() }

        shuffleboardTab.addNumber("Steering Adjust") { steeringAdjust }
        shuffleboardTab.addBoolean("On Target") { onTarget }

        shuffleboardTab.addNumber("Distance") { distance }
    }

    override fun zeroSensors() {
        turnController.reset()
    }
}
