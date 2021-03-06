package com.team4099.robot2020.config

import com.team4099.lib.config.PIDGains
import com.team4099.lib.config.ServoMotorSubsystemConfig
import com.team4099.lib.config.ServoMotorSubsystemMotionConstraints
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory
import com.team4099.robot2020.Robot
import kotlin.math.PI

/**
 * Stores constants used by the robot.
 */
@SuppressWarnings("MagicNumber")
object Constants {
    object Universal {
        const val CTRE_CONFIG_TIMEOUT = 0
        const val EPSILON = 1E-9
    }

    object Tuning {
        const val TUNING_TOGGLE_PIN = 0
        val ROBOT_ID_PINS = 1..2

        enum class RobotName {
            COMPETITION,
            PRACTICE,
            MULE
        }

        val ROBOT_ID_MAP = mapOf<Int, RobotName>(
            0 to RobotName.COMPETITION,
            1 to RobotName.PRACTICE,
            2 to RobotName.MULE
        )
    }

    object Drive {
        const val LEFT_MASTER_ID = 2
        const val LEFT_SLAVE_1_ID = 3

        const val RIGHT_MASTER_ID = 12
        const val RIGHT_SLAVE_1_ID = 13

        const val STATUS_FRAME_PERIOD_MS = 5

        const val VOLTAGE_COMP_LEVEL = 12.0

        const val MAX_LEFT_OPEN_LOOP_POWER = 1.0
        const val MAX_RIGHT_OPEN_LOOP_POWER = 1.0

        const val CLOSED_LOOP_RAMP = 0.0

        const val OUTPUT_POWER_DEADBAND = 0.0

        const val SLOW_MODE_SCALE = 0.4

        const val CONTINUOUS_CURRENT_LIMIT = 40

        const val ENCODER_RESOLUTION = 2048
        const val NATIVE_UNITS_PER_REV = ENCODER_RESOLUTION / 0.08665966387

        const val WHEEL_DIAMETER_METERS = 0.1524
        const val WHEEL_TRACK_WIDTH_INCHES = 21.0
        const val WHEEL_TRACK_WIDTH_METERS = WHEEL_TRACK_WIDTH_INCHES * 0.0254
        const val WHEEL_GAIN = 0.05
        const val WHEEL_NON_LINEARITY = 0.05

        const val TRACK_SCRUB_FACTOR = 1.1

        const val GYRO_BAD_VALUE = -31337.0

        const val MAX_VEL_METERS_PER_SEC = 4.0
        const val SLOW_VEL_METERS_PER_SEC = 0.66
        const val MAX_ACCEL_METERS_PER_SEC_SQ = 2.0
        const val SLOW_ACCEL_METERS_PER_SEC_SQ = 2.0

        const val CENTRIPETAL_ACCEL_METERS_PER_SEC_SQ = 1.0

        object Characterization {
            const val LEFT_KV_FORWARD = 2.67
            const val RIGHT_KV_FORWARD = 2.67

            const val LEFT_KA_FORWARD = 0.101
            const val RIGHT_KA_FORWARD = 0.114

            const val LEFT_KS_FORWARD = 0.0371
            const val RIGHT_KS_FORWARD = 0.0408

            const val LEFT_KV_REVERSE = 2.63
            const val RIGHT_KV_REVERSE = 2.63

            const val LEFT_KA_REVERSE = 0.196
            const val RIGHT_KA_REVERSE = 0.216

            const val LEFT_KS_REVERSE = -0.0893
            const val RIGHT_KS_REVERSE = -0.0803
        }

        object Gains {
            const val RAMSETE_B = 2.0
            const val RAMSETE_ZETA = 0.7

            const val LEFT_KP = 0.1000 // .1 * 1500 / 70
            const val LEFT_KI = 0.0000
            const val LEFT_KD = 0.0000
            const val LEFT_KF = 0.0000 // 1023.0 / 2220.0

            const val RIGHT_KP = 0.1000 // .1 * 1023 / 70
            const val RIGHT_KI = 0.0000
            const val RIGHT_KD = 0.0000
            const val RIGHT_KF = 0.0000 // 1023.0 / 4420.0
        }
    }

    object Vision {
        const val DRIVER_PIPELINE_ID = 1
        const val TARGETING_PIPELINE_ID = 0
        const val TARGET_HEIGHT = 98.25
        const val CAMERA_HEIGHT = 35.0
        const val CAMERA_ANGLE = 24.0 * PI / 180
        const val SHOOTING_DISTANCE = 0

        const val MAX_DIST_ERROR = 0.1
        const val MAX_ANGLE_ERROR = 1.0

        val TURN_GAINS = PIDGains(0, 0.013, 0.0, 0.00025, 0.0, 0)
        val ON_TARGET_GAINS = PIDGains(0, 0.011, 0.0, 0.00025, 0.0, 0)
        const val MIN_TURN_COMMAND = 0.06 // 0.35
        const val ON_TARGET_TIME_THRESHOLD = 0.1
    }

    object Shooter {
        const val MASTER_SPARKMAX_ID = 1
        const val SLAVE_SPARKMAX_ID = 14

        const val SHOOTER_KS = 0.149 / 60 // .192
        const val SHOOTER_KV = 0.126 / 60 // .12545
        val SHOOTER_PID = PIDGains(0, 1.7 / 300, 0.0, 5.5 * 14000, 0.0, 0)

        const val REG_QUAD_TERM = 0.0694
        const val REG_LIN_TERM = -16.4
        const val REG_CONST_TERM = 5100

        const val TARGET_VELOCITY = 4500.0
        const val VELOCITY_ERROR_THRESHOLD = 60.0
    }

    object Climber : ServoMotorSubsystemConfig(
        CTREMotorControllerFactory.defaultConfiguration,
        "CLIMBER",
        "inches",
        PIDGains(0, 0.0, 0.0, 0.0, 0.0, 0),
        PIDGains(1, 0.0, 0.0, 0.0, 0.0, 0),
        0.0,
        ServoMotorSubsystemMotionConstraints(0.0, 90.0, 90.0, 90.0, 0),
        0.562345085,
        1, // Spark MAX API gives position in rotations
        brakeMode = true
    ) {
        const val MASTER_ID = 20
        const val SLAVE_ID = 15

        enum class ClimberPosition(val position: Double) {
            DOWN(0.0),
            UP(45.0)
        }
    }

    object Intake {
        const val INTAKE_TALON_ID = 5
        const val INTAKE_POWER = 1.0
        const val STALL_LIMIT_AMPS = 13.0
        const val STALL_LIMIT_SECONDS = 0.25 // seconds
    }

    object Looper {
        const val LOOPER_DT = 0.02 // 50 Hz
    }

    object Autonomous {
        const val AUTON_DT = 0.02 // 50 Hz
        const val DEFAULT_MODE_NAME = "Shoot 3 + Forward"
        const val DEFAULT_DELAY = 0.0

        const val TURN_POWER = 0.5
        const val SLOW_TURN_POWER = 0.2

        const val FORWARD_POWER = 1.0
        const val SLOW_FORWARD_POWER = 0.6
        const val FORWARD_MAX_TIME_SECONDS = 3
        const val SHOOT_MAX_TIME_SECONDS = 2.5
        const val FORWARD_CORRECTION_KP = 0.01
        const val FORWARD_GIVE_UP_ANGLE = 30.0
    }

    object Dashboard {
        const val AUTO_OPTIONS_KEY = "autonomous/autoOptions"
        const val SELECTED_AUTO_MODE_KEY = "autonomous/selectedMode"

        const val AUTO_STARTS_KEY = "autonomous/autoStarts"
        const val SELECTED_AUTO_START_POS_KEY = "autonomous/selectedStart"

        const val SELECTED_AUTO_START_DELAY_KEY = "autonomous/selectedDelay"
    }

    object Joysticks {
        const val DRIVER_PORT = 0
        const val SHOTGUN_PORT = 1

        const val QUICK_TURN_THROTTLE_TOLERANCE = 0.1
        const val THROTTLE_DEADBAND = 0.04
        const val TURN_DEADBAND = 0.035
    }

    object BrownoutDefender {
        const val COMPRESSOR_STOP_VOLTAGE = 10
        const val COMPRESSOR_STOP_CURRENT = 70
    }

    object Wrist : ServoMotorSubsystemConfig(
        CTREMotorControllerFactory.defaultConfiguration,
        "WRIST",
        "degrees",
        PIDGains(0, 1.3, 0.0, 0.0, 3.5, 0),
        PIDGains(1, 0.8, 0.0, 0.0, 0.15, 0),
        if (Robot.robotName == Tuning.RobotName.PRACTICE) 154.5 else 161.9,
        ServoMotorSubsystemMotionConstraints(-130.0, 1.0, 1500.0, 3072.0, 1),
        360.0,
        4096
    ) {
        const val MASTER_ID = 9

        const val OPERATOR_CONTROL_VEL = 90.0

        enum class WristPosition(val position: Double) {
            HORIZONTAL(-3.0),
            VERTICAL(-126.0)
        }
    }

    object Feeder {
        const val FEEDER_IN_ID = 7
        const val FEEDER_UP_ID = 8

        const val STOPPER_ID = 6

        const val FEEDER_MAX_POWER = 1.0
        const val FEEDER_HOLD_POWER = 0.1
        const val FEEDER_INTAKE_POWER = 0.35
        const val FEEDER_AUTO_INTAKE_POWER = 0.3
        const val STOPPER_MAX_POWER = 0.9
        const val STOPPER_HOLD_POWER = 0.45
    }
}
