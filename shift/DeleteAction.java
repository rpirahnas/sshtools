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

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.StandardAction;

abstract class DeleteAction
    extends StandardAction {
  /**
   * Creates a new DeleteAction object.
   */
  public DeleteAction() {
    putValue(Action.NAME, "Delete");
    putValue(Action.SMALL_ICON,
             new ResourceIcon(DeleteAction.class, "delete.png"));
    putValue(StandardAction.LARGE_ICON,
             new ResourceIcon("/com/sshtools/shift/largedelete.png"));
    putValue(Action.SHORT_DESCRIPTION, "Delete selection");
    putValue(Action.LONG_DESCRIPTION, "Delete the selected files");
    putValue(Action.MNEMONIC_KEY, new Integer('d'));
    putValue(Action.ACTION_COMMAND_KEY, "delete-command");
    putValue(StandardAction.MENU_NAME, "Tools");
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(95));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(0));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(60));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(10));
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
    putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(15));
    putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(20));
    putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(false));
  }
}
