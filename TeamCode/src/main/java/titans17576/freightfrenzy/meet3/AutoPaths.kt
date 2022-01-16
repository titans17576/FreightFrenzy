package titans17576.freightfrenzy.meet3

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import titans17576.ftcrc7573.*

class NoCarouselAutoPath(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(-28.0, -63.0 * (if (is_red) 1.0 else -1.0), 90.0.toRadians * (if (is_red) 1.0 else -1.0)), factory) {
    val side = if (is_red) 1.0 else -1.0

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .splineTo(Vector2d(-58.0, -35.0 * side), 90.0.toRadians * side)
                .splineTo(Vector2d(-58.0, -12.0 * side), 90.0.toRadians * side)
                .waitSeconds(1.5)
                .setReversed(true)
                .splineTo(Vector2d(-30.0, -18 * side), -20.0.toRadians * side)
                .build()
        )
        trajectories.add(
            new_movement()
                .setReversed(false)
                .splineTo(Vector2d(-60.0, -50.0 * side), -90.0.toRadians * side)
                //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                .build()
        )
        trajectories.add(
            new_movement()
                .setReversed(true)
                .splineTo(Vector2d(-60.0, -37.5 * side), -90.0.toRadians * side)
                .build()
        )
    }

}