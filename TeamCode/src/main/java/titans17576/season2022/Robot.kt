package titans17576.season2022

import com.qualcomm.hardware.rev.Rev2mDistanceSensor
import com.qualcomm.robotcore.hardware.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.OP
import titans17576.ftcrc7573.TouchSensor7573
import kotlin.math.absoluteValue

//Arm positions in encoder ticks
val ARM_LOADING: Int = 0;
val ARM_TRANSITION_RISING = 475;
val ARM_TRANSITION_LOWERING = 475;
val ARM_LEVEL_1: Int = 975;
val ARM_LEVEL_2: Int = 522;
val ARM_LEVEL_3: Int = 800;
val ARM_LEVEL_MAX: Int = 1000;
//Arm motor powers
val ARM_POWER_RESET: Double = -0.325;
val ARM_POWER_COMMAND = 0.7
val ARM_POWER_COMMAND_SLOW = 0.3
val ARM_POWER_CORRECT = 0.7

//Bucket servo positions
val BUCKET_LOADING = 0.5
val BUCKET_TRANSITION_RISING = 0.13
val BUCKET_TRANSITION_LOWERING = 0.13
val BUCKET_DUMP = 0.85
val BUCKET_DUMP_MORE = 1.0
val BUCKET_BALANCED = 0.0
val BUCKET_TRANSITION_FALLING = 0.07

//Bucket clamp servo positions
val BUCKET_CLAMP_CLAMPING = 0.02
val BUCKET_CLAMP_RELEASE = 0.55

//TSE arm servo positions
val TSE_RAISED = 0.36
val TSE_DEPLOY = 0.5
val TSE_LOWERED = 0.85
val TSE_INSIDE = 0.05

//Carousel motor powers
val CAROUSEL_MAXPOW = 0.6
val CAROUSEL_MINPOW = 0.3
val DISTANCE_SENSOR_POSITION = 8.5;

lateinit var R: Robot

class Robot() {
    //Drive train motors
    val left_front = OP.hardwareMap.get("left_front") as DcMotorEx
    val left_back = OP.hardwareMap.get("left_back") as DcMotorEx
    val right_front = OP.hardwareMap.get("right_front") as DcMotorEx
    val right_back = OP.hardwareMap.get("right_back") as DcMotorEx

    //Intake peripherals
    val intake_motor = OP.hardwareMap.get("intake") as DcMotorEx
    //Intake usage control
    val intake_commander = Semaphore(1)

    //Outtake peripherals
    val outtake_arm = OP.hardwareMap.get("outtake_arm") as DcMotorEx
    val outtake_bucket = OP.hardwareMap.get("outtake_bucket") as Servo
    val outtake_distance_sensor = OP.hardwareMap.get("outtake_distance_sensor") as Rev2mDistanceSensor
    val outtake_limit_switch = TouchSensor7573(OP.hardwareMap.get("outtake_arm_limit"))
    val outtake_clamp = OP.hardwareMap.get("outtake_clamp") as Servo
    //Semaphore to control access to outtake peripherals
    val outtake_commander: Semaphore = Semaphore(1)

    //Carousel peripherals
    val carousel = OP.hardwareMap.get("carousel") as DcMotorEx

    //TSE peripherals
    val tse = OP.hardwareMap.get("tse") as Servo
    //Semaphore to control access to outtake peripherals
    val tse_commander = Semaphore(1)

    init {
        //Configure and reset all drive motors
        left_front.direction = DcMotorSimple.Direction.FORWARD
        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_front.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        left_back.direction = DcMotorSimple.Direction.FORWARD
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        right_back.direction = DcMotorSimple.Direction.REVERSE
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        //Configure and reset the outtake motor
        outtake_arm.direction = DcMotorSimple.Direction.REVERSE
        outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        outtake_arm.setPositionPIDFCoefficients(8.5)

        //Configure, initialize, and disable the TSE arm servo
        //tse.direction = Servo.Direction.REVERSE
        tse.direction = Servo.Direction.FORWARD
        tse.position = TSE_RAISED
        attempt_servo_pwm(tse, false)

        //Set the global R variable
        R = this
    }

    /**
     * @return whether outtake automation "is allowed", aka whether manual controls are disabled
     */
    suspend fun automation_allowed(): Boolean {
        return OP.gamepad2.left_stick_x > -0.75
    }

    /**
     * Commands the outtake to a certain arm and bucket position
     * @param target - the target encoder position of the arm
     * @param bucket_position - the final bucket servo position
     * @param delay_ms - how long to wait inbetween actions
     * @param threshold - acceptable error from the target arm position
     * @param command_power - how much power to run the arm with
     * @param predicate - a boolean to check continuously, the automation will break if it returns false
     */
    suspend fun command_outtake(target: Int, bucket_position: Double? = null, delay_ms: Long = 200, threshold: Int = 10, command_power: Double = ARM_POWER_COMMAND, predicate: () -> Boolean = { true }) {
        outtake_commander.acquire()
        var alive = true
        try {
            //Wait for the arm to raech the target position within the desired threshold
            //Returns true if it succeeded, returns false it if was interrupted by the user,
            //the predicate function, or emergency controls
            suspend fun wait_for_arm(): Boolean {
                if (!automation_allowed()) return false;
                while ((outtake_arm.currentPosition - outtake_arm.targetPosition).absoluteValue > threshold) {
                    OP.log("Bucket Position", R.outtake_bucket.position, -1);
                    OP.log("Arm Position Current", R.outtake_arm.currentPosition, -1);
                    if (OP.stop_event.has_fired() || !predicate() || !automation_allowed()) return false;
                    yield();
                }
                return true;
            }

            val real_bucket_position: Double
            if (bucket_position == null) {
                if (target < ARM_TRANSITION_RISING) real_bucket_position = BUCKET_LOADING
                else real_bucket_position = BUCKET_DUMP
            } else {
                real_bucket_position = bucket_position
            }

            R.outtake_arm.targetPosition = target
            R.outtake_arm.mode = DcMotor.RunMode.RUN_TO_POSITION
            R.outtake_arm.power = ARM_POWER_COMMAND
            OP.launch {
                OP.while_live {
                    if (!alive) it()
                    if (R.outtake_arm.currentPosition > ARM_TRANSITION_RISING) {
                        R.outtake_bucket.position = real_bucket_position
                        it()
                    }
                }
            }
            wait_for_arm()

            R.outtake_bucket.position = real_bucket_position
        } finally {
            alive = false
            outtake_commander.release()
        }
    }

    /**
     * Reset the outtake to it's inside position
     */
    suspend fun reset_outtake(/*bucket_controls: Boolean = true*/) {
        R.outtake_arm.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.outtake_arm.power = ARM_POWER_RESET
        while (!R.outtake_limit_switch.is_touched && !OP.stop_event.has_fired() && automation_allowed()) {
            //if (bucket_controls) R.outtake_bucket.position += OP.gamepad2.left_stick_y / 200;
            yield();
        }
        if (R.outtake_limit_switch.is_touched) R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    /**
     * Attempt to set the PWM state of the controller of a servo.
     * AKA whether the servo "does work" or not
     * @param servo - the servo to modify
     * @param new_mode - whether PWM should be enabled or not
     * @return whether the servo was modified successfully
     */
    fun attempt_servo_pwm(servo: Servo, new_mode: Boolean): Boolean {
        try {
            val servoimpl = servo as ServoImplEx
            if (new_mode) servoimpl.setPwmEnable()
            else servoimpl.setPwmDisable()
            return true;
        } catch(e: Exception) {
            println(e);
        }
        return false;
    }

    /**
     * Atempt to read the PWM status of the controller of a servo.
     * @return the status of the servo controller, or null if it was not successful
     */
    fun attempt_servo_pwm_status(servo: Servo): String? {
        try {
            val servoimpl = servo as ServoImplEx
            return servoimpl.controller.pwmStatus.toString();
        } catch (e: Exception) {
            println(e);
        }
        return null;
    }
}