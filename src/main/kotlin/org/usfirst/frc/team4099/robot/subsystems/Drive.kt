package org.usfirst.frc.team4099.robot.subsystems


import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj.DoubleSolenoid
import edu.wpi.first.wpilibj.SPI
import edu.wpi.first.wpilibj.livewindow.LiveWindow
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.lib.util.CANMotorControllerFactory
import org.usfirst.frc.team4099.robot.Constants
import org.usfirst.frc.team4099.robot.loops.Loop


class Drive private constructor() : Subsystem {





    init {

}


    @Synchronized
    fun setOpenLoop(signal: DriveSignal) {

    }

    /**
     * Powers the left and right talons during OPEN_LOOP
     * @param left
     * @param right
     */
    @Synchronized
    private fun setLeftRightPower(left: Double, right: Double) {

    }

    override fun zeroSensors() {
        if (ahrs.isConnected) {
            ahrs.reset()
        }
        resetEncoders()
    }

    @Synchronized
    fun resetEncoders() {
        leftMasterSRX.setSelectedSensorPosition(0, 0, 0)
        leftMasterSRX.sensorCollection.setPulseWidthPosition(0, 0)
        leftSlave1SRX.setSelectedSensorPosition(0, 0, 0)
        leftSlave2SPX.setSelectedSensorPosition(0, 0, 0)
        rightMasterSRX.setSelectedSensorPosition(0, 0, 0)
        rightMasterSRX.sensorCollection.setPulseWidthPosition(0, 0)
        rightSlave1SRX.setSelectedSensorPosition(0, 0, 0)
        rightSlave2SPX.setSelectedSensorPosition(0, 0, 0)

    }

    fun getAHRS(): AHRS? {
        return if (ahrs.isConnected) ahrs else null
    }

    override fun outputToSmartDashboard() {
        if (this.getAHRS() != null) {
            SmartDashboard.putNumber("gyro", this.getAHRS()!!.yaw.toDouble())
        } else {
            SmartDashboard.putNumber("gyro", -31337.0)
        }
        SmartDashboard.putNumber("leftTalon", leftMasterSRX.motorOutputVoltage)
        SmartDashboard.putNumber("rightTalon", rightMasterSRX.motorOutputVoltage)
        SmartDashboard.putNumber("leftEncoderInches", getLeftDistanceInches())
        SmartDashboard.putNumber("rightEncoderInches", rightMasterSRX.sensorCollection.quadraturePosition.toDouble())
    }

    fun startLiveWindowMode() {
        LiveWindow.addSensor("Drive", "Gyro", ahrs);
    }

    fun stopLiveWindowMode() {
        //TODO
    }

    fun updateLiveWindowTables() {

    }

    @Synchronized
    fun arcadeDrive(outputMagnitude: Double, curve: Double) {
        val leftOutput: Double
        val rightOutput: Double

        when {
            curve < 0 -> {
                val value = Math.log(-curve)
                var ratio = (value - .5) / (value + .5)
                if (ratio == 0.0) {
                    ratio = .0000000001
                }
                leftOutput = outputMagnitude / ratio
                rightOutput = outputMagnitude
            }
            curve > 0 -> {
                val value = Math.log(curve)
                var ratio = (value - .5) / (value + .5)
                if (ratio == 0.0) {
                    ratio = .0000000001
                }
                leftOutput = outputMagnitude
                rightOutput = outputMagnitude / ratio
            }
            else -> {
                leftOutput = outputMagnitude
                rightOutput = outputMagnitude
            }
        }
        setLeftRightPower(leftOutput, rightOutput)
    }

    @Synchronized
    fun usesTalonVelocityControl(state: DriveControlState): Boolean {

    }

    @Synchronized
    fun usesTalonPositionControl(state: DriveControlState): Boolean {
    }

    @Synchronized
    fun setVelocitySetpoint(leftInchesPerSec: Double, rightInchesPerSec: Double) {

    }

    @Synchronized
    fun updateVelocitySetpoint(leftInchesPerSec: Double, rightInchesPerSec: Double) {


    }

    @Synchronized
    private fun updatePositionSetpoint(leftPositionInches: Double, rightPositionInches: Double) {

    }

    @Synchronized
    private fun configureTalonsForVelocityControl() { //should further review cause im bad

    }

    @Synchronized
    private fun configureTalonsforPositionControl() {

    }


    fun onStart(timestamp: Double) {

    }

    override fun stop() {

    }

    val loop: Loop = object : Loop {

        }

        override fun onStop() {

        }
    }


//TODO: Adjust functions to new robot if needed
    private fun pulsesToInches(pulses: Double): Double {
        return pulses*12/2336
    }

    private fun rpmToInchesPerSecond(rpm: Double): Double {
        return pulsesToInches(rpm) / 60
    }

    private fun inchesToRotations(inches: Double): Double {
        return inches / (Constants.Wheels.DRIVE_WHEEL_DIAMETER_INCHES * Math.PI)
    }

    private fun inchesPerSecondToRpm(inches_per_second: Double): Double {
        return inchesToRotations(inches_per_second) * 60
    }

    fun getLeftDistanceInches(): Double {
        return pulsesToInches(leftMasterSRX.sensorCollection.quadraturePosition.toDouble())
    }

    fun getRightDistanceInches(): Double {
        return pulsesToInches(rightMasterSRX.sensorCollection.quadraturePosition.toDouble())
    }

    fun getLeftVelocityInchesPerSec(): Double {
        return rpmToInchesPerSecond(leftMasterSRX.getSelectedSensorVelocity(0).toDouble())
    }

    fun getRightVelocityInchesPerSec(): Double {
        return rpmToInchesPerSecond(rightMasterSRX.getSelectedSensorVelocity(0).toDouble())
    }


    companion object {
        val instance = Drive()
    }

}

