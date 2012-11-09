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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.10 $
 */
public class FileSizeTableCellRenderer
    extends DefaultTableCellRenderer {
  private final static NumberFormat KB_FILE_SIZE_FORMAT = new DecimalFormat();
  private final static NumberFormat MB_FILE_SIZE_FORMAT = new DecimalFormat();

  static {
    KB_FILE_SIZE_FORMAT.setMinimumFractionDigits(0);
    KB_FILE_SIZE_FORMAT.setMaximumFractionDigits(0);
  }

  static {
    MB_FILE_SIZE_FORMAT.setMinimumFractionDigits(0);
    MB_FILE_SIZE_FORMAT.setMaximumFractionDigits(2);
  }

  /**
   * Creates a new FileSizeTableCellRenderer object.
   */
  public FileSizeTableCellRenderer() {
    super();
    setHorizontalAlignment(SwingConstants.RIGHT);
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

    long size = ( (DirectoryListingTableModel) table.getModel()).getFileSize(
        row);

    setText(formatFileSize(size));

    return this;
  }

  /**
   *
   *
   * @param size
   *
   * @return
   */
  public static String formatFileSize(long size) {
    double bytes = (double) size;

    if (bytes < 1024) {
      return String.valueOf( (int) bytes) + " bytes";
    }
    else if (bytes < 1048576) {
      return KB_FILE_SIZE_FORMAT.format(bytes / 1024) + " KB";
    }
    else {
      return MB_FILE_SIZE_FORMAT.format(bytes / 1024 / 1024) + " MB";
    }
  }
}
