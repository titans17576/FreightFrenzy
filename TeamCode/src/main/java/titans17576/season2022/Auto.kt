package titans17576.freightfrenzy.Regionals

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import org.firstinspires.ftc.teamcode.drive.RegionalsDrive
import titans17576.freightfrenzy.meet2.Barcode
import titans17576.freightfrenzy.meet2.camera_disable
import titans17576.freightfrenzy.meet2.camera_init
import titans17576.ftcrc7573.*
import titans17576.season2022.*

abstract class AutoBase(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op
}

suspend fun deposit_freight(level: Int) {
    R.command_outtake(level, BUCKET_DUMP, delay_ms = 400);
    val emergency_stop = Stopwatch()
    R.command_outtake(ARM_LOADING) { emergency_stop.ellapsed() < 2000 }
    R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    R.outtake_bucket.controller.pwmDisable()
}
suspend fun deposit_level_3() { deposit_freight(ARM_LEVEL_3) }
suspend fun deposit_level_2() { deposit_freight(ARM_LEVEL_2) }
suspend fun deposit_level_1() { deposit_freight(ARM_LEVEL_1) }

suspend fun do_carousel(is_red: Boolean) {
    val direction = if (is_red) { 1.0 } else { -1.0 }
    R.carousel.power = CAROUSEL_MAXPOW * direction;
    delay(1000);
    R.carousel.power = 0.0;
    delay(200);
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
        deposit_level_3()
         //dump into correct level

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        do_carousel(is_red);

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
        do_carousel(is_red)

        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(30000)
    }
}

class BarcodeCarouselWarehousePark(is_red: Boolean, op: AsyncOpMode) : titans17576.freightfrenzy.Regionals.AutoBase(op) {
    val is_red = is_red

    override suspend fun op_mode() {
        val bot = RegionalsDrive(op.hardwareMap)
        val path = Barcode_Carousel_Warehouse_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        //val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        op.start_event.await()
        //val barcode = barcode_eventually.await()

        //drive to hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        deposit_level_3()
        delay(2000)

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        do_carousel(is_red);

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(2000)

        follow_trajectory_sequence(path.trajectories.poll()!!, bot, op)
        delay(30000)
    }
}

class BarcodeWarehousePark(val is_red: Boolean) : DeferredAsyncOpMode {
    override suspend fun op_mode() {
        val bot = RegionalsDrive(OP.hardwareMap)
        val path = Barcode_Warehouse_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        OP.start_event.await()

        //Drive to team shipping hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
        deposit_level_3()

        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
    }
}