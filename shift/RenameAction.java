package com.sshtools.shift;

/*
 *  My3SP
 *
 *  Copyright (C) 2003 3SP LTD. All Rights Reserved
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.StandardAction;

abstract class RenameAction
    extends StandardAction {
  /**
   * Creates a new NewFolderAction object.
   */
  public RenameAction() {
    putValue(Action.NAME, "Rename");
    putValue(Action.SMALL_ICON,
             new ResourceIcon(NewFolderAction.class, "rename.png"));
    putValue(Action.SHORT_DESCRIPTION, "Rename");
    putValue(Action.LONG_DESCRIPTION, "Rename a file or folder");
    putValue(Action.MNEMONIC_KEY, new Integer('r'));
    putValue(Action.ACTION_COMMAND_KEY, "rename-command");
    putValue(StandardAction.MENU_NAME, "File");
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(85));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(15));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(70));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(10));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
    putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
    putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(15));
    putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(15));

  }
}
