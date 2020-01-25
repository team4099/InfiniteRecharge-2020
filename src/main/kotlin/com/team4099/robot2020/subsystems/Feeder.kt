package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.Spark
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Feeder : Subsystem {
    private val inMasterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_OUT_ID)
    private val inSlaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Feeder.FEEDER_IN_SLAVE_ID,
            inMasterSparkMax
    )

    private val outTalon = CTREMotorControllerFactory.createDefaultTalonFX(Constants.Feeder.FEEDER_OUT_ID)

    var feederState = FeederState.IDLE
        set(value) {
            if (value != field) {
                HelixEvents.addEvent("FEEDER", "state changed to $value")
            }
        }
    private var feederInPower = 0.0
        set(value) {
            if (value != field) {
                inMasterSparkMax.set(value)
            }
            field = value
        }
    private var feederOutPower = 0.0
        set(value) {
            if (value != field) {
                outTalon.set(ControlMode.PercentOutput, value)
            }
            field = value
        }

    init {
        inMasterSparkMax.inverted = false
        inSlaveSparkMax.inverted = true

        outTalon.inverted = true
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT, EXHAUST, IDLE
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("feeder/feederState", feederState.toString())
        SmartDashboard.putNumber("feeder/feederInPower", feederInPower)
        SmartDashboard.putNumber("feeder/feederOutPower", feederOutPower)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Feeder master motor power") { inMasterSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder slave motor power") { inSlaveSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder out motor power") { outTalon.motorOutputPercent }

        HelixLogger.addSource("Feeder master motor current") { inMasterSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder slave motor current") { inSlaveSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder out motor current") { outTalon.supplyCurrent }

        HelixLogger.addSource("Feeder state") { feederState }
    }

    override fun zeroSensors() { }

    override fun onStart(timestamp: Double) {
        feederState = FeederState.HOLD
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        when (feederState) {
            FeederState.INTAKE -> {
                feederOutPower = -Constants.Feeder.FEEDER_HOLD_POWER
                feederInPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.HOLD -> {
                feederOutPower = -Constants.Feeder.FEEDER_HOLD_POWER
                feederInPower = Constants.Feeder.FEEDER_HOLD_POWER
            }
            FeederState.SHOOT -> {
                feederOutPower = Constants.Feeder.FEEDER_MAX_POWER
                feederInPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.EXHAUST -> {
                feederOutPower = -Constants.Feeder.FEEDER_MAX_POWER
                feederInPower = -Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.IDLE -> {
                feederOutPower = 0.0
                feederInPower = 0.0
            }
        }
    }

    override fun onStop(timestamp: Double) {
        feederState = FeederState.IDLE
        feederInPower = 0.0
        feederOutPower = 0.0
    }
}
