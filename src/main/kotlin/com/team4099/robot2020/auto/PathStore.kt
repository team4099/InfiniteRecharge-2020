package com.team4099.robot2020.auto

import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.trajectory.Trajectory
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import com.team4099.robot2020.config.Constants

/**
 * Stores trajectories for use in autonomous modes. Must be invoked before autonomous
 * starts in order to not need to wait for trajectories to be created.
 */
object PathStore {
    val driveForward: Trajectory = TrajectoryGenerator.generateTrajectory(
        listOf(Pose2d(0.0, 0.0, Rotation2d(0.0)), Pose2d(1.0, 0.0, Rotation2d(0.0))),
        TrajectoryConfig(Constants.Drive.MAX_VEL_METERS_PER_SEC, Constants.Drive.MAX_ACCEL_METERS_PER_SEC_SQ)
    )
}
