package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Intake : Subsystem {

    private val talon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Intake.INTAKE_TALON_ID)

    private var intakePower = 0.0
        set(value) {
            if (field != value) {
                talon.set(ControlMode.PercentOutput, value)
                field = value
            }
        }
    var intakeState = IntakeState.IDLE

    enum class IntakeState {
        IN, IDLE, OUT
    }

    init {
        talon.inverted = false
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("intake/intakeState", intakeState.toString())
        SmartDashboard.putNumber("intake/intakePower", intakePower)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Intake Motor Power") { talon.motorOutputPercent }
        HelixLogger.addSource("Intake Motor Current") { talon.supplyCurrent }
        HelixLogger.addSource("Intake State") { intakeState.toString() }
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
