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

public abstract class TerminalPropertiesAction
    extends StandardAction {
  public TerminalPropertiesAction() {
    putValue(Action.NAME, "Terminal Settings");
    putValue(Action.SMALL_ICON,
             getIcon("/com/sshtools/sshterm/terminal.png"));
    putValue(Action.SHORT_DESCRIPTION, "Terminal Settings");
    putValue(Action.LONG_DESCRIPTION, "Change the current terminal settings");
    putValue(Action.MNEMONIC_KEY, new Integer('t'));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK));
    putValue(Action.ACTION_COMMAND_KEY, "terminal-settings-command");
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "Edit");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(80));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(0));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(5));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(40));
  }
}
