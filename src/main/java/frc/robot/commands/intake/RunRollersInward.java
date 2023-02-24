// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.commands.intake;

import edu.wpi.first.wpilibj2.command.CommandBase;

import frc.robot.subsystems.Intake;

public class RunRollersInward extends CommandBase {
  private Intake m_intake;
  
  public RunRollersInward(Intake intake) {
    m_intake = intake;
    addRequirements(m_intake);
  }

  @Override
  public void initialize() {
    m_intake.runRollersInward();
  }

  @Override
  public void execute() {}

  @Override
  public void end(boolean interrupted) {
    m_intake.stopRollers();
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}
