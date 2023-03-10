// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot;

import com.pathplanner.lib.PathPlanner;
import com.pathplanner.lib.PathPlannerTrajectory;

import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType;
import edu.wpi.first.wpilibj.RobotState;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.Trigger;
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
import frc.robot.commands.auto.AutoBalance;
import frc.robot.commands.auto.AutoMiddleScoreMove;
import frc.robot.commands.auto.AutoMove;
import frc.robot.commands.auto.AutoScore;
import frc.robot.commands.auto.AutoScoreBalance;
import frc.robot.commands.auto.AutoScoreMove;
import frc.robot.commands.clamps.AttachLeft;
import frc.robot.commands.clamps.AttachRight;
import frc.robot.commands.clamps.ReleaseLeft;
import frc.robot.commands.clamps.ReleaseRight;
import frc.robot.commands.controllers.RumbleControllers;
import frc.robot.commands.controllers.RumbleControllers.RumblePattern;
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
import frc.robot.subsystems.Clamps;
import frc.robot.subsystems.Drive;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.PrettyLights;
import frc.robot.subsystems.Suction;
import frc.robot.subsystems.PrettyLights.PanelLocation;
import frc.robot.subsystems.PrettyLights.Pattern;

public class RobotContainer {
  private final PowerDistribution m_powerDistribution = new PowerDistribution(1, ModuleType.kRev);
  private Drive m_drive = new Drive();
  private Suction m_suction = new Suction();
  private ArmExtension m_armExtension = new ArmExtension();
  private ArmTilt m_armTilt = new ArmTilt();
  private Intake m_intake = null; // HACK: disabling intake since not installed
  private Clamps m_clamps = new Clamps();
  private PrettyLights m_lights = new PrettyLights();

  private final XboxController m_driverController = new XboxController(Constants.Controllers.kDriverControllerPort);
  private final XboxController m_manipulatorController = new XboxController(Constants.Controllers.kManipulatorControllerPort);

  private final SendableChooser<Command> m_autonomousChooser = new SendableChooser<Command>();
 
  public RobotContainer() {
    setupDrive(); 
    setupControllers();
    setupAuto();
    setupLights();
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
    m_drive.resetSwerve();
  }

  private void setupControllers() {
    
    // DRIVER
    new Trigger(() -> Math.abs(m_driverController.getRightTriggerAxis()) > 0.9)
      .whileTrue(new DriveRobotCentric(m_drive));

    new Trigger(m_driverController::getBackButton)
      .onTrue(new ZeroHeading(m_drive));

    new Trigger(m_driverController::getStartButton)
      .onTrue(new ResetSwerve(m_drive));

    new Trigger(() -> m_driverController.getRightBumper())
      .whileTrue(new AttachRight(m_clamps));

    new Trigger(() -> m_driverController.getPOV() == 90)
      .whileTrue(new ReleaseRight(m_clamps));

    new Trigger(() -> m_driverController.getLeftBumper())
      .whileTrue(new AttachLeft(m_clamps));

    new Trigger(() -> m_driverController.getPOV() == 270)
      .whileTrue(new ReleaseLeft(m_clamps));

    // new Trigger(m_driverController::getAButton)
    //   .whileTrue(new RunRollersInward(m_intake));

    // new Trigger(m_driverController::getBButton)
    //   .whileTrue(new RunRollersOutward(m_intake));

    // MANIPULATOR

    /* Toggles Suction on or off */
    new Trigger(m_manipulatorController::getAButton)
      .onTrue(new ToggleSuction(m_suction));

    new Trigger(m_manipulatorController::getXButton)
      .onTrue(new InstantCommand(() -> {m_lights.setPattern(Pattern.Heart, PanelLocation.Both);}));

    new Trigger(m_manipulatorController::getBButton)
      .onTrue(new InstantCommand(() -> {m_lights.setPattern(Pattern.None, PanelLocation.Both);}));

    new Trigger(m_manipulatorController::getLeftBumper)
      .onTrue(new InstantCommand(() -> {m_lights.setPattern(Pattern.Cube, PanelLocation.Both);}));

    new Trigger(m_manipulatorController::getRightBumper)
      .onTrue(new InstantCommand(() -> {m_lights.setPattern(Pattern.Cone, PanelLocation.Both);}));

    /* Runs the arm using joysticks */
    new Trigger(() -> Math.abs(m_manipulatorController.getLeftY()) > 0.1)
      .whileTrue(new ExtendArm(m_armExtension, m_manipulatorController::getLeftY));

    new Trigger(() -> Math.abs(m_manipulatorController.getRightY()) > 0.1)
      .whileTrue(new TiltArm(m_armTilt, m_manipulatorController::getRightY));

    /* Overrides soft limits and zeros the arm */
    new Trigger(m_manipulatorController::getBackButton)
      .whileTrue(new ArmExtendOverride(m_armExtension));

    new Trigger(m_manipulatorController::getStartButton)
      .whileTrue(new ArmTiltOverride(m_armTilt));
    
    /* Uses D-Pad to move the arm to position */
    new Trigger(() -> m_manipulatorController.getPOV() == 0)
      .whileTrue(new MoveToHigh(m_armExtension, m_armTilt, 1.0));

    new Trigger(() -> m_manipulatorController.getPOV() == 90)
      .whileTrue(new MoveToMedium(m_armExtension, m_armTilt, 1.0));

    new Trigger(() -> m_manipulatorController.getPOV() == 180)
      .whileTrue(new MoveToLow(m_armExtension, m_armTilt, 1.0));

    new Trigger(() -> m_manipulatorController.getPOV() == 270)
      .whileTrue(new MoveToPickup(m_armExtension, m_armTilt, 1.0, m_suction));

    /* Uses D-Pad + Y button to score */
    new Trigger(() -> m_manipulatorController.getPOV() == 0)
      .and(m_manipulatorController::getYButton)
      .whileTrue(new ScoreHigh(m_armExtension, m_armTilt, 1.0, m_suction));

    new Trigger(() -> m_manipulatorController.getPOV() == 90)
      .and(m_manipulatorController::getYButton)
      .whileTrue(new ScoreMedium(m_armExtension, m_armTilt, 1.0, m_suction));

    // RUMBLES
    new Trigger(() -> (RobotState.isTeleop() && m_suction.hasMinVacuum()))
      .onTrue(new RumbleControllers(m_driverController, m_manipulatorController, RumblePattern.GOOD))
      .onFalse(new RumbleControllers(m_driverController, m_manipulatorController, RumblePattern.BAD));
  }

  public void setupAuto() {
    
    PathPlannerTrajectory balancePath = PathPlanner.loadPath("Balance", 1.0, 1.0);
    PathPlannerTrajectory balanceMidPath = PathPlanner.loadPath("Balance Mid", 1.0, 1.0);
    PathPlannerTrajectory moveWallPath = PathPlanner.loadPath("Move Wall", 1.5, 1.5);
    PathPlannerTrajectory moveDividerPath = PathPlanner.loadPath("Move Divider", 1.5, 1.5);
    PathPlannerTrajectory moveMiddlePath = PathPlanner.loadPath("Move Middle", 3, 3);
    PathPlannerTrajectory wallBalancePath = PathPlanner.loadPath("Wall Balance", 2, 3);
    PathPlannerTrajectory dividerBalancePath = PathPlanner.loadPath("Divider Balance", 2, 3);
    PathPlannerTrajectory middleBalancePath = PathPlanner.loadPath("Middle Balance", 2, 3);
    
    m_autonomousChooser.setDefaultOption("None", null);

    m_autonomousChooser.addOption("Score", 
      new AutoScore(m_suction, m_armExtension, m_armTilt, m_intake));

    m_autonomousChooser.addOption("Middle Balance",
      new AutoBalance(m_drive, middleBalancePath, balanceMidPath));

    m_autonomousChooser.addOption("Middle Score Move",
      new AutoMiddleScoreMove(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, moveMiddlePath));

    m_autonomousChooser.addOption("Middle Score Balance", 
      new AutoScoreBalance(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, middleBalancePath, balanceMidPath));

    m_autonomousChooser.addOption("Divider Move", 
      new AutoMove(m_drive, moveDividerPath));
    
    m_autonomousChooser.addOption("Divider Score Move", 
      new AutoScoreMove(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, moveDividerPath));

    m_autonomousChooser.addOption("Divider Score Balance", 
      new AutoScoreBalance(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, dividerBalancePath, balancePath));

    m_autonomousChooser.addOption("Wall Move", 
      new AutoMove(m_drive, moveWallPath));

    m_autonomousChooser.addOption("Wall Score Move", 
      new AutoScoreMove(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, moveWallPath));

    m_autonomousChooser.addOption("Wall Score Balance", 
      new AutoScoreBalance(m_drive, m_suction, m_armExtension, m_armTilt, m_intake, wallBalancePath, balancePath));

    SmartDashboard.putData("Auto/Command", m_autonomousChooser);
  }

  public Command getAutonomousCommand() {
    return m_autonomousChooser.getSelected();
  }

  private void setupLights() {
    m_lights.setPattern(Pattern.Heart, PanelLocation.Both);
  }
  
  public void resetRobot() {
      m_drive.resetSwerve();
      m_drive.resetPhotonCameras();
      m_suction.reset();
      m_armTilt.reset();
      m_armExtension.reset();
  }

}
