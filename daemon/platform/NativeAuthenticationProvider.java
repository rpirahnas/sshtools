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

package com.sshtools.daemon.platform;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.14 $
 */
public abstract class NativeAuthenticationProvider {
  private static Log log = LogFactory.getLog(NativeAuthenticationProvider.class);
  private static Class cls;
  private static NativeAuthenticationProvider instance;

  static {
    try {
      if (ConfigurationLoader.isConfigurationAvailable(
          PlatformConfiguration.class)) {
        cls = ConfigurationLoader.getExtensionClass( ( (PlatformConfiguration)
            ConfigurationLoader
            .getConfiguration(PlatformConfiguration.class))
            .getNativeAuthenticationProvider());

        //
      }
    }
    catch (Exception e) {
      log.error("Failed to load native authentication provider", e);
      instance = null;
    }
  }

  /**
   *
   *
   * @param cls
   */
  public static void setProvider(Class cls) {
    NativeAuthenticationProvider.cls = cls;
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
  public abstract String getHomeDirectory(String username) throws IOException;

  /**
   *
   *
   * @param username
   * @param password
   *
   * @return
   *
   * @throws PasswordChangeException
   * @throws IOException
   */
  public abstract boolean logonUser(String username, String password) throws
      PasswordChangeException, IOException;

  /**
   *
   *
   * @param username
   *
   * @return
   *
   * @throws IOException
   */
  public abstract boolean logonUser(String username) throws IOException;

  /**
   *
   *
   * @throws IOException
   */
  public abstract void logoffUser() throws IOException;

  /**
   *
   *
   * @param username
   * @param oldpassword
   * @param newpassword
   *
   * @return
   */
  public abstract boolean changePassword(String username, String oldpassword,
                                         String newpassword);

  /**
   *
   *
   * @return
   *
   * @throws IOException
   */
  public static NativeAuthenticationProvider getInstance() throws IOException {
    if (instance == null) {
      try {
        if (cls == null) {
          throw new IOException(
              "There is no authentication provider configured");
        }

        instance = (NativeAuthenticationProvider) cls.newInstance();
      }
      catch (Exception e) {
        throw new IOException(
            "The authentication provider failed to initialize: "
            + e.getMessage());
      }
    }

    return instance;
  }
}
