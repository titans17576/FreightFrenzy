package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import titans17576.ftcrc7573.OP

class ArmPIDAdjuster : Teleop(philip = false, dashboard_logging = false) {
    companion object {
        var position_p: Double = 10.0
    }

    override suspend fun op_mode() {
        OP.launch { logging_subsystem() }
        OP.launch { distance_sensor_subsystem() }
        OP.launch { outtake_arm_subsystem() }
        OP.launch { outtake_manual_subsystem() }
        OP.launch { drive_subsystem() }

        OP.launch { pid_change_subsystem() }
        OP.launch { pid_logging_subsystem() }
    }

    suspend fun pid_change_subsystem() {

        fun set_coefficients() {
            R.outtake_arm.setPositionPIDFCoefficients(position_p)
        }
        val increment = 0.25
        OP.while_live(false) {
            if (OP.gamepad1.left_bumper) {
                position_p -= increment
                set_coefficients()
                OP.wait_for { !OP.gamepad1.left_bumper }
            }
            if (OP.gamepad1.right_bumper) {
                position_p += increment
                set_coefficients()
                OP.wait_for { !OP.gamepad1.right_bumper }
            }
        }
    }

    suspend fun pid_logging_subsystem() {
        OP.while_live(false) {
            OP.log("PID P-P", R.outtake_arm.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).p);
            OP.log("PID P-A", R.outtake_arm.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION).algorithm);
            OP.log("PID V-P", R.outtake_arm.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER).p);
            OP.log("PID V-I", R.outtake_arm.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER).i);
            OP.log("PID V-D", R.outtake_arm.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER).d);
            OP.log("PID V-A", R.outtake_arm.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER).algorithm);
        }
    }
}