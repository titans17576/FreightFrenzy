package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.delay
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.OP

class Teleop : DeferredAsyncOpMode {
    val R = Robot()

    override suspend fun op_mode() {
        OP.launch { drive_subsystem() }
        OP.launch { peripherals_subsystem() }
        OP.launch { endgame_notification_subsystem() }
    }

    suspend fun drive_subsystem(){
        OP.start_signal.await()

        R.left_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.left_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.right_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.right_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        OP.while_live{

            OP.telemetry.addData("LEFT", OP.gamepad1.left_stick_y)
            OP.telemetry.addData("RIGHT", OP.gamepad1.right_stick_x)
            OP.telemetry.addData("left_front", R.left_front.currentPosition)
            OP.telemetry.addData("left_back", R.left_back.currentPosition)
            OP.telemetry.addData("right_front", R.right_front.currentPosition)
            OP.telemetry.addData("right_back", R.right_back.currentPosition)

            var drive = -OP.gamepad1.left_stick_y.toDouble()
            var turn = OP.gamepad1.right_stick_x.toDouble()
            drive *= java.lang.Math.abs(drive)
            turn *= java.lang.Math.abs(turn)
            val slow = if (OP.gamepad1.right_bumper || OP.gamepad2.right_bumper) 0.25 else if (OP.gamepad1.left_bumper) 1.0 else  0.8;

            //POV drive (not tank)
            R.left_back.power = (drive + turn) * slow
            R.left_front.power = (drive + turn) * slow
            R.right_back.power = (drive - turn) * slow
            R.right_front.power = (drive - turn) * slow

            if (OP.gamepad2.x){
                BreakDance()
            }
        }
    }

    suspend fun peripherals_subsystem() {
        /*val lock_pos_locked: Double = 0.19
        val lock_pos_go: Double = 0.36
        R.lock_servo.position = lock_pos_locked

        OP.start_signal.await()

        R.lock_servo.position = lock_pos_go

        var intake_power = 0.0
        OP.while_live {
            intake_power = 0.0
            intake_power += OP.gamepad1.right_trigger
            intake_power -= OP.gamepad1.left_trigger
            R.intake_motor.power = intake_power

            R.carousel.power = (OP.gamepad2.left_trigger - OP.gamepad2.right_trigger).toDouble()

            if (OP.gamepad2.dpad_down || OP.gamepad1.dpad_down) R.intake_clamp.position = CLAMP_POS_HOLD_CUBE
            else if (OP.gamepad2.dpad_up || OP.gamepad1.dpad_up) R.intake_clamp.position = CLAMP_POS_RELEASE
            else if (OP.gamepad2.dpad_left || OP.gamepad1.dpad_left) R.intake_clamp.position = CLAMP_POS_HOLD_BALL
            else if (OP.gamepad2.dpad_right || OP.gamepad1.dpad_right) R.intake_clamp.position = CLAMP_POS_NEUTRAL
        }*/
    }

    suspend fun endgame_notification_subsystem() {
        OP.start_signal.await()
        delay(80000)
        OP.gamepad1.rumble(0.25, 0.25, 750)
        OP.gamepad2.rumble(0.25, 0.25, 750)
        delay(5000)
        OP.gamepad1.rumble(0.5, 0.5, 1250)
        OP.gamepad2.rumble(0.5, 0.5, 1250)
        delay(5000)
        OP.gamepad1.rumble(1.0, 1.0, 350)
        OP.gamepad1.rumble(1.0, 1.0, 350)
    }

    suspend fun balance_bucket(){
        OP.start_signal.await()
        OP.while_live {
            if (OP.gamepad2.a && Math.abs(ARM_LEVEL3 - R.outtake_arm.currentPosition) <= 30){
                R.outtake_bucket.position = BUCKET_POSITION_DUMP
            }
            else{
                var current_bucket_position = (((BUCKET_BALANCED-BUCKET_POSITION_LOADING)/(ARM_LEVEL3 - ARM_INSIDE))*(R.outtake_arm.currentPosition - ARM_INSIDE)) + BUCKET_POSITION_LOADING
                if (current_bucket_position > BUCKET_POSITION_LOADING) {
                    current_bucket_position = BUCKET_POSITION_LOADING
                } else if (current_bucket_position < BUCKET_BALANCED) {
                    current_bucket_position = BUCKET_BALANCED
                }
                R.outtake_bucket.position = current_bucket_position
            }
        }
    }

    suspend fun BreakDance(){
        val timeout = OP.async {
            delay(10000)
        }
        OP.while_live {
            R.left_back.power = 0.7
            R.left_front.power = 0.7
            if (timeout.isCompleted){
                it()
            }
        }
    }
}