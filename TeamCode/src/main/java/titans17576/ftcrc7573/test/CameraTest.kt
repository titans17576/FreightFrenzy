package titans17576.ftcrc7573.test

import titans17576.freightfrenzy.meet2.camera_init
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode
import java.lang.RuntimeException

class CameraTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op
    override suspend fun op_mode() {
        val feed = camera_init(op)
        op.launch {
            op.while_live(false) {
                op.telemetry.addData("Location", feed.get_next(this))
            }
        }
        op.stop_event.await()
    }
}