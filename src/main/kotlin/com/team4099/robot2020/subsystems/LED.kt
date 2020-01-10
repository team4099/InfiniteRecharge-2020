package com.team4099.robot2020.subsystems


import com.team4099.lib.subsystem.Subsystem
import edu.wpi.first.wpilibj.AddressableLED
import edu.wpi.first.wpilibj.AddressableLEDBuffer

object LED : Subsystem {
    // PWM port 9
    // Must be a PWM header, not MXP or DIO
    var led = AddressableLED(Constants.LED.PWMPORT)

    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    var ledBuffer = AddressableLEDBuffer(60)
    led.setLength(ledBuffer.getLength())

    // Set the data

    led.setData(ledBuffer)
    led.start()
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
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStop(timestamp: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}