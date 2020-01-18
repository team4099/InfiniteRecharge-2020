package com.team4099.robot2020.subsystems

import com.team4099.lib.config.Configurable
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Intake : Subsystem {

    private val sparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Intake.INTAKE_SPARK_MAX_ID)

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

    override val configurableProperties: List<Configurable<out Number>> = listOf()

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
    override fun onLoop(timestamp: Double, dT: Double) {
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
