package titans17576.ftcrc7573;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.PathBuilder;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint;
import org.firstinspires.ftc.teamcode.drive.Meet2Drive
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequenceBuilder

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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

open class PathBuilder7573(initial_pose: Pose2d, factory: TrajectoryBuilderFactory) {
    val initial_pose: Pose2d = initial_pose
    /*var vel_constraint: TrajectoryVelocityConstraint = vel_constraint
    var accel_constraint: TrajectoryAccelerationConstraint = accel_constraint
    var max_vel: Double = max_vel
    var max_acc: Double = max_acc*/
    private val factory = factory
    private var queue: ArrayDeque<TrajectorySequenceBuilder> = ArrayDeque()
    val trajectories: Queue<TrajectorySequence> = LinkedList()
    fun last_builder(): TrajectorySequenceBuilder? {
        return if (this.queue.isEmpty()) null else this.queue.getLast();
    }

    protected fun new_movement(): TrajectorySequenceBuilder { return new_movement(false); }
    protected fun new_movement(reversed: Boolean): TrajectorySequenceBuilder {
        var starting_pose: Pose2d = initial_pose
        if (last_builder() != null) starting_pose = last_builder()!!.build().end()
        //TrajectoryBuilder builder = new TrajectoryBuilder(starting_pose, reversed, vel_constraint, accel_constraint);
        //var seq: TrajectorySequenceBuilder = TrajectorySequenceBuilder(starting_pose, this.vel_constraint, this.accel_constraint, this.max_vel, this.max_acc)
        var seq = factory.make_trajectory_sequence_builder(starting_pose).setReversed(reversed)
        queue.addLast(seq);
        return seq;
    }
    //Continuity Exeptions go brrrr
    /*protected TrajectoryBuilder continue_movement() {
        return new TrajectoryBuilder(last_builder().build(), last_builder().build().duration(), constraints);
    }*/
}