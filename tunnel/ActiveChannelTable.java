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

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableModel;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.sshterm.BooleanIconRenderer;

public class ActiveChannelTable
    extends JTable {
  public ActiveChannelTable(TableModel model) {
    super(model);
    setShowGrid(false);
    setCellSelectionEnabled(false);
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    getColumnModel().getColumn(0).setCellRenderer(new BooleanIconRenderer(
        new ResourceIcon(ActiveChannelTable.class, "greenledon.png"),
        new ResourceIcon(ActiveChannelTable.class, "greenledoff.png")));
    getColumnModel().getColumn(1).setCellRenderer(new BooleanIconRenderer(
        new ResourceIcon(ActiveChannelTable.class, "greenledon.png"),
        new ResourceIcon(ActiveChannelTable.class, "greenledoff.png")));
    getTableHeader().setFont(getTableHeader().getFont().deriveFont(10f));
  }

  public boolean getScrollableTracksViewportHeight() {
    Component parent = getParent();
    if (parent instanceof JViewport) {
      return parent.getHeight() > getPreferredSize().height;
    }
    else {
      return false;
    }
  }
}
