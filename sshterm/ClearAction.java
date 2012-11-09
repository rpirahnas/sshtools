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

import com.sshtools.common.ui.EmptyIcon;
import com.sshtools.common.ui.StandardAction;

public class ClearAction
    extends StandardAction {
  private final static String ACTION_COMMAND_KEY_CLEAR = "clear-command";
  private final static String NAME_CLEAR = "Clear";
  private final static String SHORT_DESCRIPTION_CLEAR = "Clear terminal";
  private final static String LONG_DESCRIPTION_CLEAR = "Clear terminal";
  private final static int MNEMONIC_KEY_CLEAR = 'r';
  public ClearAction() {
    putValue(Action.NAME, NAME_CLEAR);
    putValue(Action.SMALL_ICON, new EmptyIcon(16, 16));
    putValue(LARGE_ICON, new EmptyIcon(24, 24));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
    putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_CLEAR);
    putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_CLEAR);
    putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_CLEAR));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_CLEAR);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "View");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(20));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(0));
  }
}
