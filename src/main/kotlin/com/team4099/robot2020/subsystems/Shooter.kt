import com.revrobotics.ControlType
import com.team4099.lib.motorcontroller.SparkMaxControllerFactory
import com.team4099.lib.subsystem.Subsystem
import com.team4099.robot2020.config.Constants
import java.lang.Math.abs

object Shooter : Subsystem {

    private val masterSparkMax = SparkMaxControllerFactory.createDefaultSparkMax(
            Constants.Shooter.MASTER_SPARKMAX_ID)
    private val slaveSparkMax = SparkMaxControllerFactory.createPermanentSlaveSparkMax(
            Constants.Shooter.SLAVE_SPARKMAX_ID, masterSparkMax)

    // set these later
    private var targetSpeed = 0.0
    private var currentSpeed = 0.0
    private var speedThreshold = 0.0

    enum class State {
        SHOOTING, IDLE, ACCELERATING
    }

    private var currentState = State.IDLE

    init {
        masterSparkMax.pidController.setP(Constants.Shooter.P_VALUE)
        masterSparkMax.pidController.setI(Constants.Shooter.I_VALUE)
        masterSparkMax.pidController.setD(Constants.Shooter.D_VALUE)
        masterSparkMax.pidController.setFF(Constants.Shooter.F_VALUE)

        // change later
        masterSparkMax.setSmartCurrentLimit(0)

        slaveSparkMax.inverted = false
        // slaveSparkMax.setInverted(false)
    }

    fun setOpenLoop(speed: Double) {
        masterSparkMax.set(speed)
    }
    fun setVelocity(speed: Double) {
        masterSparkMax.set(ControlType.kSmartVelocity, speed)
    }

    @Synchronized
    override fun onStart(timestamp: Double) {
    }

    @Synchronized
    override fun onStop(timestamp: Double) {
       // setOpenLoop(DriveSignal.IDLE)
    }

    @Synchronized
    override fun onLoop(timestamp: Double, dT: Double) {
        currentSpeed = masterSparkMax.encoder.velocity
        if (!(currentState == State.IDLE)) {
            if (abs(currentSpeed - targetSpeed) <= speedThreshold) {
                currentState = State.SHOOTING
            }
            else {
                currentState = State.ACCELERATING
            }
        }
        when (currentState) {
            State.IDLE -> {
                setOpenLoop(0.0)
            }
            State.SHOOTING, State.ACCELERATING -> {
                setVelocity(targetSpeed)
            }
        }
    }

    override fun checkSystem() {
    }
    override fun registerLogging() {
    }
    override fun outputTelemetry() {
    }
    override fun zeroSensors() {
    }

}