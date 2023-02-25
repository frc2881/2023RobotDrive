// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands.auto;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import frc.robot.commands.arm.ExtendArmToLength;
import frc.robot.commands.arm.ResetArm;
import frc.robot.commands.arm.TiltArm;
import frc.robot.commands.arm.TiltArmToHeight;
import frc.robot.commands.arm.Score.ScoreHigh;
import frc.robot.commands.suction.DisableSuction;
import frc.robot.commands.suction.EnableSuction;
import frc.robot.subsystems.Arm;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Suction;

// NOTE:  Consider using this command inline, rather than writing a subclass.  For more
// information, see:
// https://docs.wpilib.org/en/stable/docs/software/commandbased/convenience-features.html
public class SimpleAuto extends SequentialCommandGroup {
  /** Creates a new SimpleAuto. */
  public SimpleAuto(Suction suction, Arm arm, Intake intake) {
    addCommands(
      new EnableSuction(suction),
      new TiltArmToHeight(arm, intake, 0.15, 0.67),
      new ExtendArmToLength(arm, 0.15, 5.1),
      new WaitUntilCommand(suction::hasVacuumSeal),
      new ExtendArmToLength(arm, 0.15, 0.0),
      new ScoreHigh(arm, intake, 0.15, suction),
      new WaitCommand(0.5),
      new DisableSuction(suction),
      new ResetArm(arm, intake, 0.15)
    );
    addRequirements(suction, arm);
  }
}
