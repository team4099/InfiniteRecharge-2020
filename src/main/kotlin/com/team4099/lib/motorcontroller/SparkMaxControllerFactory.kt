package com.team4099.lib.motorcontroller

import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import com.revrobotics.ControlType
import edu.wpi.first.wpilibj.Timer

/**
 * Create Spark Max objects with consistent default configurations.
 */
object SparkMaxControllerFactory {
    /**
     * Represents the configuration of a Spark Max
     */
    @Suppress("MagicNumber")
    data class Configuration(
        var idleMode: CANSparkMax.IdleMode = CANSparkMax.IdleMode.kCoast,
        var inverted: Boolean = false,

        var openLoopRampRate: Double = 0.0,
        var closedLoopRampRate: Double = 0.0,

        var statusFrame0RateMs: Int = 10,
        var statusFrame1RateMs: Int = 10,
        var statusFrame2RateMs: Int = 20,

        var enableVoltageCompensation: Boolean = false,
        var nominalVoltage: Double = 12.0,

        var delayBeforeCreation: Double = 0.25
    )

    private val defaultConfiguration = Configuration()

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
    fun createPermanentSlaveSparkMax(
        id: Int,
        master: CANSparkMax,
        config: Configuration = defaultConfiguration,
        invertToMaster: Boolean = false
    ): LazySparkMax {
        val slaveConfiguration = config.copy(
            statusFrame0RateMs = 1000,
            statusFrame1RateMs = 1000,
            statusFrame2RateMs = 1000
        )
        val sparkMax: LazySparkMax = createSparkMax(id, slaveConfiguration)
        sparkMax.follow(master, invertToMaster)
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
        Timer.delay(config.delayBeforeCreation)

        return LazySparkMax(id).apply {
            restoreFactoryDefaults()
            set(ControlType.kDutyCycle, 0.0)

            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, config.statusFrame0RateMs)
            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus1, config.statusFrame1RateMs)
            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus2, config.statusFrame2RateMs)

            clearFaults()

            idleMode = config.idleMode
            inverted = config.inverted

            openLoopRampRate = config.openLoopRampRate
            closedLoopRampRate = config.closedLoopRampRate

            if (config.enableVoltageCompensation) {
                enableVoltageCompensation(config.nominalVoltage)
            } else {
                disableVoltageCompensation()
            }
        }
    }
}
