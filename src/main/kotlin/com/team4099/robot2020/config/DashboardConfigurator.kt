package com.team4099.robot2020.config

import com.team4099.lib.logging.HelixEvents
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import com.team4099.lib.auto.AutoMode
import com.team4099.lib.auto.AutoModeProvider
import com.team4099.lib.config.Configurable
import com.team4099.lib.loop.Loop
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.auto.modes.StandStillMode

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
            Constants.Autonomous.DEFAULT_MODE_NAME to defaultMode
    )

    /**
     * A list of [Configurable] objects to be manipulated through SmartDashboard
     */
    private val configurables = mutableListOf<Configurable<out Number>>()

    /**
     * Add a subsystem configuration to be manipulated through SmartDashboard. Also
     * enforces the contract that when values change, the subsystem will reconfigure.
     *
     * @param subsystem A [Subsystem] to be configured remotely.
     */
    fun registerSubsystem(subsystem: Subsystem) {
        configurables.addAll(subsystem.configurableProperties)
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
        configurables.forEach {
            configurable -> configurable.properties.forEach {
                SmartDashboard.putNumber("${configurable.keyPrefix}/${it.key}", it.value.getter.call().toDouble())
            }
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
        // Read new values for configurable properties.
        configurables.forEach {
            configurable -> configurable.properties.forEach {
                configurable.updateProperty(it.key, SmartDashboard.getNumber(
                    "${configurable.keyPrefix}/${it.key}",
                    it.value.get().toDouble())
                )
            }
        }
    }

    override fun onStop(timestamp: Double) {}
}
