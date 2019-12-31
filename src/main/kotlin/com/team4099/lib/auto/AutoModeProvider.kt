package com.team4099.lib.auto

import com.team4099.robot2020.config.DashboardConfigurator

typealias AutoModeProvider = (startingPos: DashboardConfigurator.StartingPosition, delay: Double) -> AutoMode
