/*
 *  Sshtools - SSHTerm
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
package com.sshtools.sshterm;

import java.io.IOException;

import com.sshtools.common.ui.SshToolsApplicationApplet;
import com.sshtools.common.ui.SshToolsApplicationClientApplet;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationPanel;

public class SshTermApplet
    extends SshToolsApplicationClientApplet {
  //     eurgghh!
  public final static String[][] SSHTERM_PARAMETER_INFO = {
      {
      "sshterm.ui.scrollBar", "boolean", "Enable / Disable the menu bar"}
      , {
      "sshterm.ui.autoHide", "boolean",

      "Enable / Disable auto-hiding "
      + "of the tool bar, menu bar, status bar and scroll bar"
  }
  };
  private boolean scrollBar;
  private boolean autoHide;
  public void initApplet() throws IOException {
    super.initApplet();
    scrollBar = getParameter("sshterm.ui.scrollBar", "true").equals("true");
    autoHide = getParameter("sshterm.ui.autoHide", "false").equals("true");
  }

  public String[][] getParameterInfo() {
    String[][] s = super.getParameterInfo();
    String[][] x = new String[s.length + SSHTERM_PARAMETER_INFO.length][];
    System.arraycopy(s, 0, x, 0, s.length);
    System.arraycopy(SSHTERM_PARAMETER_INFO, 0, x, s.length,
                     SSHTERM_PARAMETER_INFO.length);
    return x;
  }

  public String getAppletInfo() {
    return "SSHTerm";
  }

  public SshToolsApplicationPanel createApplicationPanel() throws
      SshToolsApplicationException {
    SshTerm term = new SshTerm();
    term.init(new String[] {});
    SshToolsApplicationClientApplet.SshToolsApplicationAppletContainer
        container =
        new SshToolsApplicationClientApplet.SshToolsApplicationAppletContainer();
    term.newContainer(container);
    SshTerminalPanel panel = (SshTerminalPanel) container
        .getApplicationPanel();
    panel.setScrollBarVisible(scrollBar);
    panel.setAutoHideTools(autoHide);
    panel.setToolsVisible(true);
    return panel;
  }
}
