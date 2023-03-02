// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.commands.auto;

import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;

import frc.robot.commands.arm.ResetArm;
import frc.robot.subsystems.ArmExtension;
import frc.robot.subsystems.ArmTilt;
import frc.robot.subsystems.Drive;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Suction;

public class AutoSequenceA extends SequentialCommandGroup {
  public AutoSequenceA(
    Drive drive, 
    Suction suction, 
    ArmExtension armExtension, 
    ArmTilt armTilt, 
    Intake intake, 
    PathPlannerTrajectory trajectory, 
    PathPlannerTrajectory trajectory2
  ) {
    addCommands(
      new AutoScoreHigh(suction, armExtension, armTilt, intake),
      new ParallelCommandGroup(
        new ResetArm(armExtension, armTilt, 1.0),
        new FollowTrajectory(trajectory, true, drive)
      ),
      new FollowTrajectory(trajectory2, false, drive)
    );
  }
}