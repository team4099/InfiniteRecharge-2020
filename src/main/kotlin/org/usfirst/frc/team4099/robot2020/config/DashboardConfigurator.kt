package org.usfirst.frc.team4099.robot2020.config

import com.team2363.logger.HelixEvents
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.auto.AutoMode
import org.usfirst.frc.team4099.lib.auto.AutoModeProvider
import org.usfirst.frc.team4099.robot2020.auto.modes.StandStillMode

/**
 * Controls the interactive elements of SmartDashboard.
 *
 * Keeps the network tables keys in one spot and enforces autonomous mode
 * invariants.
 */
object DashboardConfigurator {
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

    // Maps the name of a mode to a function that creates an instance of it.
    private val allModes = mapOf<String, AutoModeProvider>(
            Constants.Autonomous.DEFAULT_MODE_NAME to defaultMode
    )

    /**
     * Adds keys to SmartDashboard containing the possible modes and starting locations.
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

        HelixEvents.addEvent("Autonomous",
                "Selected autonomous: $selectedModeName from ${selectedStartEnum.dashboardName}")
        return mode(selectedStartEnum, selectedStartingDelay)
    }
}
