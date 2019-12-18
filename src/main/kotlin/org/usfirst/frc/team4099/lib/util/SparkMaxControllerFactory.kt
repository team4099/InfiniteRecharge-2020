package org.usfirst.frc.team4099.lib.util

import com.revrobotics.CANError
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel
import com.revrobotics.ControlType
import org.usfirst.frc.team4099.lib.util.conversions.LazySparkMax
import edu.wpi.first.wpilibj.Timer


object SparkMaxControllerFactory {
    class Configuration {
        var ENABLE_BRAKE = CANSparkMax.IdleMode.kCoast
        var INVERTED = false

        var RAMP_RATE = 0.0

        var STATUS_FRAME_0_RATE_MS = 10
        var STATUS_FRAME_1_RATE_MS = 1000
        var STATUS_FRAME_2_RATE_MS = 1000

        var ENABLE_VOLTAGE_COMPENSATION = false
        var NOMINAL_VOLTAGE = 12.0
    }

    private val kDefaultConfiguration = Configuration()
    private val kSlaveConfiguration = Configuration()

    init {
        kSlaveConfiguration.STATUS_FRAME_0_RATE_MS = 1000
        kSlaveConfiguration.STATUS_FRAME_1_RATE_MS = 1000
        kSlaveConfiguration.STATUS_FRAME_2_RATE_MS = 1000
    }

    fun createDefaultSparkMax(id: Int): LazySparkMax {
        return createSparkMax(id, kDefaultConfiguration)
    }

    fun createPermanentSlaveSparkMax(id: Int, master: CANSparkMax) : LazySparkMax {
        val sparkMax : LazySparkMax = createSparkMax(id, kSlaveConfiguration)
        sparkMax.follow(master)
        return sparkMax
    }

    fun createSparkMax(id: Int, config: Configuration): LazySparkMax {
        // Apparently to wait for CAN bus bandwidth to clear up
        Timer.delay(0.25)
        val sparkMax : LazySparkMax = LazySparkMax(id).apply {
            set(ControlType.kDutyCycle, 0.0)

            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus0, config.STATUS_FRAME_0_RATE_MS)
            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus1, config.STATUS_FRAME_1_RATE_MS)
            setPeriodicFramePeriod(CANSparkMaxLowLevel.PeriodicFrame.kStatus2, config.STATUS_FRAME_2_RATE_MS)

            clearFaults()

            setIdleMode(config.ENABLE_BRAKE)
            setInverted(config.INVERTED)
            setOpenLoopRampRate(config.RAMP_RATE) //assumes same ramp rate for closed and open loop
            setClosedLoopRampRate(config.RAMP_RATE)

            if (config.ENABLE_VOLTAGE_COMPENSATION)
                enableVoltageCompensation(config.NOMINAL_VOLTAGE)
            else
                disableVoltageCompensation()
        }

        return sparkMax
    }
}