package com.team4099.robot2020.subsystems

// import com.ctre.phoenix.motorcontrol.can.TalonSRX
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.ServoMotorSubsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard

object Climber : ServoMotorSubsystem(
    Constants.Climber,
    CTREMotorControllerFactory.createDefaultTalon(Constants.Climber.MASTER_ID),
    listOf(
        CTREMotorControllerFactory.createPermanentSlaveVictor(
            Constants.Climber.SLAVE_ID,
            Constants.Climber.MASTER_ID
        )
    )
) {

    var positionSetpoint: Constants.Climber.ClimberPosition = Constants.Climber.ClimberPosition.DOWN
        set(value) {
            Climber.positionSetpointMotionProfile = value.position
            field = value
        }

    override fun onStop(timestamp: Double) {
        super.onStop(timestamp)
        movementState = MovementState.STILL
    }

    override fun outputTelemetry() {
        super.outputTelemetry()
        SmartDashboard.putString("climber state", movementState.toString())
    }
}
