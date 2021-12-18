package titans17576.ftcrc7573.test

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode

class MotorTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op;
    val left_front = op.hardwareMap["left_front"] as DcMotorEx
    val left_back = op.hardwareMap["left_back"] as DcMotorEx
    val right_front = op.hardwareMap["right_front"] as DcMotorEx
    val right_back = op.hardwareMap["right_back"] as DcMotorEx
    init {
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_back.direction = DcMotorSimple.Direction.REVERSE
    }
    override suspend fun op_mode() {
        op.launch {
            drive_subsystem()
        }
    }
    suspend fun drive_subsystem(){
        op.start_signal.await()
        op.while_live{

            left_back.power = 0.0
            left_front.power = 0.0
            right_front.power = 0.0
            right_back.power = 0.0

            if (op.gamepad1.a) left_back.power = 1.0
            if (op.gamepad1.b) left_front.power = 1.0
            if (op.gamepad1.x) right_back.power = 1.0
            if (op.gamepad1.y) right_front.power = 1.0

        }
    }
}

class ReverseMotorTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op;
    val left_front = op.hardwareMap["left_front"] as DcMotorEx
    val left_back = op.hardwareMap["left_back"] as DcMotorEx
    val right_front = op.hardwareMap["right_front"] as DcMotorEx
    val right_back = op.hardwareMap["right_back"] as DcMotorEx
    init {
        left_front.direction = DcMotorSimple.Direction.REVERSE
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_back.direction = DcMotorSimple.Direction.REVERSE
    }
    override suspend fun op_mode() {
        op.launch {
            drive_subsystem()
        }
    }
    suspend fun drive_subsystem(){
        op.start_signal.await()
        op.while_live{
            op.telemetry.addData("left_front", left_front.currentPosition)
            op.telemetry.addData("left_back", left_back.currentPosition)
            op.telemetry.addData("right_front", right_front.currentPosition)
            op.telemetry.addData("right_back", right_back.currentPosition)
            op.telemetry.update()
        }
    }
}

class BadMotorTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op

    val left_front = op.hardwareMap["left_front"] as DcMotorEx

    override suspend fun op_mode() {
        op.launch { drive_subsystem() }
        op.launch { toggle_subsystem() }
    }

    suspend fun drive_subsystem() {
        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        op.start_signal.await()
        left_front.mode = DcMotor.RunMode.RUN_USING_ENCODER
        op.while_live {
            left_front.power = op.gamepad1.left_stick_y.toDouble()
            op.telemetry.addData("Power: ", left_front.power)
            op.telemetry.addData("Encoder: ", left_front.currentPosition)
        }
    }

    suspend fun toggle_subsystem() {
        op.start_signal.await()
        op.while_live {
            if (op.gamepad1.y) {
                if (left_front.direction == DcMotorSimple.Direction.FORWARD) left_front.direction = DcMotorSimple.Direction.REVERSE
                else left_front.direction = DcMotorSimple.Direction.FORWARD
            }
        }
    }
}