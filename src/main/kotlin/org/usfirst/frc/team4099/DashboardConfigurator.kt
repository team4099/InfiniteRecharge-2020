package org.usfirst.frc.team4099

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import main.java.org.usfirst.frc.team4099.lib.util.AutoModeCreator
import org.usfirst.frc.team4099.auto.modes.*
import org.usfirst.frc.team4099.robot.Constants
import org.usfirst.frc.team4099.robot.Constants.Autonomous.AUTO_OPTIONS_DASHBOARD_KEY
import org.usfirst.frc.team4099.robot.Constants.Autonomous.AUTO_STARTS_DASHBOARD_KEY
import org.usfirst.frc.team4099.robot.Constants.Autonomous.SELECTED_AUTO_MODE_DASHBOARD_KEY
import org.usfirst.frc.team4099.robot.Constants.Autonomous.SELECTED_AUTO_START_DELAY_KEY
import org.usfirst.frc.team4099.robot.Constants.Autonomous.SELECTED_AUTO_START_POS_KEY


/**
 * Controls the interactive elements of SmartDashboard.
 *
 * Keeps the network tables keys in one spot and enforces autonomous mode
 * invariants.
 */
object DashboardConfigurator {


    enum class StartingPosition(val dashboardName: String)  { //TODO: Investigate starting locations
        LEFT("LEFT"),
        CENTER("CENTER"),
        RIGHT("RIGHT")
    }



    fun initDashboard() {

    }

    fun getSelectedAutoMode(allianceOwnership: String): AutoModeBase {


    }

    fun updateAllianceOwnership(): String {

    }


}