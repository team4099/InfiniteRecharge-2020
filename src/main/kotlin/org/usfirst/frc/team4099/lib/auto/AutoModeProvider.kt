package org.usfirst.frc.team4099.lib.auto

import org.usfirst.frc.team4099.robot2020.config.DashboardConfigurator

typealias AutoModeProvider = (startingPos: DashboardConfigurator.StartingPosition, delay: Double) -> AutoMode
