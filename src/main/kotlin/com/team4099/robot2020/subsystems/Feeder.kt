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

    private val inSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_IN_ID)
//    private val inSlaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
//            Constants.Feeder.FEEDER_IN_SLAVE_ID,
//            inMasterSparkMax,
//            invertToMaster = true
//    )

    private val upSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_UP_ID)

    private val inEncoder = inSparkMax.encoder

    private val stopperTalon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Feeder.FEEDER_OUT_ID)

    private var outBeamBroken = false
        get() = stopperTalon.isFwdLimitSwitchClosed > 0
    private var beamNeverBroken = true

    private var outBeamBrokenTimestamp = -1.0

    // take this out after adding one ballCount in superstructure to work with intake
    var ballCount = 0

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
        stopperTalon.inverted = true
    }

    enum class FeederState {
        INTAKE, SHOOT, EXHAUST, IDLE
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("feeder/feederState", feederState.toString())
        SmartDashboard.putNumber("feeder/inPower", inPower)
        SmartDashboard.putNumber("feeder/stopperPower", stopperPower)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Feeder Master Motor Power") { inSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Vertical Motor Power") { upSparkMax.appliedOutput }
        HelixLogger.addSource("Feeder Stopper Motor Power") { stopperTalon.motorOutputPercent }

        HelixLogger.addSource("Feeder Master Motor Current") { inSparkMax.outputCurrent }
        HelixLogger.addSource("Feeder Vertical Motor Current") { upSparkMax.outputCurrent }
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

        if (outBeamBroken) {
            if (outBeamBrokenTimestamp == -1.0) {
                outBeamBrokenTimestamp = timestamp
            }
            beamNeverBroken = false
        } else {
            ballCount -= ((timestamp - outBeamBrokenTimestamp) / Constants.Feeder.OUT_BEAM_BROKEN_BALL_TIME).toInt()
            outBeamBrokenTimestamp = -1.0
        }

        if (Intake.currentSensed && Shooter.shooterState != Shooter.State.SHOOTING) {
            feederState = FeederState.INTAKE
        } else {
            feederState = FeederState.IDLE
        }

        if (timestamp - Intake.currentSensedTimestamp > Constants.Intake.CURRENT_TO_IN_BEAM_BREAK_TIME) {
            feederState = FeederState.IDLE
        }

        when (feederState) {
            FeederState.INTAKE -> {
                stopperPower = -Constants.Feeder.FEEDER_HOLD_POWER
                inPower = Constants.Feeder.FEEDER_MAX_POWER
                upPower = Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.SHOOT -> {
                if (beamNeverBroken || outBeamBroken) {
                    stopperPower = Constants.Feeder.FEEDER_MAX_POWER
                    inPower = Constants.Feeder.FEEDER_MAX_POWER
                    upPower = Constants.Feeder.FEEDER_MAX_POWER
                } else {
                    feederState = FeederState.IDLE
                }
            }
            FeederState.EXHAUST -> {
                stopperPower = -Constants.Feeder.FEEDER_MAX_POWER
                inPower = -Constants.Feeder.FEEDER_MAX_POWER
                upPower = -Constants.Feeder.FEEDER_MAX_POWER
            }
            FeederState.IDLE -> {
                stopperPower = 0.0
                inPower = 0.0
                upPower = 0.0
            }
        }
    }

    override fun onStop(timestamp: Double) {
        feederState = FeederState.IDLE
    }
}
