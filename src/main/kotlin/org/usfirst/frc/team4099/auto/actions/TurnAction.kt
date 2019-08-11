package org.usfirst.frc.team4099.auto.actions

import edu.wpi.first.wpilibj.Timer
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.lib.util.Utils
import org.usfirst.frc.team4099.robot.subsystems.Drive

/**
 * Created by Oksana on 2/16/2017.
 */
class TurnAction(angleToTurn: Double) : Action {
    private val mDrive: Drive = Drive.instance
    private val angleToTurn: Double = Math.abs(angleToTurn)
    private val direction: Int = Math.abs(angleToTurn.toInt()) / angleToTurn.toInt()
    private var power: Double = 0.5
    private var startAngle: Double = 0.toDouble()
    private var resetGyro: Boolean = true
    private var done: Boolean = false

    private val turnSignal: DriveSignal = DriveSignal(direction * power, -direction * power)

    constructor(angleToTurn: Double, slowMode: Boolean, resetGyro: Boolean) : this(angleToTurn) {
        if (slowMode) {
            this.power = .2
        }
        this.resetGyro = resetGyro
    }

    override fun isFinished(): Boolean {
        return Math.abs(mDrive.getAHRS()!!.angle - startAngle) >= angleToTurn || done
    }

    override fun update() {
        mDrive.setOpenLoop(turnSignal)
    }

    override fun done() {
        //        mDrive.finishForward()
        mDrive.setOpenLoop(DriveSignal.NEUTRAL)
        println("------- END FORWARD -------")
    }

    override fun start() {
        if (resetGyro) {
            while (!Utils.around(mDrive.getAHRS()!!.yaw.toDouble(), 0.0, 1.0)) {
                mDrive.getAHRS()!!.zeroYaw()
            }
            Timer.delay(1.0)
        }
        startAngle = mDrive.getAHRS()!!.yaw.toDouble()
        println("------- NEW START AUTONOMOUS RUN -------")
        println("Starting angle: " + startAngle)
    }
}
