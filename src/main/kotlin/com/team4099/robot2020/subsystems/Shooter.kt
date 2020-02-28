package com.team4099.robot2020.subsystems

import com.revrobotics.ControlType
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import kotlin.math.abs
import kotlin.math.sign
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard

object Shooter : Subsystem {
    private val masterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(
            Constants.Shooter.MASTER_SPARKMAX_ID)

    private val slaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Shooter.SLAVE_SPARKMAX_ID, masterSparkMax, invertToMaster = true)

    private val encoder = masterSparkMax.encoder

    var idleTime = 0.0
    var spinupTime = 0.0

    private var openLoopPowerTarget: Double = 0.0
        set(value) {
            masterSparkMax.set(value)
            field = value
        }
    private var velocitySetpoint: Double = 0.0
        set(value) {
            // Feedforward constants comes from characterizing using WPILib frc-characterization.
            // kS represents the static friction in the shooter system and kV represents the percent output
            // needed to increase velocity by 1 unit if no other loads are applied.
            masterSparkMax.set(
                ControlType.kVelocity,
                value + 41,
                0,
                Constants.Shooter.SHOOTER_KS * sign(value) + Constants.Shooter.SHOOTER_KV * value // Volts
            )
            field = value
        }

    enum class State {
        SHOOTING, IDLE
    }

    private val currentVelocity
        get() = encoder.velocity

    var shooterState = State.IDLE

    var shooterReady = false

    init {
        masterSparkMax.pidController.setP(Constants.Shooter.SHOOTER_PID.kP)
        masterSparkMax.pidController.setI(Constants.Shooter.SHOOTER_PID.kI)
        masterSparkMax.pidController.setD(Constants.Shooter.SHOOTER_PID.kD)
        masterSparkMax.pidController.setFF(Constants.Shooter.SHOOTER_PID.kF)
        masterSparkMax.pidController.setIZone(Constants.Shooter.SHOOTER_PID.iZone.toDouble())

        masterSparkMax.pidController.setOutputRange(Constants.Shooter.SHOOTER_KS, 1.0, 0)

        masterSparkMax.setSmartCurrentLimit(0)

        masterSparkMax.inverted = true

//        masterSparkMax.enableVoltageCompensation(12.0)
//        slaveSparkMax.enableVoltageCompensation(12.0)

        masterSparkMax.disableVoltageCompensation()
        slaveSparkMax.disableVoltageCompensation()

        masterSparkMax.closedLoopRampRate = 1.0

        masterSparkMax.burnFlash()
        slaveSparkMax.burnFlash()
    }

    @Synchronized
    override fun onStart(timestamp: Double) {
        openLoopPowerTarget = 0.0
    }

    @Synchronized
    override fun onStop(timestamp: Double) {
        openLoopPowerTarget = 0.0
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        when (shooterState) {
            State.IDLE -> {
                openLoopPowerTarget = 0.0
                spinupTime = 0.0
                idleTime = timestamp
            }
            State.SHOOTING -> {
                velocitySetpoint = Constants.Shooter.TARGET_VELOCITY
                shooterReady = abs(currentVelocity - velocitySetpoint) <=
                    Constants.Shooter.VELOCITY_ERROR_THRESHOLD
                if (shooterReady && spinupTime == 0.0) {
                    spinupTime = idleTime - timestamp
                }
            }
        }
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Shooter State") { shooterState.toString() }
        HelixLogger.addSource("Shooter Velocity Setpoint") { velocitySetpoint }
        HelixLogger.addSource("Shooter Current Velocity") { currentVelocity }
        HelixLogger.addSource("Shooter Spin-up Time") { spinupTime }
        HelixLogger.addSource("Shooter Master Output Current") {
            masterSparkMax.outputCurrent
        }
        HelixLogger.addSource("Shooter Slave Output Current") {
            slaveSparkMax.outputCurrent
        }
        HelixLogger.addSource("Shooter Master Percent Output") {
            masterSparkMax.appliedOutput
        }
        HelixLogger.addSource("Shooter Slave Percent Output") {
            slaveSparkMax.appliedOutput
        }

        val shuffleboardTab = Shuffleboard.getTab("Shooter")
        shuffleboardTab.addString("State") { shooterState.toString() }
        shuffleboardTab.addNumber("Velocity Setpoint") { velocitySetpoint }
        shuffleboardTab.addNumber("Master Percent Output") { masterSparkMax.appliedOutput }
        shuffleboardTab.addNumber("Current Speed") { currentVelocity }
    }

    override fun outputTelemetry() {}

    override fun zeroSensors() {}
}
