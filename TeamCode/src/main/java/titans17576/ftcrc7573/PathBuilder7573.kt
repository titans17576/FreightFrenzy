package titans17576.ftcrc7573;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.PathBuilder;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import acmerobotics.roadrunner.quickstart.trajectorysequence.TrajectorySequence;
import acmerobotics.roadrunner.quickstart.trajectorysequence.TrajectorySequenceBuilder

open class PathBuilder7573(initial_pose: Pose2d, vel_constraint: TrajectoryVelocityConstraint, accel_constraint: TrajectoryAccelerationConstraint, max_vel: Double, max_acc: Double) {
    var initial_pose: Pose2d = Pose2d(0.0)
    var vel_constraint: TrajectoryVelocityConstraint = vel_constraint
    var accel_constraint: TrajectoryAccelerationConstraint = accel_constraint
    var max_vel: Double = max_vel
    var max_acc: Double = max_acc
    var queue: ArrayDeque<TrajectorySequenceBuilder> = ArrayDeque()
    val trajectories: Queue<TrajectorySequence> = LinkedList()
    fun last_builder(): TrajectorySequenceBuilder? {
        return if (this.queue.isEmpty()) null else this.queue.getLast();
    }

    protected fun new_movement(): TrajectorySequenceBuilder { return new_movement(false); }
    protected fun new_movement(reversed: Boolean): TrajectorySequenceBuilder {
        var starting_pose: Pose2d = initial_pose
        if (last_builder() != null) starting_pose = last_builder()!!.build().end()
        //TrajectoryBuilder builder = new TrajectoryBuilder(starting_pose, reversed, vel_constraint, accel_constraint);
        var seq: TrajectorySequenceBuilder = TrajectorySequenceBuilder(starting_pose, this.vel_constraint, this.accel_constraint, this.max_vel, this.max_acc)
        queue.addLast(seq);
        return seq;
    }
    //Continuity Exeptions go brrrr
    /*protected TrajectoryBuilder continue_movement() {
        return new TrajectoryBuilder(last_builder().build(), last_builder().build().duration(), constraints);
    }*/
}