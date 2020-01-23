package com.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.TalonFXFeedbackDevice
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod
import com.ctre.phoenix.motorcontrol.can.TalonFX
import com.kauailabs.navx.frc.AHRS
import com.team4099.lib.logging.HelixEvents
import com.team4099.lib.logging.HelixLogger
import edu.wpi.first.wpilibj.SPI
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.controller.RamseteController
import edu.wpi.first.wpilibj.geometry.Rotation2d
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj.trajectory.Trajectory
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin
import com.team4099.lib.around
import com.team4099.lib.drive.DriveSignal
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants

object Drive : Subsystem {
    private val rightMasterTalon: TalonFX
    private val rightSlaveTalon = CTREMotorControllerFactory.createPermanentSlaveTalonFX(
            Constants.Drive.RIGHT_SLAVE_1_ID,
            Constants.Drive.RIGHT_MASTER_ID
    )

    private val leftMasterTalon: TalonFX
    private val leftSlaveTalon = CTREMotorControllerFactory.createPermanentSlaveTalonFX(
            Constants.Drive.LEFT_SLAVE_1_ID,
            Constants.Drive.LEFT_MASTER_ID
    )

    private val ahrs = AHRS(SPI.Port.kMXP)
    private var trajDuration = 0.0
    private var trajCurTime = 0.0
    private var trajStartTime = 0.0

    private var leftTargetVel = 0.0
    private var rightTargetVel = 0.0

    val leftDistanceMeters
        get() = nativeToMeters(leftMasterTalon.selectedSensorPosition)

    val rightDistanceMeters
        get() = nativeToMeters(rightMasterTalon.selectedSensorPosition)

    val leftVelocityMetersPerSec
        get() = nativeToMetersPerSecond(leftMasterTalon.selectedSensorVelocity)

    val rightVelocityMetersPerSec
        get() = nativeToMetersPerSecond(rightMasterTalon.selectedSensorVelocity)

    private val autoOdometry = DifferentialDriveOdometry(Rotation2d())
    private var pathFollowController = RamseteController()
    private var kinematics = DifferentialDriveKinematics(Constants.Drive.WHEEL_TRACK_WIDTH_METERS)
    var path: Trajectory = Trajectory(listOf(Trajectory.State()))
        set(value) {
            trajDuration = value.totalTimeSeconds
            trajStartTime = Timer.getFPGATimestamp()

            enterVelocityClosedLoop()
            zeroSensors()
            val initialSample = value.sample(0.0)
            autoOdometry.resetPosition(initialSample.poseMeters, Rotation2d.fromDegrees(-angle))
            pathFollowController = RamseteController(
                    Constants.Drive.Gains.RAMSETE_B,
                    Constants.Drive.Gains.RAMSETE_ZETA
            )
            lastWheelSpeeds = kinematics.toWheelSpeeds(
                pathFollowController.calculate(autoOdometry.poseMeters, initialSample)
            )

            currentState = DriveControlState.PATH_FOLLOWING
            HelixEvents.addEvent("DRIVETRAIN", "Begin path following")

            field = value
        }
    private lateinit var lastWheelSpeeds: DifferentialDriveWheelSpeeds

    var yaw: Double = 0.0
        get() {
            if (ahrs.isConnected) field = ahrs.yaw.toDouble()
            else HelixEvents.addEvent("DRIVETRAIN", "Gyroscope queried but not connected")
            return field
        }
    var angle: Double = 0.0
        get() {
            if (ahrs.isConnected) field = ahrs.angle
            else HelixEvents.addEvent("DRIVETRAIN", "Gyroscope queried but not connected")
            return field
        }

    var brakeMode: NeutralMode =
            NeutralMode.Coast // sets whether the brake mode should be coast (no resistance) or by force
        set(type) {
            if (brakeMode != type) {
                rightMasterTalon.setNeutralMode(type)
                rightSlaveTalon.setNeutralMode(type)

                leftMasterTalon.setNeutralMode(type)
                leftSlaveTalon.setNeutralMode(type)
            }
            field = type
        }

    enum class DriveControlState(val usesVelocityControl: Boolean, val usesPositionControl: Boolean) {
        OPEN_LOOP(false, false),
        VELOCITY_SETPOINT(true, false),
        PATH_FOLLOWING(true, false),
        MOTION_MAGIC(false, true)
    }

    private var currentState = DriveControlState.OPEN_LOOP

    init {
        val masterConfig = CTREMotorControllerFactory.Configuration()
        masterConfig.feedbackStatusFrameRateMs = Constants.Drive.STATUS_FRAME_PERIOD_MS

        masterConfig.sensorPhase = true
        masterConfig.enableVoltageCompensation = true

        masterConfig.voltageCompensationLevel = Constants.Drive.VOLTAGE_COMP_LEVEL

        masterConfig.neutralDeadband = Constants.Drive.OUTPUT_POWER_DEADBAND

        masterConfig.velocityMeasurementPeriod = VelocityMeasPeriod.Period_50Ms
        masterConfig.voltageCompensationRampRate = Constants.Drive.CLOSED_LOOP_RAMP

        masterConfig.enableCurrentLimit = true
        masterConfig.currentLimit = Constants.Drive.CONTINUOUS_CURRENT_LIMIT

        masterConfig.motionMagicCruiseVelocity = metersPerSecondToNative(Constants.Drive.MAX_VEL_METERS_PER_SEC).toInt()
        masterConfig.motionMagicAcceleration =
            metersPerSecondToNative(Constants.Drive.MAX_ACCEL_METERS_PER_SEC_SQ).toInt()

        rightMasterTalon = CTREMotorControllerFactory.createTalonFX(Constants.Drive.RIGHT_MASTER_ID, masterConfig)
        leftMasterTalon = CTREMotorControllerFactory.createTalonFX(Constants.Drive.RIGHT_MASTER_ID, masterConfig)

        rightMasterTalon.inverted = true
        rightSlaveTalon.inverted = true
        leftMasterTalon.inverted = false
        leftMasterTalon.inverted = false

        rightMasterTalon.configSelectedFeedbackSensor(
            TalonFXFeedbackDevice.IntegratedSensor,
            0,
            Constants.Universal.CTRE_CONFIG_TIMEOUT
        )

        leftMasterTalon.configSelectedFeedbackSensor(
            TalonFXFeedbackDevice.IntegratedSensor,
            0,
            Constants.Universal.CTRE_CONFIG_TIMEOUT
        )

        // TODO: SET CONVERSION FACTORS

        leftMasterTalon.config_kP(0, Constants.Drive.Gains.LEFT_KP, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        leftMasterTalon.config_kI(0, Constants.Drive.Gains.LEFT_KI, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        leftMasterTalon.config_kD(0, Constants.Drive.Gains.LEFT_KD, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        leftMasterTalon.config_kF(0, Constants.Drive.Gains.LEFT_KF, Constants.Universal.CTRE_CONFIG_TIMEOUT)

        rightMasterTalon.config_kP(0, Constants.Drive.Gains.RIGHT_KP, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        rightMasterTalon.config_kI(0, Constants.Drive.Gains.RIGHT_KI, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        rightMasterTalon.config_kD(0, Constants.Drive.Gains.RIGHT_KD, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        rightMasterTalon.config_kF(0, Constants.Drive.Gains.RIGHT_KF, Constants.Universal.CTRE_CONFIG_TIMEOUT)

        setOpenLoop(DriveSignal.NEUTRAL)
        zeroSensors()
    }

    override fun checkSystem() {}

    @Synchronized
    override fun onStart(timestamp: Double) {
        setOpenLoop(DriveSignal.NEUTRAL)
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        synchronized(this@Drive) {
            when (currentState) {
                DriveControlState.OPEN_LOOP -> {
                    leftTargetVel = 0.0
                    rightTargetVel = 0.0
                }
                DriveControlState.VELOCITY_SETPOINT -> {}
                DriveControlState.PATH_FOLLOWING -> {
                    updatePathFollowing(timestamp, dT)
                }
                DriveControlState.MOTION_MAGIC -> {}
            }
        }
    }

    @Synchronized
    override fun onStop(timestamp: Double) {
        setOpenLoop(DriveSignal.NEUTRAL)
    }

    override fun registerLogging() {
        HelixLogger.addSource("DT Left Output %") { leftMasterTalon.motorOutputPercent }
        HelixLogger.addSource("DT Right Output %") { rightMasterTalon.motorOutputPercent }

        HelixLogger.addSource("DT Left Master Supply Current") { leftMasterTalon.supplyCurrent }
        HelixLogger.addSource("DT Left Slave Supply Current") { leftSlaveTalon.supplyCurrent }
        HelixLogger.addSource("DT Right Master Supply Current") { rightMasterTalon.supplyCurrent }
        HelixLogger.addSource("DT Right Slave Supply Current") { rightSlaveTalon.supplyCurrent }

        HelixLogger.addSource("DT Left Master Stator Current") { leftMasterTalon.statorCurrent }
        HelixLogger.addSource("DT Left Slave Stator Current") { leftSlaveTalon.statorCurrent }
        HelixLogger.addSource("DT Right Master Stator Current") { rightMasterTalon.statorCurrent }
        HelixLogger.addSource("DT Right Slave Stator Current") { rightSlaveTalon.statorCurrent }

        HelixLogger.addSource("DT Left Master Temp") { leftMasterTalon.temperature }
        HelixLogger.addSource("DT Left Slave Temp") { leftSlaveTalon.temperature }
        HelixLogger.addSource("DT Right Master Temp") { rightMasterTalon.temperature }
        HelixLogger.addSource("DT Right Slave Temp") { rightSlaveTalon.temperature }

        HelixLogger.addSource("DT Left Velocity (in/s)") { leftVelocityMetersPerSec }
        HelixLogger.addSource("DT Right Velocity (in/s)") { rightVelocityMetersPerSec }
        HelixLogger.addSource("DT Left Target Velocity (in/s)") { leftTargetVel }
        HelixLogger.addSource("DT Left Target Velocity (in/s)") { rightTargetVel }

        HelixLogger.addSource("DT Left Position (in)") { leftDistanceMeters }
        HelixLogger.addSource("DT Right Position (in)") { rightDistanceMeters }

        HelixLogger.addSource("DT Gyro Angle") { angle }

        HelixLogger.addSource("DT Pathfollow Timestamp") { trajCurTime }
    }

    override fun outputTelemetry() {
        if (ahrs.isConnected) {
            SmartDashboard.putNumber("drive/gyro", yaw)
        } else {
            SmartDashboard.putNumber("drive/gyro", Constants.Drive.GYRO_BAD_VALUE)
        }
    }

    override fun zeroSensors() {
        if (ahrs.isConnected) {
            while (!yaw.around(0.0, 1.0)) {
                ahrs.reset()
            }
        } else {
            HelixEvents.addEvent("DRIVETRAIN", "Gyroscope queried but not connected")
        }

        rightMasterTalon.sensorCollection.setIntegratedSensorPosition(0.0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
        leftMasterTalon.sensorCollection.setIntegratedSensorPosition(0.0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
    }

    /**
     * Controls the drivetrain using open loop.
     *
     * @param signal A [DriveSignal] containing left and right side powers,
     * as well as a desired brake mode.
     */
    @Synchronized
    fun setOpenLoop(signal: DriveSignal) {
        setLeftRightPower(
            signal.leftMotor * Constants.Drive.MAX_LEFT_OPEN_LOOP_POWER,
            signal.rightMotor * Constants.Drive.MAX_RIGHT_OPEN_LOOP_POWER
        )
        if (currentState !== DriveControlState.OPEN_LOOP) {
            leftMasterTalon.configNominalOutputForward(0.0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
            rightMasterTalon.configNominalOutputForward(0.0, Constants.Universal.CTRE_CONFIG_TIMEOUT)
            currentState = DriveControlState.OPEN_LOOP
            HelixEvents.addEvent("DRIVETRAIN", "Entered open loop control")
        }
        brakeMode = if (signal.brakeMode) NeutralMode.Brake else NeutralMode.Coast
    }

    @Synchronized
    private fun setLeftRightPower(left: Double, right: Double) {
        leftMasterTalon.set(ControlMode.PercentOutput, left)
        rightMasterTalon.set(ControlMode.PercentOutput, right)
    }

    /**
     * Drive the robot using a simple arcade drive.
     * Copied from WPILib arcade drive with no functional modification.
     *
     * @param outputMagnitude The magnitude of the output. Typically controlled by the forward/back axis.
     * @param curve The curvature of the path. Typically controlled by the left/right axis.
     */
    @Suppress("MagicNumber")
    @Synchronized
    fun arcadeDrive(outputMagnitude: Double, curve: Double) {
        val leftOutput: Double
        val rightOutput: Double

        when {
            curve < 0 -> {
                val value = ln(-curve)
                var ratio = (value - .5) / (value + .5)
                if (ratio == 0.0) {
                    ratio = .0000000001
                }
                leftOutput = outputMagnitude / ratio
                rightOutput = outputMagnitude
            }
            curve > 0 -> {
                val value = ln(curve)
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
        setOpenLoop(DriveSignal(leftOutput, rightOutput))
    }

    /**
     * Drive the robot using "Cheesyish Drive".
     * Uses a sinusoidal scaling function for curvature.
     *
     * @param throttle The magnitude of the output. Controlled by the triggers on the driver controller.
     * @param wheel The curvature of the path. Controlled by the left/right axis of a joystick.
     * @param quickTurn True if the curvature should not be scaled. Typically used when turning in place.
     */
    // thank you team 254 but i like 148 better...
    @Synchronized
    fun setCheesyishDrive(throttle: Double, wheel: Double, quickTurn: Boolean) {
        var mThrottle = throttle
        var mWheel = wheel
        if (mThrottle.around(0.0, Constants.Joysticks.THROTTLE_DEADBAND)) {
            mThrottle = 0.0
        }

        if (mWheel.around(0.0, Constants.Joysticks.TURN_DEADBAND)) {
            mWheel = 0.0
        }

        val denominator = sin(Math.PI / 2.0 * Constants.Drive.WHEEL_NON_LINEARITY)
        // Apply a sin function that's scaled to make it feel better.
        if (!quickTurn) {
            mWheel = sin(Math.PI / 2.0 * Constants.Drive.WHEEL_NON_LINEARITY * mWheel)
            mWheel = sin(Math.PI / 2.0 * Constants.Drive.WHEEL_NON_LINEARITY * mWheel)
            mWheel = mWheel / (denominator * denominator) * abs(mThrottle)
        }

        mWheel *= Constants.Drive.WHEEL_GAIN
        val driveSignal = if (abs(mWheel) < Constants.Universal.EPSILON) {
            DriveSignal(mThrottle, mThrottle)
        } else {
            val deltaV = Constants.Drive.WHEEL_TRACK_WIDTH_INCHES * mWheel / (2 * Constants.Drive.TRACK_SCRUB_FACTOR)
            DriveSignal(mThrottle - deltaV, mThrottle + deltaV)
        }

        val scalingFactor = max(1.0, max(abs(driveSignal.leftMotor), abs(driveSignal.rightMotor)))
        setOpenLoop(DriveSignal(driveSignal.leftMotor / scalingFactor, driveSignal.rightMotor / scalingFactor))
    }

    /**
     * Sets velocity targets for both sides of the drivetrain. Uses arbitrary feed forward with
     * values from drivetrain characterization to target a specific velocity and acceleration combination.
     *
     * @param leftMetersPerSec The velocity of the left side.
     * @param rightMetersPerSec The velocity of the right side.
     * @param leftMetersPerSecSq The acceleration of the left side.
     * @param rightMetersPerSecSq The acceleration of the right side.
     */
    @Synchronized
    fun setVelocitySetpoint(
        leftMetersPerSec: Double,
        rightMetersPerSec: Double,
        leftMetersPerSecSq: Double,
        rightMetersPerSecSq: Double
    ) {
        if (!currentState.usesVelocityControl) {
            // Not previously in velocity control.
            enterVelocityClosedLoop()
            currentState = DriveControlState.VELOCITY_SETPOINT
        }
        leftTargetVel = metersPerSecondToNative(leftMetersPerSec)
        rightTargetVel = metersPerSecondToNative(rightMetersPerSec)

        // Calculate feed forward values based on the desired state.
        // kV and kA values come from characterizing the drivetrain using
        // the WPILib characterization suite.
        val leftFeedForward: Double = if (leftMetersPerSec > 0) {
            Constants.Drive.Characterization.LEFT_KV_FORWARD * leftMetersPerSec +
                Constants.Drive.Characterization.LEFT_KA_FORWARD * leftMetersPerSecSq +
                Constants.Drive.Characterization.LEFT_V_INTERCEPT_FORWARD
        } else {
            Constants.Drive.Characterization.LEFT_KV_REVERSE * leftMetersPerSec +
                Constants.Drive.Characterization.LEFT_KA_REVERSE * leftMetersPerSecSq +
                Constants.Drive.Characterization.LEFT_V_INTERCEPT_REVERSE
        }
        val rightFeedForward: Double = if (rightMetersPerSec > 0) {
            Constants.Drive.Characterization.RIGHT_KV_FORWARD * rightMetersPerSec +
                Constants.Drive.Characterization.RIGHT_KA_FORWARD * rightMetersPerSecSq +
                Constants.Drive.Characterization.RIGHT_V_INTERCEPT_FORWARD
        } else {
            Constants.Drive.Characterization.RIGHT_KV_REVERSE * rightMetersPerSec +
                Constants.Drive.Characterization.RIGHT_KA_REVERSE * rightMetersPerSecSq +
                Constants.Drive.Characterization.RIGHT_V_INTERCEPT_REVERSE
        }

        leftMasterTalon.set(
            ControlMode.Velocity,
            leftTargetVel,
            DemandType.ArbitraryFeedForward,
            leftFeedForward
        )
        rightMasterTalon.set(
            ControlMode.Velocity,
            rightTargetVel,
            DemandType.ArbitraryFeedForward,
            rightFeedForward
        )
    }

    /**
     * Target a position for the drivetrain using motion magic.
     * Caveat: There are many possible positions for a given distance traveled on both sides.
     * This function should not be used to travel to a far away position, as no correction
     * is applied to ensure the desired curvature and it is not guaranteed that the motion
     * profile for each side will match up.
     *
     * @param leftMeters The position for the left side.
     * @param rightMeters The position for the right side.
     */
    @Synchronized
    fun setPositionSetpoint(leftMeters: Double, rightMeters: Double) {
        if (!currentState.usesPositionControl) {
            // Not previously in position control.
            enterVelocityClosedLoop()
            currentState = DriveControlState.MOTION_MAGIC
        }
        leftMasterTalon.set(ControlMode.MotionMagic, metersToNative(leftMeters))
        rightMasterTalon.set(ControlMode.MotionMagic, metersToNative(rightMeters))
    }

    /**
     * Sets Talon PID slot for velocity control modes (velocity setpoint,
     * motion magic, path following).
     */
    @Synchronized
    private fun enterVelocityClosedLoop() {
        leftMasterTalon.selectProfileSlot(0, 0)
        rightMasterTalon.selectProfileSlot(0, 0)

        brakeMode = NeutralMode.Brake
        HelixEvents.addEvent("DRIVETRAIN", "Entered a closed loop state.")
    }

    /**
     * Updates the velocities of the robot based on those from the trajectory being followed
     *
     * @param timestamp The current time. Value originates from Timer.getFPGATimestamp.
     * @param dT The interval since the last time that this function was called.
     */
    private fun updatePathFollowing(timestamp: Double, dT: Double) {
        trajCurTime = timestamp - trajStartTime
        autoOdometry.update(Rotation2d.fromDegrees(-angle), leftDistanceMeters, rightDistanceMeters)

        val sample = path.sample(trajCurTime)
        val wheelSpeeds = kinematics.toWheelSpeeds(pathFollowController.calculate(autoOdometry.poseMeters, sample))
        val leftAcceleration = (wheelSpeeds.leftMetersPerSecond - lastWheelSpeeds.leftMetersPerSecond) / dT
        val rightAcceleration = (wheelSpeeds.rightMetersPerSecond - lastWheelSpeeds.rightMetersPerSecond) / dT

        setVelocitySetpoint(wheelSpeeds.leftMetersPerSecond, wheelSpeeds.rightMetersPerSecond,
            leftAcceleration, rightAcceleration)

        lastWheelSpeeds = wheelSpeeds
    }

    /**
     * Checks if path following has reached the end of the path.
     *
     * @param timestamp The current time. Value originates from Timer.getFPGATimestamp.
     * @return If path following is finished.
     */
    fun isPathFinished(timestamp: Double): Boolean {
        trajCurTime = timestamp - trajStartTime
        return trajCurTime > trajDuration
    }

    private fun nativeToMeters(nativeUnits: Int): Double {
        return (nativeUnits / Constants.Drive.NATIVE_UNITS_PER_REV) * Constants.Drive.WHEEL_DIAMETER_METERS * Math.PI
    }

    private fun metersToNative(meters: Double): Double {
        return (meters * Constants.Drive.NATIVE_UNITS_PER_REV) / (Constants.Drive.WHEEL_DIAMETER_METERS * Math.PI)
    }

    private fun rpmToMetersPerSecond(rpm: Double): Double {
        return (rpm) / 60 * Math.PI * Constants.Drive.WHEEL_DIAMETER_METERS
    }

    private fun nativeToMetersPerSecond(nativeUnits: Int): Double {
        return nativeToMeters(nativeUnits) * 10
    }

    private fun metersPerSecondToNative(meters: Double): Double {
        return metersToNative(meters) / 10
    }

    private fun metersToRotations(meters: Double): Double {
        return meters / (Constants.Drive.WHEEL_DIAMETER_METERS * Math.PI)
    }

    private fun metersPerSecondToRpm(metersPerSecond: Double): Double {
        return metersToRotations(metersPerSecond) * 60
    }
}
