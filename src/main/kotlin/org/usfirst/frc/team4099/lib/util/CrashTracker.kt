package org.usfirst.frc.team4099.lib.util

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.*

/**
 * Tracks startup and caught crash events, logging them to a file
 * while doesn't roll over
 */

object CrashTracker {

    private val RUN_INSTANCE_UUID = UUID.randomUUID()

    fun logRobotStartup() {
        logMarker("Robot Startup")
    }

    fun logRobotConstruction() {
        logMarker("Robot Constructed")
    }

    fun logRobotInit() {
        logMarker("Robot Initialized")
    }

    fun logAutoInit() {
        logMarker("Autonomous Initialized")
    }

    fun logTeleopInit() {
        logMarker("Teleop Initialized")
    }

    fun logDisabledInit() {
        logMarker("Disabled Initialized")
    }

    fun logThrowableCrash(function: String, throwable: Throwable) {
        logMarker("Exception @ " + function, throwable)
    }

    fun logMarker(mark: String) {
        logMarker(mark, null)
    }

    private fun logMarker(mark: String, nullableException: Throwable?) {
        try {
            FileWriter(
                    "/home/lvuser/crash_tracking.txt",
                    true).use { fw ->
                BufferedWriter(fw).use { bw ->
                    PrintWriter(bw).use { out ->

                        out.print(RUN_INSTANCE_UUID.toString())
                        out.print(", ")
                        out.print(mark)
                        out.print(", ")
                        out.print(Date().toString())

                        if (nullableException != null) {
                            out.print(", ")
                            nullableException.printStackTrace(out)
                        }

                        out.println()
                    }
                }
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

    }
}
