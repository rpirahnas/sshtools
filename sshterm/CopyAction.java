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

public class CopyAction
    extends StandardAction {
  private final static String ACTION_COMMAND_KEY_COPY = "copy-command";
  private final static String NAME_COPY = "Copy";
  private final static String SMALL_ICON_COPY = "copy.png";
  private final static String LARGE_ICON_COPY = "";
  private final static String SHORT_DESCRIPTION_COPY = "Copy selection";
  private final static String LONG_DESCRIPTION_COPY =
      "Copy the current selection to the clipboard";
  private final static int MNEMONIC_KEY_COPY = 'C';
  public CopyAction() {
    putValue(Action.NAME, NAME_COPY);
    putValue(Action.SMALL_ICON, getIcon(SMALL_ICON_COPY));
    putValue(LARGE_ICON, getIcon(LARGE_ICON_COPY));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
    putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_COPY);
    putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_COPY);
    putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_COPY));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_COPY);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "Edit");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(10));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(0));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(10));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(0));
  }
}
