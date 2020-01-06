package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.Talon

object Feeder : Subsystem{
    private val inMasterTalon: TalonSRX
    private val inSlaveVictor = CTREMotorControllerFactory.createPermanentSlaveVictor(
            Constants.Feeder.FEEDER_IN_MASTER_TALON_ID,
            Constants.Feeder.FEEDER_IN_SLAVE_VICTOR_ID
    )

    private val outTalon: TalonSRX

    var feederState = FeederState.HOLD
    private var feederInPower = 0.0
    private var feederOutPower = 0.0

    init {
        inMasterTalon = CTREMotorControllerFactory.createDefaultTalon(Constants.Feeder.FEEDER_IN_MASTER_TALON_ID)

        outTalon = CTREMotorControllerFactory.createDefaultTalon(Constants.Feeder.FEEDER_OUT_TALON_ID)

        inMasterTalon.inverted = false
        inSlaveVictor.inverted = true

        outTalon.inverted = true
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT
    }

    fun setFeederInPower(power: Double) {
        inMasterTalon.set(ControlMode.PercentOutput, power)
    }

    fun setFeederOutPower(power: Double) {
        outTalon.set(ControlMode.PercentOutput, power)
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
        feederState = FeederState.HOLD
    }

    override fun onLoop(timestamp: Double, dT: Double) {
        synchronized(this@Feeder) {
            when (feederState) {
                FeederState.INTAKE -> {
                    setFeederOutPower(-0.5)
                    setFeederInPower(1.0)
                }
                FeederState.HOLD -> {
                    setFeederOutPower(-0.5)
                    setFeederInPower(0.25)
                }
                FeederState.SHOOT -> {
                    setFeederOutPower(1.0)
                    setFeederInPower(1.0)
                }
            }
        }
    }

    override fun onStop(timestamp: Double) {
        feederState = FeederState.HOLD
        setFeederInPower(0.0)
        setFeederOutPower(0.0)
    }

}