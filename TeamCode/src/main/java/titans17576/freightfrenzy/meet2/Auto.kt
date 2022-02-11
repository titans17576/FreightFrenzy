package titans17576.freightfrenzy.meet2

import com.qualcomm.robotcore.hardware.*
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield
import org.firstinspires.ftc.teamcode.drive.DriveConstants

abstract class AutoBase(op: AsyncOpMode): DeferredAsyncOpMode {
    val op = op
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
    val lock_servo = op.hardwareMap["intake_hard_stop"] as Servo

    val lift_limit = op.hardwareMap["lift_limit"] as TouchSensor

    init {
        left_front.direction = DcMotorSimple.Direction.FORWARD
        left_back.direction = DcMotorSimple.Direction.FORWARD
        right_front.direction = DcMotorSimple.Direction.REVERSE
        right_back.direction = DcMotorSimple.Direction.REVERSE

        lift_right.direction = DcMotorSimple.Direction.FORWARD
        outake_left.direction = Servo.Direction.REVERSE

        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    suspend fun drive(ticks: Int, power: Double) {
        left_front.targetPosition = ticks
        left_back.targetPosition = ticks
        right_front.targetPosition = ticks
        right_back.targetPosition = ticks

        left_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        left_back.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_back.mode = DcMotor.RunMode.RUN_TO_POSITION

        left_front.power = power
        left_back.power = power
        right_front.power = power
        right_back.power = power

        while (left_front.isBusy && left_back.isBusy && right_front.isBusy && right_back.isBusy == true && !op.stop_event.has_fired()) {
            yield()
            op.telemetry.addData("LeftFront", left_front.currentPosition)
            op.telemetry.addData("LeftBack", left_back.currentPosition)
            op.telemetry.addData("RightFront", right_front.currentPosition)
            op.telemetry.addData("RightBack", right_back.currentPosition)
        }

        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        left_front.power = 0.0
        left_back.power = 0.0
        right_front.power = 0.0
        right_back.power = 0.0

        delay(250)
    }

    suspend fun turn(ticks: Int, power: Double) {
        left_front.targetPosition = ticks
        left_back.targetPosition = ticks
        right_front.targetPosition = -ticks
        right_back.targetPosition = -ticks

        left_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        left_back.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_front.mode = DcMotor.RunMode.RUN_TO_POSITION
        right_back.mode = DcMotor.RunMode.RUN_TO_POSITION

        left_front.power = power
        left_back.power = power
        right_front.power = power
        right_back.power = power

        while (left_front.isBusy && left_back.isBusy && right_front.isBusy && right_back.isBusy == true && !op.stop_event.has_fired()) {
            yield()
            op.telemetry.addData("LeftFront", left_front.currentPosition)
            op.telemetry.addData("LeftBack", left_back.currentPosition)
            op.telemetry.addData("RightFront", right_front.currentPosition)
            op.telemetry.addData("RightBack", right_back.currentPosition)
        }

        left_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        left_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_front.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        right_back.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        left_front.power = 0.0
        left_back.power = 0.0
        right_front.power = 0.0
        right_back.power = 0.0

        delay(250)
    }

    val outake_position_left_down: Double = 0.0
    val outake_position_left_up: Double = 0.34
    val outake_position_left_go: Double = 0.57
    val outtake_position_left_horizontal: Double = 0.67
    val position_hold_cube: Double = 0.47
    val position_hold_ball: Double = 0.43
    val position_let_go: Double = 0.25
    val position_neutral: Double = 0.33
    val lift_lvl1 = 0;
    val lift_lvl2 = -400;
    val lift_lvl3 = -750;

    suspend fun reset_outtake() {
        intake_servo.position = position_hold_cube
        outake_left.position = outake_position_left_up
        outake_right.position = outake_position_left_up
        delay(100)
        lift_left.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lift_right.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lift_left.power = 0.6
        lift_right.power = 0.6
        while (!op.stop_event.has_fired() && !lift_limit.isPressed()) {
            yield()
        }
        lift_left.power = 0.0
        lift_right.power = 0.0
        lift_left.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lift_right.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        outake_left.position = outake_position_left_down
        outake_right.position = outake_position_left_down
        intake_servo.position = position_neutral
        delay(250)
    }
    suspend fun raise_outtake(ticks: Int) {
        outake_left.position = outake_position_left_up
        outake_right.position = outake_position_left_up
        intake_servo.position = position_hold_cube
        delay(100)
        lift_left.targetPosition = ticks;
        lift_right.targetPosition = ticks;
        lift_left.mode = DcMotor.RunMode.RUN_TO_POSITION;
        lift_right.mode = DcMotor.RunMode.RUN_TO_POSITION;
        lift_left.power = -0.5;
        lift_right.power = -0.5;
        while (!op.stop_event.has_fired() && lift_left.currentPosition - 10 > lift_left.targetPosition && !op.gamepad2.left_bumper) {
            yield()
            op.telemetry.addData("Cool", lift_left.currentPosition)

        }
        outake_left.position = outake_position_left_go
        outake_right.position = outake_position_left_go
        delay(250)
    }
    suspend fun raise_outtake_lvl_1() { raise_outtake(lift_lvl1) }
    suspend fun raise_outtake_lvl_2() { raise_outtake(lift_lvl2) }
    suspend fun raise_outtake_lvl_3() { raise_outtake(lift_lvl3) }
    suspend fun outtake() {
        intake_servo.position = position_let_go
        delay(500)
    }
}

public class AutoNoCarouselRed(op: AsyncOpMode) : AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(-500, 0.2)
        turn(250,0.2)
        drive(-500, 0.2)
        raise_outtake_lvl_3()
        outtake()
        reset_outtake()
    }
}

public class AutoCarouselBlue(op: AsyncOpMode) : AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(DriveConstants.inchesToTicks(8.0), 0.4)
        carousel.power = 1.0
        delay(5000)
        carousel.power = 0.0
        drive(DriveConstants.inchesToTicks(-24.0 * 1.5), 0.4)
        turn(250, 0.4)
        drive(DriveConstants.inchesToTicks(-24.0 * 2.5), 0.4)
    }
}

public class ParkCloseBlue(op: AsyncOpMode): AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(DriveConstants.inchesToTicks(24.0 * 0.75), 0.4)
        turn(-500, 0.4)
        drive(DriveConstants.inchesToTicks(24.0 * 2), 0.8)
    }
}
public class ParkFarBlue(op: AsyncOpMode): AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(DriveConstants.inchesToTicks(24.0), 0.4)
        turn(500, 0.4)
        drive(DriveConstants.inchesToTicks(24.0), 0.4)
    }
}
public class ParkFarRed(op: AsyncOpMode): AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(DriveConstants.inchesToTicks(24.0), 0.4)
        turn(-500, 0.4)
        drive(DriveConstants.inchesToTicks(24.0), 0.4)
    }
}
public class ParkCloseRed(op: AsyncOpMode): AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(DriveConstants.inchesToTicks(24.0 * 0.75), 0.4)
        turn(500, 0.4)
        drive(DriveConstants.inchesToTicks(24.0 * 2), 0.8)
    }
}
public class DriveForawardAuto(op: AsyncOpMode) : AutoBase(op) {
    override suspend fun op_mode() {
        op.start_event.await()
        drive(DriveConstants.inchesToTicks(24.0 * 3), 0.8);
    }
}