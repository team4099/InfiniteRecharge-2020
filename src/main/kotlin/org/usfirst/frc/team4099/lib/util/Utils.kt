package org.usfirst.frc.team4099.lib.util

import org.usfirst.frc.team4099.robot.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object Utils {
    /**
     * Limits the given input to the given magnitude.
     * @param v         value to limit
     * @param limit     limited magnitude
     * @return          the limited value
     */

    fun limit(v: Double, limit: Double): Double {
        if (Math.abs(v) < limit)
            return v
        return if (v < 0)
            -limit
        else
            limit
    }

    fun diff(current: Double, prev: Double): Double {
        return Math.abs(current - prev)
    }

    fun around(value: Double, around: Double, tolerance: Double): Boolean {
        return diff(value, around) <= tolerance
    }

    fun sameSign(new_: Double, old_: Double): Boolean {
        return new_ >= 0 && old_ >= 0 || new_ <= 0 && old_ <= 0
    }

    fun sign(value: Double): Int {
        return if (value >= 0) 1 else -1
    }

    private fun getHTML(urlToRead: String): String {
        try {
            val result = StringBuilder()
            val url = URL(urlToRead)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = Constants.Autonomous.CONNECTION_TIMEOUT_MILLIS
            conn.readTimeout = Constants.Autonomous.CONNECTION_TIMEOUT_MILLIS
            conn.requestMethod = "GET"
            val rd = BufferedReader(InputStreamReader(conn.inputStream))
            var line: String? = rd.readLine()
            while (line != null) {
                result.append(line)
                line = rd.readLine()
            }
            rd.close()
            return result.toString()
        } catch (e: Exception) {
            return "-1"
        }

    }

    fun getAverageFromList(list: List<Double>): Double {
        var total = 0.0
        for (d in list) {
            total += d
        }
        return total / list.size
    }

}
