package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants

object Feeder : Subsystem {
    private val inMasterTalon = CTREMotorControllerFactory.createDefaultTalon(Constants.Feeder.FEEDER_IN_MASTER_TALON_ID)
    private val inSlaveVictor = CTREMotorControllerFactory.createPermanentSlaveVictor(
            Constants.Feeder.FEEDER_IN_MASTER_TALON_ID,
            Constants.Feeder.FEEDER_IN_SLAVE_VICTOR_ID
    )

    private val outTalon = CTREMotorControllerFactory.createDefaultTalon(Constants.Feeder.FEEDER_OUT_TALON_ID)

    var feederState = FeederState.HOLD
    private var feederInPower = 0.0
        set(value) {
            if (value != field) {
                inMasterTalon.set(ControlMode.PercentOutput, value)
            }
            field = value
        }
    private var feederOutPower = 0.0
        set(value) {
            if (value != field) {
                outTalon.set(ControlMode.PercentOutput, value)
            }
            field = value
        }

    init {

        inMasterTalon.inverted = false
        inSlaveVictor.inverted = true

        outTalon.inverted = true
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT
    }

    override fun outputTelemetry() { }

    override fun checkSystem() { }

    override fun registerLogging() { }

    override fun zeroSensors() { }

    override fun onStart(timestamp: Double) {
        feederState = FeederState.HOLD
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        synchronized(this) {
            when (feederState) {
                FeederState.INTAKE -> {
                    feederOutPower = -Constants.Feeder.FEEDER_HOLD_POWER
                    feederInPower = Constants.Feeder.FEEDER_MAX_POWER
                }
                FeederState.HOLD -> {
                    feederOutPower = -Constants.Feeder.FEEDER_HOLD_POWER
                    feederInPower = Constants.Feeder.FEEDER_HOLD_POWER
                }
                FeederState.SHOOT -> {
                    feederOutPower = Constants.Feeder.FEEDER_MAX_POWER
                    feederInPower = Constants.Feeder.FEEDER_MAX_POWER
                }
            }
        }
    }

    override fun onStop(timestamp: Double) {
        feederState = FeederState.HOLD
        feederInPower = 0.0
        feederOutPower = 0.0
    }
}
