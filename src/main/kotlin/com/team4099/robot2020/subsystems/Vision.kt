package com.team4099.robot2020.subsystems

import com.team4099.lib.logging.HelixLogger
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.controller.PIDController
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlin.math.abs
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
    var distance = 0.0
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
        Constants.Vision.TURN_GAINS.kD
//        Constants.Vision.TURN_GAINS.kF
    )
    private val distanceController = PIDController(
        Constants.Vision.DISTANCE_GAINS.kP,
        Constants.Vision.DISTANCE_GAINS.kI,
        Constants.Vision.DISTANCE_GAINS.kD,
        Constants.Vision.DISTANCE_GAINS.kF
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
        distance = (Constants.Vision.TARGET_HEIGHT - Constants.Vision.CAMERA_HEIGHT) /
            tan(Constants.Vision.CAMERA_ANGLE + Math.toRadians(ty))
        distanceError = distance - Constants.Vision.SHOOTING_DISTANCE
        when (state) {
            VisionState.IDLE -> {}
            VisionState.AIMING -> {
                if (tv != 0.0) {
                    onTarget = abs(tx) < Constants.Vision.MAX_ANGLE_ERROR && distance < Constants.Vision.MAX_DIST_ERROR
                    steeringAdjust = turnController.calculate(tx, 0.0)

                    steeringAdjust += sign(tx) * Constants.Vision.MIN_TURN_COMMAND
                    distanceAdjust = distanceController.calculate(distanceError)
                    distanceAdjust += sign(distanceError) * Constants.Vision.MIN_DIST_COMMAND
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

    override fun outputTelemetry() {
        SmartDashboard.putString("vision/state", state.toString())
        SmartDashboard.putNumber("vision/pipeline", pipeline.toDouble())

        SmartDashboard.putNumber("vision/steeringAdjust", steeringAdjust)
        SmartDashboard.putNumber("vision/distanceAdjust", steeringAdjust)
        SmartDashboard.putBoolean("vision/onTarget", onTarget)

        SmartDashboard.putNumber("vision/distance", distance)
        SmartDashboard.putNumber("vision/distanceError", distanceError)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("VISION State") { state.toString() }
        HelixLogger.addSource("VISION Pipeline") { pipeline }

        HelixLogger.addSource("VISION Steering Adjust") { steeringAdjust }
        HelixLogger.addSource("VISION Distance Adjust") { distanceAdjust }
        HelixLogger.addSource("VISION On Target") { onTarget }

        HelixLogger.addSource("VISION Distance") { distance }
        HelixLogger.addSource("VISION Distance Error") { distanceError }
    }

    override fun zeroSensors() {
        turnController.reset()
        distanceController.reset()
    }
}
