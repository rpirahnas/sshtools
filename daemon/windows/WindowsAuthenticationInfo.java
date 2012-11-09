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
 * @version $Revision: 1.12 $
 */
public class WindowsAuthenticationInfo {
  private int hProfile = 0;
  private int hToken = 0;

  /**
   * Creates a new WindowsAuthenticationInfo object.
   */
  public WindowsAuthenticationInfo() {
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
   * @param hToken
   */
  public void setLogonToken(int hToken) {
    this.hToken = hToken;
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
   * @return
   */
  public int getLogonToken() {
    return hToken;
  }
}
