import com.revrobotics.ControlType
import com.team4099.lib.logging.HelixLogger
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import java.lang.Math.abs

object Shooter : Subsystem {
    private val masterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(
            Constants.Shooter.MASTER_SPARKMAX_ID)

    var openLoopPower: Double = 0.0
        set(value) {
            masterSparkMax.set(value)
        }
    var velocityPower: Double = 0.0
        set(value) {
            masterSparkMax.set(ControlType.kVelocity,value)
        }
    fun setOpenLoop() {
        masterSparkMax.set(openLoopPower)
    }

    private val slaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Shooter.SLAVE_SPARKMAX_ID, masterSparkMax)

    enum class State {
        SHOOTING, IDLE, ACCELERATING
    }

    private var currentSpeed = 0.0
    var shooterState = State.IDLE
        set(value) {
            when(value) {
                State.IDLE -> {
                    setOpenLoop(0.0)
                }
                State.SHOOTING -> {
                    setVelocity(Constants.Shooter.TARGET_SPEED)
                }
            }
            field = value
        }

    var shooterReady: Boolean = false

    init {
        masterSparkMax.pidController.setP(Constants.Shooter.SHOOTER_PID.kP)
        masterSparkMax.pidController.setI(Constants.Shooter.SHOOTER_PID.kI)
        masterSparkMax.pidController.setD(Constants.Shooter.SHOOTER_PID.kD)
        // masterSparkMax.pidController.setFF(Constants.Shooter.SHOOTER_PID.kF)
        masterSparkMax.pidController.setIZone(Constants.Shooter.SHOOTER_PID.iZone.toDouble())

        masterSparkMax.setSmartCurrentLimit(0)

        masterSparkMax.inverted = true
        slaveSparkMax.inverted = true
    }

    fun setOpenLoop(power: Double) {
        masterSparkMax.set(power)
    }
    fun setVelocity(velocity: Double) {
        masterSparkMax.set(ControlType.kSmartVelocity, velocity)
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
        if (shooterState == State.SHOOTING) {
            shooterReady = abs(currentSpeed - Constants.Shooter.TARGET_SPEED) <= Constants.Shooter.SPEED_THRESHOLD
        }
    }

    override fun checkSystem() {}

    override fun registerLogging() {
        // not sure if this should go here
        HelixLogger.addSource("Shooter master motor power") { masterSparkMax.outputCurrent }
        HelixLogger.addSource("Shooter slave motor power") { slaveSparkMax.outputCurrent }
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("shooter/shooterState", shooterState.toString())
        SmartDashboard.putNumber("shooter/currentSpeed", currentSpeed)
    }

    override fun zeroSensors() {}
}
