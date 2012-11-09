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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.daemon.platform.NativeProcessProvider;
import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.27 $
 */
public class WindowsProcess
    extends NativeProcessProvider {
  static {
    System.loadLibrary("sshtools-daemon-win32");
  }

  private static Log log = LogFactory.getLog(WindowsProcess.class);
  WindowsAuthenticationInfo authInfo;
  WindowsProcessInfo processInfo;
  WindowsProcessInputStream input;
  WindowsProcessInputStream error;
  WindowsProcessOutputStream output;

  /**
   * Creates a new WindowsProcess object.
   */
  public WindowsProcess() {
  }

  /**
   *
   *
   * @return
   */
  public InputStream getInputStream() {
    return input;
  }

  /**
   *
   *
   * @return
   */
  public OutputStream getOutputStream() {
    return output;
  }

  /**
   *
   *
   * @return
   */
  public InputStream getStderrInputStream() {
    return error;
  }

  /**
   *
   */
  public void kill() {
    killProcess(processInfo);
  }

  /**
   *
   *
   * @return
   */
  public String getDefaultTerminalProvider() {
    return getNativeTerminalProvider();
  }

  /**
   *
   *
   * @param term
   *
   * @return
   */
  public boolean supportsPseudoTerminal(String term) {
    return false;
  }

  /**
   *
   *
   * @param term
   * @param cols
   * @param rows
   * @param width
   * @param height
   * @param modes
   *
   * @return
   */
  public boolean allocatePseudoTerminal(String term, int cols, int rows,
                                        int width, int height, String modes) {
    return false;
  }

  /**
   *
   *
   * @return
   */
  public int waitForExitCode() {
    return waitForProcessExitCode(processInfo.getProcessHandle());
  }

  /**
   *
   *
   * @return
   */
  public boolean stillActive() {
    return isProcessActive(processInfo.getProcessHandle());
  }

  /**
   *
   *
   * @throws IOException
   */
  public void start() throws IOException {
    if (!resumeProcess(processInfo)) {
      throw new IOException("Process could not be resumed");
    }
  }

  /**
   *
   *
   * @param command
   * @param environment
   *
   * @return
   *
   * @throws IOException
   */
  public boolean createProcess(String command, Map environment) throws
      IOException {
    if (! (Thread.currentThread()instanceof SshThread)) {
      log.error(
          "Calling thread is not an SshThread, failed to read username");

      return false;
    }

    SshThread thread = (SshThread) Thread.currentThread();

    if (thread.containsProperty("Win32UserAuthInfo")) {
      authInfo = (WindowsAuthenticationInfo) thread.getProperty(
          "Win32UserAuthInfo");

      WindowsAuthentication auth = (WindowsAuthentication)
          NativeAuthenticationProvider
          .getInstance();
      String env = "";
      String username = thread.getUsername();
      String directory = auth.getHomeDirectory(username);

      if ( (directory == null) || directory.trim().equals("")) {
        // Try to get the profile directory instead
        directory = auth.getNativeProfileDirectory(authInfo
            .getLogonToken());
      }

      if ( (directory == null) || directory.equals("")) {
        directory = ( (PlatformConfiguration) ConfigurationLoader
                     .getConfiguration(PlatformConfiguration.class)).getSetting(
            "DefaultHomeDir");
      }

      // Create the process
      processInfo = createProcess(command, env, directory,
                                  authInfo.getLogonToken());
    }
    else {
      // Create the process
      //			processInfo = createProcess(command, env, directory, 0);
      return false;
    }

    if (processInfo == null) {
      return false;
    }

    input = new WindowsProcessInputStream(processInfo.getStdoutReadHandle());
    output = new WindowsProcessOutputStream(processInfo.getStdinWriteHandle());
    error = new WindowsProcessInputStream(processInfo.getStderrReadHandle());

    return true;
  }

  private native int waitForProcessExitCode(int processHandle);

  private native boolean isProcessActive(int processHandle);

  private native WindowsProcessInfo createProcess(String command,
                                                  String environment,
                                                  String directory,
                                                  int userTokenHandle);

  private native void killProcess(WindowsProcessInfo processInfo);

  private native String getNativeTerminalProvider();

  private native boolean resumeProcess(WindowsProcessInfo processInfo);
}
