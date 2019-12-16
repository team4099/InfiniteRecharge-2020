package org.usfirst.frc.team4099.lib.util

import com.team2363.logger.HelixEvents
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.util.Date
import java.util.UUID

/**
 * Tracks startup and caught crash events, logging them to a file
 * while doesn't roll over
 */

object CrashTracker {
    private val RUN_INSTANCE_UUID = UUID.randomUUID()

    fun logThrowableCrash(function: String, throwable: Throwable) {
        logMarker("Exception @ $function, ${throwable.message}", throwable)
    }

    fun logMarker(mark: String) {
        logMarker(mark, null)
    }

    private fun logMarker(mark: String, exception: Throwable?) {
        HelixEvents.addEvent("CRASH", mark)
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

                        if (exception != null) {
                            out.print(", ")
                            exception.printStackTrace(out)
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
