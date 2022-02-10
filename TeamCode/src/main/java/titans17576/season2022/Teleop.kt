package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.OP
import kotlin.math.absoluteValue


class Teleop(val philip: Boolean) : DeferredAsyncOpMode {
    val R = Robot()

    override suspend fun op_mode() {
        OP.launch { drive_subsystem() }
        OP.launch { peripherals_subsystem() }
        OP.launch { endgame_notification_subsystem() }
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

            OP.log("LEFT", OP.gamepad1.left_stick_y)
            OP.log("RIGHT", OP.gamepad1.right_stick_x)
            OP.log("left_front", R.left_front.currentPosition)
            OP.log("left_back", R.left_back.currentPosition)
            OP.log("right_front", R.right_front.currentPosition)
            OP.log("right_back", R.right_back.currentPosition)

            var drive = -OP.gamepad1.left_stick_y.toDouble()
            var turn = OP.gamepad1.right_stick_x.toDouble()
            var strafe = OP.gamepad1.left_stick_x.toDouble()

            val slow = if (OP.gamepad1.left_bumper || OP.gamepad2.left_bumper ) 0.4 else 1.0

            //POV drive (not tank)
            R.left_back.power = (drive + turn - strafe) * slow
            R.left_front.power = (drive + turn + strafe) * slow
            R.right_back.power = (drive - turn + strafe) * slow
            R.right_front.power = (drive - turn - strafe) * slow

            OP.log("Limit Switch: ", R.outtake_limit_switch.is_touched)
        }
    }

    suspend fun peripherals_subsystem() {
        OP.start_signal.await()

        var intake_power = 0.0
        OP.while_live {
            intake_power = 0.0
            intake_power += OP.gamepad1.right_trigger
            intake_power -= OP.gamepad1.left_trigger
            intake_power += OP.gamepad2.left_stick_y
            R.intake_motor.power = intake_power

            R.carousel.power = (OP.gamepad2.left_trigger - OP.gamepad2.right_trigger).toDouble()
            if (philip) R.carousel.power += (OP.gamepad1.left_trigger - OP.gamepad1.right_trigger).toDouble()
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
        var current_bucket_position = BUCKET_POSITION_LOADING

        OP.while_live {
            if ((OP.gamepad2.a || (philip && OP.gamepad1.a) ) /* && Math.abs(ARM_LEVEL_MAX - R.outtake_arm.currentPosition ) <= 30 */) {
                R.outtake_bucket.position = BUCKET_POSITION_DUMP
            } else {
                if (R.outtake_arm.currentPosition <= 250){
                    current_bucket_position = BUCKET_POSITION_LOADING
                } else if (R.outtake_arm.currentPosition > 250 && R.outtake_arm.currentPosition < 500) {
                    current_bucket_position = 0.15
                } else {
                    current_bucket_position = BUCKET_BALANCED
                }

                R.outtake_bucket.position = current_bucket_position
            }
            OP.log("Bucket position", R.outtake_bucket.position);
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

    suspend fun outtake_arm_subsystem() {
        OP.start_signal.await()

        R.reset_outtake()
        R.outtake_arm.targetPosition = 0;
        R.outtake_arm.targetPosition = ARM_INSIDE
        //OP.launch { balance_bucket_subsystem() }

        var armposition = 0

        OP.while_live {

            var bucket_position: Double? = null
            if (OP.gamepad2.a || (philip && OP.gamepad1.a)) {
                bucket_position = BUCKET_POSITION_DUMP
            }

            val automation_allowed = OP.gamepad2.left_stick_x < 0.75
            if ((OP.gamepad2.b || (philip && OP.gamepad1.b)) && automation_allowed) {
                armposition = ARM_LEVEL_3
            } else if ((OP.gamepad2.right_bumper || (philip && OP.gamepad1.right_bumper)) && automation_allowed) {
                R.command_outtake(0)
                R.reset_outtake()
                armposition = ARM_INSIDE
            } else /*if (OP.gamepad2.right_stick_y.absoluteValue > 0.25)*/ {
                armposition += (OP.gamepad2.right_stick_y * 10).toInt()
                if (R.outtake_arm.currentPosition > ARM_LEVEL_MAX) armposition = ARM_LEVEL_MAX
                else if (R.outtake_arm.currentPosition < ARM_INSIDE) armposition = ARM_INSIDE
            }
            R.command_outtake(armposition, bucket_position)

            OP.log("Arm Position Target", armposition);
            OP.log("Arm Position Current", R.outtake_arm.currentPosition);
        }
    }
}