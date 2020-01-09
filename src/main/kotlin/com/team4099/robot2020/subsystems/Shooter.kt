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
    private val slaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Shooter.SLAVE_SPARKMAX_ID, masterSparkMax)

    enum class State {
        SHOOTING, IDLE, ACCELERATING
    }

    private var currentSpeed = 0.0
    var shooterState = State.IDLE

    init {
        masterSparkMax.pidController.setP(Constants.Shooter.P_VALUE)
        masterSparkMax.pidController.setI(Constants.Shooter.I_VALUE)
        masterSparkMax.pidController.setD(Constants.Shooter.D_VALUE)
        masterSparkMax.pidController.setFF(Constants.Shooter.F_VALUE)

        masterSparkMax.setSmartCurrentLimit(0)

        slaveSparkMax.inverted = false
        // slaveSparkMax.setInverted(false)
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
        if (!(shooterState == State.IDLE)) {
            if (abs(currentSpeed - Constants.Shooter.targetSpeed) <= Constants.Shooter.speedThreshold) {
                shooterState = State.SHOOTING
            } else {
                shooterState = State.ACCELERATING
            }
        }
        when (shooterState) {
            State.IDLE -> {
                setOpenLoop(0.0)
            }
            State.SHOOTING, State.ACCELERATING -> {
                setVelocity(Constants.Shooter.targetSpeed)
            }
        }
    }

    override fun checkSystem() {
    }

    override fun registerLogging() {
        // not sure if this should go here
        HelixLogger.addSource("Shooter master motor power") { masterSparkMax.outputCurrent }
        HelixLogger.addSource("Shooter slave motor power") { slaveSparkMax.outputCurrent }
    }

    override fun outputTelemetry() {
        SmartDashboard.putString("shooter/shooterState", shooterState.toString())
        SmartDashboard.putNumber("shooter/currentSpeed", currentSpeed)
    }

    override fun zeroSensors() {
    }
}
