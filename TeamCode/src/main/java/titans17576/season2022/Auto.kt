package titans17576.freightfrenzy.Regionals

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.firstinspires.ftc.teamcode.drive.RegionalsDrive
import titans17576.freightfrenzy.meet2.Barcode
import titans17576.freightfrenzy.meet2.camera_disable
import titans17576.freightfrenzy.meet2.camera_init

import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.follow_trajectory_sequence
import titans17576.ftcrc7573.trajectory_builder_factory
import titans17576.season2022.lift_lvl1
import titans17576.season2022.lift_lvl2
import titans17576.season2022.lift_lvl3

abstract class AutoBase(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op

}

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

class BarcodeCarouselDepot(is_red: Boolean, op: AsyncOpMode) : titans17576.freightfrenzy.Regionals.AutoBase(op) {
    val is_red = is_red

    override suspend fun op_mode() {
        val bot = RegionalsDrive(op.hardwareMap)
        val path = Barcode_Carousel_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        //val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        op.start_event.await()
        //val barcode = barcode_eventually.await()

        //drive to hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(2000) //dump into correct level

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(2000) //do carousel

        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(30000)

    }
}

class CarouselDepot(is_red: Boolean, op: AsyncOpMode) : titans17576.freightfrenzy.Regionals.AutoBase(op) {
    val is_red = is_red

    override suspend fun op_mode() {
        val bot = RegionalsDrive(op.hardwareMap)
        val path = Carousel_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        //val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        op.start_event.await()
        //val barcode = barcode_eventually.await()

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(2000) //dump into correct level


        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(30000)

    }
}