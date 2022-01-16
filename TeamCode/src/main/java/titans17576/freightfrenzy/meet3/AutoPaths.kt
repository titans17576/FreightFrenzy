package titans17576.freightfrenzy.meet3

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import titans17576.ftcrc7573.*

class Path_StartCarousel_Vision_Carousel_Depo(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(
    if (is_red) -28.0 else -40.0,
    -63.0 * (if (is_red) 1.0 else -1.0),
    90.0.toRadians * (if (is_red) 1.0 else -1.0)
), factory) {
    val side = if (is_red) 1.0 else -1.0

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .splineTo(Vector2d(-55.0, -35.0 * side), 90.0.toRadians * side)
                .lineTo(Vector2d(-55.0, -12.0 * side))
                .lineTo(Vector2d(-55.0, -12.5 * side))
                .waitSeconds(1.5)
                .setReversed(true)
                .splineTo(Vector2d(-30.0, -18 * side), -20.0.toRadians * side)
                .lineTo(Vector2d(-30.0 + Math.cos((-20.0.toRadians * side)), -18 * side + Math.sin(-20.0.toRadians * side)))
                .lineTo(Vector2d(-30.0, -18 * side))
                .build()
        )
        if (is_red) {
            trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(-60.0, -50.0 * side), -90.0.toRadians * side)
                    .lineTo(Vector2d(-60.0,-53.0 * side))
                    //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                    .build()
            )
        } else {
            trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(-50.0, -60.0 * side), -180.0.toRadians * side)
                    .lineTo(Vector2d(-53.0, -60.0 * side))
                    //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                    .build()
            )
        }
        trajectories.add(
            new_movement()
                .setReversed(true)
                .splineTo(Vector2d(-60.0, -36.5 * side), 90.0.toRadians * side)
                .lineTo(Vector2d(-60.0, -36 * side))
                .lineTo(Vector2d(-60.0, -36.5 * side))
                .build()
        )
    }

}

class Path_StartWarehouse_Vision_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(
    if (is_red) 12.0 else 4.0,
    -63.0 * (if (is_red) 1.0 else -1.0),
    90.0.toRadians * (if (is_red) 1.0 else -1.0)
), factory) {
    val side = if (is_red) 1.0 else -1.0

    init {
        trajectories.add(
            new_movement()
                .splineTo(Vector2d(8.25, -24.0 * side), 90.0.toRadians * side)
                .waitSeconds(5.0)
                .lineTo(Vector2d(8.25, -23.0 * side))
                .lineTo(Vector2d(8.25, -24.0 * side))
                .turn(-90.0.toRadians * side)
                //.splineTo(Vector2d(7.0, -24.0 * side), -180.0.toRadians * side)
                .build()
        )
        trajectories.add(
            new_movement()
                .turn(90.0.toRadians * side)
                .lineTo(Vector2d(8.25, -40.0 * side))
                .turn(-90.0.toRadians * side)
                .lineTo(Vector2d(41.5, -40.0 * side))
                .build()
        )
    }
}