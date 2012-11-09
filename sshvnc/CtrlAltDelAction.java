/*
 *  Sshtools - SshVNC
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

package com.sshtools.sshvnc;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.StandardAction;

public class CtrlAltDelAction
    extends StandardAction {

  public CtrlAltDelAction() {
    putValue(Action.NAME, "Ctrl+Alt+Delete");
    putValue(Action.SMALL_ICON, getIcon("/com/sshtools/sshvnc/ctrlaltdel.png"));
    putValue(Action.SHORT_DESCRIPTION, "Send Ctrl+Alt+Delete");
    putValue(Action.LONG_DESCRIPTION, "Send the Ctrl+Alt+Delete keys");
    putValue(Action.ACCELERATOR_KEY,
             KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.ALT_MASK));
    putValue(Action.MNEMONIC_KEY, new Integer('a'));
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "View");
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(20));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(15));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(5));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(25));
  }
}
