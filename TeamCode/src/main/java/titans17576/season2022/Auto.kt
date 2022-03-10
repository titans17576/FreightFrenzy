package titans17576.freightfrenzy.Regionals

import com.qualcomm.robotcore.hardware.DcMotor
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.teamcode.drive.RegionalsDrive
import titans17576.freightfrenzy.meet2.Barcode
import titans17576.freightfrenzy.meet2.camera_disable
import titans17576.freightfrenzy.meet2.camera_init
import titans17576.ftcrc7573.*
import titans17576.season2022.*

/**
 * Base class for auto code, handles vision and a configurable delay
 */
abstract class AutoBase : DeferredAsyncOpMode {
    val R = Robot()
    lateinit var barcode: Deferred<Barcode>

    /** An event fired after a potential configurable delay */
    val auto_start_event = Event();

    override suspend fun op_mode() {
        OP.launch { peripherals() }
        barcode = OP.async { camera() }
        OP.launch { start_event() }
        OP.launch { auto() }
    }

    /**
     * Listens to the gamepad before start to configure a
     * start delay, then fires `auto_start_event` after OP
     * mode start and the delay
     */
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

            //Break when the op mode starts
            if (OP.start_event.has_fired()) it()
        }

        delay(seconds * 1000)
        //Wrap in a try catch loop to pass exceptions through to auto_start_event
        try {
            OP.start_event.await()
            auto_start_event.fire()
        } catch (e: Exception) {
            auto_start_event.throw_exception(e)
        }
    }

    /**
     * Manages the the camera lifecycle and fetching the fianl value
     * for the rest of the OP mode
     */
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
            catch(e: Exception) { print(e); e.printStackTrace() } //Just incase
        }
        return Barcode.Center
    }

    /**
     * Raise the TSE when the OP mode starts so it's out of the way of the arm
     */
    suspend fun peripherals() {
        OP.start_event.await()
        R.tse.position = TSE_RAISED
        R.attempt_servo_pwm(R.tse, true)
        R.tse.position = TSE_RAISED
    }

    /** Put your auto code in here, and wait for auto_start_event */
    abstract suspend fun auto()
}

/**
 * Deposit freight with a given arm level, and optionally crawl forward a distance
 * to approach the team hub
 */
suspend fun deposit_freight(level: Int, prearm_distance: Double?, postarm_distance: Double?, drive: RegionalsDrive) {
    R.intake_drawer.position = INTAKE_DRAWER_IN

    if (prearm_distance != null) follow_trajectory_sequence(drive.trajectorySequenceBuilder(drive.poseEstimate).back(prearm_distance).build(), drive, OP)

    //Allow a maxinum amount of 4 seconds incase the arm dies on the way
    val emergency_stop_one = Stopwatch()
    R.command_outtake(level, null) { emergency_stop_one.ellapsed() < 4000 };
    delay(200)

    if (postarm_distance != null) follow_trajectory_sequence(drive.trajectorySequenceBuilder(drive.poseEstimate).back(postarm_distance).build(), drive, OP)

    //Dump the bucket
    //R.outtake_bucket.position = BUCKET_DUMP
    R.outtake_clamp.position = BUCKET_CLAMP_RELEASE
    delay(600)

    if (postarm_distance != null) follow_trajectory_sequence(drive.trajectorySequenceBuilder(drive.poseEstimate).forward(postarm_distance).build(), drive, OP)

    //Reset the bucket as the arm retracts
    val emergency_stop = Stopwatch()
    R.command_outtake(ARM_LOADING) { emergency_stop.ellapsed() < 2000 }
    R.outtake_arm.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

    //Chuck the TSE arm to the inside of the robot, so when PWM dies
    //it doesn't fall forward
    R.tse.position = TSE_INSIDE
    delay(50)
    //Kill PWM, I think for all servos. This is to ensure that if the bucket is stuck
    //it isn't straining til the end of time
    R.attempt_servo_pwm(R.outtake_bucket, false)

    if (prearm_distance != null) follow_trajectory_sequence(drive.trajectorySequenceBuilder(drive.poseEstimate).forward(prearm_distance).build(), drive, OP)
}

suspend fun deposit_level_3(drive: RegionalsDrive) { deposit_freight(ARM_LEVEL_3, null, null, drive) }
suspend fun deposit_level_2(drive: RegionalsDrive) { deposit_freight(ARM_LEVEL_2, -15.0, 4.0, drive) }
suspend fun deposit_level_1(drive: RegionalsDrive) { deposit_freight(ARM_LEVEL_3, -4.0, null, drive) }
suspend fun deposit_correct_level(barcode: Barcode, drive: RegionalsDrive) {
    when (barcode) {
        Barcode.Left -> deposit_level_1(drive)
        Barcode.Center -> deposit_level_2(drive)
        Barcode.Right -> deposit_level_3(drive)
    }
}
suspend fun intake_elements(drive: RegionalsDrive) {
    R.outtake_clamp.position = BUCKET_CLAMP_RELEASE;
    R.intake_drawer.position = INTAKE_DRAWER_OUT
    OP.while_live {
        R.intake_motor.power = 1.0

        val wiggle = drive.trajectorySequenceBuilder(drive.poseEstimate)
            .turn(-5.0.toRadians)
            .turn( 5.0.toRadians)
            .build()
        follow_trajectory_sequence(wiggle, drive, OP)

        if(R.outtake_distance_sensor.getDistance(DistanceUnit.CM) < DISTANCE_SENSOR_ACTIVATION_UPPER) {
            R.outtake_clamp.position = BUCKET_CLAMP_CLAMPING;
            R.intake_motor.power = 0.0
            it();
        }
    }
}

/**
 * Spin the carousel, score a duck, celebrate :D
 */
suspend fun do_carousel(is_red: Boolean) {
    val direction = if (is_red) { -1.0 } else { 1.0 }
    R.carousel.power = CAROUSEL_MINPOW * direction
    R.carousel.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
    delay(3000);
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

        auto_start_event.await()

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

        auto_start_event.await()

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

class BarcodeWarehouseTwicePark(val is_red: Boolean) : AutoBase() {

    override suspend fun auto() {
        val bot = RegionalsDrive(OP.hardwareMap)
        val path = Barcode_Warehouse_Twice_Park(is_red, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose

        auto_start_event.await()

        //Detects and deposits the barcode
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
        deposit_correct_level(barcode.await(), bot)

        //Goes to the warehouse, and then goes to deposit level 3
        for (i in 1..2){
            follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
            intake_elements(bot);

            follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
            deposit_level_3(bot);
        }

        //Goes to the warehouse again
        follow_trajectory_sequence(path.trajectories.poll()!!, bot, OP);
    }
}