package titans17576.ftcrc7573.test

import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.TouchSensor
import kotlinx.coroutines.delay
import titans17576.ftcrc7573.AsyncOpMode
import titans17576.ftcrc7573.DeferredAsyncOpMode

class ServoTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op
    var servoright: Servo? = null
    var servoname = ""
    //val servoleft = op.hardwareMap["outtake_left"] as Servo
    var position = 0.0
    //var position2 = 0.0
    override suspend fun op_mode() {
        //servoleft.direction = Servo.Direction.REVERSE
        //op.launch { servo_direction_change(servoleft, op.gamepad2) }
        op.launch { servo_switcher() }
        op.launch { servo_direction_change(op.gamepad1) }

        op.start_signal.await()
        op.while_live {
            if (op.gamepad1.dpad_up) position += 0.01
            if (op.gamepad1.dpad_down) position -= 0.01
            //if (op.gamepad2.dpad_up) position2 += 0.01
            //if (op.gamepad2.dpad_down) position2 -= 0.01
            servoright!!.position = position
            //servoleft.position = position
            op.telemetry.addData("Servo Direction Right: ", servoright!!.direction)
            op.telemetry.addData("Servo Value Right: ", position)
            op.telemetry.addData("Servo Name", servoname);
            //op.telemetry.addData("Servo Direction Left: ", servoleft.direction)
            //op.telemetry.addData("Servo Value Left: ", position)
            op.telemetry.update()
            delay(100)
        }
        //Midd position = 0.4
        //End position = 0.71
    }

    suspend fun servo_direction_change(gamepad: Gamepad) {
        op.start_signal.await()
        op.while_live {
            if (gamepad.a) {
                if (servoright!!.direction == Servo.Direction.FORWARD) servoright!!.direction = Servo.Direction.REVERSE
                else servoright!!.direction = Servo.Direction.FORWARD
                delay(1000)
            }
        }
    }

    suspend fun servo_switcher() {
        var iter = op.hardwareMap.servo.entrySet().iterator();
        var entry = iter.next()!!
        servoright = entry.value
        servoname = entry.key
        op.start_signal.await()
        op.while_live {
            if (op.gamepad1.b) {
                if (!iter.hasNext()) iter = op.hardwareMap.servo.entrySet().iterator();
                entry = iter.next()!!
                servoright = entry.value
                servoname = entry.key
                delay(1000)
            }
        }
    }
}

class TouchTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op
    val touchSensor = op.hardwareMap["lift_limit"] as TouchSensor
    override suspend fun op_mode() {
        op.start_signal.await()
        op.while_live {
            op.telemetry.addData("Touch Sensor Pressed", touchSensor.isPressed)
            op.telemetry.update()
        }
    }
}

class DoubleServoTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op
    var servoright = op.hardwareMap["intake_clamp"] as Servo
    //var servoleft = op.hardwareMap["outtake_left"] as Servo
    var position = 0.0
    override suspend fun op_mode() {
        //op.launch { servo_switcher() }
        //op.launch { servo_direction_change(op.gamepad1) }
        //servoleft.direction = Servo.Direction.REVERSE

        op.start_signal.await()
        op.while_live {
            if (op.gamepad1.dpad_up) position += 0.01
            if (op.gamepad1.dpad_down) position -= 0.01
            servoright!!.position = position
            //servoleft!!.position = position
            op.telemetry.addData("Servo Value: ", position)
            op.telemetry.update()
            delay(100)
        }
        //Midd position = 0.4
        //End position = 0.71
    }
}