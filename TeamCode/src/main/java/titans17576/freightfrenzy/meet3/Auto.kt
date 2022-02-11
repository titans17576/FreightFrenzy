package titans17576.freightfrenzy.meet3

import com.qualcomm.robotcore.hardware.CRServo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import org.firstinspires.ftc.teamcode.drive.Meet2Drive
import titans17576.freightfrenzy.meet2.*
import titans17576.ftcrc7573.follow_trajectory_sequence
import titans17576.ftcrc7573.trajectory_builder_factory
import titans17576.season2022.*

suspend fun get_grasshopper_location(op: AsyncOpMode, scope: CoroutineScope): Barcode {
    val feed = camera_init(op)
    try {
        op.start_event.await()
        return feed.get_next(scope)
    } finally {
        camera_disable(op)
    }
}

fun calc_lift_height(barcode: Barcode): Int {
    return when (barcode) {
        Barcode.Left -> lift_lvl1
        Barcode.Center -> lift_lvl2
        Barcode.Right -> lift_lvl3
    }
}

abstract class AutoBase(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op

    val carousel = op.hardwareMap["carousel"] as CRServo
}

class Auto_StartCarousel_Vision_Carousel_Depo(is_red: Boolean, op: AsyncOpMode) : AutoBase(op) {
    val is_red = is_red

    override suspend fun op_mode() {
        val bot = Meet2Drive(op.hardwareMap)
        val path = Path_StartCarousel_Vision_Carousel_Depo(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose
        val outtake = OuttakeController(op, false)

        val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        op.start_event.await()
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        val barcode = barcode_eventually.await()
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        op.launch { op.while_live { op.telemetry.addData("Barcode", barcode) } }
        delay(2000)

        //Drive to team shipping hub
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        //Deploy into shipping hub
        outtake.outtake_arm_go(calc_lift_height(barcode), OUTTAKE_POSITION_OUTSIDE)
        delay(500)
        outtake.intake_servo.position = CLAMP_POS_RELEASE
        delay(2000)
        outtake.outtake_reset()
        delay(500)

        //Drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)

        //Do carousel
        carousel.power = 1.0
        delay(4000)
        carousel.power = 0.0

        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)

        delay(30000)
    }
}

class Auto_StartWarehouse_Vision_Depo(is_red: Boolean, op: AsyncOpMode) : AutoBase(op) {
    val is_red = is_red

    override suspend fun op_mode() {
        val bot = Meet2Drive(op.hardwareMap)
        val path = Path_StartWarehouse_Vision_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose
        val outtake = OuttakeController(op, false)

        val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        op.start_event.await()
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        val barcode = barcode_eventually.await()
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        op.launch { op.while_live { op.telemetry.addData("Barcode", barcode) } }
        delay(500)
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE

        //Drive to team shipping hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        outtake.intake_servo.position = CLAMP_POS_HOLD_CUBE
        //Deploy into shipping hub
        outtake.outtake_arm_go(calc_lift_height(barcode), OUTTAKE_POSITION_OUTSIDE)
        delay(500)
        outtake.intake_servo.position = CLAMP_POS_RELEASE
        delay(2000)
        outtake.outtake_reset()
        delay(500)

        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)

        delay(30000)
    }
}