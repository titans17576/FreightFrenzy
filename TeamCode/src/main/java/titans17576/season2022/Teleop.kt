package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.ServoImplEx
import kotlinx.coroutines.delay
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.OP
import titans17576.ftcrc7573.Stopwatch
import kotlin.math.absoluteValue


class Teleop(val philip: Boolean) : DeferredAsyncOpMode {
    val R = Robot()

    override suspend fun op_mode() {
        OP.launch { drive_subsystem() }
        OP.launch { peripherals_subsystem() }
        OP.launch { endgame_notification_subsystem() }
        OP.launch { distance_sensor_subsystem() }
        OP.launch { outtake_arm_subsystem() }
        OP.launch { tse_arm_subsystem() }
        OP.launch { outtake_manual_subsystem() }
    }

    suspend fun drive_subsystem() {
        OP.start_event.await()

        R.left_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.left_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.right_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.right_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        OP.while_live {

            OP.log("LEFT", OP.gamepad1.left_stick_y)
            OP.log("RIGHT", OP.gamepad1.right_stick_x)
            OP.log("left_front", R.left_front.currentPosition)
            OP.log("left_back", R.left_back.currentPosition)
            OP.log("right_front", R.right_front.currentPosition)
            OP.log("right_back", R.right_back.currentPosition)

            var drive = -OP.gamepad1.left_stick_y.toDouble()
            var turn = OP.gamepad1.right_stick_x.toDouble()
            var strafe = OP.gamepad1.left_stick_x.toDouble()

            val slow = if (OP.gamepad1.left_bumper || OP.gamepad2.left_bumper) 0.4 else 1.0

            //POV drive (not tank)
            R.left_back.power = (drive + turn - strafe) * slow
            R.left_front.power = (drive + turn + strafe) * slow
            R.right_back.power = (drive - turn + strafe) * slow
            R.right_front.power = (drive - turn - strafe) * slow

            OP.log("Limit Switch: ", R.outtake_limit_switch.is_touched)
        }
    }

    suspend fun peripherals_subsystem() {
        OP.start_event.await()

        var intake_power = 0.0
        var tse_pos = TSE_RAISED
        R.attempt_servo_pwm(R.tse, true)
        OP.while_live {
            intake_power = 0.0
            intake_power += OP.gamepad1.right_trigger
            intake_power -= OP.gamepad1.left_trigger
            intake_power += OP.gamepad2.left_stick_y
            R.intake_motor.power = intake_power

            var carousel_power =
                (OP.gamepad2.left_trigger - OP.gamepad2.right_trigger).toDouble() * CAROUSEL_MAXPOW
            if (carousel_power.absoluteValue < CAROUSEL_MINPOW && carousel_power.absoluteValue > 0.075) carousel_power =
                CAROUSEL_MINPOW * Math.sin(carousel_power)
            R.carousel.power = carousel_power
            if (philip) R.carousel.power += (OP.gamepad1.left_trigger - OP.gamepad1.right_trigger).toDouble() * CAROUSEL_MAXPOW

            if (OP.gamepad1.y) {
                tse_pos = TSE_RAISED
            } else if (OP.gamepad1.b) {
                tse_pos = TSE_LOWERED
            } else if (OP.gamepad1.x) {
                tse_pos = TSE_DEPLOY
            } else if (OP.gamepad1.a) {
                tse_pos -= 0.0075
            }
            R.tse.position = tse_pos
        }
    }

    suspend fun endgame_notification_subsystem() {
        OP.start_event.await()
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

    suspend fun distance_sensor_subsystem() {
        OP.start_event.await()
        /*OP.while_live {
            if (R.outtake_distance_sensor.getDistance(DistanceUnit.CM) < 2.5){
                OP.gamepad1.rumble(0.25, 0.25, 750)
                OP.wait_for { R.outtake_distance_sensor.getDistance(DistanceUnit.CM) > 2.5 && !OP.stop_event.has_fired() }
            }
        }*/
    }

    suspend fun outtake_arm_subsystem() {
        OP.start_event.await()

        R.reset_outtake()
        R.outtake_arm.targetPosition = 0;
        R.outtake_arm.targetPosition = ARM_LOADING
        //OP.launch { balance_bucket_subsystem() }

        var armposition = 0
        val stopwatch = Stopwatch()
        OP.while_live {
            OP.log("OutttakeSubsystem", stopwatch.reset(), 10000);

            var bucket_position: Double? = null

            val automation_allowed = R.automation_allowed()
            OP.wait_for {
                OP.gamepad2.b || (philip && OP.gamepad1.b)
                        || OP.gamepad2.y || (philip && OP.gamepad1.y)
                        || OP.gamepad2.x || (philip && OP.gamepad1.x)
                        || (OP.gamepad2.right_bumper) || (philip && OP.gamepad1.right_bumper)
                        || (OP.gamepad2.right_stick_y.absoluteValue > 0.1)
                        || OP.gamepad2.a || (philip && OP.gamepad1.a)
                        || !automation_allowed
            }

            if ((OP.gamepad2.a || (philip && OP.gamepad1.a)) && (R.outtake_arm.currentPosition - ARM_LOADING).absoluteValue > 200) {
                R.outtake_commander.acquire()
                try {
                    R.outtake_bucket.position = BUCKET_DUMP
                    OP.wait_for { !OP.gamepad2.a && (!philip || !OP.gamepad1.a) }
                    R.outtake_bucket.position = BUCKET_BALANCED
                    delay(300)
                } finally {
                    R.outtake_commander.release()
                }
            }

            if ((
                        OP.gamepad2.b || (philip && OP.gamepad1.b)
                                || OP.gamepad2.y || (philip && OP.gamepad1.y)
                                || OP.gamepad2.x || (philip && OP.gamepad1.x)
                        ) && automation_allowed
            ) {
                var power = ARM_POWER_COMMAND
                if (OP.gamepad2.x || (philip && OP.gamepad1.x)) {
                    armposition = ARM_LEVEL_1
                    power = ARM_POWER_COMMAND_SLOW
                }
                if (OP.gamepad2.y || (philip && OP.gamepad1.y)) armposition = ARM_LEVEL_2
                if (OP.gamepad2.b || (philip && OP.gamepad1.b)) armposition = ARM_LEVEL_3
                R.command_outtake(armposition, bucket_position, command_power = power)
            } else if ((OP.gamepad2.right_bumper || (philip && OP.gamepad1.right_bumper)) && automation_allowed) {
                armposition = ARM_LOADING
                R.command_outtake(armposition, bucket_position)
                R.reset_outtake()
            } else if (OP.gamepad2.right_stick_y.absoluteValue > 0.1 && automation_allowed) {
                armposition -= (OP.gamepad2.right_stick_y * 100).toInt()
                if (armposition < 0) armposition = 0;
                /*if (R.outtake_arm.currentPosition > ARM_LEVEL_MAX) armposition = ARM_LEVEL_MAX
                else if (R.outtake_arm.currentPosition < ARM_LOADING) armposition = ARM_LOADING*/
                R.command_outtake(armposition, bucket_position, delay_ms = 0)
            }
        }
    }

    suspend fun tse_arm_subsystem() {
        OP.start_event.await()
        var tse_pos = TSE_RAISED;
        OP.while_live {
            if (OP.gamepad2.dpad_right) {
                if ((tse_pos - TSE_RAISED).absoluteValue < (tse_pos - TSE_LOWERED).absoluteValue) tse_pos =
                    TSE_LOWERED
                else tse_pos = TSE_RAISED
                OP.wait_for { !OP.gamepad2.dpad_right }
            } else if (OP.gamepad2.dpad_up) {
                tse_pos -= 0.03
            } else if (OP.gamepad2.dpad_down) {
                tse_pos += 0.03
            }
        }
    }

    suspend fun outtake_manual_subsystem() {
        OP.start_event.await()
        OP.while_live {
            if (!R.automation_allowed()) {
                R.outtake_commander.acquire()
                R.outtake_arm.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                R.attempt_servo_pwm(R.outtake_bucket, false)
                var cont = true
                val bucket_job = OP.launch {
                    OP.while_live {
                        if (!cont) it()
                        if (OP.gamepad2.dpad_down) {
                            R.attempt_servo_pwm(R.outtake_bucket, false)
                            delay(750)
                        } else if (OP.gamepad2.dpad_left) {
                            R.outtake_bucket.position = BUCKET_LOADING
                            R.attempt_servo_pwm(R.outtake_bucket, true)
                            delay(750)
                        } else if (OP.gamepad2.dpad_up) {
                            R.outtake_bucket.position = BUCKET_TRANSITION_RISING
                            R.attempt_servo_pwm(R.outtake_bucket, true)
                            delay(750)
                        } else if (OP.gamepad2.dpad_right) {
                            R.outtake_bucket.position = BUCKET_BALANCED
                            R.attempt_servo_pwm(R.outtake_bucket, true)
                            delay(750)
                        }
                        OP.log(
                            "MANUAL bucket enabled",
                            R.attempt_servo_pwm_status(R.outtake_bucket).orEmpty(),
                            50
                        )
                        OP.log("MANUAL bucket position", R.outtake_bucket.position, 50)
                    }
                }
                OP.while_live {
                    if (R.automation_allowed()) it()
                    R.outtake_arm.power = -OP.gamepad2.right_stick_y * 0.75
                    OP.log("MANUAL arm power", R.outtake_arm.power, 50)
                }
                R.outtake_arm.power = 0.0;
                cont = false;
                bucket_job.join()
                R.attempt_servo_pwm(R.outtake_bucket, true)
                R.outtake_commander.release()
            }
        }
    }
}
