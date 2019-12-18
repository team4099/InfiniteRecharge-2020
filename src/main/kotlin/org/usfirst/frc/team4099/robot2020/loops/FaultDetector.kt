package org.usfirst.frc.team4099.robot2020.loops

import com.team2363.logger.HelixEvents
import edu.wpi.first.wpilibj.RobotController
import org.usfirst.frc.team4099.lib.loop.Loop

object FaultDetector : Loop {
    var rio3v3Faults = 0
        set(faults) {
            if (field != faults) {
                HelixEvents.addEvent("Fault Detector", "roboRIO 3.3V Fault")
            }
            field = faults
        }
    var rio5vFaults = 0
        set(faults) {
            if (field != faults) {
                HelixEvents.addEvent("Fault Detector", "roboRIO 5V Fault")
            }
            field = faults
        }
    var rio6vFaults = 0
        set(faults) {
            if (field != faults) {
                HelixEvents.addEvent("Fault Detector", "roboRIO 6V Fault")
            }
            field = faults
        }

    override fun onStart(timestamp: Double) {}

    override fun onLoop(timestamp: Double, dT: Double) {
        rio3v3Faults = RobotController.getFaultCount3V3()
        rio5vFaults = RobotController.getFaultCount5V()
        rio6vFaults = RobotController.getFaultCount6V()
    }

    override fun onStop(timestamp: Double) {}
}
