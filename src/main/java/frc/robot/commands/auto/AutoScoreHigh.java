// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.commands.arm.ExtendArmToLength;
import frc.robot.commands.arm.ResetArm;
import frc.robot.commands.arm.TiltArmToHeight;
import frc.robot.commands.arm.Score.ScoreHigh;
import frc.robot.commands.suction.DisableSuction;
import frc.robot.commands.suction.EnableSuction;
import frc.robot.subsystems.ArmExtension;
import frc.robot.subsystems.ArmTilt;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Suction;

public class AutoScoreHigh extends SequentialCommandGroup {
  public AutoScoreHigh(Suction suction, ArmExtension armExtension, ArmTilt armTilt, Intake intake) {
    addCommands(
      new EnableSuction(suction),
      new TiltArmToHeight(armTilt, 0.15, 0.67),
      new ExtendArmToLength(armExtension, 0.15, 5.1),
      new WaitUntilCommand(suction::hasVacuumSeal),
      new ExtendArmToLength(armExtension, 0.15, 0.0),
      new ScoreHigh(armExtension, armTilt, 0.15, suction),
      new WaitCommand(0.5),
      new DisableSuction(suction),
      new ResetArm(armExtension, armTilt, 0.15)
    );
  }
}
