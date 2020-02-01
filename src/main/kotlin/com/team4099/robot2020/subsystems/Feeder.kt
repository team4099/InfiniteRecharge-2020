package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Feeder : Subsystem {
    private val inMasterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_OUT_ID)
    private val inSlaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Feeder.FEEDER_IN_SLAVE_ID,
            inMasterSparkMax,
            invertToMaster = true
    )

    private val stopperTalon = CTREMotorControllerFactory.createDefaultTalonFX(Constants.Feeder.FEEDER_OUT_ID)

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
    private var feederStopperPower = 0.0
        set(value) {
            if (value != field) {
                stopperTalon.set(ControlMode.PercentOutput, value)
            }
            field = value
        }

    init {
        inMasterSparkMax.inverted = false
        inSlaveSparkMax.inverted = true

        stopperTalon.inverted = true
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT, EXHAUST, IDLE
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("feeder/feederState", feederState.toString())
        SmartDashboard.putNumber("feeder/feederInPower", feederInPower)
        SmartDashboard.putNumber("feeder/feederStopperPower", feederStopperPower)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Feeder Master Motor Power") { inMasterSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Slave Motor Power") { inSlaveSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Stopper Motor Power") { stopperTalon.motorOutputPercent }

        HelixLogger.addSource("Feeder Master Motor Current") { inMasterSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Slave Motor Current") { inSlaveSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Stopper Motor current") { stopperTalon.supplyCurrent }

        HelixLogger.addSource("Feeder State") { feederState }
    }

    override fun zeroSensors() { }

    override fun onStart(timestamp: Double) {
        feederState = FeederState.HOLD
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        when (feederState) {
            FeederState.INTAKE -> {
                feederStopperPower = -Constants.Feeder.FEEDER_HOLD_POWER
                feederInPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.HOLD -> {
                feederStopperPower = -Constants.Feeder.FEEDER_HOLD_POWER
                feederInPower = Constants.Feeder.FEEDER_HOLD_POWER
            }
            FeederState.SHOOT -> {
                feederStopperPower = Constants.Feeder.FEEDER_MAX_POWER
                feederInPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.EXHAUST -> {
                feederStopperPower = -Constants.Feeder.FEEDER_MAX_POWER
                feederInPower = -Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.IDLE -> {
                feederStopperPower = 0.0
                feederInPower = 0.0
            }
        }
    }

    override fun onStop(timestamp: Double) {
        feederState = FeederState.IDLE
        feederInPower = 0.0
        feederStopperPower = 0.0
    }
}
