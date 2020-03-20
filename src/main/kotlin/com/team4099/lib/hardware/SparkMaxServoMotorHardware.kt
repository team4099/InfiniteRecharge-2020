package com.team4099.lib.hardware

import com.revrobotics.CANPIDController
import com.revrobotics.CANSparkMax
import com.revrobotics.ControlType
import com.team4099.lib.config.PIDGains
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import kotlin.math.roundToInt

class SparkMaxServoMotorHardware(
    masterId: Int,
    slaveIds: List<Int>
) : ServoMotorHardware {
    override var pidSlot = 0

    val masterMotorController = SparkMaxControllerFactory.createDefaultSparkMax(masterId)
    val slaveMotorControllers = slaveIds.map {
        SparkMaxControllerFactory.createPermanentSlaveSparkMax(it, masterMotorController, invertToMaster = true)
    }

    val encoder = masterMotorController.encoder

    override var timeout = 0
    override val positionTicks
        get() = encoder.position.roundToInt()
    override val velocityTicksPer100Ms
        get() = encoder.velocity.roundToInt()

    override fun setMotionProfile(setpoint: Double) {
        masterMotorController.set(ControlType.kSmartMotion, setpoint, pidSlot)
    }

    override fun setPosition(setpoint: Double) {
        masterMotorController.set(ControlType.kPosition, setpoint, pidSlot)
    }

    override fun setVelocity(setpoint: Double) {
        masterMotorController.set(ControlType.kVelocity, setpoint, pidSlot)
    }

    override fun setOpenLoop(percentOutput: Double) {
        masterMotorController.set(percentOutput)
    }

    override fun zeroSensors() {
        encoder.position = 0.0
    }

    override fun applyPIDGains(gains: PIDGains) {
        masterMotorController.pidController.setP(gains.kP, gains.slotNumber)
        masterMotorController.pidController.setI(gains.kI, gains.slotNumber)
        masterMotorController.pidController.setD(gains.kD, gains.slotNumber)
        masterMotorController.pidController.setIZone(gains.iZone.toDouble(), gains.slotNumber)
    }

    override fun applyMotionConstraints(
        enableReverseSoftLimit: Boolean,
        reverseSoftLimit: Int,
        enableForwardSoftLimit: Boolean,
        forwardSoftLimit: Int,
        cruiseVel: Int,
        maxAccel: Int,
        motionProfileCurveStrength: Int,
        velocityPIDSlot: Int,
        brakeMode: Boolean
    ) {
        masterMotorController.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, reverseSoftLimit.toFloat())
        masterMotorController.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward, forwardSoftLimit.toFloat())
        masterMotorController.pidController.setSmartMotionMaxVelocity(cruiseVel.toDouble(), velocityPIDSlot)
        masterMotorController.pidController.setSmartMotionMaxAccel(maxAccel.toDouble(), velocityPIDSlot)
        masterMotorController.pidController.setSmartMotionAccelStrategy(
            if (motionProfileCurveStrength > 0) CANPIDController.AccelStrategy.kSCurve
            else CANPIDController.AccelStrategy.kTrapezoidal,
            velocityPIDSlot
        )
        if (brakeMode) {
            masterMotorController.idleMode = CANSparkMax.IdleMode.kBrake
            slaveMotorControllers.forEach { it.idleMode = CANSparkMax.IdleMode.kBrake }
        } else {
            masterMotorController.idleMode = CANSparkMax.IdleMode.kCoast
            slaveMotorControllers.forEach { it.idleMode = CANSparkMax.IdleMode.kCoast }
        }

        masterMotorController.burnFlash()
        slaveMotorControllers.forEach { it.burnFlash() }
    }
}
