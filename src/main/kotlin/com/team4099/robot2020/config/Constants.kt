package com.team4099.robot2020.config

import com.team4099.lib.config.PIDGains
import com.team4099.lib.config.ServoMotorSubsystemConfig
import com.team4099.lib.config.ServoMotorSubsystemMotionConstraints
import com.team4099.lib.motorcontroller.CTREMotorControllerFactory

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
        const val LEFT_MASTER_ID = 10
        const val LEFT_SLAVE_1_ID = 9

        const val RIGHT_MASTER_ID = 5
        const val RIGHT_SLAVE_1_ID = 6

        const val STATUS_FRAME_PERIOD_MS = 5

        const val VOLTAGE_COMP_LEVEL = 12.0

        const val MAX_LEFT_OPEN_LOOP_POWER = 1.0
        const val MAX_RIGHT_OPEN_LOOP_POWER = 1.0

        const val CLOSED_LOOP_RAMP = 0.0

        const val OUTPUT_POWER_DEADBAND = 0.04

        const val CONTINUOUS_CURRENT_LIMIT = 40

        const val ENCODER_RESOLUTION = 2048
        const val NATIVE_UNITS_PER_REV = ENCODER_RESOLUTION / 0.08665966387

        const val WHEEL_DIAMETER_METERS = 0.1524
        const val WHEEL_TRACK_WIDTH_INCHES = 27.0
        const val WHEEL_TRACK_WIDTH_METERS = WHEEL_TRACK_WIDTH_INCHES * 0.0254
        const val WHEEL_GAIN = 0.05
        const val WHEEL_NON_LINEARITY = 0.05

        const val TRACK_SCRUB_FACTOR = 1.1

        const val GYRO_BAD_VALUE = -31337.0

        const val MAX_VEL_METERS_PER_SEC = 3.5
        const val MAX_ACCEL_METERS_PER_SEC_SQ = 3.5

        object Characterization {
            const val LEFT_KV_FORWARD = 0.4993
            const val RIGHT_KV_FORWARD = 0.5412

            const val LEFT_KA_FORWARD = 0.0468
            const val RIGHT_KA_FORWARD = 0.0601

            const val LEFT_V_INTERCEPT_FORWARD = 0.1879
            const val RIGHT_V_INTERCEPT_FORWARD = 0.1364

            const val LEFT_KV_REVERSE = 0.4987
            const val RIGHT_KV_REVERSE = 0.5194

            const val LEFT_KA_REVERSE = 0.0372
            const val RIGHT_KA_REVERSE = 0.0644

            const val LEFT_V_INTERCEPT_REVERSE = -0.1856
            const val RIGHT_V_INTERCEPT_REVERSE = -0.2003
        }

        object Gains {
            const val RAMSETE_B = 2.0
            const val RAMSETE_ZETA = 0.7

            const val LEFT_KP = 0.0000 // .1 * 1500 / 70
            const val LEFT_KI = 0.0000
            const val LEFT_KD = 0.0000
            const val LEFT_KF = 0.0000 // 1023.0 / 2220.0

            const val RIGHT_KP = 0.0000 // .1 * 1023 / 70
            const val RIGHT_KI = 0.0000
            const val RIGHT_KD = 0.0000
            const val RIGHT_KF = 0.0000 // 1023.0 / 4420.0
        }
    }


    object Climber : ServoMotorSubsystemConfig(
            CTREMotorControllerFactory.defaultConfiguration,
            "CLIMBER",
            "inches",
            PIDGains(0, 1.0, 0.0, 0.0, 0),
            PIDGains(1, 1.0, 0.0, 0.0, 0),
            0.0,
            ServoMotorSubsystemMotionConstraints(-20.0, 90.0, 90.0, 90.0, 0),
            0.0,
            1024
    ) {
        const val MASTER_ID = 9
        const val SLAVE_ID = 10

        const val OPERATOR_CONTROL_VEL = 90.0

    object Shooter {
        const val MASTER_SPARKMAX_ID = 0
        const val SLAVE_SPARKMAX_ID = 1

//        const val P_VALUE = 0.0
//        const val I_VALUE = 0.0
//        const val D_VALUE = 0.0
//        const val F_VALUE = 0.0
        val SHOOTER_PID = PIDGains(0, 1.0, 1.0, 1.0, 0)

        const val targetSpeed = 0.0
        const val speedThreshold = 0.0
    }

        enum class ClimberPosition(val position: Double) {
            DOWN(0.0),
            UP(45.0)
        }
    }

    object Intake {
        const val INTAKE_SPARK_MAX_ID = 69

        object Shooter {
            // change these later
            const val MASTER_SPARKMAX_ID = 0
            const val SLAVE_SPARKMAX_ID = 1

            // set later
            const val P_VALUE = 0.0
            const val I_VALUE = 0.0
            const val D_VALUE = 0.0
            const val F_VALUE = 0.0
        }

        object Looper {
            const val LOOPER_DT = 0.02 // 50 Hz
        }

        object Autonomous {
            const val AUTON_DT = 0.02 // 50 Hz
            const val DEFAULT_MODE_NAME = "Stand Still"
            const val DEFAULT_DELAY = 0.0

            const val TURN_POWER = 0.5
            const val SLOW_TURN_POWER = 0.2

            const val FORWARD_POWER = 1.0
            const val SLOW_FORWARD_POWER = 0.6
            const val FORWARD_MAX_TIME_SECONDS = 3
            const val FORWARD_CORRECTION_KP = 0.01
            const val FORWARD_GIVE_UP_ANGLE = 30.0
        }

        object Dashboard {
            const val ALLIANCE_COLOR_KEY = "dashboard/allianceColor"

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
                PIDGains(0, 1.0, 0.0, 0.0, 0),
                PIDGains(1, 1.0, 0.0, 0.0, 0),
                -90.0,
                ServoMotorSubsystemMotionConstraints(-20.0, 90.0, 90.0, 90.0, 0),
                0.0,
                1024
        ) {
            const val MASTER_ID = 4
            const val SLAVE1_ID = 5

            const val OPERATOR_CONTROL_VEL = 90.0

            enum class WristPosition(val position: Double) {
                HORIZONTAL(0.0),
                VERTICAL(90.0)
            }
        }

        object Feeder {
            const val FEEDER_IN_MASTER_ID = 11
            const val FEEDER_IN_SLAVE_ID = 12

            const val FEEDER_OUT_ID = 13

            const val FEEDER_MAX_POWER = 1.0
            const val FEEDER_HOLD_POWER = 0.1
        }
    }
}
