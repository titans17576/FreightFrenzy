package titans17576

import com.qualcomm.robotcore.eventloop.opmode.OpModeManager
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar
import com.qualcomm.robotcore.hardware.HardwareMap
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.current_op_mode_manager
import titans17576.ftcrc7573.register_defered_async_opmode
import titans17576.ftcrc7573.register_opmode
import kotlin.reflect.KClass

@OpModeRegistrar
fun generated_op_mode_registrar(m: OpModeManager) {
    current_op_mode_manager = m
    op_modes_current()
    op_modes_other()
}

fun op_modes_current() {
    freight_frenzy_meet_2()
}
fun op_modes_other() {
    freight_frenzy_meet_1()
    ftcrc7573_tests()
}

fun freight_frenzy_meet_2() {
    register_defered_async_opmode("/17576/LM2/Teleop", false, titans17576.freightfrenzy.meet2.Teleop::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode("/17576/LM2/Auto/Red", true, titans17576.freightfrenzy.meet2.AutoNoCarouselRed::class as KClass<DeferredAsyncOpMode>)
    //register_opmode("/17576/LM2/SimpleParkCenter", true, titans17576.freightfrenzy.meet2.SimpleParkCenter())
    //register_opmode("/17576/LM2/SimpleParkWall", true, titans17576.freightfrenzy.meet2.SimpleParkWall())
}

fun freight_frenzy_meet_1() {
    //register_defered_async_opmode("/17576/LM1/Teleop", false, titans17576.freightfrenzy.meet1.Teleop::class as KClass<DeferredAsyncOpMode>)
    //register_opmode("/17576/LM1/SimpleParkCenter", true, titans17576.freightfrenzy.meet1.SimpleParkCenter())
    //register_opmode("/17576/LM1/SimpleParkWall", true, titans17576.freightfrenzy.meet1.SimpleParkWall())
}

fun ftcrc7573_tests() {
    register_defered_async_opmode("/7573/ServoTest", false, titans17576.ftcrc7573.test.ServoTest::class as KClass<DeferredAsyncOpMode>)
    register_defered_async_opmode("/7573/DoubleServoTest", false, titans17576.ftcrc7573.test.DoubleServoTest::class as KClass<DeferredAsyncOpMode>)
}