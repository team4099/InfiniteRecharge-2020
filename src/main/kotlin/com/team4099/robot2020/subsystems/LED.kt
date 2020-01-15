package com.team4099.robot2020.subsystems

import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.AddressableLEDBuffer

object LED : Subsystem {
    // PWM port 9
    // Must be a PWM header, not MXP or DIO
    private var led = AddressableLED(Constants.LED.PWMPORT)
    var currentColor = Color.ORANGE
    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    var ledBuffer = AddressableLEDBuffer(60);
    init {
        led.setLength(ledBuffer.length)
    }

    enum class Color(var h:Int,val s:Int,val v:Int) {
        //Pink is for firing
        PINK(227, 117, 128),
        //Turquoise is for getting ready to fire
        TURQUOISE(124, 255, 118),
        //orange is for when the intake is empty
        ORANGE(18, 184, 128),
        //magenta is 1 ball
        MAGENTA(209, 242, 128),
        //sky blue is 2 ball
        SKY_BLUE(145, 242, 128),
        //tan is 3 ball
        TAN(17, 122, 128),
        //green is 4 ball
        GREEN(74, 207, 128),
        //brown is final state of climb
        BROWN(34, 255, 45),
        //rainbow is for climbing and when 5 balls
        RAINBOW(0, 255, 128),
    }
    var initialRainbowHue: Int = 0
    fun updateColor(wantedColor:Color) {
        for (i in 0 until ledBuffer.length) {
            ledBuffer.setHSV(i, wantedColor.h, wantedColor.s, wantedColor.v)
            if (wantedColor == Color.RAINBOW) {
                wantedColor.h = (initialRainbowHue + (i * 180 / ledBuffer.length)) % 180;
                ledBuffer.setHSV(i, wantedColor.h, wantedColor.s, wantedColor.v)
                initialRainbowHue += 6;
                initialRainbowHue %= 180;

            }

        }
        led.setData(ledBuffer);
    }

    override fun outputTelemetry() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun checkSystem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerLogging() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun zeroSensors() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStart(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        led.setData(ledBuffer)
        led.start()
    }
    override fun onLoop(timestamp: Double, dT: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        updateColor(currentColor)
    }

    override fun onStop(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}