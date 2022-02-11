package titans17576.freightfrenzy.Regionals

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import titans17576.ftcrc7573.*

class Barcode_Carousel_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(
    -40.0, //if (is_red) -28.0 else -40.0,
    -63.0 * (if (is_red) 1.0 else -1.0),
    90.0.toRadians * (if (is_red) 1.0 else -1.0)
), factory) {
    val side = if (is_red) 1.0 else -1.0

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .forward(10.0)
                .turn(180.0.toRadians)
                .setReversed(true)
                .splineTo(Vector2d(-12.0, -42.0 * side), 90.0.toRadians * side)
                .setReversed(false)
                .splineTo(Vector2d(-52.0, -55.0*side), -135.0.toRadians * side)
                .build()
        )
        if (is_red) {
            /*trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(-60.0, -50.0 * side), -90.0.toRadians * side)
                    .lineTo(Vector2d(-60.0,-53.0 * side))
                    //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                    .build()
            )*/
        } else {
            trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(-55.0, -55.0 * side), -180.0.toRadians * side)
                    .lineTo(Vector2d(-58.0, -55.0 * side))
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

class Carousel_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
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
                .forward(10.0)
                .setReversed(false)
                .splineTo(Vector2d(-52.0, -55.0*side), -135.0.toRadians * side)
                .build()
        )
        if (is_red) {
            /*trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(-60.0, -50.0 * side), -90.0.toRadians * side)
                    .lineTo(Vector2d(-60.0,-53.0 * side))
                    //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                    .build()
            )*/
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



class Barcode_Carousel_Warehouse_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
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
                .forward(10.0)
                .turn(180.0.toRadians)
                .setReversed(true)
                .splineTo(Vector2d(-12.0, -42.0 * side), 90.0.toRadians * side)
                .setReversed(false)
                .splineTo(Vector2d(-52.0, -55.0*side), -135.0.toRadians * side)
                .build()
        )
        if (is_red) {
            /*trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(-60.0, -50.0 * side), -90.0.toRadians * side)
                    .lineTo(Vector2d(-60.0,-53.0 * side))
                    //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                    .build()
            )*/
        } else {
            trajectories.add(
                new_movement()
                    .setReversed(false)
                    .splineTo(Vector2d(20.0, -60.0 * side), -180.0.toRadians * side)
                    //.splineTo(Vector2d(40.0, -45.0 * side), 0.0)
                    .build()
            )
        }
        trajectories.add(
            new_movement()
                .setReversed(false)
                .back(12.0)
                .turn(90.0.toRadians * side)
                .splineTo(Vector2d(0.0, -63.0 * side), 0.0 * side)
                .splineTo(Vector2d(50.0, -63.0 * side), 0.0 * side)
                .build()
        )
    }

}
