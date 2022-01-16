package titans17576.freightfrenzy.meet3

import com.qualcomm.robotcore.hardware.CRServo
import kotlinx.coroutines.delay
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import org.firstinspires.ftc.teamcode.drive.Meet2Drive
import titans17576.freightfrenzy.meet2.*
import titans17576.ftcrc7573.follow_trajectory_sequence
import titans17576.ftcrc7573.trajectory_builder_factory

suspend fun get_grasshopper_location(op: AsyncOpMode): Barcode {
    val feed = camera_init(op)
    try {
        op.start_signal.await()
        return feed.get_now()
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

        val barcode_eventually = op.async { get_grasshopper_location(op) }
        op.start_signal.await()
        val barcode = barcode_eventually.await()

        //Drive to team shipping hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        //Deploy into shipping hub
        outtake.outtake_arm_go(calc_lift_height(barcode), OUTTAKE_POSITION_OUTSIDE)
        delay(1000)

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