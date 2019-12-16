package org.usfirst.frc.team4099.robot2020

import com.team2363.logger.HelixEvents
import com.team2363.logger.HelixLogger
import edu.wpi.first.cameraserver.CameraServer
import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.TimedRobot
import org.usfirst.frc.team4099.lib.auto.AutoModeExecuter
import org.usfirst.frc.team4099.lib.loop.Looper
import org.usfirst.frc.team4099.lib.util.CrashTracker
import org.usfirst.frc.team4099.lib.util.around
import org.usfirst.frc.team4099.robot2020.config.Constants
import org.usfirst.frc.team4099.robot2020.config.ControlBoard
import org.usfirst.frc.team4099.robot2020.config.DashboardConfigurator
import org.usfirst.frc.team4099.robot2020.loops.BrownoutDefender
import org.usfirst.frc.team4099.robot2020.loops.FaultDetector
import org.usfirst.frc.team4099.robot2020.loops.VoltageEstimator
import org.usfirst.frc.team4099.robot2020.subsystems.Drive

object Robot : TimedRobot() {
    private lateinit var autoModeExecutor: AutoModeExecuter

    private val disabledLooper = Looper("disabledLooper", Constants.Looper.LOOPER_DT)
    private val enabledLooper = Looper("enabledLooper", Constants.Looper.LOOPER_DT)

    init {
        HelixEvents.addEvent("ROBOT", "Robot Construction")
        HelixLogger.addSource("Battery Voltage", RobotController::getBatteryVoltage)

        HelixLogger.addSource("Enabled Looper dT") { enabledLooper.dt }
        HelixLogger.addSource("Disabled Looper dT") { disabledLooper.dt }
    }

    override fun robotInit() {
        try {
            HelixEvents.startLogging()
            CameraServer.getInstance().startAutomaticCapture()

            DashboardConfigurator.initDashboard()

            SubsystemManager.register(Drive)

            enabledLooper.register(SubsystemManager.enabledLoop)
            enabledLooper.register(BrownoutDefender.instance)
            enabledLooper.register(FaultDetector)

            disabledLooper.register(SubsystemManager.disabledLoop)
            disabledLooper.register(VoltageEstimator.instance)
            enabledLooper.register(FaultDetector)

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
            autoModeExecutor = AutoModeExecuter()
            HelixEvents.addEvent("ROBOT", "Autonomous Enabled")
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("autonomousInit", t)
            throw t
        }
    }

    override fun teleopInit() {
        try {
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
        teleopPeriodic()
    }

    override fun teleopPeriodic() {
        try {
            Drive.setCheesyishDrive(
                    ControlBoard.throttle,
                    ControlBoard.turn,
                    ControlBoard.throttle.around(0.0, Constants.Joysticks.QUICK_TURN_THROTTLE_TOLERANCE)
            )
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("teleopPeriodic", t)
            throw t
        }
    }

    override fun testInit() {
        try {
            enabledLooper.start()
        } catch (t: Throwable) {
            CrashTracker.logThrowableCrash("testInit", t)
            throw t
        }
    }

    override fun testPeriodic() {}
}
