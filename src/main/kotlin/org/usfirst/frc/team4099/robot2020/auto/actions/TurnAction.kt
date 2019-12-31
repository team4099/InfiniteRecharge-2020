package org.usfirst.frc.team4099.robot2020.auto.actions

import kotlin.math.abs
import kotlin.math.sign
import org.usfirst.frc.team4099.lib.auto.Action
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.robot2020.config.Constants
import org.usfirst.frc.team4099.robot2020.subsystems.Drive

class TurnAction(angleToTurn: Double, slowMode: Boolean) : Action {
    private val direction = sign(angleToTurn)
    private val angleToTurn = abs(angleToTurn)
    private var power = Constants.Autonomous.TURN_POWER
    private var startAngle = 0.0
    private var done = false

    private val turnSignal = DriveSignal(direction * power, -direction * power)

    init {
        if (slowMode) {
            this.power = Constants.Autonomous.SLOW_TURN_POWER
        }
    }

    override fun isFinished(timestamp: Double): Boolean {
        return abs(Drive.angle - startAngle) >= angleToTurn || done
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        Drive.setOpenLoop(turnSignal)
    }

    override fun onStop(timestamp: Double) {
        Drive.setOpenLoop(DriveSignal.NEUTRAL)
    }

    override fun onStart(timestamp: Double) {
        startAngle = Drive.angle
    }
}
