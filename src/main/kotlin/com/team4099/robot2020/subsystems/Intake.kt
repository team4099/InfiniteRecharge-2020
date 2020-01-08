package com.team4099.robot2020.subsystems

import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Intake : Subsystem {

    private val sparkMax = CANSparkMax(Constants.Intake.INTAKE_SPARK_MAX_ID, MotorType.kBrushless)

    var intakeState = IntakeState.IDLE

    enum class IntakeState {
        IN, IDLE, OUT
    }

    fun outputToSmartDashboard() {
        SmartDashboard.putString("intake/intakeState", intakeState.toString())
    }

    init {
        sparkMax.inverted = false
    }

    @Synchronized

    fun setIntakePower(power: Double) {
        sparkMax.set(power)
    }

    override fun outputTelemetry() { }

    override fun checkSystem() { }

    override fun registerLogging() { }

    override fun zeroSensors() { }

    override fun onStart(timestamp: Double) {
        intakeState = IntakeState.IDLE
    }

    override fun onLoop(timestamp: Double, dt: Double) {
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
