package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.OP
import kotlin.math.absoluteValue


class Teleop : DeferredAsyncOpMode {
    val R = Robot()

    override suspend fun op_mode() {
        OP.launch { drive_subsystem() }
        OP.launch { peripherals_subsystem() }
        OP.launch { endgame_notification_subsystem() }
        OP.launch { balance_bucket_subsystem() }
        OP.launch { distance_sensor_subsystem() }
        OP.launch { outtake_arm_subsystem() }
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
            var strafe = OP.gamepad1.left_stick_x.toDouble()

            val slow = if (OP.gamepad1.right_bumper) 0.4 else 1.0

            //POV drive (not tank)
            R.left_back.power = (drive + turn - strafe) * slow
            R.left_front.power = (drive + turn + strafe) * slow
            R.right_back.power = (drive - turn + strafe) * slow
            R.right_front.power = (drive - turn - strafe) * slow

            OP.telemetry.addData("Limit Switch: ", R.outtake_limit_switch.is_touched)
        }
    }

    suspend fun peripherals_subsystem() {
        OP.start_signal.await()

        var intake_power = 0.0
        OP.while_live {
            intake_power = 0.0
            intake_power += OP.gamepad1.right_trigger
            intake_power -= OP.gamepad1.left_trigger
            R.intake_motor.power = intake_power

            R.carousel.power = (OP.gamepad2.left_trigger - OP.gamepad2.right_trigger).toDouble()
        }
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

    suspend fun balance_bucket_subsystem(){
        OP.start_signal.await()
        OP.while_live {
            if (OP.gamepad2.a && Math.abs(ARM_LEVEL_MAX - R.outtake_arm.currentPosition) <= 30){
                R.outtake_bucket.position = BUCKET_POSITION_DUMP
            }
            else{
                var current_bucket_position = (((BUCKET_BALANCED-BUCKET_POSITION_LOADING)/(ARM_LEVEL_3 - ARM_BUCKET_VROOM))*(R.outtake_arm.currentPosition - ARM_BUCKET_VROOM)) + BUCKET_POSITION_LOADING
                if (current_bucket_position > BUCKET_POSITION_LOADING) {
                    current_bucket_position = BUCKET_POSITION_LOADING
                } else if (current_bucket_position < BUCKET_BALANCED) {
                    current_bucket_position = BUCKET_BALANCED
                }
                R.outtake_bucket.position = current_bucket_position

            }
            OP.telemetry.addData("Bucket position", R.outtake_bucket.position);
        }
    }

    suspend fun distance_sensor_subsystem(){
        OP.start_signal.await()
        /*OP.while_live {

            if (R.outtake_distance_sensor.getDistance(DistanceUnit.CM) < 2.5){
                OP.gamepad1.rumble(0.25, 0.25, 750)

                while(R.outtake_distance_sensor.getDistance(DistanceUnit.CM) > 2.5 && !OP.stop_signal.is_greenlight()){
                    yield();
                }

            }

        }*/
    }

    suspend fun outtake_reset() {
        R.outtake_arm.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.outtake_arm.power = ARM_INSIDE_POWER
        while (!R.outtake_limit_switch.is_touched && !OP.stop_signal.is_greenlight()) {
            yield();
        }
        R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }
    suspend fun outtake_arm_subsystem() {
        OP.start_signal.await()

        outtake_reset()
        R.outtake_arm.targetPosition = 0;
        R.outtake_arm.targetPosition = ARM_INSIDE
        val travel_power = 0.5

        var armposition = 0

        OP.while_live {

            if (OP.gamepad2.b) {
                armposition = ARM_LEVEL_3
                R.outtake_arm.targetPosition = armposition
                R.outtake_arm.mode = DcMotor.RunMode.RUN_TO_POSITION;
                R.outtake_arm.power = travel_power
            } else if (OP.gamepad2.right_bumper) {
                outtake_reset()
                armposition = ARM_INSIDE
                R.outtake_arm.targetPosition = armposition
            } else if (OP.gamepad2.right_stick_y.absoluteValue > 0.25) {
                armposition += (OP.gamepad2.right_stick_y * 10).toInt()
                if (R.outtake_arm.currentPosition > ARM_LEVEL_MAX) armposition = ARM_LEVEL_MAX
                else if (R.outtake_arm.currentPosition < ARM_INSIDE) armposition = ARM_INSIDE
                R.outtake_arm.targetPosition = armposition
                R.outtake_arm.mode = DcMotor.RunMode.RUN_TO_POSITION;
                R.outtake_arm.power = travel_power
            }

            OP.telemetry.addData("Arm Position Target", armposition);
            OP.telemetry.addData("Arm Position Current", R.outtake_arm.currentPosition);
        }
    }
}