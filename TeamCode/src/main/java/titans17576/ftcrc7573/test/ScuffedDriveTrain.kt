package titans17576.ftcrc7573.test

import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode

class ScuffedDriveTrain(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op;
    val left_front = op.hardwareMap["left_front"] as DcMotorEx
    val left_back = op.hardwareMap["left_back"] as DcMotorEx
    val right_front = op.hardwareMap["right_front"] as DcMotorEx
    val right_back = op.hardwareMap["right_back"] as DcMotorEx
    val lift_left = op.hardwareMap["lift_left"] as DcMotorEx
    val lift_right = op.hardwareMap["lift_right"] as DcMotorEx
    val intake = op.hardwareMap["intake"] as DcMotorEx

    init {
        left_front.direction = DcMotorSimple.Direction.REVERSE
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_back.direction = DcMotorSimple.Direction.REVERSE
        
        lift_right.direction = DcMotorSimple.Direction.REVERSE
    }
   override suspend fun op_mode() {
   op.launch {
       drive_subsystem()
     }
       op.launch { peripherals_subsystem() }
   }
    suspend fun drive_subsystem(){
        op.start_event.await()
        op.while_live{

            op.telemetry.addData("LEFT", op.gamepad1.left_stick_y)
            op.telemetry.addData("RIGHT", op.gamepad1.right_stick_y)
            println("Left " + op.gamepad1.left_stick_y)
            println("Right " + op.gamepad1.right_stick_y)
            left_back.power = -op.gamepad1.left_stick_y.toDouble()
            left_front.power = -op.gamepad1.left_stick_y.toDouble()
            right_front.power = -op.gamepad1.right_stick_y.toDouble()
            right_back.power = -op.gamepad1.right_stick_y.toDouble()
            op.telemetry.update()
        }
    }

    suspend fun peripherals_subsystem() {
        op.start_event.await()
        op.while_live {
            if (op.gamepad2.left_bumper) intake.power = -1.0
            else if (op.gamepad2.right_bumper) intake.power = 1.0
            else intake.power = 0.0

            lift_left.power = op.gamepad2.right_stick_y.toDouble()
            lift_right.power = op.gamepad2.right_stick_y.toDouble()
        }
    }
}