package main.java.org.usfirst.frc.team4099.lib.util

import kotlinx.serialization.Serializable
import org.usfirst.frc.team4099.DashboardConfigurator
import org.usfirst.frc.team4099.auto.modes.AutoModeBase

@Serializable
data class AutoModeCreator(val dashboardName: String, val creator: (startPos: DashboardConfigurator.StartingPosition, ownershipConfig: String, delay: Double) -> AutoModeBase)