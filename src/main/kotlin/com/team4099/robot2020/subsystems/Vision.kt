package com.team4099.robot2020.subsystems

import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import kotlin.math.sign

// import kotlin.math.sign

object Vision : Subsystem {

    enum class VisionState {
        IDLE, AIMING, ALIGNING
    }

    private var state = VisionState.IDLE
        set(value) {
            if (value != field) {
                field = value
            }
        }
    private var aimingAdjust = 0.0
    private var distanceAdjust = 0.0
    private var distance = 0.0

    private val table: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight")
    var tx = table.getEntry("tx").getDouble(0.0)
    var tv = table.getEntry("tv").getDouble(0.0) // is this a valid target?
    var ty = table.getEntry("ty").getDouble(0.0)
    var ta = table.getEntry("ta").getDouble(0.0) // target area

    var pipeline = table.getEntry("pipeline")

    override fun onStart(timestamp: Double) {
        state = VisionState.IDLE
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        synchronized(this) {
            when (state) {
                VisionState.IDLE -> {
                    pipeline.setNumber(1)
                    aimingAdjust = 0.0
                    distanceAdjust = 0.0
                }
                VisionState.ALIGNING -> {
                    pipeline.setNumber(0)
                    if (tv == 0.0) {
                    }
                    else {
                        distanceAdjust = (Constants.Vision.SHOOTING_DISTANCE - distance) * Constants.Vision.ALIGNING_KP
                        distanceAdjust += sign(distanceAdjust) * Constants.Vision.MIN_COMMAND
                    }
                }
                VisionState.AIMING -> {
                    pipeline.setNumber(0)
                    if (tv == 0.0) {
                    }
                    else {
                        aimingAdjust = tx * Constants.Vision.AIMING_KP
                        // aimingAdjust += sign(aimingAdjust) * Constants.Vision.MIN_COMMAND
                    }
                }
            }
        }
    }

    override fun onStop(timestamp: Double) {
        state = VisionState.IDLE
        pipeline.setNumber(Constants.Vision.DRIVER_PIPELINE_ID)
    }

    override fun outputTelemetry() {}

    override fun checkSystem() {}

    override fun registerLogging() {}

    override fun zeroSensors() {}
}
