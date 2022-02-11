package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.OP
import kotlin.math.absoluteValue

class ArmBucketTest : DeferredAsyncOpMode {
    val R = Robot()
    override suspend fun op_mode() {
        OP.launch { outtake_arm_subsystem() }
        OP.launch { bucket_manual_subsystem() }
    }

    suspend fun outtake_reset() {
        R.outtake_arm.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.outtake_arm.power = ARM_INSIDE_POWER
        var reset = true;
        while (!R.outtake_limit_switch.is_touched && !OP.stop_signal.is_greenlight()) {
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
    suspend fun outtake_arm_subsystem() {
        OP.start_signal.await()

        outtake_reset()
        R.outtake_arm.targetPosition = 0;
        R.outtake_arm.targetPosition = ARM_INSIDE
        val travel_power = 0.275

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

    //256 0.18

    suspend fun bucket_manual_subsystem() {
        OP.start_signal.await()
        var pos = BUCKET_POSITION_LOADING
        OP.while_live {
            pos += OP.gamepad2.left_stick_y / 50
            R.outtake_bucket.position = pos
            OP.telemetry.addData("Bucket", pos)
        }
    }
}