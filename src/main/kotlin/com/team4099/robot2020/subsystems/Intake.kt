package com.team4099.robot2020.subsystems

import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Intake : Subsystem {

    private val sparkMax = CANSparkMax(Constants.Intake.INTAKE_SPARK_MAX_ID, MotorType.kBrushless)

    private var intakePower = 0.0
        set(value) {
            if (field != value) {
                sparkMax.set(value)
                field = value
            }
        }
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

    override fun outputTelemetry() {
        SmartDashboard.putString("intake/intakeState", intakeState.toString())
        SmartDashboard.putNumber("intake/intakePower", intakePower)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Intake output current") { sparkMax.outputCurrent }
        HelixLogger.addSource("Intake state") { intakeState.toString() }
        HelixLogger.addSource("Intake output power") { sparkMax.outputCurrent }
    }

    override fun zeroSensors() {}

    override fun onStart(timestamp: Double) {
        intakeState = IntakeState.IDLE
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dt: Double) {
        synchronized(this) {
            when (intakeState) {
                IntakeState.IN -> intakePower = -1.0
                IntakeState.OUT -> intakePower = 1.0
                IntakeState.IDLE -> intakePower = 0.0
            }
        }
    }

    override fun onStop(timestamp: Double) {
        intakeState = IntakeState.IDLE
        intakePower = 0.0
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
