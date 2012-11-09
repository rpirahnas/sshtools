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
package com.sshtools.shift;

import javax.swing.Action;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.StandardAction;

abstract class NewFolderAction
    extends StandardAction {
  /**
   * Creates a new NewFolderAction object.
   */
  public NewFolderAction() {
    putValue(Action.NAME, "New folder");
    putValue(Action.SMALL_ICON,
             new ResourceIcon(NewFolderAction.class, "newfolder.png"));
    putValue(Action.SHORT_DESCRIPTION, "New folder");
    putValue(Action.LONG_DESCRIPTION, "Create a new folder");
    putValue(Action.MNEMONIC_KEY, new Integer('n'));
    putValue(Action.ACTION_COMMAND_KEY, "new-folder-command");
    putValue(StandardAction.MENU_NAME, "File");
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(80));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(0));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(70));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(10));
    putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
    putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(5));
    putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(10));
  }
}
