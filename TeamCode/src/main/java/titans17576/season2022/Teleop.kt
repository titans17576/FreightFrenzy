package titans17576.season2022

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.delay
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573    .OP
import titans17576.ftcrc7573.Stopwatch
import kotlin.math.absoluteValue


open class Teleop(val philip: Boolean, val dashboard_logging: Boolean) : DeferredAsyncOpMode {
    val R = Robot()

    /**
     * The main method, responsible for launching all of the systems.
     */
    override suspend fun op_mode() {
        OP.launch { drive_subsystem() }
        OP.launch { peripherals_subsystem() }
        OP.launch { intake_drawer_subsystem() }
        OP.launch { endgame_notification_subsystem() }
        OP.launch { distance_sensor_subsystem() }
        OP.launch { outtake_arm_subsystem() }
        OP.launch { tse_arm_subsystem() }
        OP.launch { outtake_manual_subsystem() }
        OP.launch { logging_subsystem() }
    }

    /**
     * The method for driver input
     */
    suspend fun drive_subsystem() {
        OP.start_event.await()

        //Control motors with power values
        R.left_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.left_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.right_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        R.right_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        OP.while_live {
            //Standard gobuilda mechanum code
            var drive = -OP.gamepad1.left_stick_y.toDouble()
            var turn = OP.gamepad1.right_stick_x.toDouble()
            var strafe = OP.gamepad1.left_stick_x.toDouble()

            //Scale motor power by this constant
            val slow = if (OP.gamepad1.left_bumper || OP.gamepad2.left_bumper) 0.3 else 1.0
            //Put power on an exponential curve and scale
            fun calculate_final_power(power: Double) = power * power * Math.signum(power) * slow

            //Apply powers
            R.left_back.power = calculate_final_power(drive + turn - strafe)
            R.left_front.power = calculate_final_power(drive + turn + strafe)
            R.right_back.power = calculate_final_power(drive - turn + strafe)
            R.right_front.power = calculate_final_power(drive - turn - strafe)
        }
    }

    /**
     * Responsible for stateless mechanical components,
     * like the carousel and intake
     */
    suspend fun peripherals_subsystem() {
        OP.start_event.await()

        //Enable TSE servo pulse width modulation (killed previously)
        R.attempt_servo_pwm(R.tse, true)

        OP.while_live {
            //Calculate directional intake power from ranging (-1.0 to 1.0) inputs
            var intake_power = 0.0
            intake_power += OP.gamepad1.right_trigger
            intake_power -= OP.gamepad1.left_trigger
            intake_power += OP.gamepad2.left_stick_y

            //If the intake is available, set the power
            if (R.intake_commander.tryAcquire()) {
                try {
                    R.intake_motor.power = intake_power
                } finally {
                    R.intake_commander.release()
                }
            }

            //Calculate directional carousel power from ranging inputs
            var carousel_power = (OP.gamepad2.left_trigger - OP.gamepad2.right_trigger).toDouble() * CAROUSEL_MAXPOW
            if (philip) R.carousel.power += (OP.gamepad1.left_trigger - OP.gamepad1.right_trigger).toDouble() * CAROUSEL_MAXPOW
            //Clamp carousel power between a minimum and maximum if in use
            if (carousel_power.absoluteValue < CAROUSEL_MINPOW && carousel_power.absoluteValue > CAROUSEL_MINPOW_ACTIVATION_LOWER) {
                carousel_power = CAROUSEL_MINPOW * Math.signum(carousel_power)
            }
            R.carousel.power = carousel_power

            //Dpad up to release the clamp, dpad down to clamp the clamp
            if (OP.gamepad2.dpad_up || OP.gamepad1.dpad_up) R.outtake_clamp.position = BUCKET_CLAMP_RELEASE
            else if (OP.gamepad2.dpad_down || OP.gamepad1.dpad_down) R.outtake_clamp.position = BUCKET_CLAMP_CLAMPING
        }
    }
    suspend fun intake_drawer_subsystem() {
        OP.start_event.await()
        var is_out = true
        R.intake_drawer.position = INTAKE_DRAWER_OUT
        OP.while_live {
            OP.wait_for { OP.gamepad1.left_bumper }
            is_out = !is_out
            R.intake_drawer.position = if (is_out) { INTAKE_DRAWER_OUT } else { INTAKE_DRAWER_IN }
            OP.wait_for { !OP.gamepad1.left_bumper }
        }
    }

    /**
     * Notification thing for the drivers
     */
    suspend fun endgame_notification_subsystem() {
        OP.start_event.await()

        //Wait 1 minute 20 seconds
        delay(80 * 1000)
        OP.telemetry.speak("10 seconds to end game")

        //Wait 5 more seconds
        delay(5 * 1000)
        OP.telemetry.speak("5 seconds to end game")
    }

    /**
     * Monitors the distance sensor, and shakes the TSE arm when
     * a mineral has been taken in. Also speaks now
     */
    suspend fun distance_sensor_subsystem() {
        OP.start_event.await()

        OP.while_live {
            //Wait for distance sensor to activate
            if (R.outtake_distance_sensor.getDistance(DistanceUnit.CM) < DISTANCE_SENSOR_ACTIVATION_UPPER){
                //Clamp
                R.outtake_clamp.position = BUCKET_CLAMP_CLAMPING

                //Launch to not delay this function
                /*OP.launch {
                    R.intake_commander.acquire()
                    try {
                        //Eject any excess freight
                        R.intake_motor.power = -INTAKE_EJECT_POWER
                        delay(INTAKE_EJECT_DURATION)
                        R.intake_motor.power = INTAKE_IDLE_POWER
                    } finally {
                        R.intake_commander.release()
                    }
                }*/

                OP.launch {
                    //Wiggle the TSE to notify of a freight
                    R.tse_commander.acquire()
                    try {
                        val prev_pos = R.tse.position
                        R.tse.position = TSE_INSIDE
                        delay(TSE_WIGGLE_DURATION)
                        R.tse.position = prev_pos
                        delay(TSE_WIGGLE_DURATION)
                    } finally {
                        R.tse_commander.release()
                    }
                }

                OP.telemetry.speak("Mineral");

                //Wait until the outtake resets before the system is prime again
                OP.wait_for { OP.gamepad2.right_bumper || (philip && OP.gamepad1.right_bumper) }
            }
        }
    }

    /**
     * Responsible for the outtake arm and bucket
     */
    suspend fun outtake_arm_subsystem() {
        OP.start_event.await()

        //Don't reset the outtake on initial run
        /*R.outtake_bucket.position = BUCKET_LOADING
        R.reset_outtake()
        R.outtake_arm.targetPosition = 0;
        R.outtake_arm.targetPosition = ARM_LOADING*/

        var armposition = 0
        var bucket_dump_pos = BUCKET_DUMP

        //The driver wants to command the arm to a certain position
        fun wants_level_1() = OP.gamepad2.x || (philip && OP.gamepad1.x)
        fun wants_level_2() = OP.gamepad2.y || (philip && OP.gamepad1.y)
        fun wants_level_3() = OP.gamepad2.b || (philip && OP.gamepad1.b)
        fun wants_command_arm() = wants_level_1() || wants_level_2() || wants_level_3()
        //The driver requested the arm be reset
        fun wants_reset() = OP.gamepad2.right_bumper || (philip && OP.gamepad1.right_bumper)

        //The driver wants to adjust the position of the arm manually
        fun wants_command_arm_manual() = OP.gamepad2.right_stick_y.absoluteValue > 0.1
        //The driver wants to dump the freight
        fun wants_dump() = OP.gamepad2.a || (philip && OP.gamepad1.a)

        //If the driver wants to do one of the above
        fun wants_something() = wants_command_arm() || wants_reset() || wants_command_arm_manual() || wants_dump()

        //Only permit dumping freight if the arm is away from the intake position
        fun can_dump() = (R.outtake_arm.currentPosition - ARM_LOADING).absoluteValue > 200

        OP.while_live {
            //Waits until something is wanted
            OP.wait_for { R.automation_allowed() && wants_something() }

            if (wants_dump() && can_dump()) {
                R.outtake_commander.acquire()
                try {
                    //R.outtake_bucket.position = bucket_
                    R.outtake_clamp.position = BUCKET_CLAMP_RELEASE
                    val stopwatch = Stopwatch()
                    OP.wait_for { !wants_dump() && stopwatch.ellapsed() > 500 }
                    //R.outtake_bucket.position = BUCKET_BALANCED
                    R.outtake_clamp.position = BUCKET_CLAMP_CLAMPING
                    delay(300)
                } finally {
                    R.outtake_commander.release()
                }
            }

            if (wants_command_arm()) {
                var power = ARM_POWER_COMMAND
                R.outtake_clamp.position = BUCKET_CLAMP_CLAMPING
                if (OP.gamepad2.x || (philip && OP.gamepad1.x)) {
                    armposition = ARM_LEVEL_1
                    //power = ARM_POWER_COMMAND_SLOW
                    bucket_dump_pos = BUCKET_DUMP
                }
                if (OP.gamepad2.y || (philip && OP.gamepad1.y)) {
                    armposition = ARM_LEVEL_2
                    bucket_dump_pos = BUCKET_DUMP
                }
                if (OP.gamepad2.b || (philip && OP.gamepad1.b)) {
                    armposition = ARM_LEVEL_3
                    bucket_dump_pos = BUCKET_DUMP
                }
                R.command_outtake(armposition, /*bucket_dump_pos,*/ command_power = power)

            } else if (wants_reset()) {
                armposition = ARM_LOADING
                val bucket_position = BUCKET_LOADING
                R.outtake_bucket.position = bucket_position
                R.outtake_clamp.position = BUCKET_CLAMP_RELEASE
                delay(400)
                R.command_outtake(armposition, bucket_position)
                R.reset_outtake()
            } else if (wants_command_arm_manual()) {
                armposition -= (OP.gamepad2.right_stick_y * 100).toInt()
                if (armposition < 0) armposition = 0;
                /*if (R.outtake_arm.currentPosition > ARM_LEVEL_MAX) armposition = ARM_LEVEL_MAX
                else if (R.outtake_arm.currentPosition < ARM_LOADING) armposition = ARM_LOADING*/
                R.command_outtake(armposition, delay_ms = 0)
            }
        }
    }

    /**
     * Sets the TSE position for manual control
     */
    suspend fun tse_arm_subsystem() {
        var tse_pos = TSE_RAISED
        OP.start_event.await()
        if (philip) return
        OP.while_live {
            R.tse_commander.acquire()
            if (OP.gamepad1.y) {
                tse_pos = TSE_RAISED
            } else if (OP.gamepad1.b) {
                tse_pos = TSE_LOWERED
            } else if (OP.gamepad1.x) {
                tse_pos = TSE_DEPLOY
            } else if (OP.gamepad1.a) {
                tse_pos += 0.0075
            }
            R.tse.position = tse_pos
            R.tse_commander.release()
        }
    }

    /**
     * Manual emergency controls for the outtake
     */
    suspend fun outtake_manual_subsystem() {
        OP.start_event.await()
        OP.while_live {
            if (!R.automation_allowed()) {
                R.outtake_commander.acquire()
                R.outtake_arm.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                R.attempt_servo_pwm(R.outtake_bucket, false)
                var cont = true

                //A seperate coroutine for the bucket, since blocking is desired
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
                            500
                        )
                        OP.log("MANUAL bucket position", R.outtake_bucket.position, 500)
                    }
                }

                OP.while_live {
                    if (R.automation_allowed()) it() // Break if automation becomes allowed again
                    R.outtake_arm.power = -OP.gamepad2.right_stick_y * 0.75
                    OP.log("MANUAL arm power", R.outtake_arm.power, 500)
                }
                R.outtake_arm.power = 0.0;
                //Shutdown the bucket job
                cont = false;
                bucket_job.join()
                R.attempt_servo_pwm(R.outtake_bucket, true)
                R.outtake_commander.release()
            }
        }
    }

    /**
     * Logs various values for our convienience
     */
    suspend fun logging_subsystem() {
        OP.while_live(false) {
            OP.log("LEFT", OP.gamepad1.left_stick_y)
            OP.log("RIGHT", OP.gamepad1.right_stick_x)
            OP.log("left_front", R.left_front.currentPosition)
            OP.log("left_back", R.left_back.currentPosition)
            OP.log("right_front", R.right_front.currentPosition)
            OP.log("right_back", R.right_back.currentPosition)
            OP.log("Limit Switch: ", R.outtake_limit_switch.is_touched)
            OP.log( "Distance Sensor", R.outtake_distance_sensor.getDistance(DistanceUnit.CM).toString() + " CM")
        }
    }
}