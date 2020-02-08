import com.revrobotics.ControlType
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import java.lang.Math.abs

object Shooter : Subsystem {
    private val masterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(
            Constants.Shooter.MASTER_SPARKMAX_ID)

    private val slaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Shooter.SLAVE_SPARKMAX_ID, masterSparkMax, invertToMaster = true)

    private val feedForward = SimpleMotorFeedforward(Constants.Shooter.SHOOTER_KS, Constants.Shooter.SHOOTER_KV)

    var idleTime = 0.0
    var spinTime = 0.0

    var openLoopPower: Double = 0.0
        set(value) {
            masterSparkMax.set(value)
        }
    var velocityPower: Double = 0.0
        set(value) {
            masterSparkMax.set(ControlType.kVelocity, value)
        }
    fun setOpenLoop() {
        masterSparkMax.set(openLoopPower)
    }

    enum class State {
        SHOOTING, IDLE
    }

    private var currentSpeed = 0.0
    var shooterState = State.IDLE

    var shooterReady = false

    init {
        masterSparkMax.pidController.setP(Constants.Shooter.SHOOTER_PID.kP)
        masterSparkMax.pidController.setI(Constants.Shooter.SHOOTER_PID.kI)
        masterSparkMax.pidController.setD(Constants.Shooter.SHOOTER_PID.kD)
        masterSparkMax.pidController.setFF(Constants.Shooter.SHOOTER_PID.kF)
        masterSparkMax.pidController.setIZone(Constants.Shooter.SHOOTER_PID.iZone.toDouble())

        masterSparkMax.pidController.setOutputRange(Constants.Shooter.SHOOTER_KS, 1.0, 0)

        masterSparkMax.setSmartCurrentLimit(0)

        masterSparkMax.inverted = true
    }

    fun setOpenLoop(power: Double) {
        masterSparkMax.set(power)
    }
    fun setVelocity(velocity: Double, ff: Double) {
//        println("vel: $velocity, actual: ${masterSparkMax.encoder.velocity} ff: $ff")
        masterSparkMax.set(ControlType.kVelocity, velocity, 0, ff)
    }

    @Synchronized
    override fun onStart(timestamp: Double) {
        setOpenLoop(0.0)
    }

    @Synchronized
    override fun onStop(timestamp: Double) {
       setOpenLoop(0.0)
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        currentSpeed = masterSparkMax.encoder.velocity

        when (shooterState) {
            State.IDLE -> {
                setOpenLoop(0.0)
                idleTime = timestamp
            }
            State.SHOOTING -> {
                setVelocity(Constants.Shooter.TARGET_SPEED, feedForward.calculate(Constants.Shooter.TARGET_SPEED))
                shooterReady = abs(currentSpeed - Constants.Shooter.TARGET_SPEED) <= Constants.Shooter.SPEED_THRESHOLD
                if (shooterReady && spinTime == 0.0) {
                    spinTime = idleTime - timestamp
                }
                println(spinTime)
            }
        }

//        println(currentSpeed)
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        // not sure if this should go here
        HelixLogger.addSource("Shooter Master Motor Power") { masterSparkMax.outputCurrent }
        HelixLogger.addSource("Shooter Slave Motor Power") { slaveSparkMax.outputCurrent }
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("shooter/shooterState", shooterState.toString())
        SmartDashboard.putNumber("shooter/currentSpeed", currentSpeed)
    }

    override fun zeroSensors() {}
}
