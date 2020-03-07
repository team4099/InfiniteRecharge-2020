package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.InvertType
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard

object Feeder : Subsystem {
//    private val frontLimitSwitch = DigitalInput(2)

    var ballIn = false

    private val inMasterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_IN_MASTER_ID)
    private val inSlaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Feeder.FEEDER_IN_SLAVE_ID,
            inMasterSparkMax,
            invertToMaster = false
    )

    private val inEncoder = inMasterSparkMax.encoder

    private val stopperTalon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Feeder.STOPPER_ID)

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
        @Suppress("MagicNumber")
        inMasterSparkMax.setSmartCurrentLimit(30)
        stopperTalon.enableCurrentLimit(true)
        stopperTalon.configContinuousCurrentLimit(20)
        stopperTalon.enableVoltageCompensation(true)
        stopperTalon.configVoltageCompSaturation(8.0)
        stopperTalon.setInverted(InvertType.None)
        inMasterSparkMax.burnFlash()
        inSlaveSparkMax.burnFlash()
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT, EXHAUST, IDLE, AUTO_SHOOT, AUTO_INTAKE, SLOW_INTAKE
    }

    override fun outputTelemetry() {}

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Feeder Master Motor Power") { inMasterSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Slave Motor Power") { inSlaveSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Stopper Motor Power") { stopperTalon.motorOutputPercent }

        HelixLogger.addSource("Feeder Master Motor Current") { inMasterSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Slave Motor Current") { inSlaveSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Stopper Motor Current") { stopperTalon.supplyCurrent }

        HelixLogger.addSource("Feeder State") { feederState.toString() }

        val shuffleboardTab = Shuffleboard.getTab("Feeder")
        shuffleboardTab.addString("State") { feederState.toString() }
        shuffleboardTab.addNumber("In Power") { inPower }
        shuffleboardTab.addNumber("Stopper Power") { stopperPower }
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
            FeederState.AUTO_INTAKE -> {
//                ballIn = !frontLimitSwitch.get()
                stopperPower = -Constants.Feeder.STOPPER_MAX_POWER
                inPower = Constants.Feeder.FEEDER_AUTO_INTAKE_POWER
            }
            FeederState.SLOW_INTAKE -> {
                stopperPower = -Constants.Feeder.STOPPER_MAX_POWER
                inPower = Constants.Feeder.FEEDER_MAX_POWER / 3
            }
            FeederState.INTAKE -> {
                stopperPower = -Constants.Feeder.STOPPER_MAX_POWER
                inPower = Constants.Feeder.FEEDER_INTAKE_POWER
            }
            FeederState.HOLD -> {
                stopperPower = -Constants.Feeder.STOPPER_HOLD_POWER
                inPower = Constants.Feeder.FEEDER_HOLD_POWER
            }
            FeederState.SHOOT -> {
                stopperPower = Constants.Feeder.STOPPER_MAX_POWER
                inPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.AUTO_SHOOT -> {
                if (Shooter.shooterReady && Vision.onTarget) {
                    stopperPower = Constants.Feeder.STOPPER_MAX_POWER
                    inPower = when (Vision.currentDistance) {
                        Vision.DistanceState.LINE -> Constants.Feeder.FEEDER_MAX_POWER
                        Vision.DistanceState.NEAR -> Constants.Feeder.FEEDER_MAX_POWER
                        Vision.DistanceState.MID -> Constants.Feeder.FEEDER_MAX_POWER / 2.33
                        Vision.DistanceState.FAR -> Constants.Feeder.FEEDER_MAX_POWER / 3
                    }
                } else {
                    stopperPower = 0.0
                    inPower = 0.0
                }
            }
            FeederState.EXHAUST -> {
                stopperPower = -Constants.Feeder.STOPPER_MAX_POWER
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
