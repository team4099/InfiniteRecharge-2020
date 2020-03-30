package com.team4099.robot2020.subsystems

import com.team4099.lib.geometry.Pose3d
import com.team4099.lib.geometry.Translation3d
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.controller.PIDController
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.geometry.Translation2d
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object Vision : Subsystem {
    enum class VisionState {
        IDLE, AIMING
    }

    var state = VisionState.IDLE
        set(value) {
            if (value != field) {
                pipeline = when (value) {
                    VisionState.IDLE -> {
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

//    val currentDistance: DistanceState
//        get() {
//            return when (distance) {
//                in 0.0..100.0 -> DistanceState.LINE
//                in 101.0..129.0 -> DistanceState.NEAR
//                in 129.0..241.0 -> DistanceState.MID
//                else -> DistanceState.FAR
//            }
//        }

    /**
     * If the camera sees the target. Vision data should not
     * be relied on if false.
     */
    val seesTarget
        get() = tv != 0.0 && tcornx.size == 4 && tcorny.size == 4

    /**
     * Estimated robot pose based on vision target
     */
    var robotPosition = Pose2d(0.0, 0.0, Rotation2d(0.0))

    private val table: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight")
    private val tv get() = table.getEntry("tv").getDouble(0.0)
    private val tcornx get() = table.getEntry("tcornx").getDoubleArray(Array(0) {0.0})
    private val tcorny get() = table.getEntry("tcorny").getDoubleArray(Array(0) {0.0})


    private val pipelineEntry: NetworkTableEntry = table.getEntry("pipeline")
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
        when (state) {
            VisionState.IDLE -> {}
            VisionState.AIMING -> {
                if (seesTarget) {
                    // Zip the corners together into a Pair and sort, first by x, then by y
                    // Order is now top left, top right, bottom left, bottom right
                    val corners = tcornx.zip(tcorny).sortedBy { (x, _) -> x }.sortedBy { (_, y) -> y }

                    val topCornerPositions = corners.map { (rawX, rawY) ->
                        // Normalize the corners by converting them to be in [-1.0, 1.0],
                        // with (0, 0) being the center of the viewport. At this point,
                        // we also convert from camera coordinate space to the robot perspective,
                        // where the camera X maps to robot Y and camera Y maps to robot Z.

                        val normalizedY = (2.0 / Constants.Vision.Camera.X_RESOLUTION) *
                            (rawX - (Constants.Vision.Camera.X_RESOLUTION - 1.0) / 2.0)
                        val normalizedZ = (2.0 / Constants.Vision.Camera.Y_RESOLUTION) *
                            (rawY - (Constants.Vision.Camera.Y_RESOLUTION - 1.0) / 2.0)

                        val viewPlaneY = normalizedY * Constants.Vision.Camera.VIEW_PLANE_WIDTH / 2
                        val viewPlaneZ = normalizedZ * Constants.Vision.Camera.VIEW_PLANE_HEIGHT / 2

                        // alpha = angle between the X axis and the point towards the Y axis.
                        val alpha = atan2(1.0, viewPlaneY)
                        // beta = angle between the X axis and the point towards the Z axis.
                        // Here we compensate for the pitch of the camera.
                        val beta = atan2(1.0, viewPlaneZ) + Constants.Vision.CAMERA_POSITION.pitch.radians

                        // z is the known height above the ground
                        // z = rho * sin(beta)
                        // rho = z / sin(beta)
                        val rho = Constants.Vision.Target.TOP_CORNERS_HEIGHT / sin(beta)

                        // x = rho * cos(alpha) * cos(beta)
                        // y = rho * sin(alpha) * cos(beta)
                        val x = rho * cos(alpha) * cos(beta)
                        val y = rho * sin(alpha) * cos(beta)

                        Translation3d(x, y, Constants.Vision.Target.TOP_CORNERS_HEIGHT)
                     }

                    val targetPairDeltaX = topCornerPositions[1].x - topCornerPositions[0].x
                    val targetPairDeltaY = topCornerPositions[1].y - topCornerPositions[0].y

                    val pitch = Rotation2d(targetPairDeltaX, targetPairDeltaY)

                    val targetCenterTranslation = (topCornerPositions[0] + topCornerPositions[1]) / 2.0
                    val robotPose3d = Pose3d(targetCenterTranslation, pitch, Rotation2d(0.0))
                    robotPosition = robotPose3d.pose2d
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
//        HelixLogger.addSource("VISION State") { state.toString() }
//        HelixLogger.addSource("VISION Pipeline") { pipeline }
//
//        HelixLogger.addSource("VISION Steering Adjust") { steeringAdjust }
//        HelixLogger.addSource("VISION On Target") { onTarget }
//
//        HelixLogger.addSource("VISION Distance") { distance }
//
//        val shuffleboardTab = Shuffleboard.getTab("Vision")
//        shuffleboardTab.addString("State") { state.toString() }
//        shuffleboardTab.addNumber("Pipeline") { pipeline.toDouble() }
//
//        shuffleboardTab.addNumber("Steering Adjust") { steeringAdjust }
//        shuffleboardTab.addBoolean("On Target") { onTarget }
//
//        shuffleboardTab.addNumber("Distance") { distance }
    }

    override fun zeroSensors() {
//        turnController.reset()
    }
}
