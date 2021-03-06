package com.team4099.robot2020

import com.team4099.lib.around
import com.team4099.lib.auto.AutoModeExecuter
import com.team4099.lib.logging.CrashTracker
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.loop.Looper
import com.team4099.robot2020.auto.PathStore
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.config.ControlBoard
import com.team4099.robot2020.config.DashboardConfigurator
import com.team4099.robot2020.loops.FaultDetector
import com.team4099.robot2020.loops.VoltageEstimator
import com.team4099.robot2020.subsystems.Climber
import com.team4099.robot2020.subsystems.Drive
import com.team4099.robot2020.subsystems.Feeder
import com.team4099.robot2020.subsystems.Intake
import com.team4099.robot2020.subsystems.Shooter
import com.team4099.robot2020.subsystems.Vision
import com.team4099.robot2020.subsystems.Wrist
import edu.wpi.first.wpilibj.DigitalInput
import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.TimedRobot
import kotlin.math.pow

object Robot : TimedRobot() {
    private lateinit var autoModeExecuter: AutoModeExecuter

    private val disabledLooper = Looper("disabledLooper", Constants.Looper.LOOPER_DT)
    private val enabledLooper = Looper("enabledLooper", Constants.Looper.LOOPER_DT)

    //    private val tuningTogglePin = DigitalInput(Constants.Tuning.TUNING_TOGGLE_PIN)
    val tuningEnabled: Boolean
        get() = true

    var robotName: Constants.Tuning.RobotName

    init {
        // Determine what robot we're running on.
        val robotId = Constants.Tuning.ROBOT_ID_PINS.withIndex().map { (i, pin) ->
            if (DigitalInput(pin).get()) 0 else 2.0.pow(i).toInt()
        }.sum()
        robotName = Constants.Tuning.ROBOT_ID_MAP.getOrDefault(robotId, Constants.Tuning.RobotName.COMPETITION)

        PathStore // Invoke path store to initialize it and generate the contained trajectories

        HelixEvents.addEvent("ROBOT", "Robot Construction (running on $robotName)")
        HelixLogger.addSource("Battery Voltage", RobotController::getBatteryVoltage)

        HelixLogger.addSource("Enabled Looper dT") { enabledLooper.dt }
        HelixLogger.addSource("Disabled Looper dT") { disabledLooper.dt }
    }

    override fun robotInit() {
        try {
            HelixEvents.startLogging()
//            CameraServer.getInstance().startAutomaticCapture()

            // Register all subsystems
            SubsystemManager.register(Drive)
            SubsystemManager.register(Climber)
            SubsystemManager.register(Intake)
            SubsystemManager.register(Wrist)
            SubsystemManager.register(Vision)
            SubsystemManager.register(Feeder)
            SubsystemManager.register(Shooter)

            enabledLooper.register(SubsystemManager.enabledLoop)
            enabledLooper.register(FaultDetector)

            disabledLooper.register(SubsystemManager.disabledLoop)
            disabledLooper.register(VoltageEstimator)
            disabledLooper.register(FaultDetector)
            if (tuningEnabled) disabledLooper.register(DashboardConfigurator)

            // Must come after subsystem registration
            HelixLogger.createFile()
            DashboardConfigurator.initDashboard()
            HelixEvents.addEvent("ROBOT", "Robot Initialized")
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("robotInit", t)
            throw t
        }
    }

    override fun disabledInit() {
        try {
            enabledLooper.stop() // end EnabledLooper
            disabledLooper.start() // start DisabledLooper

            HelixEvents.addEvent("ROBOT", "Robot Disabled")
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("disabledInit", t)
            throw t
        }
    }

    override fun autonomousInit() {
        try {
            if (::autoModeExecuter.isInitialized) autoModeExecuter.stop()
            Drive.zeroSensors()

            disabledLooper.stop() // end DisabledLooper
            enabledLooper.start() // start EnabledLooper

            autoModeExecuter = AutoModeExecuter(DashboardConfigurator.getSelectedAutoMode())
            autoModeExecuter.start()

            HelixEvents.addEvent("ROBOT", "Autonomous Enabled")
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("autonomousInit", t)
            throw t
        }
    }

    override fun teleopInit() {
        try {
            if (::autoModeExecuter.isInitialized) autoModeExecuter.stop()
            disabledLooper.stop()
            enabledLooper.start()
            HelixEvents.addEvent("ROBOT", "Teleop Enabled")
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("teleopInit", t)
            throw t
        }
    }

    override fun disabledPeriodic() {
        try {
            // outputAllToSmartDashboard()
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("disabledPeriodic", t)
            throw t
        }
    }

    override fun autonomousPeriodic() {
        try {
            // outputAllToSmartDashboard()
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("autonomousPeriodic", t)
            throw t
        }
    }

    override fun teleopPeriodic() {
        try {
            if (ControlBoard.enableVisionAlignment) {
                Vision.state = Vision.VisionState.AIMING
                Drive.setCheesyishDrive(
                    0.0,
                    Vision.steeringAdjust,
                    true
                )
            } else {
                Vision.state = Vision.VisionState.IDLE
                if (ControlBoard.slowMode) {
                    Drive.setCheesyishDrive(
                        ControlBoard.throttle * Constants.Drive.SLOW_MODE_SCALE,
                        ControlBoard.turn * Constants.Drive.SLOW_MODE_SCALE,
                        ControlBoard.throttle.around(0.0, Constants.Joysticks.QUICK_TURN_THROTTLE_TOLERANCE)
                    )
                } else {
                    Drive.setCheesyishDrive(
                        ControlBoard.throttle,
                        ControlBoard.turn,
                        ControlBoard.throttle.around(0.0, Constants.Joysticks.QUICK_TURN_THROTTLE_TOLERANCE)
                    )
                }
            }

            @Suppress("MagicNumber")
            when {
//                ControlBoard.climberUp -> Climber.positionSetpoint = Constants.Climber.ClimberPosition.UP
//                ControlBoard.climberDown -> Climber.positionSetpoint = Constants.Climber.ClimberPosition.DOWN
                else -> Climber.openLoopPower = (ControlBoard.sampleClimberVelocity).pow(3) * 0.65
            }

            when {
                ControlBoard.runIntakeIn -> {
                    Intake.intakeState = Intake.IntakeState.IN
                    Feeder.feederState = Feeder.FeederState.AUTO_INTAKE
                    Wrist.positionSetpoint = Constants.Wrist.WristPosition.HORIZONTAL
                }
                ControlBoard.runIntakeOut -> {
                    Intake.intakeState = Intake.IntakeState.OUT
                    Wrist.positionSetpoint = Constants.Wrist.WristPosition.HORIZONTAL
                }
                else -> {
                    Intake.intakeState = Intake.IntakeState.IDLE
                    Wrist.positionSetpoint = Constants.Wrist.WristPosition.VERTICAL
                }
            }

            when {
                ControlBoard.startShooter -> {
                    Shooter.shooterState = Shooter.State.SHOOTING
                    Feeder.feederState = Feeder.FeederState.AUTO_SHOOT
                }
                else -> {
                    Shooter.shooterState = Shooter.State.IDLE
                }
            }

            when {
                ControlBoard.runFeederIn -> {
                    Feeder.feederState = Feeder.FeederState.INTAKE
                }
                ControlBoard.runFeederShoot -> {
                    Feeder.feederState = Feeder.FeederState.SHOOT
                }
                ControlBoard.runFeederOut -> {
                    Feeder.feederState = Feeder.FeederState.EXHAUST
                }
            }

            if (
                !ControlBoard.startShooter &&
                !ControlBoard.runFeederOut &&
                !ControlBoard.runFeederIn &&
                !ControlBoard.runFeederShoot
            ) {
                Feeder.feederState = Feeder.FeederState.IDLE
            }

//            if (Intake.hasPowerCell) {
//                Feeder.feederState = Feeder.FeederState.AUTO_INTAKE
//            }
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("teleopPeriodic", t)
            throw t
        }
    }

    override fun testInit() {
        try {
            disabledLooper.stop()
            enabledLooper.start()
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("testInit", t)
            throw t
        }
    }

    override fun testPeriodic() {}
}
