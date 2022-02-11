package titans17576

import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import titans17576.ftcrc7573.*
import kotlin.reflect.KClass

@OpModeRegistrar
fun generated_op_mode_registrar(m: OpModeManager) {
    current_op_mode_manager = m
    //op_modes_current()
    register_defered_async_opmode("/17576/State/Teleop", false) { op -> titans17576.season2022.Teleop(false) }
    register_defered_async_opmode("/17576/State/Teleop/Philip", false) { op -> titans17576.season2022.Teleop(true ) }
    register_defered_async_opmode("/7573/ArmBucketTest", false) { titans17576.season2022.ArmBucketTest() }
    op_modes_other()
}

fun freight_frenzy_regionals(){
    register_defered_async_opmode("/17576/Regionals/Barcode-Carousel-Park", true) {op -> titans17576.freightfrenzy.Regionals.BarcodeCarouselDepot(true, op)}
}

fun freight_frenzy_meet_3() {
    register_defered_async_opmode("/17576/LM3/Teleop", false) { op -> titans17576.freightfrenzy.meet2.Teleop(op) }
    register_defered_async_opmode("/17576/LM3/Red/Carousel/Vision-Carousel-Park", true) { op -> titans17576.freightfrenzy.meet3.Auto_StartCarousel_Vision_Carousel_Depo(true, op) }
    register_defered_async_opmode("/17576/LM3/Blue/CaServoTestrousel/Vision-Carousel-Park", true) { op -> titans17576.freightfrenzy.meet3.Auto_StartCarousel_Vision_Carousel_Depo(false, op) }
    register_defered_async_opmode("/17576/LM3/Red/Warehouse/Vision-Park", true) { op -> titans17576.freightfrenzy.meet3.Auto_StartWarehouse_Vision_Depo(true, op) }
    register_defered_async_opmode("/17576/LM3/Blue/Warehouse/Vision-Park", true) { op -> titans17576.freightfrenzy.meet3.Auto_StartWarehouse_Vision_Depo(false, op) }
}

fun op_modes_current() {
    freight_frenzy_meet_3()
    register_defered_async_opmode("/7573/CameraTest", false) { op -> titans17576.ftcrc7573.test.CameraTest(op) }
}
fun op_modes_other() {
    freight_frenzy_regionals()
    ftcrc7573_tests()
}

fun freight_frenzy_meet_2() {
    register_defered_async_opmode("/17576/LM2/Teleop", false) { op -> titans17576.freightfrenzy.meet2.Teleop(op) }
    //register_defered_async_opmode("/17576/LM2/Auto/Red", true, titans17576.freightfrenzy.meet2.AutoNoCarouselRed::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode_legacy("/17576/LM2/CarousselAuto/Blue", true, titans17576.freightfrenzy.meet2.AutoCarouselBlue::class)
    register_defered_async_opmode_legacy("/17576/LM2/ParkAutoClose/Blue", true, titans17576.freightfrenzy.meet2.ParkCloseBlue::class)
    register_defered_async_opmode_legacy("/17576/LM2/ParkAutoFar/Blue", true, titans17576.freightfrenzy.meet2.ParkFarBlue::class)
    register_defered_async_opmode_legacy("/17576/LM2/ParkAutoClose/Red", true, titans17576.freightfrenzy.meet2.ParkCloseRed::class)
    register_defered_async_opmode_legacy("/17576/LM2/ParkAutoFar/Red", true, titans17576.freightfrenzy.meet2.ParkFarRed::class)
    register_defered_async_opmode_legacy("/17576/LM2/DriveForwardAuto", true, titans17576.freightfrenzy.meet2.DriveForawardAuto::class)
    //register_opmode("/17576/LM2/SimpleParkCenter", true, titans17576.freightfrenzy.meet2.SimpleParkCenter())
    //register_opmode("/17576/LM2/SimpleParkWall", true, titans17576.freightfrenzy.meet2.SimpleParkWall())
}

fun freight_frenzy_meet_1() {
    //register_defered_async_opmode("/17576/LM1/Teleop", false, titans17576.freightfrenzy.meet1.Teleop::class as KClass<DeferredAsyncOpMode>)
    //register_opmode("/17576/LM1/SimpleParkCenter", true, titans17576.freightfrenzy.meet1.SimpleParkCenter())
    //register_opmode("/17576/LM1/SimpleParkWall", true, titans17576.freightfrenzy.meet1.SimpleParkWall())
}

fun ftcrc7573_tests() {
    register_defered_async_opmode_legacy("/7573/ServoTest", false, titans17576.ftcrc7573.test.ServoTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode_legacy("/7573/DoubleServoTest", false, titans17576.ftcrc7573.test.DoubleServoTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode_legacy("/7573/MotorTest", false, titans17576.ftcrc7573.test.MotorTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode("/7573/TouchTest", false) { op -> titans17576.ftcrc7573.test.TouchTest(op) }
}