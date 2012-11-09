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
package com.sshtools.tunnel;

import javax.swing.Action;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.StandardAction;

public class PortForwardingAction
    extends StandardAction {
  private final static String ACTION_COMMAND_KEY_FORWARD = "port-forwards";
  private final static String NAME_FORWARD = "Port Forwarding";
  private final static ResourceIcon SMALL_ICON_FORWARD =
      new ResourceIcon(PortForwardingAction.class, "forward.png");
  private final static String LARGE_ICON_FORWARD = "";
  private final static String SHORT_DESCRIPTION_FORWARD =
      "Toggle port forwarding";
  private final static String LONG_DESCRIPTION_FORWARD =
      "Toggle the port forwarding window";
  private final static int MNEMONIC_KEY_FORWARD = 'p';
  public PortForwardingAction() {
    putValue(Action.NAME, NAME_FORWARD);
    putValue(Action.SMALL_ICON, SMALL_ICON_FORWARD);
    putValue(LARGE_ICON, getIcon(LARGE_ICON_FORWARD));
    putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION_FORWARD);
    putValue(Action.LONG_DESCRIPTION, LONG_DESCRIPTION_FORWARD);
    putValue(Action.MNEMONIC_KEY, new Integer(MNEMONIC_KEY_FORWARD));
    putValue(Action.ACTION_COMMAND_KEY, ACTION_COMMAND_KEY_FORWARD);
    putValue(StandardAction.ON_MENUBAR, new Boolean(true));
    putValue(StandardAction.MENU_NAME, "Tools");
    putValue(StandardAction.MENU_ITEM_GROUP, new Integer(10));
    putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(50));
    putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
    putValue(StandardAction.TOOLBAR_GROUP, new Integer(40));
    putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(10));
  }
}
