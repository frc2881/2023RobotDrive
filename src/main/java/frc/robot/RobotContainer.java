// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.Arm;
import frc.robot.commands.arm.ArmExtendOverride;
import frc.robot.commands.arm.ArmTiltOverride;
import frc.robot.commands.arm.ExtendArm;
import frc.robot.commands.arm.TiltArm;
import frc.robot.commands.arm.MoveTo.MoveToHigh;
import frc.robot.commands.arm.MoveTo.MoveToLow;
import frc.robot.commands.arm.MoveTo.MoveToMedium;
import frc.robot.commands.arm.MoveTo.MoveToPickup;
import frc.robot.commands.arm.Score.ScoreHigh;
import frc.robot.commands.arm.Score.ScoreMedium;
import frc.robot.commands.auto.FollowTrajectory;
import frc.robot.commands.auto.AutoScoreHigh;
import frc.robot.commands.auto.AutoSequenceA;
import frc.robot.commands.drive.DriveRobotCentric;
import frc.robot.commands.drive.DriveWithJoysticks;
import frc.robot.commands.drive.ResetSwerve;
import frc.robot.commands.drive.ZeroHeading;
//import frc.robot.commands.intake.RunRollersInward;
//import frc.robot.commands.intake.RunRollersOutward;
import frc.robot.commands.suction.ToggleSuction;
import frc.robot.lib.Utils;
import frc.robot.subsystems.ArmExtension;
import frc.robot.subsystems.ArmTilt;
import frc.robot.subsystems.Drive;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.Suction;

public class RobotContainer {
  private Drive m_drive = new Drive();
  private Suction m_suction = new Suction();
  private ArmExtension m_armExtension = new ArmExtension();
  private ArmTilt m_armTilt = new ArmTilt();
  private Intake m_intake = null; // HACK: disabling intake since not installed

  private final XboxController m_driverController = new XboxController(Constants.Controllers.kDriverControllerPort);
  private final XboxController m_manipulatorController = new XboxController(Constants.Controllers.kManipulatorControllerPort);

  private final PathPlannerTrajectory m_flatPath = PathPlanner.loadPath("Flat", 1, 1);

  public boolean m_robotResetState = true;
  
  public RobotContainer() {
    setupDrive(); 
    setupTriggers();
  }

  private void setupDrive() {
    m_drive.setDefaultCommand(
      new DriveWithJoysticks(
        m_drive,
        () -> Utils.applyDeadband(-m_driverController.getLeftY(), Constants.Controllers.kDeadband),
        () -> Utils.applyDeadband(-m_driverController.getLeftX(), Constants.Controllers.kDeadband),
        () -> Utils.applyDeadband(-m_driverController.getRightX(), Constants.Controllers.kDeadband)
      )
    );
    Timer.delay(1);
    m_drive.resetSwerve();
  }

  private void setupTriggers() {
    
    //DRIVER
    new Trigger(() -> Math.abs(m_driverController.getRightTriggerAxis()) > 0.9)
      .whileTrue(new DriveRobotCentric(m_drive));

    new Trigger(m_driverController::getBackButton)
      .onTrue(new ZeroHeading(m_drive));

    new Trigger(m_driverController::getStartButton)
      .onTrue(new ResetSwerve(m_drive));

    // new Trigger(m_driverController::getAButton)
    //   .whileTrue(new RunRollersInward(m_intake));

    // new Trigger(m_driverController::getBButton)
    //   .whileTrue(new RunRollersOutward(m_intake));

    //MANIPULATOR
    new Trigger(m_manipulatorController::getAButton)
      .onTrue(new ToggleSuction(m_suction));

    new Trigger(() -> Math.abs(m_manipulatorController.getLeftY()) > 0.1)
      .whileTrue(new ExtendArm(m_armExtension, m_manipulatorController::getLeftY));

    new Trigger(() -> Math.abs(m_manipulatorController.getRightY()) > 0.1)
      .whileTrue(new TiltArm(m_armTilt, m_manipulatorController::getRightY));

    new Trigger(m_manipulatorController::getBackButton)
      .whileTrue(new ArmExtendOverride(m_armExtension));

    new Trigger(m_manipulatorController::getStartButton)
      .whileTrue(new ArmTiltOverride(m_armTilt));
    
    new Trigger(() -> m_manipulatorController.getPOV() == 0)
      .whileTrue(new MoveToHigh(m_armExtension, m_armTilt, 0.15));

    new Trigger(() -> m_manipulatorController.getPOV() == 90)
      .whileTrue(new MoveToMedium(m_armExtension, m_armTilt, 0.15));

    new Trigger(() -> m_manipulatorController.getPOV() == 180)
      .whileTrue(new MoveToLow(m_armExtension, m_armTilt, 0.15));

    new Trigger(() -> m_manipulatorController.getPOV() == 270)
      .whileTrue(new MoveToPickup(m_armExtension, m_armTilt, 0.15));

    new Trigger(() -> m_manipulatorController.getPOV() == 0)
      .and(m_manipulatorController::getYButton)
      .whileTrue(new ScoreHigh(m_armExtension, m_armTilt, 0.5, m_suction));

    new Trigger(() -> m_manipulatorController.getPOV() == 90)
      .and(m_manipulatorController::getYButton)
      .whileTrue(new ScoreMedium(m_armExtension, m_armTilt, 0.5, m_suction));
  }

  public Command getAutonomousCommand() {
    return new AutoSequenceA(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, m_flatPath);
  }

  public void resetRobot() {
    if(m_robotResetState == true) {
      m_suction.reset();
      m_robotResetState = false;
    }
  }

  public void robotShouldReset() {
    m_robotResetState = true;
  }
}
