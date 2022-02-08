package titans17576.season2022

import com.qualcomm.robotcore.hardware.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.OP
import titans17576.ftcrc7573.TouchSensor7573

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


val ARM_INSIDE: Int = 0;
val ARM_BUCKET_VROOM: Int = 330;
val ARM_LEVEL_3: Int = 865;
val ARM_LEVEL_MAX: Int = 1000;
val ARM_INSIDE_POWER: Double = -0.255;

val BUCKET_POSITION_LOADING = 0.326
val BUCKET_POSITION_DUMP = 0.74
val BUCKET_BALANCED = 0.05

lateinit var R: Robot

class Robot() {

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
        R = this
    }
}