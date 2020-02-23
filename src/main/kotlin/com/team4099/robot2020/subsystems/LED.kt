package com.team4099.robot2020.subsystems

import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants.LED.Color
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.AddressableLEDBuffer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object LED : Subsystem {

    // PWM port 9
    // Must be a PWM header, not MXP or DIO
    var mLED = AddressableLED(Constants.LED.PWMPORT)
    var currentColor = Color.ORANGE

    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    var mLEDBuffer = AddressableLEDBuffer(60)

    private var initialRainbowHue: Int = 0

    init {
        mLED.setLength(mLEDBuffer.length)
    }

    private fun updateColor(wantedColor: Constants.LED.Color) {
        for (i in 0 until mLEDBuffer.length) {
            mLEDBuffer.setHSV(i, wantedColor.h, wantedColor.s, wantedColor.v)
            if (wantedColor == Color.RAINBOW) {
                wantedColor.h = (initialRainbowHue + (i * 180 / mLEDBuffer.length)) % 180
                mLEDBuffer.setHSV(i, wantedColor.h, wantedColor.s, wantedColor.v)
                initialRainbowHue += 6
                initialRainbowHue %= 180
            }
        }
        mLED.setData(mLEDBuffer)
        HelixEvents.addEvent("LED", "LED color changed to $currentColor")
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("led/ledColor", LED.currentColor.toString())
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        HelixLogger.addSource("Current color") { LED.currentColor }
    }

    override fun zeroSensors() {}

    override fun onStart(timestamp: Double) {
        mLED.setData(mLEDBuffer)
        mLED.start()
    }
    override fun onLoop(timestamp: Double, dT: Double) {
        updateColor(currentColor)
    }

    override fun onStop(timestamp: Double) {
        mLED.setLength(0)
    }
}
