/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */

package com.sshtools.daemon.windows;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.15 $
 */
public class WindowsProcessInfo {
  private int hProcess = 0;
  private int hStderrRead = 0;
  private int hStderrWrite = 0;
  private int hStdinRead = 0;
  private int hStdinWrite = 0;
  private int hStdoutRead = 0;
  private int hStdoutWrite = 0;
  private int hThread = 0;
  private int processId = 0;
  private int hProfile = 0;

  /**
   * Creates a new WindowsProcessInfo object.
   */
  public WindowsProcessInfo() {
  }

  /**
   *
   *
   * @param hProcess
   */
  public void setProcessHandle(int hProcess) {
    this.hProcess = hProcess;
  }

  /**
   *
   *
   * @return
   */
  public int getProcessHandle() {
    return hProcess;
  }

  /**
   *
   *
   * @param processId
   */
  public void setProcessId(int processId) {
    this.processId = processId;
  }

  /**
   *
   *
   * @param hProfile
   */
  public void setProfileHandle(int hProfile) {
    this.hProfile = hProfile;
  }

  /**
   *
   *
   * @return
   */
  public int getProcessId() {
    return processId;
  }

  /**
   *
   *
   * @param hStderrRead
   */
  public void setStderrReadHandle(int hStderrRead) {
    this.hStderrRead = hStderrRead;
  }

  /**
   *
   *
   * @return
   */
  public int getStderrReadHandle() {
    return hStderrRead;
  }

  /**
   *
   *
   * @param hStderrWrite
   */
  public void setStderrWriteHandle(int hStderrWrite) {
    this.hStderrWrite = hStderrWrite;
  }

  /**
   *
   *
   * @return
   */
  public int getStderrWriteHandle() {
    return hStderrWrite;
  }

  /**
   *
   *
   * @param hStdinRead
   */
  public void setStdinReadHandle(int hStdinRead) {
    this.hStdinRead = hStdinRead;
  }

  /**
   *
   *
   * @return
   */
  public int getStdinReadHandle() {
    return hStdinRead;
  }

  /**
   *
   *
   * @return
   */
  public int getProfileHandle() {
    return hProfile;
  }

  /**
   *
   *
   * @param hStdinWrite
   */
  public void setStdinWriteHandle(int hStdinWrite) {
    this.hStdinWrite = hStdinWrite;
  }

  /**
   *
   *
   * @return
   */
  public int getStdinWriteHandle() {
    return hStdinWrite;
  }

  /**
   *
   *
   * @param hStdoutRead
   */
  public void setStdoutReadHandle(int hStdoutRead) {
    this.hStdoutRead = hStdoutRead;
  }

  /**
   *
   *
   * @return
   */
  public int getStdoutReadHandle() {
    return hStdoutRead;
  }

  /**
   *
   *
   * @param hStdoutWrite
   */
  public void setStdoutWriteHandle(int hStdoutWrite) {
    this.hStdoutWrite = hStdoutWrite;
  }

  /**
   *
   *
   * @return
   */
  public int getStdoutWriteHandle() {
    return hStdoutWrite;
  }

  /**
   *
   *
   * @param hThread
   */
  public void setThreadHandle(int hThread) {
    this.hThread = hThread;
  }

  /**
   *
   *
   * @return
   */
  public int getThreadHandle() {
    return hThread;
  }
}
