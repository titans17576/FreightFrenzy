package titans17576.ftcrc7573

import com.qualcomm.robotcore.hardware.*
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

class Box<T>(v: T) { var v = v }
class Tuple<A, B>(a: A, b: B) { var a = a; var b = b }

class Either<L: Any, R: Any> private constructor(is_left: Boolean, obj: Any) {
    val obj = obj;
    val is_left = is_left;
    val is_right = !is_left;

    companion object {
        fun<L: Any, R: Any> Left(obj: L) = Either<L, R>(true, obj)
        fun<L: Any, R: Any> Right(obj: R) = Either<L, R>(false, obj)
    }

    var left: L? = null
        get() = if (is_left) obj as L else null
    var right: R? = null
        get() = if (is_right) obj as R else null
}

val Double.toRadians: Double
    get() = this/180*Math.PI

class MotorReversedEncoder(motor: DcMotorEx) : DcMotorEx {
    val motor = motor

    override fun getManufacturer(): HardwareDevice.Manufacturer { return motor.getManufacturer(); }

    override fun getDeviceName(): String { return motor.getDeviceName(); }

    override fun getConnectionInfo(): String { return motor.getConnectionInfo(); }

    override fun getVersion(): Int { return motor.getVersion(); }

    override fun resetDeviceConfigurationForOpMode() { return motor.resetDeviceConfigurationForOpMode(); }

    override fun close() { return motor.close(); }

    override fun setDirection(direction: DcMotorSimple.Direction?) { return motor.setDirection(direction); }

    override fun getDirection(): DcMotorSimple.Direction { return motor.getDirection(); }

    override fun setPower(power: Double) { return motor.setPower(power); }

    override fun getPower(): Double { return motor.getPower(); }

    override fun getMotorType(): MotorConfigurationType { return motor.getMotorType(); }

    override fun setMotorType(motorType: MotorConfigurationType?) { return motor.setMotorType(motorType); }

    override fun getController(): DcMotorController { return motor.getController(); }

    override fun getPortNumber(): Int { return motor.getPortNumber(); }

    override fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior?) { return motor.setZeroPowerBehavior(zeroPowerBehavior); }

    override fun getZeroPowerBehavior(): DcMotor.ZeroPowerBehavior { return motor.getZeroPowerBehavior(); }

    override fun setPowerFloat() { return motor.setPowerFloat(); }

    override fun getPowerFloat(): Boolean { return motor.getPowerFloat(); }

    override fun setTargetPosition(position: Int) { return motor.setTargetPosition(-position); }

    override fun getTargetPosition(): Int { return -motor.getTargetPosition(); }

    override fun isBusy(): Boolean { return motor.isBusy(); }

    override fun getCurrentPosition(): Int { return -motor.getCurrentPosition(); }

    override fun setMode(mode: DcMotor.RunMode?) { return motor.setMode(mode); }

    override fun getMode(): DcMotor.RunMode { return motor.getMode(); }

    override fun setMotorEnable() { return motor.setMotorEnable(); }

    override fun setMotorDisable() { return motor.setMotorDisable(); }

    override fun isMotorEnabled(): Boolean { return motor.isMotorEnabled(); }

    override fun setVelocity(angularRate: Double) { return motor.setVelocity(angularRate); }

    override fun setVelocity(angularRate: Double, unit: AngleUnit?) { return motor.setVelocity(angularRate, unit); }

    override fun getVelocity(): Double { return motor.getVelocity(); }

    override fun getVelocity(unit: AngleUnit?): Double { return motor.getVelocity(unit); }

    override fun setPIDCoefficients(mode: DcMotor.RunMode?, pidCoefficients: PIDCoefficients?) { return motor.setPIDCoefficients(mode, pidCoefficients); }

    override fun setPIDFCoefficients(mode: DcMotor.RunMode?, pidfCoefficients: PIDFCoefficients?) { return motor.setPIDFCoefficients(mode, pidfCoefficients); }

    override fun setVelocityPIDFCoefficients(p: Double, i: Double, d: Double, f: Double) { return motor.setVelocityPIDFCoefficients(p, i, d, f); }

    override fun setPositionPIDFCoefficients(p: Double) { return motor.setPositionPIDFCoefficients(p); }

    override fun getPIDCoefficients(mode: DcMotor.RunMode?): PIDCoefficients { return motor.getPIDCoefficients(mode); }

    override fun getPIDFCoefficients(mode: DcMotor.RunMode?): PIDFCoefficients { return motor.getPIDFCoefficients(mode); }

    override fun setTargetPositionTolerance(tolerance: Int) { return motor.setTargetPositionTolerance(tolerance); }

    override fun getTargetPositionTolerance(): Int { return motor.getTargetPositionTolerance(); }

    override fun getCurrent(unit: CurrentUnit?): Double { return motor.getCurrent(unit); }

    override fun getCurrentAlert(unit: CurrentUnit?): Double { return motor.getCurrentAlert(unit); }

    override fun setCurrentAlert(current: Double, unit: CurrentUnit?) { return motor.setCurrentAlert(current, unit); }

    override fun isOverCurrent(): Boolean { return motor.isOverCurrent(); }

}