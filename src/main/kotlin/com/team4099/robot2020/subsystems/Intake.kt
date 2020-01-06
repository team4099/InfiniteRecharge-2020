package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.NeutralMode
import com.revrobotics.SparkMax
import com.team4099.lib.loop.Loop
import com.team4099.lib.motorcontroller.GenericSmartMotorController
import com.team4099.lib.motorcontroller.LazySparkMax
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Intake: Subsystem {

    //private val sparkmax = SparkMax(Constants.Intake.SPARKMAX.ID)
    private val sparkMax: LazySparkMax

    var intakeState = IntakeState.IDLE

    enum class IntakeState {
        IN, IDLE, OUT
    }

    fun outputToSmartDashboard() {
        SmartDashboard.putString("intake/intakeState", intakeState.toString())
    }

    init {
        sparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Intake.INTAKE_SPARK_MAX_ID)

        sparkMax.inverted = false
    }

    @Synchronized

    fun setIntakePower(power: Double) {
        sparkMax.set(power)
    }

    override fun outputTelemetry() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkSystem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLogging() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun zeroSensors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStart(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        intakeState = IntakeState.IDLE
    }

    override fun onLoop(timestamp: Double, dt: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        synchronized(this@Intake) {
            when (intakeState) {
                IntakeState.IN -> setIntakePower(-1.0)
                IntakeState.OUT -> setIntakePower(1.0)
                IntakeState.IDLE -> setIntakePower(0.0)
            }
        }
    }

    override fun onStop(timestamp: Double) {
        intakeState = IntakeState.IDLE
        setIntakePower(0.0)
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
//        val loop = object: Loop {
//            override fun onStart(timestamp: Double) {
//                intakeState = IntakeState.IDLE
//            }
//
//            /**
//             * Sets Intake to -1 if pulling in, to 0 if stationary, and 1 if pushing out
//             */
//           fun onLoop(timestamp: Double) {
//                synchronized(this@Intake) {
//                    when (intakeState) {
//                        IntakeState.IN -> setIntakePower(-1.0)
//                        IntakeState.IDLE -> setIntakePower(0.0)
//                        IntakeState.OUT -> setIntakePower(1.0)
//                    }
//                }
//            }
//            override fun onStop(timestamp: Double) = stop()
//        }
//        fun stop() {
//            intakeState = IntakeState.IDLE
//            setIntakePower(0.0)
//        }
//
//        override fun zeroSensors() {}
