// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.commands.auto;

import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import frc.robot.commands.drive.ZeroHeading;
import frc.robot.subsystems.Drive;

public class AutoMove extends SequentialCommandGroup {

  public AutoMove(
    Drive drive,
    PathPlannerTrajectory trajectory) {
      addCommands(
      new FollowTrajectory(trajectory, true, drive),
      new ZeroHeading(drive)
    );
  }
}
