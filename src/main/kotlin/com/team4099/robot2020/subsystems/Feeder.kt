package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.InvertType
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard

object Feeder : Subsystem {
    private val feederLimitSwitch = DigitalInput(2)
    private val shooterLimitSwitch = DigitalInput(3)

    var ballIn = false

    private val inSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_IN_ID)
//    private val inSlaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
//            Constants.Feeder.FEEDER_IN_SLAVE_ID,
//            inMasterSparkMax,
//            invertToMaster = false
//    )

    private val upSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_UP_ID)
    private val inEncoder = inSparkMax.encoder

    private val stopperTalon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Feeder.STOPPER_ID)

    private var lastFeederBeamBroken = 0.0

    private val feederBeamBroken
        get() = feederLimitSwitch.get()

    private val shooterBeamBroken
        get() = shooterLimitSwitch.get()

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
                inSparkMax.set(value)
            }
            field = value
        }

    private var upPower = 0.0
        set(value) {
            if (value != field) {
                upSparkMax.set(value)
            }
        }

    private var stopperPower = 0.0
        set(value) {
            if (value != field) {
                stopperTalon.set(ControlMode.PercentOutput, value)
            }
            field = value
        }

    init {
        @Suppress("MagicNumber")
        inSparkMax.setSmartCurrentLimit(30)
        @Suppress("MagicNumber")
        upSparkMax.setSmartCurrentLimit(30)
        stopperTalon.enableCurrentLimit(true)
        @Suppress("MagicNumber")
        stopperTalon.configContinuousCurrentLimit(20)
        stopperTalon.enableVoltageCompensation(true)
        @Suppress("MagicNumber")
        stopperTalon.configVoltageCompSaturation(8.0)
        stopperTalon.setInverted(InvertType.None)
        inSparkMax.burnFlash()
        upSparkMax.burnFlash()
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT, EXHAUST, IDLE, AUTO_SHOOT, AUTO_INTAKE, SLOW_INTAKE
    }

    override fun outputTelemetry() {}

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Feeder In Motor Power") { inSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Up Motor Power") { upSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Stopper Motor Power") { stopperTalon.motorOutputPercent }

        HelixLogger.addSource("Feeder In Motor Current") { inSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Up Motor Current") { upSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Stopper Motor Current") { stopperTalon.supplyCurrent }

        HelixLogger.addSource("Feeder State") { feederState.toString() }

        val shuffleboardTab = Shuffleboard.getTab("Feeder")
        shuffleboardTab.addString("State") { feederState.toString() }
        shuffleboardTab.addNumber("In Power") { inPower }
        shuffleboardTab.addNumber("Up Power") { upPower }
        shuffleboardTab.addNumber("Stopper Power") { stopperPower }
    }

    override fun zeroSensors() {
        inEncoder.position = 0.0
    }

    override fun onStart(timestamp: Double) {
        feederState = FeederState.IDLE
    }

    @Synchronized
    @Suppress("MagicNumber")
    override fun onLoop(timestamp: Double, dT: Double) {
        if (feederBeamBroken) {
            lastFeederBeamBroken = timestamp
        }
        if (feederState == FeederState.IDLE || feederState == FeederState.AUTO_INTAKE) {
            upPower = if ((timestamp - lastFeederBeamBroken < 0.5) && !shooterBeamBroken) {
                Constants.Feeder.FEEDER_AUTO_INTAKE_POWER
            } else {
                0.0
            }
        }

        when (feederState) {
            FeederState.AUTO_INTAKE -> {
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
                upPower = Constants.Feeder.FEEDER_INTAKE_POWER
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
                    upPower = when (Vision.currentDistance) {
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
                upPower = -Constants.Feeder.FEEDER_MAX_POWER
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
