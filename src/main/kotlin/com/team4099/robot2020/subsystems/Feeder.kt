package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Feeder : Subsystem {
    private val inMasterTalon = CTREMotorControllerFactory.createDefaultTalonSRX(Constants.Feeder.FEEDER_IN_MASTER_ID)
    private val inSlaveVictor = CTREMotorControllerFactory.createPermanentSlaveVictorSPX(
        Constants.Feeder.FEEDER_IN_MASTER_ID,
        Constants.Feeder.FEEDER_IN_SLAVE_ID
    )

    private val outSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(Constants.Feeder.FEEDER_OUT_ID)

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
                outSparkMax.set(value)
            }
            field = value
        }

    init {

        inMasterTalon.inverted = false
        inSlaveVictor.inverted = true

        outSparkMax.inverted = true
    }

    enum class FeederState {
        HOLD, INTAKE, SHOOT
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("feeder/feederState", feederState.toString())
        SmartDashboard.putNumber("feeder/feederInPower", feederInPower)
        SmartDashboard.putNumber("feeder/feederOutPower", feederOutPower)
    }

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
