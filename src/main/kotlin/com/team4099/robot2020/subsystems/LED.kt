package com.team4099.robot2020.subsystems

import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants.LED.Color
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.AddressableLEDBuffer

object LED : Subsystem {

    // PWM port 9
    // Must be a PWM header, not MXP or DIO
    private var mLED = AddressableLED(Constants.LED.PWMPORT)
    private var currentColor = Color.ORANGE

    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    private var mLEDBuffer = AddressableLEDBuffer(60)

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
    }

    override fun outputTelemetry() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun checkSystem() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLogging() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun zeroSensors() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onStart(timestamp: Double) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        mLED.setData(mLEDBuffer)
        mLED.start()
    }
    override fun onLoop(timestamp: Double, dT: Double) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        updateColor(currentColor)
    }

    override fun onStop(timestamp: Double) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
