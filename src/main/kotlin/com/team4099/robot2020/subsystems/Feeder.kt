package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.InvertType
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Feeder : Subsystem {
    private val inMasterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_IN_MASTER_ID)
    private val inSlaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Feeder.FEEDER_IN_SLAVE_ID,
            inMasterSparkMax,
            invertToMaster = false
    )

    private val inEncoder = inMasterSparkMax.encoder

    private val stopperTalon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Feeder.FEEDER_OUT_ID)

    var feederState = FeederState.IDLE
        set(value) {
            if (value != field) {
                HelixEvents.addEvent("FEEDER", "state changed to $value")
            }
            field = value
        }
    private var inPower = 0.0
        set(value) {
            if (value != field) {
                inMasterSparkMax.set(value)
            }
            field = value
        }
    private var stopperPower = 0.0
        set(value) {
            if (value != field) {
                stopperTalon.set(ControlMode.PercentOutput, value)
            }
            field = value
        }

    init {
        inMasterSparkMax.inverted = true
        inMasterSparkMax.setSmartCurrentLimit(30)
        stopperTalon.setInverted(InvertType.InvertMotorOutput)
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT, EXHAUST, IDLE
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("feeder/feederState", feederState.toString())
        SmartDashboard.putNumber("feeder/inPower", inPower)
        SmartDashboard.putNumber("feeder/stopperPower", stopperPower)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Feeder Master Motor Power") { inMasterSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Slave Motor Power") { inSlaveSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Stopper Motor Power") { stopperTalon.motorOutputPercent }

        HelixLogger.addSource("Feeder Master Motor Current") { inMasterSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Slave Motor Current") { inSlaveSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Stopper Motor Current") { stopperTalon.supplyCurrent }

        HelixLogger.addSource("Feeder State") { feederState.toString() }
    }

    override fun zeroSensors() {
        inEncoder.position = 0.0
    }

    override fun onStart(timestamp: Double) {
        feederState = FeederState.IDLE
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        when (feederState) {
            FeederState.INTAKE -> {
                stopperPower = -Constants.Feeder.FEEDER_HOLD_POWER
                inPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.HOLD -> {
                stopperPower = -Constants.Feeder.FEEDER_HOLD_POWER
                inPower = Constants.Feeder.FEEDER_HOLD_POWER
            }
            FeederState.SHOOT -> {
                stopperPower = Constants.Feeder.FEEDER_MAX_POWER
                inPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.EXHAUST -> {
                stopperPower = -Constants.Feeder.FEEDER_MAX_POWER
                inPower = -Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.IDLE -> {
                stopperPower = 0.0
                inPower = 0.0
            }
        }
    }

    override fun onStop(timestamp: Double) {
        feederState = FeederState.IDLE
    }
}
