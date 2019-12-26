package org.usfirst.frc.team4099.lib.util

import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import com.revrobotics.ControlType
import edu.wpi.first.wpilibj.Timer
import org.usfirst.frc.team4099.lib.util.CTREMotorControllerFactory.Configuration

/**
 * Create Spark Max objects with consistent default configurations.
 */
object SparkMaxControllerFactory {
    /**
     * Represents the configuration of a Spark Max
     */
    @Suppress("MagicNumber")
    class Configuration {
        var idleMode = CANSparkMax.IdleMode.kCoast
        var inverted = false

        var openLoopRampRate = 0.0
        var closedLoopRampRate = 0.0

        var statusFrame0RateMs = 10
        var statusFrame1RateMs = 1000
        var statusFrame2RateMs = 1000

        var enableVoltageCompensation = false
        var nominalVoltage = 12.0
    }

    private val defaultConfiguration = Configuration()
    private val slaveConfiguration = Configuration()

    init {
        slaveConfiguration.statusFrame0RateMs = 1000
        slaveConfiguration.statusFrame1RateMs = 1000
        slaveConfiguration.statusFrame2RateMs = 1000
    }

    /**
     * Create a Spark Max with the default [Configuration]
     *
     * @param id The CAN ID of the Spark Max to create.
     */
    fun createDefaultSparkMax(id: Int): LazySparkMax {
        return createSparkMax(id, defaultConfiguration)
    }

    /**
     * Create a Spark Max following another Spark Max.
     *
     * @param id The CAN ID of the Spark Max to create.
     * @param master The CAN ID of the Spark Max to follow.
     */
    fun createPermanentSlaveSparkMax(id: Int, master: CANSparkMax): LazySparkMax {
        val sparkMax: LazySparkMax = createSparkMax(id, slaveConfiguration)
        sparkMax.follow(master)
        return sparkMax
    }

    /**
     * Create a Spark Max.
     *
     * @param id The CAN ID of the Spark Max to create.
     * @param config The [Configuration] to use for this motor controller.
     */
    fun createSparkMax(id: Int, config: Configuration): LazySparkMax {
        // Apparently to wait for CAN bus bandwidth to clear up
        Timer.delay(0.25)

        return LazySparkMax(id).apply {
            set(ControlType.kDutyCycle, 0.0)

            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, config.statusFrame0RateMs)
            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus1, config.statusFrame1RateMs)
            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus2, config.statusFrame2RateMs)

            clearFaults()

            idleMode = config.idleMode
            inverted = config.inverted
            openLoopRampRate = config.openLoopRampRate //assumes same ramp rate for closed and open loop
            closedLoopRampRate = config.closedLoopRampRate

            if (config.enableVoltageCompensation) {
                enableVoltageCompensation(config.nominalVoltage)
            } else {
                disableVoltageCompensation()
            }
        }
    }
}