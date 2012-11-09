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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;

import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.j2ssh.DirectoryOperation;
import com.sshtools.j2ssh.FileTransferProgress;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.TransferCancelledException;
import com.sshtools.j2ssh.sftp.FileAttributes;

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
 * @version $Id: FileTransferDialog.java,v 1.15 2003/09/22 15:58:12 martianx Exp $
 */

public class FileTransferDialog

    extends JFrame

    implements FileTransferProgress {

  boolean cancelled = false;

  boolean completed = false;

  long lastUpdate = System.currentTimeMillis();

  long bytesTransfered = 0;

  long currentTime;

  long elaspedTime;

  double transfer;

  long fileSize = 0;

  long bytesSoFar = 0;

  long completedTransfers = 0;

  int filesTransfered = 0;

  long totalBytes;

  JPanel mainPanel = new JPanel();

  JPanel informationPanel = new JPanel();

  JLabel lblEstimatedTime = new JLabel();

  JLabel lblTargetAction = new JLabel();

  JLabel lblTransferRate = new JLabel();

  JLabel lblTimeLeftValue = new JLabel();

  JLabel lblTargetValue = new JLabel();

  JLabel lblPathValue = new JLabel();

  JLabel lblTransferRateValue = new JLabel();

  JLabel lblAction = new JLabel("Saving:");

  JButton btnCancel = new JButton();

  JProgressBar progressbar = new JProgressBar();

  JCheckBox chkClose = new JCheckBox();

  Object lock = new Object();

  NumberFormat formatMb = NumberFormat.getNumberInstance();

  NumberFormat formatKb = NumberFormat.getNumberInstance();

  NumberFormat formatKb1 = NumberFormat.getNumberInstance();

  Timer lblTimeLeftValuer;

  String formattedTotal = "";

  String title;

  java.util.List files;

  java.util.List dirs;

  SftpClient sftp;

  String source;

  DirectoryOperation op;

  /**
   * Creates a new FileTransferDialog object.
   *
   * @param frame
   * @param title
   * @param fileCount
   */

  /*public FileTransferDialog(Frame frame, String title) {
    this(frame, title, op.getNewFiles().size()
                       + op.getUpdatedFiles().size());
    this.op = op;
     }*/

  public FileTransferDialog(Frame frame, String title, int fileCount) {

    super("0% Complete - " + title);

    this.setIconImage(ShiftSessionPanel.FILE_BROWSER_ICON.getImage());

    this.title = title;

    try {

      jbInit();

      pack();

    }

    catch (Exception ex) {

      ex.printStackTrace();

    }

    this.addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {

        if (cancelled || completed) {

          setVisible(false);

        }

        else {

          if (JOptionPane.showConfirmDialog(FileTransferDialog.this,

                                            "Cancel the file operation(s)?",

                                            "Close Window",

                                            JOptionPane.YES_NO_OPTION,

                                            JOptionPane.ERROR_MESSAGE) ==

              JOptionPane.YES_OPTION) {

            cancelOperation();

            hide();

          }

        }

      }

    });

  }

  /**
   * Creates a new FileTransferDialog object.
   */

  public FileTransferDialog() {

    try {

      jbInit();

    }

    catch (Exception ex) {

    }

  }

  public synchronized void notifyWaiting() {

    notifyAll();

  }

  public synchronized void waitForCompletion() throws InterruptedException {

    while (this.isVisible() && !cancelled) {

      wait(5000);

    }

  }

  public synchronized void cancelOperation() {

    cancelled = true;

    setVisible(false);

    notifyAll();

  }

  /**
   *
   *
   * @param sftp
   * @param localdir
   * @param files
   *
   * @throws IOException
   */

  public void putFiles(SftpClient sftp, final File localdir,

                       final java.util.List files) throws IOException {

    this.files = files;

    this.sftp = sftp;

    this.totalBytes = 0;

    lblTargetAction.setText("Uploading to:");

    File f;

    for (int i = 0; i < files.size(); i++) {

      f = new File(localdir, (String) files.get(i));

      this.totalBytes += f.length();

    }

    progressbar.setMaximum( (int) totalBytes / 1024);

    if ( (totalBytes / 1048576) > 0) {

      // Were in the megabytes

      formattedTotal = formatMb.format( (double) totalBytes / 1048576)

          + " MB";

    }

    else {

      // Were still in Kilobytes

      formattedTotal = formatKb.format( (double) totalBytes / 1024)

          + " KB";

    }

    sftp.lcd(localdir.getAbsolutePath());

    Thread thread = new Thread(new Runnable() {

      public void run() {

        try {

          for (int i = 0;

               i < FileTransferDialog.this.files.size();

               i++) {

            setSource(localdir.getAbsolutePath()

                      + File.separator + files.get(i));

            setTarget(FileTransferDialog.this.sftp.pwd()

                      + "/" + FileTransferDialog.this.files.get(i));

            FileTransferDialog.this.sftp.put( (String) FileTransferDialog.this.
                                             files.get(i),

                                             FileTransferDialog.this);

          }

          // notify any waiting threads that we've completed our operation

          notifyWaiting();

        }

        catch (IOException ex) {

          if (!cancelled) {

            SshToolsApplicationPanel.showErrorMessage(FileTransferDialog.this,

                "The file operation failed!\n"

                + ex.getMessage(), "File Transfer",

                ex);

            setVisible(false);

          }

        }

      }

    });

    thread.start();

    setVisible(true);

  }

  public void copyLocalDirectory(final SftpClient sftp,

                                 final java.util.List localfiles,

                                 final java.util.List localdirs,

                                 final String remotedir,

                                 final boolean recurse,

                                 final boolean sync)

      throws IOException {

    this.sftp = sftp;

    this.dirs = localdirs;

    this.files = localfiles;

    lblTargetAction.setText("Uploading to:");

    lblAction.setText(sync ? "Synchronizing Directory:" : "Copying Directory:");

    lblPathValue.setText("Gathering file information from remote server");

    show();

    Thread thread = new Thread(new Runnable() {

      public void run() {

        try {

          File f;

          for (int i = 0; i < localfiles.size(); i++) {

            f = (File) localfiles.get(i);

            totalBytes += f.length();

          }

          op = new DirectoryOperation();

          for (int i = 0; i < localdirs.size(); i++) {

            f = (File) localdirs.get(i);

            op.addDirectoryOperation(sftp.copyLocalDirectory(

                f.getAbsolutePath(),

                remotedir,

                recurse,

                sync,

                false,

                null), remotedir

                                     + (remotedir.endsWith("/") ? "" : "/")

                                     + f.getName());

          }

          totalBytes += op.getTransferSize();

          progressbar.setMaximum( (int) totalBytes / 1024);

          if ( (totalBytes / 1048576) > 0) {

            // Were in the megabytes

            formattedTotal = formatMb.format( (double) totalBytes / 1048576)

                + " MB";

          }

          else {

            // Were still in Kilobytes

            formattedTotal = formatKb.format( (double) totalBytes / 1024)

                + " KB";

          }

          String s;

          // Perform the directory operation first

          for (int i = 0; i < localdirs.size(); i++) {

            f = (File) localdirs.get(i);

            setSource(f.getAbsolutePath());

            setTarget(remotedir + (remotedir.endsWith("/") ? "" : "/") +
                      f.getName());

            sftp.copyLocalDirectory(

                f.getAbsolutePath(),

                remotedir,

                recurse,

                sync,

                true,

                FileTransferDialog.this);

          }

          // Now add the other files we wanted

          for (int i = 0; i < localfiles.size(); i++) {

            f = (File) localfiles.get(i);

            setSource(f.getAbsolutePath());

            setTarget(remotedir + (remotedir.endsWith("/") ? "" : "/") +
                      f.getName());

            sftp.put(f.getAbsolutePath(),

                     remotedir + (remotedir.endsWith("/") ? "" : "/") +
                     f.getName(),

                     FileTransferDialog.this);

          }

          // notify any waiting threads that weve completed our operation

          notifyWaiting();

        }

        catch (TransferCancelledException e) {

          notifyWaiting();

        }

        catch (IOException ex) {

          SshToolsApplicationPanel.showErrorMessage(FileTransferDialog.this,

              "An error occurred",

              "Copy Directories",

              ex);

        }

      }

    }

    , "Copying Directory");

    thread.start();

  }

  public void copyRemoteDirectory(final SftpClient sftp,

                                  final java.util.List remotedirs,

                                  final java.util.List remotefiles,

                                  final String localdir,

                                  final boolean recurse,

                                  final boolean sync) throws IOException {

    this.sftp = sftp;

    this.dirs = remotedirs;

    this.files = remotefiles;

    lblTargetAction.setText("Downloading to:");

    lblAction.setText(sync ? "Synchronizing Directory:" : "Copying Directory:");

    lblPathValue.setText("Gathering file information from remote server");

    show();

    Thread thread = new Thread(new Runnable() {

      public void run() {

        try {

          FileAttributes attrs;

          for (int i = 0; i < remotefiles.size(); i++) {

            attrs = sftp.stat( (String) remotefiles.get(i));

            totalBytes += attrs.getSize().longValue();

          }

          op = new DirectoryOperation();

          for (int i = 0; i < remotedirs.size(); i++) {

            op.addDirectoryOperation(sftp.copyRemoteDirectory(

                (String) remotedirs.get(i),

                localdir,

                recurse,

                sync,

                false,

                null), new File(localdir));

          }

          totalBytes += op.getTransferSize();

          progressbar.setMaximum( (int) totalBytes / 1024);

          if ( (totalBytes / 1048576) > 0) {

            // Were in the megabytes

            formattedTotal = formatMb.format( (double) totalBytes / 1048576)

                + " MB";

          }

          else {

            // Were still in Kilobytes

            formattedTotal = formatKb.format( (double) totalBytes / 1024)

                + " KB";

          }

          String s;

          // Perform the directory operation first

          for (int i = 0; i < remotedirs.size(); i++) {

            s = (String) remotedirs.get(i);

            setSource(FileTransferDialog.this.sftp.pwd()

                      + "/" + s);

            setTarget(localdir + File.separator + s);

            sftp.copyRemoteDirectory(

                s,

                localdir,

                recurse,

                sync,

                true,

                FileTransferDialog.this);

          }

          // Now add the other files we wanted

          for (int i = 0; i < remotefiles.size(); i++) {

            s = (String) remotefiles.get(i);

            setSource(FileTransferDialog.this.sftp.pwd()

                      + "/" + s);

            setTarget(localdir + File.separator + s);

            sftp.get(s, FileTransferDialog.this);

          }

          // notify any waiting threads that weve completed our operation

          notifyWaiting();

        }

        catch (TransferCancelledException e) {

          notifyWaiting();

        }

        catch (IOException ex) {

          SshToolsApplicationPanel.showErrorMessage(FileTransferDialog.this,

              "An error occurred",

              "Copy Directories",

              ex);

        }

      }

    }

    , "Copying Directory");

    thread.start();

  }

  /**
   *
   *
   * @param sftp
   * @param files
   * @param totalBytes
   */

  public void getRemoteFiles(SftpClient sftp, java.util.List files,
                             long totalBytes) {

    this.files = files;

    this.totalBytes = totalBytes;

    this.sftp = sftp;

    lblTargetAction.setText("Downloading to:");

    progressbar.setMaximum( (int) totalBytes / 1024);

    if ( (totalBytes / 1048576) > 0) {

      // Were in the megabytes

      formattedTotal = formatMb.format( (double) totalBytes / 1048576)

          + " MB";

    }

    else {

      // Were still in Kilobytes

      formattedTotal = formatKb.format( (double) totalBytes / 1024)

          + " KB";

    }

    Thread thread = new Thread(new Runnable() {

      int i;

      public void run() {

        try {

          for (i = 0;

               i < FileTransferDialog.this.files.size();

               i++) {

            // Test whether the file exists before the download commences

            File file = new File(FileTransferDialog.this.sftp.lpwd()

                                 + File.separator

                                 + FileTransferDialog.this.files.get(i));

            if (file.exists()) {

              int result = JOptionPane.showConfirmDialog(FileTransferDialog.this,

                  "This file already exists are you sure you wish to overwrite?\n\n" +

                  file.getName(),

                  "Confirm File Overwrite",

                  JOptionPane.YES_NO_CANCEL_OPTION);

              if (result == JOptionPane.NO_OPTION) {

                if (i == FileTransferDialog.this.files.size() - 1) {

                  // This was the last file for transfer

                  cancelOperation();

                }
                else {

                  // Was not the last file so goto next

                  i++;

                }

              }

              if (result == JOptionPane.CANCEL_OPTION) {

                // Cancel the upload operation

                cancelOperation();

              }

            }

            setSource(FileTransferDialog.this.sftp.pwd()

                      + "/" + FileTransferDialog.this.files.get(i));

            setTarget(FileTransferDialog.this.sftp.lpwd()

                      + File.separator

                      + FileTransferDialog.this.files.get(i));

            if (!cancelled) {

              FileTransferDialog.this.sftp.get( (String) FileTransferDialog.this.

                                               files.get(i),

                                               FileTransferDialog.this);

            }

          }

          // Notify any waiting threads that we've completed our operation

          notifyWaiting();

        }

        catch (IOException ex) {

          if (!cancelled) {

            SshToolsApplicationPanel.showErrorMessage(FileTransferDialog.this,

                "The file operation failed!\n"

                + ex.getMessage(), "File Transfer",

                ex);

          }

          // Delete the partially completed file

          File file = new File(FileTransferDialog.this.sftp.lpwd()

                               + File.separator

                               + FileTransferDialog.this.files.get(i));

          if (file.exists()) {

            file.delete();

          }

          setVisible(false);

        }

      }

    });

    thread.start();

    setVisible(true);

  }

  private void jbInit() throws Exception {

    getContentPane().setLayout(new BorderLayout());

    progressbar.setToolTipText("");

    progressbar.setValue(0);

    try {

      Method m = progressbar.getClass().getMethod("setIndeterminate", new Class[] {boolean.class});

      Object[] args = new Object[] {

          new Boolean(true)};

      m.invoke(progressbar, args);

    }

    catch (Throwable ex) {

    }

    //progressbar.setIndeterminate(true);

    progressbar.setBounds(new Rectangle(8, 45, 336, 22));

    lblEstimatedTime.setText("Estimated time left: ");

    lblEstimatedTime.setBounds(new Rectangle(8, 74, 114, 17));

    lblTimeLeftValue.setText("");

    lblTimeLeftValue.setBounds(new Rectangle(109, 74, 235, 17));

    lblTargetAction.setText("Downloading to:");

    lblTargetAction.setBounds(new Rectangle(8, 92, 96, 17));

    lblTargetValue.setText("");

    lblTargetValue.setBounds(new Rectangle(109, 92, 235, 17));

    lblTransferRate.setText("Transfer rate: ");

    lblTransferRate.setBounds(new Rectangle(8, 110, 96, 17));

    lblTransferRateValue.setText("");

    lblTransferRateValue.setBounds(new Rectangle(109, 110, 235, 17));

    btnCancel.setText("Cancel");

    btnCancel.addActionListener(new FileTransferDialog_cancel_actionAdapter(

        this));

    chkClose.setText("Close this window when the transfer completes");

    chkClose.setBounds(new Rectangle(8, 132, 307, 18));

    chkClose.setSelected(true);

    JPanel p = new JPanel(null);

    lblAction.setBounds(new Rectangle(9, 5, 334, 21));

    lblPathValue.setText("");

    lblPathValue.setBounds(new Rectangle(10, 22, 329, 22));

    p.add(progressbar);

    p.add(lblEstimatedTime);

    p.add(lblTimeLeftValue);

    p.add(lblTargetAction);

    p.add(lblTransferRate);

    p.add(lblTransferRateValue);

    p.add(lblTargetValue);

    p.add(lblAction);

    p.add(lblPathValue);

    p.add(chkClose);

    JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    b.add(btnCancel);

    p.setMaximumSize(new Dimension(350, 185));

    p.setMinimumSize(new Dimension(350, 185));

    p.setPreferredSize(new Dimension(350, 185));

    getContentPane().add(p, BorderLayout.CENTER);

    getContentPane().add(b, BorderLayout.SOUTH);

    elaspedTime = 0;

    formatMb.setMaximumFractionDigits(2);

    formatKb.setMaximumFractionDigits(0);

    formatKb1.setMaximumFractionDigits(1);

    setResizable(true);

    setSize(new Dimension(350, 184));

    UIUtil.positionComponent(UIUtil.CENTER, this);

  }

  /**
   *
   *
   * @param lblTargetValue
   */

  public void setTarget(String lblTargetValue) {

    this.lblTargetValue.setText(lblTargetValue);

    this.lblTargetValue.setToolTipText(lblTargetValue);

  }

  /**
   *
   *
   * @param args
   */

  public static void main(String[] args) {

    FileTransferDialog dialog = new FileTransferDialog();

    dialog.show();

  }

  /**
   *
   *
   * @param lblTargetValue
   */

  public void setSource(String lblTargetValue) {

    this.source = lblTargetValue;

  }

  private int getFileCount() {

    return (files == null ? 0 : files.size())

        + (op == null ? 0 : op.getFileCount());

  }

  /**
   *
   *
   * @param fileSize
   * @param from
   */

  public void started(long fileSize, String from) {

    try {

      Method m = progressbar.getClass().getMethod("setIndeterminate", new Class[] {boolean.class});

      Object[] args = new Object[] {

          new Boolean(false)};

      m.invoke(progressbar, args);

    }

    catch (Throwable ex) {

    }

    String str;

    str = source

        + ( (getFileCount() <= 1) ? ""

           : (" ("

              + String.valueOf(filesTransfered + 1) + " of "

              + String.valueOf(getFileCount()) + " files)"));

    lblPathValue.setText(str);

    lastUpdate = System.currentTimeMillis();

    this.fileSize = fileSize;

    // Determine the transfer rate every second and update necessary controls

    lblTimeLeftValuer = new Timer(1000,

                                  new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        synchronized (lock) {

          if (elaspedTime < 1000) {

            transfer = bytesSoFar + completedTransfers;

          }

          else {

            transfer = (bytesSoFar + completedTransfers) / (elaspedTime / 1000);

          }

          lblTransferRateValue.setText(formatKb1.format(

              transfer / 1024) + " KB/Sec");

          long bytesLeft = FileTransferDialog.this.totalBytes

              - (completedTransfers + bytesSoFar);

          long secondsLeft = (long) (bytesLeft / transfer);

          setTitle(formatKb.format(

              (double) ( ( (double) (completedTransfers

                                     + bytesSoFar) / totalBytes)) * 100)

                   + "% Complete - " + title);

          String estimatedTime;

          if (secondsLeft < 60) {

            estimatedTime = String.valueOf(secondsLeft)

                + " sec";

          }

          else if (secondsLeft < 3600) {

            estimatedTime = String.valueOf(secondsLeft / 60)

                + " min "

                + String.valueOf(secondsLeft % 60) + " sec";

          }

          else {

            estimatedTime = String.valueOf(secondsLeft / 3600)

                + " hrs "

                + String.valueOf( (secondsLeft % 3600) / 60)

                + " min "

                + String.valueOf( (secondsLeft % 3600) % 60)

                + " sec";

          }

          // Work out how much lblTimeLeftValue is left

          if ( (completedTransfers + (bytesSoFar / 1048576)) > 0) {

            // Were in the megabytes

            lblTimeLeftValue.setText(estimatedTime + " ("

                                     + formatMb.format(

                (double) (completedTransfers

                          + bytesSoFar) / 1048576) + " MB of "

                                     + formattedTotal + " copied)");

          }

          else {

            // Were still in Kilobytes

            lblTimeLeftValue.setText(estimatedTime + " ("

                                     + formatKb.format(

                (double) (completedTransfers

                          + bytesSoFar) / 1024) + " KB of "

                                     + formattedTotal + " copied)");

          }

        }

      }

    });

    lblTimeLeftValuer.start();

  }

  /**
   *
   *
   * @return
   */

  public boolean isCancelled() {

    if (cancelled) {

      setVisible(false);

      return true;

    }

    return false;

  }

  /**
   *
   *
   * @param bytesSoFar
   */

  public void progressed(long bytesSoFar) {

    synchronized (lock) {

      this.bytesSoFar = bytesSoFar;

    }

    progressbar.setValue( (int) (completedTransfers + this.bytesSoFar) / 1024);

    currentTime = System.currentTimeMillis();

    elaspedTime += (currentTime - lastUpdate);

    String currentTotal;

    lastUpdate = currentTime;

  }

  /**
   *
   */

  public void completed() {

    filesTransfered++;

    completedTransfers += bytesSoFar;

    bytesSoFar = 0;

    if ( (getFileCount()) == filesTransfered) {

      setTitle("100% Complete - " + title);

      lblTimeLeftValue.setText("Completed (" + formattedTotal + " of "

                               + formattedTotal + " copied)");

      btnCancel.setText("Close");

      lblTimeLeftValuer.stop();

      completed = true;

      if (chkClose.isSelected()) {

        setVisible(false);

      }

    }

  }

  void cancel_actionPerformed(ActionEvent e) {

    // If were not complete then set the cancelled flag

    if (btnCancel.getText().equals("Cancel")) {

      cancelled = true;

    }

    else {

      setVisible(false);

    }

  }

}

class FileTransferDialog_cancel_actionAdapter

    implements java.awt.event.ActionListener {

  FileTransferDialog adaptee;

  FileTransferDialog_cancel_actionAdapter(FileTransferDialog adaptee) {

    this.adaptee = adaptee;

  }

  /**
   *
   *
   * @param e
   */

  public void actionPerformed(ActionEvent e) {

    adaptee.cancel_actionPerformed(e);

  }

}
