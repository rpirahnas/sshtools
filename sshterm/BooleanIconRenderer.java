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

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class BooleanIconRenderer
    extends DefaultTableCellRenderer {
  //  Private instance variables
  private Icon trueIcon;
  //  Private instance variables
  private Icon falseIcon;
  public BooleanIconRenderer(Icon trueIcon, Icon falseIcon) {
    this.trueIcon = trueIcon;
    this.falseIcon = falseIcon;
    setHorizontalAlignment(JLabel.CENTER);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                        row, column);
    setText(null);
    setIcon( ( (Boolean) value).booleanValue() ? trueIcon : falseIcon);
    return this;
  }

  public String getText() {
    return null;
  }
}
