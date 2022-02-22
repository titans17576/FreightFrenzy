package titans17576.ftcrc7573;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.PathBuilder;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint;
import org.firstinspires.ftc.teamcode.drive.Meet2Drive
import org.firstinspires.ftc.teamcode.drive.RegionalsDrive
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/** A class that returns blank trajectory builders with the appropriate constraints */
interface TrajectoryBuilderFactory {
    fun make_trajectory_builder(start: Pose2d, reversed: Boolean = false): TrajectoryBuilder
    fun make_trajectory_sequence_builder(start: Pose2d): TrajectorySequenceBuilder
}

fun Meet2Drive.trajectory_builder_factory(): TrajectoryBuilderFactory {
    val self = this
    class MyBuilder : TrajectoryBuilderFactory {
        override fun make_trajectory_builder(start: Pose2d, reversed: Boolean): TrajectoryBuilder {
            return self.trajectoryBuilder(start, reversed)
        }
        override fun make_trajectory_sequence_builder(start: Pose2d): TrajectorySequenceBuilder {
            return self.trajectorySequenceBuilder(start)
        }
    }
    return MyBuilder()
}

fun RegionalsDrive.trajectory_builder_factory(): TrajectoryBuilderFactory {
    val self = this
    class MyBuilderRegionals : TrajectoryBuilderFactory {
        override fun make_trajectory_builder(start: Pose2d, reversed: Boolean): TrajectoryBuilder {
            return self.trajectoryBuilder(start, reversed)
        }
        override fun make_trajectory_sequence_builder(start: Pose2d): TrajectorySequenceBuilder {
            return self.trajectorySequenceBuilder(start)
        }
    }
    return MyBuilderRegionals()
}

/**
 * Convenience class for building paths. Extend it, and use `new_movement` in your
 * constructor.
 * @param initial_pose - the starting position of the robot
 * @param factory - the trajectory builder factory, which applies to the constraints to builders
 */
open class PathBuilder7573(initial_pose: Pose2d, factory: TrajectoryBuilderFactory) {
    private val factory = factory
    private var queue: ArrayDeque<TrajectorySequenceBuilder> = ArrayDeque()

    /**
     * The initial position for this path, passed from the constructor
     */
    val initial_pose: Pose2d = initial_pose

    /**
     * The built trajectories. Take them out with .poll
     */
    val trajectories: Queue<TrajectorySequence> = LinkedList()

    fun last_builder(): TrajectorySequenceBuilder? {
        return if (this.queue.isEmpty()) null else this.queue.getLast();
    }

    /**
     * Create a new movement, starting from the ending position of the last movement
     * @param reversed - whether the robot should initially drive backwards
     * @return the new builder
     */
    fun new_movement(reversed: Boolean = false): TrajectorySequenceBuilder {
        var starting_pose: Pose2d = initial_pose
        if (last_builder() != null) starting_pose = last_builder()!!.build().end()
        var seq = factory.make_trajectory_sequence_builder(starting_pose).setReversed(reversed)
        queue.addLast(seq);
        return seq;
    }
}