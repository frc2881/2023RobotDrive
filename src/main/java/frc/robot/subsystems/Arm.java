// Copyright (c) 2023 FRC Team 2881 - The Lady Cans
//
// Open Source Software; you can modify and/or share it under the terms of BSD
// license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class Arm extends SubsystemBase {
  private Intake m_intake;
  private final CANSparkMax m_extensionMotor;
  private final SparkMaxPIDController m_extensionPID;
  private final RelativeEncoder m_extensionMotorEncoder;
  private final CANSparkMax m_tiltMotor;
  private final RelativeEncoder m_tiltMotorEncoder;
  private final SparkMaxPIDController m_tiltPID;
  private boolean extendIsSafe;
  private boolean tiltIsSafe;

  /** Creates a new Arm. */
  public Arm() {
    m_extensionMotor = new CANSparkMax(Constants.Arm.kExtensionMotorId, MotorType.kBrushless);
    m_extensionMotor.setIdleMode(IdleMode.kBrake); 
    m_extensionMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);
    m_extensionMotor.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward,
                       (float)Constants.Arm.kExtendForwardLimit); 
    m_extensionMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true);
    m_extensionMotor.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse,
                       (float)Constants.Arm.kExtendReverseLimit);

    m_extensionMotorEncoder = m_extensionMotor.getEncoder();
    m_extensionMotorEncoder.setPositionConversionFactor(Constants.Arm.kExtendRotationsToInches);
    
    m_extensionPID = m_extensionMotor.getPIDController();
    m_extensionPID.setFeedbackDevice(m_extensionMotorEncoder);
    m_extensionPID.setP(Constants.Arm.kExtensionP);
    m_extensionPID.setOutputRange(Constants.Arm.kExtensionMinOutput,
                                  Constants.Arm.kExtensionMaxOutput);



    m_tiltMotor = new CANSparkMax(Constants.Arm.kTiltMotorId, MotorType.kBrushless);
    m_tiltMotor.setIdleMode(IdleMode.kBrake);
    m_tiltMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);
    m_tiltMotor.setSoftLimit(CANSparkMax.SoftLimitDirection.kForward,
                       (float)Constants.Arm.kTiltForwardLimit);
    m_tiltMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true);
    m_tiltMotor.setSoftLimit(CANSparkMax.SoftLimitDirection.kReverse,
                       (float)Constants.Arm.kTiltReverseLimit);

    m_tiltMotorEncoder = m_tiltMotor.getEncoder();
    m_tiltMotorEncoder.setPositionConversionFactor(Constants.Arm.kTiltRotationsToInches);

    m_tiltPID = m_tiltMotor.getPIDController();
    m_tiltPID.setP(Constants.Arm.kTiltP);
    m_tiltPID.setOutputRange(Constants.Arm.kTiltMinOutput,
                             Constants.Arm.kTiltMaxOutput);

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Arm/Tilt/Encoder Position", m_tiltMotorEncoder.getPosition());
    SmartDashboard.putNumber("Arm/Extend/Encoder Position", m_extensionMotorEncoder.getPosition());
  }

  /**
   * Extends or retracts the arm
   * @param speed positive value extends
   */
  public void runExtension(double speed) { 
    m_extensionMotor.set(speed);
  }

  /**
   * Tilts the arm.
   * @param speed positive value goes up.
   */
  public void runTilt(double speed) {
    m_tiltMotor.set(speed);
  }

  
  /*
   * Sets the Extension position to given value. 15 inches of movement.
   */
  public void setDesiredExtensionPosition(double position) {
    m_extensionPID.setReference(position, CANSparkMax.ControlType.kPosition);
  }
  
  /*
   * Sets the Tilt position to given value
   */
  public void setDesiredTiltPosition(double position) {
    m_tiltPID.setReference(position, CANSparkMax.ControlType.kPosition);
  }

  // In inches
  public Double getExtensionEncoderPosition(){
    return m_extensionMotorEncoder.getPosition();
  }

  // In inches
  public Double getTiltEncoderPosition(){
    return m_tiltMotorEncoder.getPosition();
  }

  public void resetTiltEncoder() {
    m_tiltMotorEncoder.setPosition(-0.1);
  }

  public void resetExtensionEncoder() {
    m_extensionMotorEncoder.setPosition(-0.1);
  }

  public void enableTiltSoftLimits(boolean enable){
    if(enable == true){
      m_tiltMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);
      m_tiltMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true);
    } else {
      m_tiltMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, false);
      m_tiltMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, false);
    }
  }

  public void enableExtendSoftLimits(boolean enable){
    if(enable == true){
      m_extensionMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, true);
      m_extensionMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, true);
    } else {
      m_extensionMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kForward, false);
      m_extensionMotor.enableSoftLimit(CANSparkMax.SoftLimitDirection.kReverse, false);
    }
  }

  public boolean isSafeToExtend(){
    double tilt = m_tiltMotorEncoder.getPosition();

    if(tilt < Constants.Arm.kMinSafeTilt){
      extendIsSafe = false; 
    } else {
      extendIsSafe = true;
    }

    return extendIsSafe;
  }

  public boolean isSafeToTilt(){
    double extensionPosition = m_extensionMotorEncoder.getPosition();
    double tiltPosition = m_tiltMotorEncoder.getPosition();

    if(tiltPosition > Constants.Arm.kMinSafeTilt){
      tiltIsSafe = true;
    } else{
      if(extensionPosition > 0){
        tiltIsSafe = false;
      } else{
        tiltIsSafe = true;
      }
    }
    return tiltIsSafe;
  }

}
