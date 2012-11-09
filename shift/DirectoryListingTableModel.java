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

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.sftp.FileAttributes;
import com.sshtools.j2ssh.sftp.SftpFile;

/**
 * <p>
 * Title:
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author Lee David Painter
 * @version $Id: DirectoryListingTableModel.java,v 1.7 2003/09/22 15:58:12 martianx Exp $
 */

public class DirectoryListingTableModel

    extends AbstractTableModel {

  /**  */

  public static final String FILENAME = "Name";

  /**  */

  public static final String SIZE = "Size";

  /**  */

  public static final String PERMISSIONS = "Rights";

  /**  */

  public static final String MODIFIED = "Modified";

  /**  */

  public static final String OWNER = "Owner";

  /**  */

  public static final String GROUP = "Group";

  List files;

  SftpClient sftp;

  Object lock = new Object();

  ShiftSessionPanel browser;

  String[] columnNames = {

      "Name", "Size", "Rights", "Modified", "Owner", "Group"

  };

  /**
   * Creates a new DirectoryListingTableModel object.
   *
   * @param sftp
   */

  public DirectoryListingTableModel(SftpClient sftp) {

    this.sftp = sftp;

  }

  /**
   *
   *
   * @return
   *
   * @throws IOException
   */

  public String getCurrentDirectory() throws IOException {

    if ( (sftp != null) && !sftp.isClosed()) {

      return sftp.pwd();

    }

    else {

      throw new IOException("The SFTP connection has been closed!");

    }

  }

  /**
   *
   *
   * @param foldername
   *
   * @throws IOException
   */

  public void createFolder(String foldername) throws IOException {

    if ( (sftp != null) && !sftp.isClosed()) {

      sftp.mkdir(foldername);

      refresh();

    }

    else {

      throw new IOException("The SFTP connection has been closed!");

    }

  }

  /**
   *
   *
   * @param row
   *
   * @throws IOException
   */

  public void removeFile(int row) throws IOException {

    sftp.rm(getFilename(row));

    files.remove(row);

    fireTableRowsDeleted(row, row);

  }

  public void removeFile(int[] row) throws IOException {

    for (int i = 0; i < row.length; i++) {

      sftp.rm(getFilename(row[i]), true, true);

    }

    refresh();

  }

  /**
   *
   *
   * @param directory
   *
   * @throws IOException
   */

  public void changeDirectory(String directory) throws IOException {

    if ( (sftp != null) && !sftp.isClosed()) {

      sftp.cd(directory);

      refresh();

    }

    else {

      throw new IOException("The SFTP connection has been closed!");

    }

  }

  /**
   *
   *
   * @param row
   *
   * @return
   *
   * @throws IndexOutOfBoundsException
   */

  public boolean isDirectory(int row) {

    synchronized (lock) {

      if (row > files.size()) {

        throw new IndexOutOfBoundsException();

      }

      SftpFile file = (SftpFile) files.get(row);

      return file.isDirectory();

    }

  }

  /**
   *
   *
   * @param row
   *
   * @return
   *
   * @throws IndexOutOfBoundsException
   */

  public long getFileSize(int row) {

    synchronized (lock) {

      if (row > files.size()) {

        throw new IndexOutOfBoundsException();

      }

      SftpFile file = (SftpFile) files.get(row);

      return file.getAttributes().getSize().longValue();

    }

  }

  /**
   *
   *
   * @param row
   *
   * @return
   *
   * @throws IndexOutOfBoundsException
   */

  public String getFilename(int row) {

    synchronized (lock) {

      if (row > files.size()) {

        throw new IndexOutOfBoundsException();

      }

      SftpFile file = (SftpFile) files.get(row);

      return file.getFilename();

    }

  }

  public FileAttributes getFileAttributes(int row) {

    synchronized (lock) {

      if (row > files.size()) {

        throw new IndexOutOfBoundsException();

      }

      SftpFile file = (SftpFile) files.get(row);

      return file.getAttributes();

    }

  }

  /**
   *
   *
   * @throws IOException
   */

  public void refresh() throws IOException {

    if ( (sftp != null) && !sftp.isClosed()) {

      synchronized (lock) {

        if (files != null) {

          fireTableRowsDeleted(1, files.size());

          files.clear();

        }

        files = sftp.ls();

        // Remove any .. or .

        SftpFile f;

        Vector remove = new Vector();

        for (Iterator x = files.iterator();

             x.hasNext(); ) {

          f = (SftpFile) x.next();

          if (f.getFilename().equals(".")

              || f.getFilename().equals("..")) {

            remove.add(f);

          }

        }

        files.removeAll(remove);

        Collections.sort(files, new FileComparator());

        fireTableRowsInserted(1, files.size());

      }

    }

    else {

      throw new IOException("The SFTP connection has been closed!");

    }

  }

  /**
   *
   *
   * @return
   */

  public int getRowCount() {

    return files == null ? 0 : files.size();

  }

  /**
   *
   *
   * @return
   */

  public int getColumnCount() {

    return columnNames.length;

  }

  /**
   *
   *
   * @param col
   *
   * @return
   */

  public String getColumnName(int col) {

    return columnNames[col];

  }

  /**
   *
   *
   * @param rowIndex
   * @param columnIndex
   *
   * @return
   */

  public Object getValueAt(int rowIndex, int columnIndex) {

    synchronized (lock) {

      SftpFile file = (SftpFile) files.get(rowIndex);

      switch (columnIndex) {

        case 0:

          return file.getFilename();

        case 1:

          return String.valueOf(file.getAttributes().getSize());

        case 2:

          return file.getAttributes().getPermissionsString();

        case 3:

          return file.getAttributes().getModTimeString();

        case 4:

          return String.valueOf(file.getAttributes().getUID().longValue());

        case 5:

          return String.valueOf(file.getAttributes().getGID().longValue());

        default:

          return "";

      }

    }

  }

  class FileComparator

      implements Comparator {

    public int compare(Object o1, Object o2) {

      SftpFile f1 = (SftpFile) o1;

      SftpFile f2 = (SftpFile) o2;

      if (f1.isDirectory() && !f2.isDirectory()) {

        return -1;

      }

      if (f2.isDirectory() && !f1.isDirectory()) {

        return 1;

      }

      return f1.getFilename().compareTo(f2.getFilename());

    }

  }

}
