package titans17576.freightfrenzy.Regionals

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import org.firstinspires.ftc.teamcode.drive.RegionalsDrive
import titans17576.freightfrenzy.meet2.Barcode
import titans17576.freightfrenzy.meet2.camera_disable
import titans17576.freightfrenzy.meet2.camera_init
import titans17576.ftcrc7573.*
import titans17576.season2022.*

abstract class AutoBase : DeferredAsyncOpMode {
    val R = Robot()
    lateinit var barcode: Deferred<Barcode>

    val auto_start_event = Event();

    override suspend fun op_mode() {
        OP.launch { peripherals() }
        barcode = OP.async { camera() }
        OP.launch { start_event() }
        OP.launch { auto() }
    }

    suspend fun start_event() {
        var seconds: Long = 0
        OP.while_live(false) {
            if (OP.gamepad1.dpad_up) {
                seconds += 1
                OP.wait_for { !OP.gamepad1.dpad_up }
            } else if (OP.gamepad1.dpad_down) {
                if (seconds > 0) seconds -= 1
                OP.wait_for { !OP.gamepad1.dpad_down }
            }
            OP.log("Start Delay", seconds.toString() + " seconds", -1)
            if (OP.start_event.has_fired()) it()
        }
        delay(seconds)
        try {
            OP.start_event.await()
            auto_start_event.fire()
        } catch (e: Exception) {
            auto_start_event.throw_exception(e)
        }
    }

    suspend fun camera(): Barcode {
        try {
            val barcode_feed = camera_init(OP)
            OP.while_live(false) {
                if (OP.start_event.has_fired()) it();
                OP.log("Barcode", barcode_feed.get_next(OP.mk_scope()), -1)
            }
            OP.log("Barcode", barcode_feed.get_now(), -1)
            return barcode_feed.get_now()
        } catch(e: Exception) {
            println(e)
            e.printStackTrace()
        } finally {
            try { camera_disable(OP) }
            catch(e: Exception) { print(e); e.printStackTrace() }
        }
        return Barcode.Center
    }

    suspend fun peripherals() {
        OP.start_event.await()
        R.tse.position = TSE_RAISED
        R.attempt_servo_pwm(R.tse, true)
        R.tse.position = TSE_RAISED
    }

    abstract suspend fun auto()
}

suspend fun deposit_freight(level: Int, distance: Double?, drive: RegionalsDrive) {
    if (distance != null) follow_trajectory_sequence(drive.trajectorySequenceBuilder(drive.poseEstimate).back(distance).build(), drive, OP)
    R.command_outtake(level, null/*, delay_ms = 300*/);
    delay(200)
    R.outtake_bucket.position = BUCKET_DUMP
    delay(600)
    val emergency_stop = Stopwatch()
    R.command_outtake(ARM_LOADING) { emergency_stop.ellapsed() < 2000 }
    R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
    R.tse.position = TSE_INSIDE
    delay(50)
    R.attempt_servo_pwm(R.outtake_bucket, false)
    if (distance != null) follow_trajectory_sequence(drive.trajectorySequenceBuilder(drive.poseEstimate).forward(distance).build(), drive, OP)
}
suspend fun deposit_level_3(drive: RegionalsDrive) { deposit_freight(ARM_LEVEL_3, 1.0, drive) }
suspend fun deposit_level_2(drive: RegionalsDrive) { deposit_freight(ARM_LEVEL_2, 6.0, drive) }
suspend fun deposit_level_1(drive: RegionalsDrive) { deposit_freight(ARM_LEVEL_2, null, drive) }

suspend fun deposit_correct_level(barcode: Barcode, drive: RegionalsDrive) {
    when (barcode) {
        Barcode.Left -> deposit_level_1(drive)
        Barcode.Center -> deposit_level_2(drive)
        Barcode.Right -> deposit_level_3(drive)
    }
}

suspend fun do_carousel(is_red: Boolean) {
    val direction = if (is_red) { -1.0 } else { -1.0 }
    R.carousel.power = (CAROUSEL_MAXPOW + CAROUSEL_MINPOW) / 2 * direction
    R.carousel.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    delay(2000);
    R.carousel.power = 0.0;
    delay(200);
}

class BarcodeCarouselDepot(is_red: Boolean) : AutoBase() {
    val is_red = is_red

    override suspend fun auto() {
        val bot = RegionalsDrive(OP.hardwareMap)
        val path = Barcode_Carousel_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        auto_start_event.await()

        //drive to hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        deposit_correct_level(barcode.await(), bot)
         //dump into correct level

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        do_carousel(is_red);

        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        delay(30000)

    }
}

class CarouselDepot(is_red: Boolean) : AutoBase() {
    val is_red = is_red

    override suspend fun auto() {
        val bot = RegionalsDrive(OP.hardwareMap)
        val path = Carousel_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        //val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        auto_start_event.await()
        //val barcode = barcode_eventually.await()

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        do_carousel(is_red)

        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        delay(30000)
    }
}

class BarcodeCarouselWarehousePark(is_red: Boolean) : AutoBase() {
    val is_red = is_red

    override suspend fun auto() {
        val bot = RegionalsDrive(OP.hardwareMap)
        val path = Barcode_Carousel_Warehouse_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        //val barcode_eventually = op.async { get_grasshopper_location(op, this) }
        auto_start_event.await()
        //val barcode = barcode_eventually.await()

        //drive to hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        deposit_correct_level(barcode.await(), bot)
        delay(2000)

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        do_carousel(is_red);

        //drive to carousel
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        delay(2000)

        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP)
        delay(30000)
    }
}

class BarcodeWarehousePark(val is_red: Boolean) : AutoBase() {
    override suspend fun auto() {
        val bot = RegionalsDrive(OP.hardwareMap)
        val path = Barcode_Warehouse_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        auto_start_event.await()

        //Drive to team shipping hub
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
        deposit_correct_level(barcode.await(), bot)

        //Park
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
    }
}