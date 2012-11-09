/*
 *  Sshtools - SshVNC
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.sshtools.sshvnc;

import java.io.IOException;

import com.sshtools.common.ui.SshToolsApplicationApplet;
import com.sshtools.common.ui.SshToolsApplicationClientApplet;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationPanel;

public class SshVNCApplet
    extends SshToolsApplicationClientApplet {
  //     eurgghh!
  public final static String[][] SSHVNC_PARAMETER_INFO = {

      {
      "sshvnc.connection.vncHostDisplay", "string",
      "Enable / Disable the menu bar"}
      , {
      "sshvnc.connection.preferredEncoding", "int",
      "preferred encoding"}
      , {
      "sshvnc.connection.useCopyRect", "boolean", "Use copyrect encoding"}
      , {
      "sshvnc.connection.compressLevel", "int", "Compression level"}
      , {
      "sshvnc.connection.jpegQuality", "int", "Set the JPEG quality"}
      , {
      "sshvnc.connection.cursorUpdates", "int", "Set cursor updates."}
      , {
      "sshvnc.connection.eightBitColors", "boolean", "Force eight bit colors"}
      , {
      "sshvnc.connection.reverseMouseButtons2And3", "boolean",
      "Reverse mouse buttons 2 and 3"}
      , {
      "sshterm.ui.autoHide", "boolean",
      "Enable / Disable auto-hiding "
      + "of the tool bar, menu bar, status bar and scroll bar"}
      , {
      "sshvnc.connection.encryptedVncPassword", "string",
      "The VNC password to use (encrypted)"}
      , {
      "sshapps.connection.password", "string", "The SSH password to use"}
  };

  protected void doExtraParameters(String param, String property) {
    String s = getParameter(param, "");
    if (!s.equals("")) {
      if (log.isDebugEnabled()) {
        log.debug("Setting property " + property + " to " + s + " from " +
                  param);
      }
      profile.setApplicationProperty(property, s);
    }
  }

  public void buildProfile() throws IOException {
    super.buildProfile();
    doExtraParameters("sshvnc.connection.compressLevel",
                      SshVNCPanel.PROFILE_PROPERTY_COMPRESS_LEVEL);
    doExtraParameters("sshvnc.connection.cursorUpdates",
                      SshVNCPanel.PROFILE_PROPERTY_CURSOR_UPDATES);
    doExtraParameters("sshvnc.connection.eightBitColors",
                      SshVNCPanel.PROFILE_PROPERTY_EIGHT_BIT_COLORS);
    doExtraParameters("sshvnc.connection.jpegQuality",
                      SshVNCPanel.PROFILE_PROPERTY_JPEG_QUALITY);
    doExtraParameters("sshvnc.connection.preferredEncoding",
                      SshVNCPanel.PROFILE_PROPERTY_PREFERRED_ENCODING);
    doExtraParameters("sshvnc.connection.reverseMouseButtons2And3",
                      SshVNCPanel.
                      PROFILE_PROPERTY_REVERSE_MOUSE_BUTTONS_2_AND_3);
    doExtraParameters("sshvnc.connection.shareDesktop",
                      SshVNCPanel.PROFILE_PROPERTY_SHARE_DESKTOP);
    doExtraParameters("sshvnc.connection.useCopyRect",
                      SshVNCPanel.PROFILE_PROPERTY_USE_COPY_RECT);
    doExtraParameters("sshvnc.connection.viewOnly",
                      SshVNCPanel.PROFILE_PROPERTY_VIEW_ONLY);
    doExtraParameters("sshvnc.connection.vncHostDisplay",
                      SshVNCPanel.PROFILE_PROPERTY_VNC_HOST + ":" +
                      SshVNCPanel.PROFILE_PROPERTY_VNC_DISPLAY);
    doExtraParameters("sshvnc.connection.encryptedVncPassword",
                      SshVNCPanel.PROFILE_PROPERTY_ENCRYPTED_VNC_PASSWORD);
    doExtraParameters("sshapps.connection.password",
                      SshVNCPanel.PROFILE_PROPERTY_SSH_PASSWORD);
  }

  public String getAppletInfo() {
    return "SSHVnc";
  }

  public String[][] getParameterInfo() {
    String[][] s = super.getParameterInfo();
    String[][] x = new String[s.length + SSHVNC_PARAMETER_INFO.length][];
    System.arraycopy(s, 0, x, 0, s.length);
    System.arraycopy(SSHVNC_PARAMETER_INFO, 0, x, s.length,
                     SSHVNC_PARAMETER_INFO.length);
    return x;
  }

  public SshToolsApplicationPanel createApplicationPanel() throws
      SshToolsApplicationException {
    SshVNC term = new SshVNC();
    SshToolsApplicationClientApplet.SshToolsApplicationAppletContainer
        container =
        new SshToolsApplicationClientApplet.SshToolsApplicationAppletContainer();
    term.newContainer(container);
    SshVNCPanel p = (SshVNCPanel) container.getApplicationPanel();
    p.setFrameResizeable(false);
    return p;
  }
}
