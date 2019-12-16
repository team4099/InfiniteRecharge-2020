package org.usfirst.frc.team4099.robot2020.auto.actions

import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.auto.Action
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.robot2020.config.Constants
import org.usfirst.frc.team4099.robot2020.subsystems.Drive
import kotlin.math.abs
import kotlin.math.sign

class DistanceAction(inchesToMove: Double, slowMode: Boolean) : Action {
    private val direction = sign(inchesToMove)
    private val inchesToMove: Double = abs(inchesToMove)
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
        return abs(Drive.getLeftDistanceInches()) - startDist >= inchesToMove ||
                abs(Drive.getRightDistanceInches()) - otherStart >= inchesToMove ||
                abort ||
                Timer.getFPGATimestamp() - startTime > Constants.Autonomous.FORWARD_MAX_TIME_SECONDS
    }

    override fun onLoop(timestamp: Double) {
        val correctionAngle = startAngle - Drive.angle
        if (abs(correctionAngle) > Constants.Autonomous.FORWARD_GIVE_UP_ANGLE) {
            abort = true
            return
        }
        Drive.arcadeDrive(power * direction,
                correctionAngle * Constants.Autonomous.FORWARD_CORRECTION_KP * direction)
        SmartDashboard.putNumber("distanceInAction", abs(Drive.getRightDistanceInches()) - otherStart)
    }

    override fun onStop(timestamp: Double) {
        Drive.setOpenLoop(DriveSignal.NEUTRAL)
    }

    override fun onStart(timestamp: Double) {
        startTime = Timer.getFPGATimestamp()
        startAngle = Drive.angle
        startDist = Drive.getLeftDistanceInches()
        otherStart = Drive.getRightDistanceInches()
    }
}
