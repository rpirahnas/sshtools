/*
 *  Sshtools - ShiFT
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.shift;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.StandardAction;
import javax.swing.Action;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

abstract class AddFavouriteAction extends StandardAction {
    public AddFavouriteAction() {
        putValue(Action.NAME, "Add to Favorites");
        putValue(Action.SMALL_ICON,
            new ResourceIcon(AddFavouriteAction.class,"favourite.png"));
        putValue(Action.SHORT_DESCRIPTION, "Add to Favorites");
        putValue(Action.LONG_DESCRIPTION,
            "Add the current selection to the favorites list");
        putValue(Action.MNEMONIC_KEY, new Integer('f'));
        putValue(Action.ACTION_COMMAND_KEY, "add-favorite-command");

        putValue(StandardAction.MENU_NAME, "Favorites");
        putValue(StandardAction.ON_MENUBAR, new Boolean(true));
        putValue(StandardAction.MENU_ITEM_GROUP, new Integer(75));
        putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(0));
        putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
        putValue(StandardAction.TOOLBAR_GROUP, new Integer(50));
        putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(25));
        putValue(Action.ACCELERATOR_KEY,
                 KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_DOWN_MASK));

    }
}
