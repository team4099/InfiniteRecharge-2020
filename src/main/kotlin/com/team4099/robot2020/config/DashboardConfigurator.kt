package com.team4099.robot2020.config

import com.team4099.lib.auto.AutoMode
import com.team4099.lib.auto.AutoModeProvider
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.loop.Loop
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.auto.modes.DriveCharacterizeMode
import com.team4099.robot2020.auto.modes.DriveForwardMode
import com.team4099.robot2020.auto.modes.SixBallMode
import com.team4099.robot2020.auto.modes.StandStillMode
import edu.wpi.first.cameraserver.CameraServer
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import kotlin.math.roundToInt

/**
 * Controls the interactive elements of SmartDashboard.
 *
 * Keeps the network tables keys in one spot and enforces autonomous mode
 * invariants.
 */
object DashboardConfigurator : Loop {
    /**
     * Enumerates possible starting positions for the robot.
     *
     * @param dashboardName A human readable name for the location.
     */
    enum class StartingPosition(val dashboardName: String) {
        LEFT("LEFT"),
        CENTER("CENTER"),
        RIGHT("RIGHT")
    }

    private val defaultStart = StartingPosition.CENTER
    private val defaultMode = { _: StartingPosition, _: Double -> StandStillMode(0.0) }

    /**
     * Maps the name of a mode to a function that creates an instance of it.
     */
    private val allModes = mapOf<String, AutoModeProvider>(
        Constants.Autonomous.DEFAULT_MODE_NAME to defaultMode,
        "Drive Forward" to { _: StartingPosition, delay: Double -> DriveForwardMode(delay) },
        "6 Ball" to { _: StartingPosition, delay: Double -> SixBallMode(delay) },
        "Drivetrain Characterization" to { _: StartingPosition, delay: Double -> DriveCharacterizeMode(delay) }
    )

    private val configurableSubsystems = mutableListOf<ServoMotorSubsystem>()

    private val driveTab = Shuffleboard.getTab("Drive")
    private val autoModeChooser = SendableChooser<String>()
    private val startingPositionChooser = SendableChooser<String>()
    private val autoDelayEntry = driveTab.add("Auto Delay", Constants.Autonomous.DEFAULT_DELAY)
        .entry

    private val subsystemConfigEntries = mutableMapOf<String, Map<String, NetworkTableEntry>>()

    /**
     * Add a subsystem configuration to be manipulated through SmartDashboard. Also
     * enforces the contract that when gains change, the subsystem will reconfigure.
     *
     * @param subsystem A [ServoMotorSubsystem] to be configured remotely.
     */
    fun registerSubsystem(subsystem: ServoMotorSubsystem) {
        subsystem.config.motionConstraints.updateHook = { subsystem.updateMotionConstraints() }
        subsystem.config.positionPIDGains.updateHook = { subsystem.updatePIDGains() }
        subsystem.config.velocityPIDGains.updateHook = { subsystem.updatePIDGains() }

        configurableSubsystems.add(subsystem)
    }

    /**
     * Adds keys to SmartDashboard containing the possible autonomous modes
     * and starting locations, as well as
     */
    fun initDashboard() {
        var color = ""
        while (color == "")
            color = DriverStation.getInstance().alliance.name
        driveTab.add("Alliance Color", color)

        // Set up autonomous selector
        allModes.forEach {
            autoModeChooser.addOption(it.key, it.key)
        }
        autoModeChooser.setDefaultOption(Constants.Autonomous.DEFAULT_MODE_NAME, Constants.Autonomous.DEFAULT_MODE_NAME)
        driveTab.add("Auto Mode", autoModeChooser)

        StartingPosition.values().forEach {
            startingPositionChooser.addOption(it.dashboardName, it.toString())
        }
        startingPositionChooser.setDefaultOption(defaultStart.toString(), defaultStart.toString())
        driveTab.add("Auto Starting Position", startingPositionChooser)

        // Add keys for subsystem configs
        configurableSubsystems.forEach {
            val tab = Shuffleboard.getTab(it.config.name)
            val propertyMap = mapOf(
                "velocityPID/kP" to tab.add("Velocity PID/kP", it.config.velocityPIDGains.kP).entry,
                "velocityPID/kI" to tab.add("Velocity PID/kI", it.config.velocityPIDGains.kI).entry,
                "velocityPID/kD" to tab.add("Velocity PID/kD", it.config.velocityPIDGains.kD).entry,
                "velocityPID/kF" to tab.add("Velocity PID/kF", it.config.velocityPIDGains.kF).entry,
                "velocityPID/iZone" to tab.add("Velocity PID/iZone", it.config.velocityPIDGains.iZone).entry,
                "positionPID/kP" to tab.add("Position PID/kP", it.config.positionPIDGains.kP).entry,
                "positionPID/kI" to tab.add("Position PID/kI", it.config.positionPIDGains.kI).entry,
                "positionPID/kD" to tab.add("Position PID/kD", it.config.positionPIDGains.kD).entry,
                "positionPID/kF" to tab.add("Position PID/kF", it.config.positionPIDGains.kF).entry,
                "positionPID/iZone" to tab.add("Position PID/iZone", it.config.positionPIDGains.iZone).entry,
                "motion/maxAccel" to tab.add("Max Accel", it.config.motionConstraints.maxAccel).entry,
                "motion/cruiseVel" to tab.add("Cruise Vel", it.config.motionConstraints.cruiseVelocity).entry,
                "motion/forwardSoftLimit" to tab.add(
                    "Forward Soft Limit",
                    it.config.motionConstraints.forwardSoftLimit
                ).entry,
                "motion/reverseSoftLimit" to tab.add(
                    "Reverse Soft Limit",
                    it.config.motionConstraints.reverseSoftLimit
                ).entry,
                "motion/motionProfileCurveStrength" to tab.add(
                    "Motion Profile Curve Strength",
                    it.config.motionConstraints.motionProfileCurveStrength
                ).entry
            )
            subsystemConfigEntries[it.config.name] = propertyMap

            val limelightServer = CameraServer.getInstance().getServer("limelight")
            if (limelightServer == null) {
                HelixEvents.addEvent("DASHBOARD", "Could not get limelight camera feed")
            } else driveTab.add(limelightServer.source)
        }
    }

    /**
     * Gets the selected autonomous mode from SmartDashboard.
     *
     * @return An instance of the selected [AutoMode].
     */
    fun getSelectedAutoMode(): AutoMode {
        val selectedModeName = autoModeChooser.selected
        val selectedStartingPosition = startingPositionChooser.selected
        val selectedStartingDelay = autoDelayEntry.getDouble(Constants.Autonomous.DEFAULT_DELAY)
        val selectedStartEnum = StartingPosition.valueOf(selectedStartingPosition)

        val mode = allModes.getOrDefault(selectedModeName, defaultMode)

        HelixEvents.addEvent("AUTONOMOUS",
            "Selected autonomous: $selectedModeName from ${selectedStartEnum.dashboardName}")
        return mode(selectedStartEnum, selectedStartingDelay)
    }

    override fun onStart(timestamp: Double) {}

    override fun onLoop(timestamp: Double, dT: Double) {
        // Read new PID gains and motion constraints.
        configurableSubsystems.forEach {
            val propertyMap = subsystemConfigEntries.getOrDefault(it.config.name, mapOf())
            val velocityPID = it.config.velocityPIDGains
            velocityPID.kP = propertyMap["velocityPID/kP"]?.getDouble(velocityPID.kP) ?: velocityPID.kP
            velocityPID.kI = propertyMap["velocityPID/kI"]?.getDouble(velocityPID.kI) ?: velocityPID.kI
            velocityPID.kD = propertyMap["velocityPID/kD"]?.getDouble(velocityPID.kD) ?: velocityPID.kD
            velocityPID.kF = propertyMap["velocityPID/kF"]?.getDouble(velocityPID.kF) ?: velocityPID.kF
            velocityPID.iZone =
                propertyMap["velocityPID/iZone"]?.getDouble(velocityPID.iZone.toDouble())?.roundToInt()
                    ?: velocityPID.iZone

            val positionPID = it.config.positionPIDGains
            positionPID.kP = propertyMap["positionPID/kP"]?.getDouble(positionPID.kP) ?: positionPID.kP
            positionPID.kI = propertyMap["positionPID/kI"]?.getDouble(positionPID.kI) ?: positionPID.kI
            positionPID.kD = propertyMap["positionPID/kD"]?.getDouble(positionPID.kD) ?: positionPID.kD
            positionPID.kF = propertyMap["positionPID/kF"]?.getDouble(positionPID.kF) ?: positionPID.kF
            positionPID.iZone =
                propertyMap["positionPID/iZone"]?.getDouble(positionPID.iZone.toDouble())?.roundToInt()
                    ?: positionPID.iZone

            val motionConstraints = it.config.motionConstraints
            motionConstraints.maxAccel = propertyMap["motion/maxAccel"]?.getDouble(motionConstraints.maxAccel)
                ?: motionConstraints.maxAccel
            motionConstraints.cruiseVelocity =
                propertyMap["motion/cruiseVelocity"]?.getDouble(motionConstraints.cruiseVelocity)
                    ?: motionConstraints.cruiseVelocity
            motionConstraints.forwardSoftLimit =
                propertyMap["motion/forwardSoftLimit"]?.getDouble(motionConstraints.cruiseVelocity)
                    ?: motionConstraints.forwardSoftLimit
            motionConstraints.reverseSoftLimit =
                propertyMap["motion/reverseSoftLimit"]?.getDouble(motionConstraints.cruiseVelocity)
                    ?: motionConstraints.reverseSoftLimit
            motionConstraints.motionProfileCurveStrength =
                propertyMap["motion/motionProfileCurveStrength"]
                    ?.getDouble(motionConstraints.motionProfileCurveStrength.toDouble())?.roundToInt()
                    ?: motionConstraints.motionProfileCurveStrength
        }
    }

    override fun onStop(timestamp: Double) {}
}
