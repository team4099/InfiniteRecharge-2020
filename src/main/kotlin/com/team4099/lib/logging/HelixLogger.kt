package com.team4099.lib.logging

import edu.wpi.first.wpilibj.DriverStation
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Instant

object HelixLogger {
    private val dataSources = mutableListOf<LogSource>()
    private lateinit var file: Path
    private var loggingLocation: String =
            if (File("/media/sda1/").exists()) "/media/sda1/logs/" else "/home/lvuser/logs/"

    var values: String = ""
        get() {
            field = dataSources.joinToString(",") { it.supplier() }
            return field
        }
        private set

    @Throws(IOException::class)
    private fun createLogDirectory() {
        val logDirectory = File(loggingLocation)
        if (!logDirectory.exists()) {
            Files.createDirectory(Paths.get(loggingLocation))
        }
    }

    fun createFile() {
        try {
            createLogDirectory()

            file = if (DriverStation.getInstance().isFMSAttached) {
                Paths.get("$loggingLocation${DriverStation.getInstance().eventName}_" +
                        "${DriverStation.getInstance().matchType}${DriverStation.getInstance().matchNumber}.csv")
            } else {
                Paths.get("${loggingLocation}test.csv")
            }

            if (Files.exists(file)) Files.delete(file)
            Files.createFile(file)

            saveTitles()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun <T> addSource(name: String, supplier: () -> T) {
        dataSources.add(LogSource(name) { supplier().toString() })
    }

    fun saveLogs() {
        try {
            val data = "${Instant.now()},${DriverStation.getInstance().matchTime},$values"
            Files.write(file, listOf(data), StandardOpenOption.APPEND)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun saveTitles() {
        val titles = "Timestamp,match_time,${dataSources.joinToString(",") { it.name }}"
        Files.write(file, listOf(titles), StandardOpenOption.APPEND)
    }
}
