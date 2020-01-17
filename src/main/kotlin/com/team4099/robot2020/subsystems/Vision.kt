package com.team4099.robot2020.subsystems

import com.team4099.lib.logging.HelixLogger
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.controller.PIDController
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
                pipeline = when(value) {
                    VisionState.IDLE -> {
                        steeringAdjust = 0.0
                        distanceAdjust = 0.0
                        Constants.Vision.DRIVER_PIPELINE_ID
                    }
                    VisionState.AIMING -> Constants.Vision.TARGETING_PIPELINE_ID
                }
                field = value
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
    private val tv get() = table.getEntry("tv").getDouble(0.0) // is this a valid target?
    private val ta get() = table.getEntry("ta").getDouble(0.0) // target area

    private val pipelineEntry: NetworkTableEntry = table.getEntry("pipeline")
    private val turnController = PIDController(
        Constants.Vision.TURN_GAINS.kP,
        Constants.Vision.TURN_GAINS.kI,
        Constants.Vision.TURN_GAINS.kD,
        Constants.Vision.TURN_GAINS.kF
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
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        distance = (Constants.Vision.TARGET_HEIGHT - Constants.Vision.CAMERA_HEIGHT) /
            tan(Constants.Vision.CAMERA_ANGLE + ty)
        distanceError = distance - Constants.Vision.SHOOTING_DISTANCE
        when (state) {
            VisionState.IDLE -> {}
            VisionState.AIMING -> {
                println("aiming")
                if (tv == 0.0)
                else {
//                    onTarget = abs(tx) < Constants.Vision.MAX_ANGLE_ERROR && distance < Constants.Vision.MAX_DIST_ERROR
//
                        steeringAdjust = turnController.calculate(tx)
//                      steeringAdjust = tx * Constants.Vision.TURN_GAINS.kP
//                      steeringAdjust += sign(tx) * Constants.Vision.MIN_COMMAND
//                      distanceAdjust = distanceError * Constants.Vision.DISTANCE_GAINS.kP
//                      distanceAdjust += sign(distanceError) * Constants.Vision.MIN_COMMAND

//                      if (tx > 0) steeringAdjust *= -1



                }
                println("adjust: $steeringAdjust")
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
        HelixLogger.addSource("VISION Distance Adjust") { distanceAdjust }
        HelixLogger.addSource("VISION On Target") { onTarget }

        HelixLogger.addSource("VISION Distance") { distance }
        HelixLogger.addSource("VISION Distance Error") { distanceError }
    }

    override fun zeroSensors() {}
}
