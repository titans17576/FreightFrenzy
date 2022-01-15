package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import org.firstinspires.ftc.teamcode.drive.Meet2Drive
import titans17576.ftcrc7573.*

class ChristianAutoTest(op: AsyncOpMode) : DeferredAsyncOpMode {
    val op = op
    override suspend fun op_mode() {
        op.start_signal.await()
        val bot = Meet2Drive(op.hardwareMap)
        val path = NoCarouselAutoPath(true, bot.trajectory_builder_factory())
        bot.poseEstimate = path.initial_pose
        for (traj in path.trajectories) {
            follow_trajectory(traj, bot, op)
        }
    }
}

public class NoCarouselAutoPath(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(-36.0, -63.0 * (if (is_red) 1.0 else -1.0), -90.0.toRadians), factory) {
    val side = if (is_red) 1.0 else -1.0

    init {
        trajectories.add(
            new_movement()
                .setReversed(true)
                .splineTo(Vector2d(-30.0, -32 * side), 20.0.toRadians)
                .build()
        )
        trajectories.add(
            new_movement()
                .setReversed(false)
                .splineTo(Vector2d(-10.0, 0.0 * side), 0.0)
                .splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                .build()
        )
        /*trajectories.add(
                new_movement()
                    .splineTo(Vector2d(-58.0, -25.0 * side), -90.0.toRadians * side)
                    .splineTo(Vector2d(-45.0, 6.0 * side), 15.0.toRadians * side)
                    .splineTo(Vector2d(-60.0, -6.0 * side), -90.0.toRadians * side)
                    .splineTo(Vector2d(-60.0, -60.0 * side), -90.0.toRadians * side)
                    .build()*/
    }

}