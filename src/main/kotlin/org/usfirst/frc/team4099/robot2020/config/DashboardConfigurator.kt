package org.usfirst.frc.team4099.robot2020.config

import com.team2363.logger.HelixEvents
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.auto.AutoMode
import org.usfirst.frc.team4099.lib.auto.AutoModeProvider
import org.usfirst.frc.team4099.lib.loop.Loop
import org.usfirst.frc.team4099.lib.subsystem.ServoMotorSubsystem
import org.usfirst.frc.team4099.robot2020.auto.modes.StandStillMode
import kotlin.math.roundToInt

/**
 * Controls the interactive elements of SmartDashboard.
 *
 * Keeps the network tables keys in one spot and enforces autonomous mode
 * invariants.
 */
object DashboardConfigurator: Loop {
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
            Constants.Autonomous.DEFAULT_MODE_NAME to defaultMode
    )

    private val configurableSubsystems = mutableListOf<ServoMotorSubsystem>()

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
        SmartDashboard.putString(Constants.Dashboard.ALLIANCE_COLOR_KEY, color)

        // Set up autonomous selector
        val autoModesString = "[ ${allModes.keys.joinToString(",")} ]"
        SmartDashboard.putString(Constants.Dashboard.AUTO_OPTIONS_KEY, autoModesString)
        SmartDashboard.putString(Constants.Dashboard.SELECTED_AUTO_MODE_KEY, Constants.Autonomous.DEFAULT_MODE_NAME)

        val autoStartsString = "[ ${StartingPosition.values().joinToString(",") { it.dashboardName }} ]"
        SmartDashboard.putString(Constants.Dashboard.AUTO_STARTS_KEY, autoStartsString)
        SmartDashboard.putString(Constants.Dashboard.SELECTED_AUTO_START_POS_KEY, defaultStart.dashboardName)

        SmartDashboard.putNumber(Constants.Dashboard.SELECTED_AUTO_START_DELAY_KEY, Constants.Autonomous.DEFAULT_DELAY)

        // Add keys for subsystem configs
        configurableSubsystems.forEach {
            SmartDashboard.putNumber("${it.config.name}/velocityPID/kP", it.config.velocityPIDGains.kP)
            SmartDashboard.putNumber("${it.config.name}/velocityPID/kI", it.config.velocityPIDGains.kI)
            SmartDashboard.putNumber("${it.config.name}/velocityPID/kD", it.config.velocityPIDGains.kD)
            SmartDashboard.putNumber("${it.config.name}/velocityPID/iZone", it.config.velocityPIDGains.iZone.toDouble())

            SmartDashboard.putNumber("${it.config.name}/positionPID/kP", it.config.positionPIDGains.kP)
            SmartDashboard.putNumber("${it.config.name}/positionPID/kI", it.config.positionPIDGains.kI)
            SmartDashboard.putNumber("${it.config.name}/positionPID/kD", it.config.positionPIDGains.kD)
            SmartDashboard.putNumber("${it.config.name}/positionPID/iZone", it.config.positionPIDGains.iZone.toDouble())

            SmartDashboard.putNumber("${it.config.name}/motion/maxAccel", it.config.motionConstraints.maxAccel)
            SmartDashboard.putNumber("${it.config.name}/motion/cruiseVel", it.config.motionConstraints.cruiseVelocity)
            SmartDashboard.putNumber(
                "${it.config.name}/motion/forwardSoftLimit",
                it.config.motionConstraints.forwardSoftLimit
            )
            SmartDashboard.putNumber(
                "${it.config.name}/motion/reverseSoftLimit",
                it.config.motionConstraints.reverseSoftLimit
            )
            SmartDashboard.putNumber(
                "${it.config.name}/motion/motionProfileCurveStrength",
                it.config.motionConstraints.motionProfileCurveStrength.toDouble()
            )
        }
    }

    /**
     * Gets the selected autonomous mode from SmartDashboard.
     *
     * @return An instance of the selected [AutoMode].
     */
    fun getSelectedAutoMode(): AutoMode {
        val selectedModeName = SmartDashboard.getString(
                Constants.Dashboard.SELECTED_AUTO_MODE_KEY,
                Constants.Autonomous.DEFAULT_MODE_NAME
        )
        val selectedStartingPosition = SmartDashboard.getString(
                Constants.Dashboard.SELECTED_AUTO_START_POS_KEY,
                defaultStart.dashboardName
        )
        val selectedStartingDelay = SmartDashboard.getNumber(
                Constants.Dashboard.SELECTED_AUTO_START_DELAY_KEY,
                Constants.Autonomous.DEFAULT_DELAY
        )

        var selectedStartEnum = defaultStart

        for (start in StartingPosition.values()) {
            if (start.dashboardName == selectedStartingPosition) {
                selectedStartEnum = start
                break
            }
        }

        val mode = allModes.getOrDefault(selectedModeName, defaultMode)

        HelixEvents.addEvent("AUTONOMOUS",
                "Selected autonomous: $selectedModeName from ${selectedStartEnum.dashboardName}")
        return mode(selectedStartEnum, selectedStartingDelay)
    }

    override fun onStart(timestamp: Double) {}

    override fun onLoop(timestamp: Double, dT: Double) {
        // Read new PID gains and motion constraints.
        configurableSubsystems.forEach {
            it.config.velocityPIDGains.kP =
                SmartDashboard.getNumber("${it.config.name}/velocityPID/kP", it.config.velocityPIDGains.kP)
            it.config.velocityPIDGains.kI =
                SmartDashboard.getNumber("${it.config.name}/velocityPID/kI", it.config.velocityPIDGains.kI)
            it.config.velocityPIDGains.kD =
                SmartDashboard.getNumber("${it.config.name}/velocityPID/kD", it.config.velocityPIDGains.kD)
            it.config.velocityPIDGains.iZone = SmartDashboard.getNumber("" +
                "${it.config.name}/velocityPID/iZone",
                it.config.velocityPIDGains.iZone.toDouble()
            ).roundToInt()

            it.config.positionPIDGains.kP =
                SmartDashboard.getNumber("${it.config.name}/positionPID/kP", it.config.positionPIDGains.kP)
            it.config.positionPIDGains.kP =
                SmartDashboard.getNumber("${it.config.name}/positionPID/kI", it.config.positionPIDGains.kP)
            it.config.positionPIDGains.kD =
                SmartDashboard.getNumber("${it.config.name}/positionPID/kD", it.config.positionPIDGains.kD)
            it.config.positionPIDGains.iZone = SmartDashboard.getNumber(
                    "${it.config.name}/positionPID/iZone",
                    it.config.positionPIDGains.iZone.toDouble()
            ).roundToInt()

            it.config.motionConstraints.maxAccel =
                SmartDashboard.getNumber("${it.config.name}/motion/maxAccel", it.config.motionConstraints.maxAccel)
            it.config.motionConstraints.cruiseVelocity =
                SmartDashboard.getNumber("" +
                    "${it.config.name}/motion/cruiseVel",
                    it.config.motionConstraints.cruiseVelocity
                )
            it.config.motionConstraints.forwardSoftLimit = SmartDashboard.getNumber(
                "${it.config.name}/motion/forwardSoftLimit",
                it.config.motionConstraints.forwardSoftLimit
            )
            it.config.motionConstraints.reverseSoftLimit = SmartDashboard.getNumber(
                "${it.config.name}/motion/reverseSoftLimit",
                it.config.motionConstraints.reverseSoftLimit
            )
            it.config.motionConstraints.motionProfileCurveStrength = SmartDashboard.getNumber(
                "${it.config.name}/motion/motionProfileCurveStrength",
                it.config.motionConstraints.motionProfileCurveStrength.toDouble()
            ).roundToInt()
        }
    }

    override fun onStop(timestamp: Double) {}
}
