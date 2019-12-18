package org.usfirst.frc.team4099.robot2020.loops

import com.team2363.logger.HelixLogger
import edu.wpi.first.wpilibj.RobotController
import org.usfirst.frc.team4099.lib.loop.Loop

/** Constantly measures battery voltage before the match begins.
 *
 */

class VoltageEstimator : Loop {
    @get:Synchronized
    var averageVoltage = 12.0
        private set
    private val weight = 15.0

    init {
        HelixLogger.addSource("VoltageEstimator Avg. Disabled Voltage") { averageVoltage }
    }

    override fun onStart(timestamp: Double) {
        println("Robot disabled: computing avg voltage")
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        val curVoltage = RobotController.getBatteryVoltage()
        averageVoltage = (curVoltage + weight * averageVoltage) / (1.0 + weight)
    }

    override fun onStop(timestamp: Double) {
        println("Robot enabled: last avg voltage: $averageVoltage")
    }

    companion object {
        val instance = VoltageEstimator()
    }
}
