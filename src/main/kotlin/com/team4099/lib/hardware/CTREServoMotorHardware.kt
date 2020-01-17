package com.team4099.lib.hardware

import com.ctre.phoenix.motorcontrol.ControlMode
import com.team4099.lib.config.PIDGains
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import kotlin.math.roundToInt

class CTREServoMotorHardware(
    masterId: Int,
    slaveIds: List<Int>
) : ServoMotorHardware {
    override var pidSlot: Int = 0
        set(value) {
            masterMotorController.selectProfileSlot(value, 0)
            field = value
        }
    val masterMotorController = CTREMotorControllerFactory.createDefaultTalonSRX(masterId)
    val slaveMotorControllers = slaveIds.map { CTREMotorControllerFactory.createPermanentSlaveVictorSPX(it, masterId) }

    override var timeout = 0
    override val positionTicks
        get() = masterMotorController.selectedSensorPosition
    override val velocityTicksPer100Ms
        get() = masterMotorController.selectedSensorVelocity

    override fun setMotionProfile(setpoint: Double) {
        masterMotorController.set(ControlMode.MotionMagic, setpoint)
    }

    override fun setPosition(setpoint: Double) {
        masterMotorController.set(ControlMode.Position, setpoint)
    }

    override fun setVelocity(setpoint: Double) {
        masterMotorController.set(ControlMode.Velocity, setpoint)
    }

    override fun setOpenLoop(percentOutput: Double) {
        masterMotorController.set(ControlMode.PercentOutput, percentOutput)
    }

    override fun zeroSensors() {
        masterMotorController.selectedSensorPosition = 0
    }

    override fun applyPIDGains(gains: PIDGains) {
        masterMotorController.config_kP(gains.slotNumber, gains.kP, timeout)
        masterMotorController.config_kI(gains.slotNumber, gains.kI, timeout)
        masterMotorController.config_kD(gains.slotNumber, gains.kD, timeout)
        masterMotorController.config_IntegralZone(gains.slotNumber, gains.iZone.roundToInt(), timeout)
    }

    override fun applyMotionConstraints(
        enableReverseSoftLimit: Boolean,
        reverseSoftLimit: Int,
        enableForwardSoftLimit: Boolean,
        forwardSoftLimit: Int,
        cruiseVel: Int,
        maxAccel: Int,
        motionProfileCurveStrength: Int,
        velocityPIDSlot: Int
    ) {
        masterMotorController.configReverseSoftLimitEnable(enableReverseSoftLimit, timeout)
        masterMotorController.configReverseSoftLimitThreshold(reverseSoftLimit, timeout)
        masterMotorController.configForwardSoftLimitEnable(enableForwardSoftLimit, timeout)
        masterMotorController.configForwardSoftLimitThreshold(forwardSoftLimit, timeout)
        masterMotorController.configMotionCruiseVelocity(cruiseVel, timeout)
        masterMotorController.configMotionAcceleration(maxAccel, timeout)
        masterMotorController.configMotionSCurveStrength(motionProfileCurveStrength, timeout)
    }
}
