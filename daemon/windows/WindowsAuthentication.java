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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.33 $
 */
public class WindowsAuthentication
    extends NativeAuthenticationProvider {
  private static Log log = LogFactory.getLog(WindowsAuthentication.class);

  static {
    System.loadLibrary("sshtools-daemon-win32");
  }

  /**
   * Creates a new WindowsAuthentication object.
   */
  public WindowsAuthentication() {
  }

  /**
   *
   *
   * @param username
   *
   * @return
   *
   * @throws IOException
   */
  public String getHomeDirectory(String username) throws IOException {
    String dir = null;

    if (Thread.currentThread()instanceof SshThread) {
      dir = (String) SshThread.getCurrentThread().getProperty("user.home");
    }

    if (dir == null) {
      // To get the user home directory under windows we will first look
      // to see if they have one set in the control panel
      dir = getNativeHomeDirectory(getDomain(), username);

      if (dir == null) {
        // The user does not have one set so lets look for the profile directory,
        // this is the nasty bit as we can only get it with a logon token, so if
        // if the user is not logged in, were going to have to create a token
        // to get it.
        if (Thread.currentThread()instanceof SshThread) {
          SshThread thread = SshThread.getCurrentThread();
          int handle;

          if (!thread.containsProperty("Win32UserAuthInfo")) {
            handle = createToken(username, getDomain());
            dir = getNativeProfileDirectory(handle);
            closeHandle(handle);
          }
          else {
            WindowsAuthenticationInfo info = (WindowsAuthenticationInfo) thread
                .getProperty("Win32UserAuthInfo");
            dir = getNativeProfileDirectory(info.getLogonToken());
          }

          if (dir == null) {
            dir = ( (PlatformConfiguration) ConfigurationLoader
                   .getConfiguration(PlatformConfiguration.class))
                .getSetting("DefaultHomeDir");
          }
        }
      }

      if (Thread.currentThread()instanceof SshThread) {
        SshThread thread = SshThread.getCurrentThread();
        thread.setProperty("user.home", dir);
      }
    }

    return dir;
  }

  /**
   *
   *
   * @throws IOException
   */
  public void logoffUser() throws IOException {
    SshThread thread = SshThread.getCurrentThread();
    log.info("Logging off user " + thread.getUsername() + " from session "
             + thread.getSessionIdString());
    nativeLogoffUser( (WindowsAuthenticationInfo) thread.getProperty(
        "Win32UserAuthInfo"));
  }

  /**
   *
   *
   * @param username
   * @param password
   *
   * @return
   *
   * @throws IOException
   */
  public boolean logonUser(String username, String password) throws IOException {
    WindowsAuthenticationInfo tokens = nativeLogonUser(username, password,
        getDomain());

    if (tokens != null) {
      log.debug("Authenticated handle is "
                + String.valueOf(tokens.getLogonToken()));

      if (! (Thread.currentThread()instanceof SshThread)) {
        log.error(
            "Calling process is not an instance of SshThread, cannot set token handle");

        return false;
      }

      ( (SshThread) Thread.currentThread()).setProperty("Win32UserAuthInfo",
          tokens);

      return true;
    }
    else {
      log.error("nativeLogonUser returned a null handle!");

      return false;
    }
  }

  /**
   *
   *
   * @param username
   * @param oldpassword
   * @param newpassword
   *
   * @return
   */
  public boolean changePassword(String username, String oldpassword,
                                String newpassword) {
    return false;
  }

  /**
   *
   *
   * @param username
   *
   * @return
   *
   * @throws IOException
   */
  public boolean logonUser(String username) throws IOException {
    WindowsAuthenticationInfo tokens = nativeLogonUserWOPassword(username,
        getDomain());

    if (tokens != null) {
      log.debug("Authenticated handle is "
                + String.valueOf(tokens.getLogonToken()));

      if (! (Thread.currentThread()instanceof SshThread)) {
        log.error(
            "Calling process is not an instance of SshThread, cannot set token handle");

        return false;
      }

      ( (SshThread) Thread.currentThread()).setProperty("Win32UserAuthInfo",
          tokens);

      return true;
    }
    else {
      log.error("nativeLogonUser returned a null handle!");

      return false;
    }
  }

  /**
   *
   *
   * @return
   *
   * @throws IOException
   */
  protected String getDomain() throws IOException {
    String domain = "."; // Local only by default
    PlatformConfiguration platform = (PlatformConfiguration)
        ConfigurationLoader
        .getConfiguration(PlatformConfiguration.class);

    if (platform.containsSetting("AuthenticateOnDomain")) {
      domain = platform.getSetting("AuthenticateOnDomain");

      if (domain.trim().length() == 0) {
        domain = ".";
      }
    }

    return domain;
  }

  private native String getNativeHomeDirectory(String domain, String username);

  /**
   *
   *
   * @param handle
   *
   * @return
   */
  public native String getNativeProfileDirectory(int handle);

  private native WindowsAuthenticationInfo nativeLogonUser(String username,
      String password, String domain);

  private native WindowsAuthenticationInfo nativeLogonUserWOPassword(
      String username, String domain);

  private native void nativeLogoffUser(WindowsAuthenticationInfo info);

  private native int createToken(String username, String domain);

  private native void closeHandle(int handle);
}
