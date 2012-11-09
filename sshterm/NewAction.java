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

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.StandardAction;

class NewAction
    extends StandardAction {
  private final static String ACTION_COMMAND_KEY_NEW = "new-command";
  private final static String NAME_NEW = "New Connection";
  private final static String SMALL_ICON_NEW = "newconnect.png";
  private final static String LARGE_ICON_NEW = "";
  private final static String SHORT_DESCRIPTION_NEW = "Create a new connection";
  private final static String LONG_DESCRIPTION_NEW =
      "Create a new SSH connection";
  private final static int MNEMONIC_KEY_NEW = 'C';
  public NewAction() {
    putValue(Action.NAME, NAME_NEW);
    putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_NEW));
    putValue(LARGE_ICON, getIcon(LARGE_ICON_NEW));
    putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_NEW);
    putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_NEW);
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK));
    putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_NEW));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_NEW);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "File");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(0));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(1));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(0));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(0));
  }
}
