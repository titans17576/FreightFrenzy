package titans17576.ftcrc7573.test

import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.coroutines.delay
import titans17576.ftcrc7573.DeferredAsyncOpMode
import titans17576.ftcrc7573.OP

class RumbleTest : DeferredAsyncOpMode {
    override suspend fun op_mode() {
        OP.launch { rumble_subsystem(OP.gamepad1) }
        OP.launch { rumble_subsystem(OP.gamepad2) }
    }

    suspend fun rumble_subsystem(gamepad: Gamepad) {
        OP.while_live {
            if (gamepad.a) {
                gamepad.rumble(1000);
                delay(1000);
            }
        }
    }
}