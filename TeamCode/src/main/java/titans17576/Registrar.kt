package titans17576

import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import titans17576.ftcrc7573.*
import kotlin.reflect.KClass

@OpModeRegistrar
fun generated_op_mode_registrar(m: OpModeManager) {
    current_op_mode_manager = m
    freight_frenzy_regionals()
    ftcrc7573_tests()
    op_modes_other()
}

fun freight_frenzy_regionals(){
    register_defered_async_opmode("/17576/R/Teleop", false) { op -> titans17576.season2022.Teleop(false) }
    register_defered_async_opmode("/17576/R/Teleop/Philip", false) { op -> titans17576.season2022.Teleop(true ) }
    register_defered_async_opmode("/17576/R/Red/Barcode-Carousel-Park", true) {op -> titans17576.freightfrenzy.Regionals.BarcodeCarouselDepot(true)}
    register_defered_async_opmode("/17576/R/Blue/Barcode-Carousel-Park", true) {op -> titans17576.freightfrenzy.Regionals.BarcodeCarouselDepot(false)}
    register_defered_async_opmode("/17576/R/Red/Carousel-Park", true) {op -> titans17576.freightfrenzy.Regionals.CarouselDepot(true)}
    register_defered_async_opmode("/17576/R/Blue/Carousel-Park", true) {op -> titans17576.freightfrenzy.Regionals.CarouselDepot(false)}
    register_defered_async_opmode("/17576/R/Blue/Barcode-Carousel-Warehouse-Park", true) {op -> titans17576.freightfrenzy.Regionals.BarcodeCarouselWarehousePark(false)}
    register_defered_async_opmode("/17576/R/Red/Barcode-Carousel-Warehouse-Park", true) {op -> titans17576.freightfrenzy.Regionals.BarcodeCarouselWarehousePark(true)}
    register_defered_async_opmode("/17576/R/Red/Barcode-Warehouse-Park", true) { titans17576.freightfrenzy.Regionals.BarcodeWarehousePark(true) }
    register_defered_async_opmode("/17576/R/Blue/Barcode-Warehouse-Park", true) { titans17576.freightfrenzy.Regionals.BarcodeWarehousePark(false) }
}

fun ftcrc7573_tests() {
    register_defered_async_opmode("/7573/CameraTest", false) { op -> titans17576.ftcrc7573.test.CameraTest(op) }
    register_defered_async_opmode_legacy("/7573/ServoTest", false, titans17576.ftcrc7573.test.ServoTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode_legacy("/7573/DoubleServoTest", false, titans17576.ftcrc7573.test.DoubleServoTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode_legacy("/7573/MotorTest", false, titans17576.ftcrc7573.test.MotorTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode("/7573/TouchTest", false) { op -> titans17576.ftcrc7573.test.TouchTest(op) }
    register_defered_async_opmode("/7573/RumbleTest", false) { titans17576.ftcrc7573.test.RumbleTest() }
    register_defered_async_opmode("/7573/ArmBucketTest", false) { titans17576.season2022.ArmBucketTest() }
}

fun op_modes_other() {
}

