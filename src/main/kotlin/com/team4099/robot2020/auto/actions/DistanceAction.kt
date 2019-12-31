package com.team4099.robot2020.auto.actions

import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import kotlin.math.abs
import kotlin.math.sign
import com.team4099.lib.auto.Action
import com.team4099.lib.drive.DriveSignal
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.subsystems.Drive

class DistanceAction(metersToMove: Double, slowMode: Boolean) : Action {
    private val direction = sign(metersToMove)
    private val metersToMove: Double = abs(metersToMove)
    private var startDist = 0.0
    private var otherStart = 0.0
    private var power = Constants.Autonomous.FORWARD_POWER
    private var startAngle = 0.0
    private var abort = false
    private var startTime = 0.0

    init {
        if (slowMode) {
            this.power = Constants.Autonomous.SLOW_FORWARD_POWER
        }
    }

    override fun isFinished(timestamp: Double): Boolean {
        return abs(Drive.leftDistanceMeters) - startDist >= metersToMove ||
                abs(Drive.rightDistanceMeters) - otherStart >= metersToMove ||
                abort ||
                timestamp - startTime > Constants.Autonomous.FORWARD_MAX_TIME_SECONDS
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        val correctionAngle = startAngle - Drive.angle
        if (abs(correctionAngle) > Constants.Autonomous.FORWARD_GIVE_UP_ANGLE) {
            abort = true
            return
        }
        Drive.arcadeDrive(power * direction,
                correctionAngle * Constants.Autonomous.FORWARD_CORRECTION_KP * direction)
        SmartDashboard.putNumber("distanceInAction", abs(Drive.rightDistanceMeters) - otherStart)
    }

    override fun onStop(timestamp: Double) {
        Drive.setOpenLoop(DriveSignal.NEUTRAL)
    }

    override fun onStart(timestamp: Double) {
        startTime = Timer.getFPGATimestamp()
        startAngle = Drive.angle
        startDist = Drive.leftDistanceMeters
        otherStart = Drive.leftDistanceMeters
    }
}
