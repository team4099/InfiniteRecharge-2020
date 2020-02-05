package com.team4099.robot2020.auto

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.trajectory.Trajectory
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import com.team4099.robot2020.config.Constants
import com.team4099.robot2020.subsystems.Drive
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward
import edu.wpi.first.wpilibj.trajectory.constraint.DifferentialDriveVoltageConstraint

/**
 * Stores trajectories for use in autonomous modes. Must be invoked before autonomous
 * starts in order to not need to wait for trajectories to be created.
 */
object PathStore {
    private val voltageConstraint = DifferentialDriveVoltageConstraint(
        SimpleMotorFeedforward(
            listOf(
                Constants.Drive.Characterization.LEFT_KS_FORWARD,
                Constants.Drive.Characterization.RIGHT_KS_FORWARD,
                Constants.Drive.Characterization.LEFT_KS_REVERSE,
                Constants.Drive.Characterization.RIGHT_KS_REVERSE
            ).min() ?: 0.0,
            listOf(
                Constants.Drive.Characterization.LEFT_KV_FORWARD,
                Constants.Drive.Characterization.RIGHT_KV_FORWARD,
                Constants.Drive.Characterization.LEFT_KV_REVERSE,
                Constants.Drive.Characterization.RIGHT_KV_REVERSE
            ).min() ?: 0.0,
            listOf(
                Constants.Drive.Characterization.LEFT_KA_FORWARD,
                Constants.Drive.Characterization.RIGHT_KA_FORWARD,
                Constants.Drive.Characterization.LEFT_KA_REVERSE,
                Constants.Drive.Characterization.RIGHT_KA_REVERSE
            ).min() ?: 0.0
        ),
        Drive.kinematics,
        10.0
    )

    val config: TrajectoryConfig = TrajectoryConfig(Constants.Drive.MAX_VEL_METERS_PER_SEC, Constants.Drive.MAX_ACCEL_METERS_PER_SEC_SQ)
        .setKinematics(Drive.kinematics)
        .addConstraint(voltageConstraint)

    val driveForward: Trajectory = TrajectoryGenerator.generateTrajectory(
        listOf(Pose2d(0.0, 0.0, Rotation2d(0.0)), Pose2d(1.0, 0.0, Rotation2d(0.0))),
        config
    )
}
