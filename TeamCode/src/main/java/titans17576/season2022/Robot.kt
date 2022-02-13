package titans17576.season2022

import com.qualcomm.robotcore.hardware.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.OP
import titans17576.ftcrc7573.TouchSensor7573
import kotlin.math.absoluteValue

//Variables for holding the cube
val CLAMP_POS_HOLD_CUBE: Double = 0.47
val CLAMP_POS_HOLD_BALL: Double = 0.43
val CLAMP_POS_RELEASE: Double = 0.25
val CLAMP_POS_NEUTRAL: Double = 0.33

//Lift heights
val lift_lvl1 = 0;
val lift_lvl2 = -400;
val lift_lvl3 = -750;

//Outtake arm positions
val OUTTAKE_POSITION_INSIDE: Double = 0.01
val OUTTAKE_POSITION_VERTICAL: Double = 0.34
val OUTTAKE_POSITION_OUTSIDE: Double = 0.57
val OUTTAKE_POSITION_OUTSIDE_HORIZONTAL: Double = 0.67


val ARM_LOADING: Int = 0;
val ARM_TRANSITION_RISING = 200;
val ARM_TRANSITION_LOWERING = 500;
val ARM_LEVEL_1: Int = 1090;
val ARM_LEVEL_2: Int = 522;
val ARM_LEVEL_3: Int = 775;
val ARM_LEVEL_MAX: Int = 1000;
val ARM_POWER_RESET: Double = -0.325;
val ARM_POWER_COMMAND = 0.55

val BUCKET_LOADING = 0.21
val BUCKET_TRANSITION_RISING = 0.17
val BUCKET_TRANSITION_LOWERING = 0.14
val BUCKET_DUMP = 0.73
val BUCKET_BALANCED = 0.05
val BUCKET_TRANSITION_FALLING = 0.07

val TSE_RAISED = 0.0
val TSE_LOWERED = 0.0

val CAROUSEL_MAXPOW = 0.6

public lateinit var R: Robot

public class Robot() {

    val left_front = OP.hardwareMap.get("left_front") as DcMotorEx
    val left_back = OP.hardwareMap.get("left_back") as DcMotorEx
    val right_front = OP.hardwareMap.get("right_front") as DcMotorEx
    val right_back = OP.hardwareMap.get("right_back") as DcMotorEx

    val intake_motor = OP.hardwareMap.get("intake") as DcMotorEx

    val outtake_arm = OP.hardwareMap.get("outtake_arm") as DcMotorEx
    val outtake_bucket = OP.hardwareMap.get("outtake_bucket") as Servo
    //val outtake_distance_sensor = OP.hardwareMap.get("outtake_distance_sensor") as DistanceSensor
    val outtake_limit_switch = TouchSensor7573(OP.hardwareMap.get("outtake_arm_limit"))
    val carousel = OP.hardwareMap.get("carousel") as DcMotorEx

    init {
        left_front.direction = DcMotorSimple.Direction.FORWARD
        left_back.direction = DcMotorSimple.Direction.FORWARD
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_back.direction = DcMotorSimple.Direction.REVERSE
        outtake_arm.direction = DcMotorSimple.Direction.REVERSE

        left_front.targetPosition = 0;
        left_back.targetPosition = 0;
        right_front.targetPosition = 0;
        right_back.targetPosition = 0;


        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        left_front.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        left_back.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        right_front.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        right_back.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        R = this
    }

    suspend fun automation_allowed(): Boolean {
        return OP.gamepad2.left_stick_x > -0.75
    }

    val outtake_commander: Semaphore = Semaphore(1)
    suspend fun command_outtake(target: Int, bucket_position: Double? = null, delay_ms: Long = 400, threshold: Int = 10, predicate: () -> Boolean = { true }) {
        outtake_commander.acquire()
        try {
            OP.log("Arm Position Target", target, -1);
            if (!automation_allowed()) return;
            if ((outtake_arm.currentPosition - target).absoluteValue > threshold) {
                if (R.outtake_limit_switch.is_touched) {
                    R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                }

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

                val is_rising =
                    target > ARM_TRANSITION_RISING && outtake_arm.currentPosition <= ARM_TRANSITION_RISING
                val is_falling =
                    target < ARM_TRANSITION_LOWERING && outtake_arm.currentPosition >= ARM_TRANSITION_LOWERING
                if (is_rising || is_falling) {
                    if (is_falling) {
                        outtake_bucket.position = BUCKET_TRANSITION_FALLING
                        delay(delay_ms)
                    }
                    val transition_arm_target = if (is_rising) {
                        ARM_TRANSITION_RISING
                    } else {
                        ARM_TRANSITION_LOWERING
                    }
                    val transition_bucket_target = if (is_rising) {
                        BUCKET_TRANSITION_RISING
                    } else {
                        BUCKET_TRANSITION_LOWERING
                    }

                    outtake_arm.targetPosition = transition_arm_target
                    outtake_arm.mode = DcMotor.RunMode.RUN_TO_POSITION
                    outtake_arm.power = ARM_POWER_COMMAND
                    if (!wait_for_arm()) return;
                    delay(delay_ms)
                    if (OP.stop_event.has_fired() || !predicate()) return;
                    outtake_bucket.position = transition_bucket_target
                    delay(delay_ms)
                }
                outtake_arm.targetPosition = target
                outtake_arm.mode = DcMotor.RunMode.RUN_TO_POSITION
                outtake_arm.power = ARM_POWER_COMMAND
                if (!wait_for_arm()) return;
                if (OP.stop_event.has_fired() || !predicate()) return;
            }
            val real_bucket_position: Double
            if (bucket_position == null) {
                if (target < ARM_TRANSITION_RISING) real_bucket_position = BUCKET_LOADING
                else real_bucket_position = BUCKET_BALANCED
            } else {
                real_bucket_position = bucket_position
            }
            outtake_bucket.position = real_bucket_position
            delay(delay_ms)
            OP.log("Arm Position Target", target, 1000);
            OP.log("Arm Position Current", outtake_arm.currentPosition, 1000);
        } finally {
            outtake_commander.release()
        }
    }

    suspend fun reset_outtake(bucket_controls: Boolean = true) {
        R.outtake_arm.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.outtake_arm.power = ARM_POWER_RESET
        var reset = true;
        while (!R.outtake_limit_switch.is_touched && !OP.stop_event.has_fired() && automation_allowed()) {
            if (bucket_controls) R.outtake_bucket.position += OP.gamepad2.left_stick_y / 200;
            if (OP.gamepad2.right_stick_y.absoluteValue > 0.25) {
                reset = false;
                OP.gamepad1.rumble(200);
                OP.gamepad2.rumble(200);
                break;
            }
            yield();
        }
        if (reset) R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }
}