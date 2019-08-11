package org.usfirst.frc.team4099.lib.util.conversions

interface EncoderConversion {
    fun inchesToPulses(radians: Double): Double
    fun pulsesToInches(pulses: Double): Double
    fun inchesPerSecondtoNativeSpeed(ips: Double): Double
    fun nativeSpeedToInchesPerSecond(nativeSpeed: Double): Double
}