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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.ui.SshToolsApplication;
import com.sshtools.common.ui.SshToolsApplicationContainer;
import com.sshtools.common.ui.StandardAction;

public class FullScreenAction
    extends StandardAction {
  protected static Log log = LogFactory.getLog(FullScreenAction.class);
  private final static String ACTION_COMMAND_KEY_FULL_SCREEN =
      "full-screen-command";
  private final static String NAME_FULL_SCREEN = "Full Screen";
  private final static String SMALL_ICON_FULL_SCREEN = "fullscreen.png";
  private final static String LARGE_ICON_FULL_SCREEN = "";
  private final static String SHORT_DESCRIPTION_FULL_SCREEN =
      "Toggle full screen";
  private final static String LONG_DESCRIPTION_FULL_SCREEN =
      "Toggle full screen mode";
  private final static int MNEMONIC_KEY_FULL_SCREEN = 'f';
  //
  private SshToolsApplication application;
  private SshToolsApplicationContainer container;
  public FullScreenAction(SshToolsApplication application,
                          SshToolsApplicationContainer container) {
    this.application = application;
    this.container = container;
    putValue(Action.NAME, NAME_FULL_SCREEN);
    putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_FULL_SCREEN));
    putValue(LARGE_ICON, getIcon(LARGE_ICON_FULL_SCREEN));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK));
    putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_FULL_SCREEN);
    putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_FULL_SCREEN);
    putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_FULL_SCREEN));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_FULL_SCREEN);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "View");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(20));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(20));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(5));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(30));
  }

  public void actionPerformed(ActionEvent evt) {
    SshTerminalPanel p = (SshTerminalPanel) container.getApplicationPanel();
    p.setFullScreenMode(!p.isFullScreenMode());
  }
}
