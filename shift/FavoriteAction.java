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

public class FavoriteAction
    extends StandardAction {

  protected String directory;

  public FavoriteAction(String name, int weight, String directory) {
    this.directory = directory;
    putValue(Action.NAME, name);
    putValue(Action.SMALL_ICON,
             new ResourceIcon(FavoriteAction.class, "favourite.png"));
    putValue(Action.SHORT_DESCRIPTION, directory);
    putValue(Action.LONG_DESCRIPTION, "Favorite");
    putValue(Action.ACTION_COMMAND_KEY, "favorite-command");
    putValue(StandardAction.MENU_NAME, "Favorites");
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(80));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(weight));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(false));
  }

}
