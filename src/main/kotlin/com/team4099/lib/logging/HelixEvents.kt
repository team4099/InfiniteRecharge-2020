package com.team4099.lib.logging

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Notifier

object HelixEvents {
    private val log = Notifier(LogSaver())
    private lateinit var file: Path
    private var loggingLocation =
            if (File("/media/sda1/").exists()) "/media/sda1/logs/" else "/home/lvuser/logs/"

    private val events = mutableListOf<String>()

    init {
        createFile()
    }

    @Throws(IOException::class)
    private fun createLogDirectory() {
        val logDirectory = File(loggingLocation)
        if (!logDirectory.exists()) {
            Files.createDirectory(Paths.get(loggingLocation))
        }
    }

    private fun createFile() {
        try {
            createLogDirectory()

            file = if (DriverStation.getInstance().isFMSAttached) {
                Paths.get("$loggingLocation${DriverStation.getInstance().eventName}_" +
                        "${DriverStation.getInstance().matchType}${DriverStation.getInstance().matchNumber}Events.csv")
            } else {
                Paths.get("${loggingLocation}testEvents.csv")
            }

            if (Files.exists(file)) {
                Files.delete(file)
            }
            Files.createFile(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun startLogging() {
        log.startPeriodic(1.0)
    }

    fun addEvent(subsystem: String, event: String) {
        val log = "${Instant.now()}\t${DriverStation.getInstance().matchTime}\t($subsystem)\t$event\n"
        events.add(log)
        print(log)
    }

    private class LogSaver : Runnable {
        override fun run() {
            while (events.isNotEmpty()) {
                try {
                    val event = events.removeAt(0)
                    println(event)
                    Files.write(file, listOf(event), StandardOpenOption.APPEND)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
