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

package com.sshtools.daemon.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.AlgorithmNotSupportedException;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.11 $
 */
public class SshAuthenticationServerFactory {
  private static Map auths;
  private static Log log = LogFactory.getLog(SshAuthenticationServerFactory.class);

  /**  */
  public final static String AUTH_PASSWORD = "password";

  /**  */
  public final static String AUTH_PK = "publickey";

  /**  */
  public final static String AUTH_KBI = "keyboard-interactive";

  static {
    auths = new HashMap();

    log.info("Loading supported authentication methods");

    auths.put(AUTH_PASSWORD, PasswordAuthenticationServer.class);
    auths.put(AUTH_PK, PublicKeyAuthenticationServer.class);
    auths.put(AUTH_KBI, KBIPasswordAuthenticationServer.class);


  }

  /**
   * Creates a new SshAuthenticationServerFactory object.
   */
  protected SshAuthenticationServerFactory() {
  }

  /**
   *
   */
  public static void initialize() {
  }

  /**
   *
   *
   * @return
   */
  public static List getSupportedMethods() {
    // Get the list of ciphers
    ArrayList list = new ArrayList(auths.keySet());

    // Return the list
    return list;
  }

  /**
   *
   *
   * @param methodName
   *
   * @return
   *
   * @throws AlgorithmNotSupportedException
   */
  public static SshAuthenticationServer newInstance(String methodName) throws
      AlgorithmNotSupportedException {
    try {
      return (SshAuthenticationServer) ( (Class) auths.get(methodName))
          .newInstance();
    }
    catch (Exception e) {
      throw new AlgorithmNotSupportedException(methodName
                                               + " is not supported!");
    }
  }
}
