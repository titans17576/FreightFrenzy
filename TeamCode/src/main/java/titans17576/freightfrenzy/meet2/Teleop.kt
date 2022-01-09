package titans17576.freightfrenzy.meet2

import com.qualcomm.robotcore.hardware.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.MotorReversedEncoder

class Teleop(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op;
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
    val lock_servo = op.hardwareMap["intake_clamp"] as Servo

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
    override suspend fun op_mode() {
        op.launch {
            drive_subsystem()
        }
        op.launch { peripherals_subsystem() }
        op.launch { outtake_subsystem() }
        op.launch { philip_button_subsystem() }
    }
    suspend fun drive_subsystem(){
        op.start_signal.await()

        left_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        left_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        right_front.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        right_back.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        op.while_live{

            op.telemetry.addData("LEFT", op.gamepad1.left_stick_y)
            op.telemetry.addData("RIGHT", op.gamepad1.right_stick_x)
            op.telemetry.addData("left_front", left_front.currentPosition)
            op.telemetry.addData("left_back", left_back.currentPosition)
            op.telemetry.addData("right_front", right_front.currentPosition)
            op.telemetry.addData("right_back", right_back.currentPosition)

            var drive = -op.gamepad1.left_stick_y.toDouble()
            var turn = op.gamepad1.right_stick_x.toDouble()
            drive *= Math.abs(drive)
            turn *= Math.abs(turn)
            val slow = if (op.gamepad1.right_bumper) 0.25 else if (op.gamepad1.left_bumper) 1.0 else  0.8;

            //POV drive (not tank)
            left_back.power = (drive + turn) * slow
            left_front.power = (drive + turn) * slow
            right_back.power = (drive - turn) * slow
            right_front.power = (drive - turn) * slow

            /*left_back.power = op.gamepad1.left_stick_y.toDouble()
            left_front.power = op.gamepad1.left_stick_y.toDouble()
            right_front.power = op.gamepad1.right_stick_y.toDouble()
            right_back.power = op.gamepad1.right_stick_y.toDouble()*/
        }
    }

    //Variables for holding the cube
    val position_hold_cube: Double = 0.47
    val position_hold_ball: Double = 0.43
    val position_let_go: Double = 0.25
    val position_neutral: Double = 0.33
    suspend fun peripherals_subsystem() {
        val lock_pos_locked: Double = 0.19
        val lock_pos_go: Double = 0.36
        lock_servo.position = lock_pos_locked

        op.start_signal.await()

        lock_servo.position = lock_pos_go

        var intake_power = 0.0
        op.while_live {
            intake_power = 0.0
            intake_power += op.gamepad1.right_trigger
            intake_power -= op.gamepad1.left_trigger
            //intake_power += op.gamepad2.right_trigger
            //intake_power -= op.gamepad2.left_trigger
            intake.power = intake_power

            //Carousel Servo
            /*if (op.gamepad2.a) {
                if (op.gamepad2.left_bumper || op.gamepad1.a) carousel.power = -1.0
                else carousel.power = 1.0
            }
            else carousel.power = 0.0*/
            carousel.power = (op.gamepad2.left_trigger - op.gamepad2.right_trigger).toDouble()


            if (op.gamepad2.dpad_down) intake_servo.position = position_hold_cube
            else if (op.gamepad2.dpad_up) intake_servo.position = position_let_go
            else if (op.gamepad2.dpad_left) intake_servo.position = position_hold_ball
            else if (op.gamepad2.dpad_right) intake_servo.position = position_neutral
            //Variables for moving the box to hold the cube
            //Left servo
        }
    }
    val outake_position_left_down: Double = 0.0
    val outake_position_left_up: Double = 0.34
    val outake_position_left_go: Double = 0.57
    val outtake_position_left_horizontal: Double = 0.67
    suspend fun outtake_subsystem() {
        lift_left.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lift_right.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        op.start_signal.await()
        op.while_live {


            val lift_lvl1 = 0;
            val lift_lvl2 = -400;
            val lift_lvl3 = -750;

            if(op.gamepad2.x) {
                outtake_arm_go(lift_lvl1, outake_position_left_go);
            }
            if(op.gamepad2.y) {
                outtake_arm_go(lift_lvl2, outake_position_left_go);
            }
            if(op.gamepad2.b) {
                outtake_arm_go(lift_lvl3, outake_position_left_go);
            }


            if (op.gamepad2.right_bumper) {
                intake_servo.position = position_hold_cube
                outake_left.position = outake_position_left_up
                outake_right.position = outake_position_left_up
                delay(100)
                lift_left.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                lift_right.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                lift_left.power = 0.6
                lift_right.power = 0.6
                while (!op.stop_signal.is_greenlight() && !lift_limit.isPressed()) {
                    yield()
                }
                lift_left.power = 0.0
                lift_right.power = 0.0
                lift_left.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                lift_right.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                outake_left.position = outake_position_left_down
                outake_right.position = outake_position_left_down
                intake_servo.position = position_neutral
            }


            if (op.gamepad2.left_bumper) {
                lift_left.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                lift_right.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                var lift_power = op.gamepad2.right_stick_y.toDouble() * 0.6
                if (lift_limit.isPressed) lift_power = Math.min(lift_power, 0.0)
                op.telemetry.addData("Limit Switch", lift_limit.isPressed)
                op.telemetry.addData("Lift Left", lift_left.currentPosition)
                op.telemetry.addData("Lift Right", lift_right.currentPosition)
                lift_left.power = lift_power
                lift_right.power = lift_power
            }

            /*if (op.gamepad2.x) {
                outake_left.position = outake_position_left_down
                outake_right.position = outake_position_left_down
            }
            if (op.gamepad2.y) {
                outake_left.position = outake_position_left_up
                outake_right.position = outake_position_left_up;
            }
            if (op.gamepad2.b) {
                outake_left.position = outake_position_left_go
                outake_right.position = outake_position_left_go
            }*/
        }
    }

    suspend fun outtake_arm_go(level:Int, outtake_position: Double){
        outake_left.position = outake_position_left_up
        outake_right.position = outake_position_left_up
        intake_servo.position = position_hold_cube
        delay(100)
        lift_left.targetPosition = level;
        lift_right.targetPosition = level;
        lift_left.mode = DcMotor.RunMode.RUN_TO_POSITION;
        lift_right.mode = DcMotor.RunMode.RUN_TO_POSITION;
        lift_left.power = -0.5;
        lift_right.power = -0.5;
        while (!op.stop_signal.is_greenlight() && lift_left.currentPosition - 10 > lift_left.targetPosition && !op.gamepad2.left_bumper) {
            yield()
            op.telemetry.addData("Cool", lift_left.currentPosition)

        }
        outake_left.position = outtake_position
        outake_right.position = outtake_position
        //lift_left.power = 0.0;
        //lift_right.power = 0.0;
    }

    suspend fun philip_button_subsystem() {
        op.start_signal.await()
        op.while_live {
            if (op.gamepad1.x) {
                lock_servo.position = 0.0
                delay(1000)
                lock_servo.position = 0.6
            }
        }
    }

}