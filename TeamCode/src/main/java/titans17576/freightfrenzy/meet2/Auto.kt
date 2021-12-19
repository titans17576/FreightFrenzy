package titans17576.freightfrenzy.meet2

import com.qualcomm.robotcore.hardware.*
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

class Auto(op: AsyncOpMode): DeferredAsyncOpMode {
    val op = op
    val left_front = (op.hardwareMap["left_front"] as DcMotorEx)
    val left_back = op.hardwareMap["left_back"] as DcMotorEx
    val right_front = (op.hardwareMap["right_front"] as DcMotorEx)
    val right_back = op.hardwareMap["right_back"] as DcMotorEx

    val lift_left = op.hardwareMap["lift_left"] as DcMotorEx
    val lift_right = op.hardwareMap["lift_right"] as DcMotorEx
    val intake = op.hardwareMap["intake"] as DcMotorEx

    val carousel = op.hardwareMap["carousel"] as CRServo
    val outake_left = op.hardwareMap["outtake_left"] as Servo
    val outake_right = op.hardwareMap["outtake_right"] as Servo
    val intake_servo = op.hardwareMap["clamp"] as Servo
    val lock_servo = op.hardwareMap["intake_hard_stop"] as Servo

    val lift_limit = op.hardwareMap["lift_limit"] as TouchSensor

    init {
        left_front.direction = DcMotorSimple.Direction.FORWARD
        left_back.direction = DcMotorSimple.Direction.FORWARD
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_back.direction = DcMotorSimple.Direction.REVERSE

        lift_right.direction = DcMotorSimple.Direction.FORWARD
        outake_left.direction =  Servo.Direction.REVERSE

        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    suspend fun drive(ticks: Int, power: Double) {
        left_front.targetPosition = ticks
        left_back.targetPosition = ticks
        right_front.targetPosition = ticks
        right_back.targetPosition = ticks

        left_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        left_back.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_back.mode = DcMotor.RunMode.RUN_TO_POSITION

        left_front.power = power
        left_back.power = power
        right_front.power = power
        right_back.power = power

        while (left_front.isBusy && left_back.isBusy && right_front.isBusy && right_back.isBusy == true && !op.stop_signal.is_greenlight()) {
            yield()
            op.telemetry.addData("LeftFront", left_front.currentPosition)
            op.telemetry.addData("LeftBack", left_back.currentPosition)
            op.telemetry.addData("RightFront", right_front.currentPosition)
            op.telemetry.addData("RightBack", right_back.currentPosition)
        }

        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        left_front.power = 0.0
        left_back.power = 0.0
        right_front.power = 0.0
        right_back.power = 0.0

        delay(250)
    }

    suspend fun turn(ticks: Int, power: Double) {
        left_front.targetPosition = ticks
        left_back.targetPosition = ticks
        right_front.targetPosition = -ticks
        right_back.targetPosition = -ticks

        left_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        left_back.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_back.mode = DcMotor.RunMode.RUN_TO_POSITION

        left_front.power = power
        left_back.power = power
        right_front.power = power
        right_back.power = power

        while (left_front.isBusy && left_back.isBusy && right_front.isBusy && right_back.isBusy == true && !op.stop_signal.is_greenlight()) {
            yield()
            op.telemetry.addData("LeftFront", left_front.currentPosition)
            op.telemetry.addData("LeftBack", left_back.currentPosition)
            op.telemetry.addData("RightFront", right_front.currentPosition)
            op.telemetry.addData("RightBack", right_back.currentPosition)
        }

        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        left_front.power = 0.0
        left_back.power = 0.0
        right_front.power = 0.0
        right_back.power = 0.0

        delay(250)
    }

    override suspend fun op_mode() {
        op.start_signal.await()
        drive(-500, 0.2)
        turn(250,0.2)
        drive(-500, 0.2)

    }
}