package org.usfirst.frc.team4099.robot2020.subsystems

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.StatusFrameEnhanced
import com.ctre.phoenix.motorcontrol.VelocityMeasPeriod
import com.kauailabs.navx.frc.AHRS
import com.team2363.logger.HelixEvents
import com.team2363.logger.HelixLogger
import edu.wpi.first.wpilibj.SPI
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.controller.RamseteController
import edu.wpi.first.wpilibj.geometry.Pose2d
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj.trajectory.Trajectory
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator
import edu.wpi.first.wpilibj.util.Units
import org.usfirst.frc.team4099.lib.drive.DriveSignal
import org.usfirst.frc.team4099.lib.loop.Loop
import org.usfirst.frc.team4099.lib.subsystem.Subsystem
import org.usfirst.frc.team4099.lib.util.CANMotorControllerFactory
import org.usfirst.frc.team4099.lib.util.around
import org.usfirst.frc.team4099.robot2020.config.Constants
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sin

object Drive : Subsystem() {
    private val rightMasterTalon = CANMotorControllerFactory.createDefaultTalon(Constants.Drive.RIGHT_MASTER_ID)
    private val rightSlaveTalon = CANMotorControllerFactory.createPermanentSlaveTalon(
            Constants.Drive.RIGHT_SLAVE_1_ID,
            Constants.Drive.RIGHT_MASTER_ID
    )

    private val leftMasterTalon = CANMotorControllerFactory.createDefaultTalon(Constants.Drive.LEFT_MASTER_ID)
    private val leftSlaveTalon = CANMotorControllerFactory.createPermanentSlaveTalon(
            Constants.Drive.LEFT_SLAVE_1_ID,
            Constants.Drive.LEFT_MASTER_ID
    )

    private val ahrs = AHRS(SPI.Port.kMXP)
    private var trajDuration = 0.0
    private var trajCurTime = 0.0
    private var trajStartTime = 0.0
    private var lastLeftError = 0.0
    private var lastRightError = 0.0

    private var leftTargetVel = 0.0
    private var rightTargetVel = 0.0

    private var pathFollowController = RamseteController()
    private var kinematics = DifferentialDriveKinematics(Units.inchesToMeters(Constants.Drive.WHEEL_TRACK_WIDTH_INCHES))
    var path: Trajectory = TrajectoryGenerator.generateTrajectory(listOf(), TrajectoryConfig(0.0, 0.0))
        set(value) {
            configureTalonsForVelocityControl()
            zeroSensors()
            trajDuration = value.totalTimeSeconds
            trajStartTime = Timer.getFPGATimestamp()
            lastPose = path.sample(0.0).poseMeters
            pathFollowController = RamseteController(
                    Constants.Drive.Gains.RAMSETE_B,
                    Constants.Drive.Gains.RAMSETE_ZETA
            )
            currentState = DriveControlState.PATH_FOLLOWING
            HelixEvents.addEvent("DRIVETRAIN", "Path following")

            field = value
        }
    private var lastPose: Pose2d = path.sample(0.0).poseMeters

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

    enum class DriveControlState {
        OPEN_LOOP,
        VELOCITY_SETPOINT,
        PATH_FOLLOWING,
        TURN_TO_HEADING, // turn in place
        MOTION_MAGIC
    }

    private var currentState = DriveControlState.OPEN_LOOP

    init {
        rightMasterTalon.configFactoryDefault()
        rightSlaveTalon.configFactoryDefault()

        leftMasterTalon.configFactoryDefault()
        leftSlaveTalon.configFactoryDefault()

        rightMasterTalon.setStatusFramePeriod(
                StatusFrameEnhanced.Status_2_Feedback0,
                Constants.Drive.STATUS_FRAME_PERIOD_MS, Constants.Universal.TIMEOUT
        )
        leftMasterTalon.setStatusFramePeriod(
                StatusFrameEnhanced.Status_2_Feedback0,
                Constants.Drive.STATUS_FRAME_PERIOD_MS, Constants.Universal.TIMEOUT
        )

        rightMasterTalon.inverted = true
        rightSlaveTalon.inverted = true
        leftMasterTalon.inverted = false
        leftMasterTalon.inverted = false

        rightMasterTalon.setSensorPhase(true)
        rightSlaveTalon.setSensorPhase(true)
        leftMasterTalon.setSensorPhase(true)
        leftSlaveTalon.setSensorPhase(true)

        rightMasterTalon.configSelectedFeedbackSensor(
                FeedbackDevice.CTRE_MagEncoder_Relative,
                0,
                Constants.Universal.TIMEOUT
        )
        rightMasterTalon.configSelectedFeedbackSensor(
                FeedbackDevice.CTRE_MagEncoder_Relative,
                1,
                Constants.Universal.TIMEOUT
        )

        leftMasterTalon.configSelectedFeedbackSensor(
                FeedbackDevice.CTRE_MagEncoder_Relative,
                0,
                Constants.Universal.TIMEOUT
        )
        leftMasterTalon.configSelectedFeedbackSensor(
                FeedbackDevice.CTRE_MagEncoder_Relative,
                1,
                Constants.Universal.TIMEOUT
        )

        rightMasterTalon.enableVoltageCompensation(true)
        rightMasterTalon.configVoltageCompSaturation(Constants.Drive.VOLTAGE_COMP_LEVEL, Constants.Universal.TIMEOUT)
        leftMasterTalon.enableVoltageCompensation(true)
        leftMasterTalon.configVoltageCompSaturation(Constants.Drive.VOLTAGE_COMP_LEVEL, Constants.Universal.TIMEOUT)

        rightMasterTalon.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_50Ms, Constants.Universal.TIMEOUT)
        leftMasterTalon.configVelocityMeasurementPeriod(VelocityMeasPeriod.Period_50Ms, Constants.Universal.TIMEOUT)

        rightMasterTalon.configClosedloopRamp(Constants.Drive.CLOSED_LOOP_RAMP, Constants.Universal.TIMEOUT)
        leftMasterTalon.configClosedloopRamp(Constants.Drive.CLOSED_LOOP_RAMP, Constants.Universal.TIMEOUT)
        rightMasterTalon.configNeutralDeadband(
                Constants.Drive.PERCENT_DEADBAND,
                Constants.Universal.TIMEOUT
        ) // 254 used 0 for timeout
        leftMasterTalon.configNeutralDeadband(Constants.Drive.PERCENT_DEADBAND, Constants.Universal.TIMEOUT)

        // TODO: SET CONVERSION FACTORS

        leftMasterTalon.config_kP(0, Constants.Drive.Gains.LEFT_KP, Constants.Universal.TIMEOUT)
        leftMasterTalon.config_kI(0, Constants.Drive.Gains.LEFT_KI, Constants.Universal.TIMEOUT)
        leftMasterTalon.config_kD(0, Constants.Drive.Gains.LEFT_KD, Constants.Universal.TIMEOUT)
        leftMasterTalon.config_kF(0, Constants.Drive.Gains.LEFT_KF, Constants.Universal.TIMEOUT)

        rightMasterTalon.config_kP(0, Constants.Drive.Gains.RIGHT_KP, Constants.Universal.TIMEOUT)
        rightMasterTalon.config_kI(0, Constants.Drive.Gains.RIGHT_KI, Constants.Universal.TIMEOUT)
        rightMasterTalon.config_kD(0, Constants.Drive.Gains.RIGHT_KD, Constants.Universal.TIMEOUT)
        rightMasterTalon.config_kF(0, Constants.Drive.Gains.RIGHT_KF, Constants.Universal.TIMEOUT)

        rightMasterTalon.configContinuousCurrentLimit(
                Constants.Drive.CONTINUOUS_CURRENT_LIMIT,
                Constants.Universal.TIMEOUT
        )
        leftMasterTalon.configContinuousCurrentLimit(
                Constants.Drive.CONTINUOUS_CURRENT_LIMIT,
                Constants.Universal.TIMEOUT
        )

        setOpenLoop(DriveSignal.NEUTRAL)

        this.zeroSensors()
        registerLogging()
    }

    override fun stop() {
        synchronized(this) {
            setOpenLoop(DriveSignal.NEUTRAL)
        }
    }

    override fun checkSystem() {}

    override val loop = object : Loop {
        override fun onStart(timestamp: Double) {
            setOpenLoop(DriveSignal.NEUTRAL)
        }

        override fun onLoop(timestamp: Double) {
            synchronized(this@Drive) {
                when (currentState) {
                    DriveControlState.OPEN_LOOP -> {
                        leftTargetVel = 0.0
                        rightTargetVel = 0.0
                    }
                    DriveControlState.VELOCITY_SETPOINT -> {
                    }
                    DriveControlState.PATH_FOLLOWING -> {
                        updatePathFollowing(timestamp)
                    }
                    DriveControlState.TURN_TO_HEADING -> {
                        leftTargetVel = 0.0
                        rightTargetVel = 0.0
                        // updateTurnToHeading(timestamp)
                    }
                    else -> {
                        HelixEvents.addEvent("DRIVETRAIN", "Unexpected drive control state: $currentState")
                    }
                }
            }
        }

        override fun onStop(timestamp: Double) = stop()
    }

    private fun registerLogging() {
        HelixLogger.addSource("DT Left Output %") { leftMasterTalon.motorOutputPercent }
        HelixLogger.addSource("DT Right Output %") { rightMasterTalon.motorOutputPercent }

        HelixLogger.addSource("DT Left Master Input Current") { leftMasterTalon.outputCurrent }
        HelixLogger.addSource("DT Left Slave Input Current") { leftSlaveTalon.outputCurrent }
        HelixLogger.addSource("DT Right Master Input Current") { rightMasterTalon.outputCurrent }
        HelixLogger.addSource("DT Right Slave Input Current") { rightSlaveTalon.outputCurrent }

        HelixLogger.addSource("DT Left Velocity (in/s)") { getLeftVelocityInchesPerSec() }
        HelixLogger.addSource("DT Right Velocity (in/s)") { getRightVelocityInchesPerSec() }
        HelixLogger.addSource("DT Left Target Velocity (in/s)") { leftTargetVel }
        HelixLogger.addSource("DT Left Target Velocity (in/s)") { rightTargetVel }

        HelixLogger.addSource("DT Left Position (in)") { getLeftDistanceInches() }
        HelixLogger.addSource("DT Right Position (in)") { getRightDistanceInches() }

        HelixLogger.addSource("DT Gyro Angle") { angle }

        HelixLogger.addSource("DT Pathfollow Timestamp") { trajCurTime }
    }

    override fun outputToSmartDashboard() {
        if (ahrs.isConnected) {
            SmartDashboard.putNumber("gyro", yaw)
        } else {
            SmartDashboard.putNumber("gyro", Constants.Drive.GYRO_BAD_VALUE)
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
        resetEncoders()
    }

    @Synchronized
    fun setOpenLoop(signal: DriveSignal) {
        if (currentState !== DriveControlState.OPEN_LOOP) {
            leftMasterTalon.configNominalOutputForward(0.0, Constants.Universal.TIMEOUT)
            rightMasterTalon.configNominalOutputForward(0.0, Constants.Universal.TIMEOUT)
            currentState = DriveControlState.OPEN_LOOP
            brakeMode = NeutralMode.Coast
            HelixEvents.addEvent("DRIVETRAIN", "Entered open loop control")
        }
        setLeftRightPower(
                signal.leftMotor * Constants.Drive.MAX_LEFT_OPEN_LOOP_POWER,
                signal.rightMotor * Constants.Drive.MAX_RIGHT_OPEN_LOOP_POWER
        )
    }

    /**
     * Powers the left and right talons during OPEN_LOOP
     * @param left
     * @param right
     */
    @Synchronized
    fun setLeftRightPower(left: Double, right: Double) {
        leftMasterTalon.set(ControlMode.PercentOutput, left)
        rightMasterTalon.set(ControlMode.PercentOutput, right)
    }

    @Synchronized
    fun resetEncoders() {
        rightMasterTalon.sensorCollection.setQuadraturePosition(0, Constants.Universal.TIMEOUT)
        leftMasterTalon.sensorCollection.setQuadraturePosition(0, Constants.Universal.TIMEOUT)
    }

    // Copied from WPIlib arcade drive with no functional modification
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

    @Synchronized
    fun usesTalonVelocityControl(state: DriveControlState): Boolean {
        if (state == DriveControlState.VELOCITY_SETPOINT || state == DriveControlState.PATH_FOLLOWING) {
            return true
        }
        return false
    }

    @Synchronized
    fun usesTalonPositionControl(state: DriveControlState): Boolean {
        if (state == DriveControlState.TURN_TO_HEADING || state == DriveControlState.MOTION_MAGIC) {
            return true
        }
        return false
    }

    @Synchronized
    fun setVelocitySetpoint(
        leftFeetPerSec: Double,
        rightFeetPerSec: Double,
        leftFeetPerSecSq: Double,
        rightFeetPerSecSq: Double
    ) {
        if (usesTalonVelocityControl(currentState)) {
            // TODO: change constants
            leftTargetVel = leftFeetPerSec * Constants.Drive.FEET_PER_SEC_TO_NATIVE
            rightTargetVel = rightFeetPerSec * Constants.Drive.FEET_PER_SEC_TO_NATIVE

            val leftFeedForward: Double = if (leftFeetPerSec > 0) {
                Constants.Drive.LEFT_KV_FORWARD * leftFeetPerSec +
                        Constants.Drive.LEFT_KA_FORWARD * leftFeetPerSecSq +
                        Constants.Drive.LEFT_V_INTERCEPT_FORWARD
            } else {
                Constants.Drive.LEFT_KV_REVERSE * leftFeetPerSec +
                        Constants.Drive.LEFT_KA_REVERSE * leftFeetPerSecSq +
                        Constants.Drive.LEFT_V_INTERCEPT_REVERSE
            }
            val rightFeedForward: Double = if (rightFeetPerSec > 0) {
                Constants.Drive.RIGHT_KV_FORWARD * rightFeetPerSec +
                        Constants.Drive.RIGHT_KA_FORWARD * rightFeetPerSecSq +
                        Constants.Drive.RIGHT_V_INTERCEPT_FORWARD
            } else {
                Constants.Drive.RIGHT_KV_REVERSE * rightFeetPerSec +
                        Constants.Drive.RIGHT_KA_REVERSE * rightFeetPerSecSq +
                        Constants.Drive.RIGHT_V_INTERCEPT_REVERSE
            }

            leftMasterTalon.set(ControlMode.Velocity, leftTargetVel, DemandType.ArbitraryFeedForward, leftFeedForward)
            rightMasterTalon.set(
                    ControlMode.Velocity,
                    rightTargetVel,
                    DemandType.ArbitraryFeedForward,
                    rightFeedForward
            )
        } else {
            configureTalonsForVelocityControl()
            currentState = DriveControlState.VELOCITY_SETPOINT
            setVelocitySetpoint(leftFeetPerSec, rightFeetPerSec, leftFeetPerSecSq, rightFeetPerSecSq)
        }
    }

    @Synchronized
    fun setPositionSetpoint(leftInches: Double, rightInches: Double) {
        if (usesTalonPositionControl(currentState)) {
            leftMasterTalon.set(ControlMode.MotionMagic, leftInches * Constants.Drive.FEET_PER_SEC_TO_NATIVE)
            rightMasterTalon.set(ControlMode.MotionMagic, rightInches * Constants.Drive.FEET_PER_SEC_TO_NATIVE)
        } else {
            configureTalonsForPositionControl()
            currentState = DriveControlState.MOTION_MAGIC
            setPositionSetpoint(leftInches, rightInches)
        }
    }

    @Synchronized
    private fun configureTalonsForVelocityControl() { // should further review cause im bad
        if (!usesTalonVelocityControl(currentState)) {
            // We entered a velocity control state.

            leftMasterTalon.set(ControlMode.Velocity, 0.0) // velocity  output value is in position change / 100ms
            leftMasterTalon.configNominalOutputForward(Constants.Drive.AUTO_NOMINAL_OUTPUT, Constants.Universal.TIMEOUT)
            leftMasterTalon.configNominalOutputReverse(Constants.Drive.AUTO_NOMINAL_OUTPUT, Constants.Universal.TIMEOUT)
            leftMasterTalon.selectProfileSlot(0, Constants.Universal.TIMEOUT)
            leftMasterTalon.configPeakOutputForward(Constants.Drive.AUTO_PEAK_OUTPUT, Constants.Universal.TIMEOUT)
            leftMasterTalon.configPeakOutputReverse(
                    Constants.Drive.AUTO_PEAK_OUTPUT * -1.0,
                    Constants.Universal.TIMEOUT
            )

            rightMasterTalon.set(ControlMode.Velocity, 0.0) // velocity  output value is in position change / 100ms
            rightMasterTalon.configNominalOutputForward(
                    Constants.Drive.AUTO_NOMINAL_OUTPUT,
                    Constants.Universal.TIMEOUT
            )
            rightMasterTalon.configNominalOutputReverse(
                    Constants.Drive.AUTO_NOMINAL_OUTPUT,
                    Constants.Universal.TIMEOUT
            )
            rightMasterTalon.selectProfileSlot(0, 0)
            rightMasterTalon.configPeakOutputForward(Constants.Drive.AUTO_PEAK_OUTPUT, Constants.Universal.TIMEOUT)
            rightMasterTalon.configPeakOutputReverse(
                    Constants.Drive.AUTO_PEAK_OUTPUT * -1.0,
                    Constants.Universal.TIMEOUT
            )
            brakeMode = NeutralMode.Brake
        }
        HelixEvents.addEvent("DRIVETRAIN", "Configured Talons for velocity control")
    }

    @Synchronized
    private fun configureTalonsForPositionControl() {
        if (!usesTalonPositionControl(currentState)) {
            // We entered a position control state.
            leftMasterTalon.configNominalOutputForward(Constants.Drive.AUTO_NOMINAL_OUTPUT, Constants.Universal.TIMEOUT)
            leftMasterTalon.configNominalOutputReverse(Constants.Drive.AUTO_NOMINAL_OUTPUT, Constants.Universal.TIMEOUT)
            leftMasterTalon.selectProfileSlot(0, Constants.Universal.TIMEOUT)
            leftMasterTalon.configPeakOutputForward(Constants.Drive.AUTO_PEAK_OUTPUT, Constants.Universal.TIMEOUT)
            leftMasterTalon.configPeakOutputReverse(
                    Constants.Drive.AUTO_PEAK_OUTPUT * -1.0,
                    Constants.Universal.TIMEOUT
            )

            rightMasterTalon.configNominalOutputForward(
                    Constants.Drive.AUTO_NOMINAL_OUTPUT,
                    Constants.Universal.TIMEOUT
            )
            rightMasterTalon.configNominalOutputReverse(
                    Constants.Drive.AUTO_NOMINAL_OUTPUT,
                    Constants.Universal.TIMEOUT
            )
            rightMasterTalon.selectProfileSlot(0, 0)
            rightMasterTalon.configPeakOutputForward(Constants.Drive.AUTO_PEAK_OUTPUT, Constants.Universal.TIMEOUT)
            rightMasterTalon.configPeakOutputReverse(
                    Constants.Drive.AUTO_PEAK_OUTPUT * -1.0,
                    Constants.Universal.TIMEOUT
            )

            brakeMode = NeutralMode.Brake
        }
        HelixEvents.addEvent("DRIVETRAIN", "Configured Talons for position control")
    }

    fun updatePathFollowing(timestamp: Double) {
        trajCurTime = timestamp - trajStartTime
        val sample = path.sample(trajCurTime)

        val wheelVelocities = kinematics.toWheelSpeeds(pathFollowController.calculate(lastPose, sample))
        lastPose = sample.poseMeters
        // TODO: finish path follow
    }

//    fun updatePathFollowing() {
//        var leftTurn = path.getLeftVelocityIndex(segment)
//        var rightTurn = path.getRightVelocityIndex(segment)
//
//        val desiredHeading = Math.toDegrees(path.getHeadingIndex(segment))
//        val angleDifference = boundHalfDegrees(desiredHeading - yaw)
//        val turn: Double = Constants.Autonomous.PATH_FOLLOW_TURN_KP * angleDifference
//
//        val leftDistance: Double = getLeftDistanceInches()
//        val rightDistance: Double = getRightDistanceInches()
//
//        val leftErrorDistance: Double = path.getLeftDistanceIndex(segment) - leftDistance
//        val rightErrorDistance: Double = path.getRightDistanceIndex(segment) - rightDistance
//
//        val leftVelocityAdjustment =
//                Constants.Gains.LEFT_LOW_KP * leftErrorDistance +
//                        Constants.Gains.LEFT_LOW_KD * ((leftErrorDistance - lastLeftError) / path.getDeltaTime())
//        val rightVelocityAdjustment =
//                Constants.Gains.RIGHT_LOW_KP * rightErrorDistance +
//                        Constants.Gains.RIGHT_LOW_KD * ((rightErrorDistance - lastRightError) / path.getDeltaTime())
//
//        leftTurn += leftVelocityAdjustment
//        rightTurn += rightVelocityAdjustment
//
//        lastLeftError = leftErrorDistance
//        lastRightError = rightErrorDistance
//
//        leftTurn += turn
//        rightTurn -= turn
//
//        setVelocitySetpoint(
//                leftTurn,
//                rightTurn,
//                path.getLeftAccelerationIndex(segment),
//                path.getRightAccelerationIndex(segment)
//        )
//        segment++
//    }

    fun isPathFinished(timestamp: Double): Boolean {
        trajCurTime = timestamp - trajStartTime
        return trajCurTime > trajDuration
    }

    private fun nativeToInches(nativeUnits: Int): Double {
        return nativeUnits * Constants.Drive.NATIVE_TO_REVS * Constants.Drive.WHEEL_DIAMETER_INCHES * Math.PI
    }

    private fun rpmToInchesPerSecond(rpm: Double): Double {
        return (rpm) / 60 * Math.PI * Constants.Drive.WHEEL_DIAMETER_INCHES
    }

    private fun nativeToInchesPerSecond(nativeUnits: Int): Double {
        return nativeToInches(nativeUnits) * 10
    }

    private fun inchesToRotations(inches: Double): Double {
        return inches / (Constants.Drive.WHEEL_DIAMETER_INCHES * Math.PI)
    }

    private fun inchesPerSecondToRpm(inchesPerSecond: Double): Double {
        return inchesToRotations(inchesPerSecond) * 60
    }

    fun getLeftDistanceInches(): Double {
        return nativeToInches(leftMasterTalon.selectedSensorPosition)
    }

    fun getRightDistanceInches(): Double {
        return nativeToInches(rightMasterTalon.selectedSensorPosition)
    }

    fun getLeftVelocityInchesPerSec(): Double {
        return nativeToInchesPerSecond(leftMasterTalon.selectedSensorVelocity)
    }

    fun getRightVelocityInchesPerSec(): Double {
        return nativeToInchesPerSecond(rightMasterTalon.selectedSensorVelocity)
    }

    fun boundHalfDegrees(angleDegrees: Double): Double {
        return ((angleDegrees + 180.0) % 360.0) - 180.0
    }
}
