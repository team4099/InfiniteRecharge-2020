package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
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

    var hasPowerCell = false
    var timerStart = 0.0
    var holdTimer = 0.0

    enum class IntakeState {
        IN, IDLE, OUT
    }

    init {
        talon.inverted = false
        @Suppress("MagicNumber")
        talon.configClosedloopRamp(0.9)
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
    override fun onLoop(timestamp: Double, dT: Double) {
        synchronized(this) {
            when (intakeState) {
                IntakeState.IN -> {
                    intakePower = Constants.Intake.INTAKE_POWER
                    if (talon.supplyCurrent > Constants.Intake.STALL_LIMIT_AMPS) {
                        holdTimer = timestamp
                        if ((holdTimer - timerStart) > Constants.Intake.STALL_LIMIT_SECONDS) {
                            hasPowerCell = true
                        }
                    } else {
                        hasPowerCell = false
                    }
                }
                IntakeState.OUT -> {
                    intakePower = -Constants.Intake.INTAKE_POWER
                    if (talon.supplyCurrent > Constants.Intake.STALL_LIMIT_AMPS) {
                        holdTimer = timestamp
                        if ((holdTimer - timerStart) > Constants.Intake.STALL_LIMIT_SECONDS) {
                            hasPowerCell = true
                        }
                    } else {
                        hasPowerCell = false
                    }
                }
                IntakeState.IDLE -> {
                    intakePower = 0.0
                    timerStart = timestamp
                }
            }
//            println("Talon current: ${talon.supplyCurrent}")
            if (talon.supplyCurrent > Constants.Intake.STALL_LIMIT_AMPS) {
                timerStart = timestamp
                holdTimer = 0.0
                if ((holdTimer) > Constants.Intake.STALL_LIMIT_SECONDS) {
                    hasPowerCell = true
                }
            } else {
                hasPowerCell = false
            }
        }
    }

    override fun onStop(timestamp: Double) {
        intakeState = IntakeState.IDLE
        intakePower = 0.0
    }
}
