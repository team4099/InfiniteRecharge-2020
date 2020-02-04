package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Intake : Subsystem {

    private val talon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Intake.INTAKE_TALON_ID)

    var inBeamBroken = false
        get() = talon.isFwdLimitSwitchClosed() > 0

    private var inBeamBrokenTimestamp = -1.0

    // take this out after adding one ballCount in superstructure to work with feeder
    var ballCount = 0

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
    override fun onLoop(timestamp: Double, dT: Double) {

        if (Intake.inBeamBroken) {
            if (inBeamBrokenTimestamp == -1.0) {
                inBeamBrokenTimestamp = timestamp
            }
        } else {
            inBeamBrokenTimestamp = -1.0
        }
        Feeder.ballCount += ((timestamp - inBeamBrokenTimestamp) / Constants.BeamBreak.IN_BEAM_BROKEN_BALL_TIME).toInt()

        @SuppressWarnings("MagicNumber")
        when (intakeState) {
            IntakeState.IN -> {
                if (ballCount >= 5) {
                    intakePower = 0.0
                } else {
                    intakePower = -1.0
                }
            }
            IntakeState.OUT -> intakePower = 1.0
            IntakeState.IDLE -> intakePower = 0.0
        }
    }

    override fun onStop(timestamp: Double) {
        intakeState = IntakeState.IDLE
        intakePower = 0.0
    }
}
