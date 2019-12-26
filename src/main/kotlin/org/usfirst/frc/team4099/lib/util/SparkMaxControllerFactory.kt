package org.usfirst.frc.team4099.lib.util

import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import com.revrobotics.ControlType
import edu.wpi.first.wpilibj.Timer

object SparkMaxControllerFactory {
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

    fun createDefaultSparkMax(id: Int): LazySparkMax {
        return createSparkMax(id, defaultConfiguration)
    }

    fun createPermanentSlaveSparkMax(id: Int, master: CANSparkMax): LazySparkMax {
        val sparkMax: LazySparkMax = createSparkMax(id, slaveConfiguration)
        sparkMax.follow(master)
        return sparkMax
    }

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