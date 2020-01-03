package com.team4099.lib.motorcontroller

import com.revrobotics.CANSparkMaxLowLevel

object GenericSmartMotorControllerFactory {
    @Suppress("MagicNumber")
    class Configuration {
        var idleMode = GenericSmartMotorController.IdleMode.COAST
        var inverted = GenericSmartMotorController.InvertType.NONE

        var sensorAgreesWithInversion = true
        var timeout = 100

        var openLoopRampSecs = 0.0
        var closedLoopRampSecs = 0.0

        var peakOutputForward = 1.0
        var peakOutputReverse = -1.0

        var nominalOutputForward = 0.0
        var nominalOutputReverse = 0.0

        var neutralDeadband = 0.0

        var maxCompensatedVoltage = 12.0
        var useVoltageCompensation = false

        var selectedEncoder = GenericSmartMotorController.EncoderType.NONE

        var encoderPPR = 1
        var encoderRevsPerUnit = 1.0

        var velocityMeasPeriod = GenericSmartMotorController.VelocityMeasPeriod.MS_100
        var velocityMeasWindow = 64

        var useLimitSwitches = false
        var useSoftLimits = false

        var useForwardSoftLimit = false
        var useReverseSoftLimit = false

        var forwardSoftLimitValue = 0.0
        var reverseSoftLimitValue = 0.0

        var forwardLimitSwitchSource = GenericSmartMotorController.LimitSwitchSource.NONE
        var reverseLimitSwitchSource = GenericSmartMotorController.LimitSwitchSource.NONE

        var forwardLimitSwitchNormal = GenericSmartMotorController.LimitSwitchNormal.NORMALLY_OPEN
        var reverseLimitSwitchNormal = GenericSmartMotorController.LimitSwitchNormal.NORMALLY_OPEN

        var allowableVelocityError = 0.0

        var maxVelocityIntegralAccumulator = 0.0
        var maxPositionIntegralAccumulator = 0.0

        var velocityPeakOutput = 1.0
        var positionPeakOutput = 1.0

        var motionCruiseVelocity = 0.0
        var motionAcceleration = 0.0
        var motionSCurveStrength = 8

        var controlFramePeriodMs = 5
        var generalStatusFrameRateMs = 5
        var feedbackStatusFrameRateMs = 100
        var analogTempVbatStatusFrameMs = 100

        var continuousInputCurrentLimit = 40
        var peakInputCurrentLimit = 100
        var peakCurrentDurationMs = 500

        var continuousStatorCurrentLimit = 80
        var peakStatorCurrentLimit = 120
    }

    private val defaultConfiguration = Configuration()
    private val defaultSparkMaxConfiguration = Configuration()
    private val slaveConfiguration = Configuration()
    private val slaveSparkMaxConfiguration = Configuration()

    init {
        slaveConfiguration.controlFramePeriodMs = 1000
        slaveConfiguration.generalStatusFrameRateMs = 1000
        slaveConfiguration.feedbackStatusFrameRateMs = 1000
        slaveConfiguration.analogTempVbatStatusFrameMs = 1000

        slaveSparkMaxConfiguration.controlFramePeriodMs = 1000
        slaveSparkMaxConfiguration.generalStatusFrameRateMs = 1000
        slaveSparkMaxConfiguration.feedbackStatusFrameRateMs = 1000
        slaveSparkMaxConfiguration.analogTempVbatStatusFrameMs = 1000

        defaultSparkMaxConfiguration.selectedEncoder = GenericSmartMotorController.EncoderType.INTERNAL_QUADRATURE
        slaveSparkMaxConfiguration.selectedEncoder = GenericSmartMotorController.EncoderType.INTERNAL_QUADRATURE
    }

    fun createDefaultTalon(id: Int): GenericTalonSRX {
        return createTalon(id, defaultConfiguration)
    }

    fun createDefaultSpark(
        id: Int,
        motorType: CANSparkMaxLowLevel.MotorType = CANSparkMaxLowLevel.MotorType.kBrushless): GenericSparkMax {
        return if (motorType == CANSparkMaxLowLevel.MotorType.kBrushless) {
            createSpark(id, defaultSparkMaxConfiguration, motorType = motorType)
        } else {
            createSpark(id, defaultConfiguration, motorType = motorType)
        }
    }

    fun createPermanentSlaveTalon(id: Int, master: GenericSmartMotorController): GenericTalonSRX {
        val talon = GenericTalonSRX(id, slaveConfiguration.timeout)
        talon.master = master
        configureTalon(talon, slaveConfiguration)
        return talon
    }

    fun createPermanentSlaveVictor(id: Int, master: GenericSmartMotorController): GenericVictorSPX {
        val victor = GenericVictorSPX(id, slaveConfiguration.timeout)
        victor.master = master
        configureMotorController(victor, slaveConfiguration)
        return victor
    }

    fun createPermanentSlaveSpark(
        id: Int,
        master: GenericSmartMotorController,
        slaveMotorType: CANSparkMaxLowLevel.MotorType = CANSparkMaxLowLevel.MotorType.kBrushless): GenericSparkMax {
        var config = slaveConfiguration
        if (slaveMotorType == CANSparkMaxLowLevel.MotorType.kBrushless) {
            config = slaveSparkMaxConfiguration
        }
        val spark = GenericSparkMax(id, config.timeout, slaveMotorType)
        spark.master = master
        configureSpark(spark, config)
        return spark
    }

    fun createTalon(id: Int, config: Configuration): GenericTalonSRX {
        val talon = GenericTalonSRX(id, config.timeout)
        configureTalon(talon, config)
        return talon
    }

    fun createSpark(
        id: Int,
        config: Configuration,
        motorType: CANSparkMaxLowLevel.MotorType = CANSparkMaxLowLevel.MotorType.kBrushless): GenericSparkMax {
        val spark = GenericSparkMax(id, config.timeout, motorType)
        configureSpark(spark, config)
        return spark
    }

    private fun configureTalon(talon: GenericTalonSRX, config: Configuration) {
        configureMotorController(talon, config)
    }

    private fun configureSpark(spark: GenericSparkMax, config: Configuration) {
        configureMotorController(spark, config)
        spark.burnFlash()
    }

    @Suppress("LongMethod")
    private fun configureMotorController(gmc: GenericSmartMotorController, config: Configuration) {
        gmc.apply {
            resetToFactoryDefault()
            idleMode = config.idleMode
            inverted = config.inverted
            sensorAgreesWithInversion = config.sensorAgreesWithInversion

            openLoopRampSecs = config.openLoopRampSecs
            closedLoopRampSecs = config.closedLoopRampSecs

            peakOutputForward = config.peakOutputForward
            peakOutputReverse = config.peakOutputReverse

            nominalOutputForward = config.nominalOutputForward
            nominalOutputReverse = config.nominalOutputReverse

            neutralDeadband = config.neutralDeadband

            maxCompensatedVoltage = config.maxCompensatedVoltage
            useVoltageCompensation = config.useVoltageCompensation

            selectedEncoder = config.selectedEncoder
            encoderPPR = config.encoderPPR
            encoderRevsPerUnit = config.encoderRevsPerUnit

            velocityMeasPeriod = config.velocityMeasPeriod
            velocityMeasWindow = config.velocityMeasWindow

            useLimitSwitches = config.useLimitSwitches
            useSoftLimits = config.useSoftLimits

            useForwardSoftLimit = config.useForwardSoftLimit
            useReverseSoftLimit = config.useReverseSoftLimit

            forwardSoftLimitValue = config.forwardSoftLimitValue
            reverseSoftLimitValue = config.reverseSoftLimitValue

            setForwardLimitSwitch(config.forwardLimitSwitchSource, config.forwardLimitSwitchNormal)
            setReverseLimitSwitch(config.reverseLimitSwitchSource, config.reverseLimitSwitchNormal)

            allowableVelocityError = config.allowableVelocityError

            maxVelocityIntegralAccumulator = config.maxVelocityIntegralAccumulator
            maxPositionIntegralAccumulator = config.maxPositionIntegralAccumulator

            velocityPeakOutput = config.velocityPeakOutput
            positionPeakOutput = config.positionPeakOutput

            motionCruiseVelocity = config.motionCruiseVelocity
            motionAcceleration = config.motionAcceleration
            motionSCurveStrength = config.motionSCurveStrength

            setControlFramePeriod(GenericSmartMotorController.ControlFrame.GENERAL, config.controlFramePeriodMs)
            if (gmc !is GenericVictorSPX) {
                setStatusFramePeriod(GenericSmartMotorController.StatusFrame.PRIMARY_FEEDBACK,
                    config.feedbackStatusFrameRateMs)
            }
            setStatusFramePeriod(GenericSmartMotorController.StatusFrame.GENERAL, config.generalStatusFrameRateMs)
            setStatusFramePeriod(GenericSmartMotorController.StatusFrame.MISC, config.analogTempVbatStatusFrameMs)

            encoderPosition = 0.0

            if (gmc is GenericTalonSRX) {
                continuousInputCurrentLimit = config.continuousInputCurrentLimit
                peakInputCurrentLimit = config.peakInputCurrentLimit
                peakCurrentDurationMs = config.peakCurrentDurationMs
            } else if (gmc is GenericSparkMax) {
                continuousStatorCurrentLimit = config.continuousStatorCurrentLimit
                peakStatorCurrentLimit = config.peakStatorCurrentLimit
            }
        }
    }
}
