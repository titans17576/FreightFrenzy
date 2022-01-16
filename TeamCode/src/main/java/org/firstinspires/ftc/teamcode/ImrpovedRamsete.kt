package org.firstinspires.ftc.teamcode

/*import com.acmerobotics.roadrunner.drive.DriveSignal
import com.acmerobotics.roadrunner.followers.TrajectoryFollower
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.kinematics.Kinematics
import com.acmerobotics.roadrunner.util.NanoClock
import com.acmerobotics.roadrunner.util.epsilonEquals
import org.firstinspires.ftc.teamcode.roadrunnerext.RamseteConstants.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Time-varying, non-linear feedback controller for nonholonomic drives. See equation 5.12 of
 * [Ramsete01.pdf](https://www.dis.uniroma1.it/~labrob/pub/papers/Ramsete01.pdf).
 *
 * @param b b parameter (non-negative)
 * @param zeta zeta parameter (on (0, 1))
 * @param admissibleError admissible/satisfactory pose error at the end of each move
 * @param timeout max time to wait for the error to be admissible
 * @param clock clock
 */
class ImprovedRamsete @JvmOverloads constructor(
    b: Double,
    zeta: Double,
    admissibleError: Pose2d = Pose2d(2.0, 2.0, Math.toRadians(5.0)),
    timeout: Double =  0.5,
    clock: NanoClock = NanoClock.system(),
) : TrajectoryFollower(admissibleError, timeout, clock) {
    override var lastError: Pose2d = Pose2d()
    val b = b
    val zeta = zeta

    private fun sinc(x: Double) =
        if (x epsilonEquals 0.0) {
            1.0 - x * x / 6.0
        } else {
            sin(x) / x
        }

    override fun internalUpdate(currentPose: Pose2d, currentRobotVel: Pose2d?): DriveSignal {
        val currentPose = currentPose
        val t = elapsedTime()
        val targetPose = trajectory[t]
        val targetVel = trajectory.velocity(t)
        val targetAccel = if(t > trajectory.duration()) Pose2d() else trajectory.acceleration(t)

        val targetRobotVel = Kinematics.fieldToRobotVelocity(targetPose, targetVel)
        val targetRobotAccel = Kinematics.fieldToRobotAcceleration(targetPose, targetVel, targetAccel)

        val targetV = targetRobotVel.x
        val targetOmega = targetRobotVel.heading

        val error = Kinematics.calculateFieldPoseError(targetPose, currentPose)

        val k1 = 2 * zeta * sqrt(targetOmega * targetOmega + b * targetV * targetV)
        val k3 = k1
        val k2 = b

        val v = targetV * cos(error.heading) +
                k1 * (cos(currentPose.heading) * error.x + sin(currentPose.heading) * error.y)
        val omega = targetOmega + k2 * targetV * sinc(error.heading) *
                (cos(currentPose.heading) * error.y - sin(currentPose.heading) * error.x) +
                k3 * error.heading

        val outV = v + (currentRobotVel?.?.let { kLinear * (v - it.x) } ?: 0.0)

        val outOmega = omega + (currentRobotVel?.let { kHeading * (omega - it.heading) } ?: 0.0)

        lastError = Kinematics.calculateRobotPoseError(targetPose.toInches(), currentPose.toInches())

        return DriveSignal(Pose2d(outV, 0.0, outOmega).toInches(), targetRobotAccel.toInches())
    }
}
*/