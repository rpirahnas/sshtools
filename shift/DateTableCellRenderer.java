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

import java.text.DateFormat;
import java.util.Date;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.10 $
 */
public class DateTableCellRenderer
    extends DefaultTableCellRenderer {
  private DateFormat format;

  /**
   * Creates a new DateTableCellRenderer object.
   *
   * @param format
   */
  public DateTableCellRenderer(DateFormat format) {
    super();
    this.format = format;
  }

  /**
   *
   *
   * @param table
   * @param value
   * @param isSelected
   * @param hasFocus
   * @param row
   * @param column
   *
   * @return
   */
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                        row, column);

    Date d = (Date) value;
    setText(format.format(d));

    return this;
  }
}
