package titans17576.freightfrenzy.Regionals

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder
import titans17576.ftcrc7573.*

fun side(is_red: Boolean): Double { return if (is_red) { 1.0 } else { -1.0 } }

fun TrajectorySequenceBuilder.spline_to_shipping_hub(is_red: Boolean): TrajectorySequenceBuilder {
    val side = side(is_red)
    return this.splineTo(Vector2d(-12.0, -45 * side), 90.0.toRadians * side)
}
fun TrajectorySequenceBuilder.spline_to_depo(is_red: Boolean): TrajectorySequenceBuilder {
    val side = side(is_red)
    return this.splineTo(Vector2d(-60.0, -37.0 * side), 90.0.toRadians * side)
}

fun TrajectorySequenceBuilder.spline_to_warehouse(is_red: Boolean): TrajectorySequenceBuilder {
    val side = side(is_red)
    this.splineTo(Vector2d(12.0, -65.0 * side), 0.0 * side)
    this.splineTo(Vector2d(50.0, -65.0 * side), 0.0 * side)
    return this
}

fun move_to_carousel(b: PathBuilder7573, is_red: Boolean) {
    val side = side(is_red)
    if (is_red) {
        b.trajectories.add(
            b.new_movement()
                .setReversed(false)
                .splineTo(Vector2d(-59.0, -59.0*side), (-130.0).toRadians * side)
                //.turn((30.0).toRadians)
                .build()
        )
    } else {
        b.trajectories.add(
            b.new_movement()
                .setReversed(false)
                .splineTo(Vector2d(-52.0, -64.0*side), (-130.0).toRadians * side)
                .turn(-15.0.toRadians)
                .build()
        )
    }
}

class Barcode_Carousel_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(
    if (is_red) -28.0 else -40.0,
    -63.0 * side(is_red),
    90.0.toRadians * (if (is_red) 1.0 else -1.0)
), factory) {
    val side = side(is_red)

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .forward(10.0)
                .turn(180.0.toRadians)
                .setReversed(true)
                .spline_to_shipping_hub(is_red)
                .build()
        )
        move_to_carousel(this, is_red)
        trajectories.add(
            new_movement()
                .setReversed(true)
                .spline_to_depo(is_red)
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
    val side = side(is_red)

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .forward(10.0)
                .build()
        )
        move_to_carousel(this, is_red)
        trajectories.add(
            new_movement()
                .setReversed(true)
                .spline_to_depo(is_red)
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
    val side = side(is_red)

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .forward(10.0)
                .turn(180.0.toRadians)
                .setReversed(true)
                .spline_to_shipping_hub(is_red)
                .build()
        )

        move_to_carousel(this, is_red)
        trajectories.add(
            new_movement()
                .setReversed(false)
                .back(12.0)
                .turn(90.0.toRadians * side)
                .spline_to_warehouse(is_red)
                .build()
        )
    }

}

class Barcode_Warehouse_Park(val is_red: Boolean, val factory: TrajectoryBuilderFactory)
    : PathBuilder7573 (
    Pose2d(
        //if (is_red) { 18.0 } else { 6.0 },
        if (is_red) { 6.0 } else { 6.0 },
        -63.0 * (if (is_red) 1.0 else -1.0),
        90.0.toRadians * (if (is_red) 1.0 else -1.0)
    ),
    factory) {
    val side = side(is_red)

    init {
        trajectories.add(
            new_movement()
                .spline_to_shipping_hub(is_red)
                .turn(180.0.toRadians)
                .build(),
        )
        trajectories.add(
            new_movement()
                .spline_to_warehouse(is_red)
                .build()

        )
    }
}

class Barcode_Warehouse_Twice_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(
    if (is_red) -28.0 else -40.0,
    -63.0 * side(is_red),
    90.0.toRadians * (if (is_red) 1.0 else -1.0)
), factory) {
    val side = side(is_red)

    init {
        trajectories.add(
            new_movement()
                .setReversed(false)
                .forward(11.0)
                .turn(180.0.toRadians)
                .setReversed(true)
                .spline_to_shipping_hub(is_red)
                .build()
        )

        var counter = 0;

        while(counter < 3){
            trajectories.add(
                new_movement()
                    .spline_to_warehouse(is_red)
                    .back(40.0)
                    .build()
            )

            trajectories.add(
                new_movement()
                    .spline_to_shipping_hub(is_red)
                    .build()
            )
            counter++
        }

        trajectories.add(
            new_movement()
                .spline_to_warehouse(is_red)
                .back(5.0)
                .build()
        )
    }
}

class Barcode_Carousel_Element_Park(is_red: Boolean, factory: TrajectoryBuilderFactory)
    : PathBuilder7573(Pose2d(
    if (is_red) -28.0 else -40.0,
    -63.0 * side(is_red),
    90.0.toRadians * (if (is_red) 1.0 else -1.0)
), factory) {
    val side = side(is_red)

    init {
        trajectories.add(
            new_movement()
                .spline_to_shipping_hub(is_red)
                .build()
        )

        var counter = 0;

        while(counter < 2){
            trajectories.add(
                new_movement()
                    .spline_to_warehouse(is_red)
                    .back(40.0)
                    .spline_to_shipping_hub(is_red)
                    .build()
            )
            counter++
        }

        trajectories.add(
            new_movement()
                .back(4.0)
                .splineTo(Vector2d(-60.0, -63.0 * side), 180.0.toRadians * side)
                .build()
        )

        trajectories.add(
            new_movement()
                .splineTo(Vector2d(-60.0, -35.0 * side), 90.0.toRadians * side)
                .build()
        )
    }
}
