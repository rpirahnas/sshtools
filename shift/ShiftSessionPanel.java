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
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.Timer;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsApplication;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.ui.SshToolsApplicationSessionPanel;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.StandardAction;
import com.sshtools.common.ui.StatusBar;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.DataNotificationListener;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventAdapter;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.sftp.FileAttributes;
import net.iharder.dnd.*;
import com.sshtools.common.configuration.*;
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
     * @version $Id: ShiftSessionPanel.java,v 1.27 2003/11/16 19:30:08 rpernavas Exp $
 */
public class ShiftSessionPanel
    extends SshToolsApplicationSessionPanel {
  /** The SFTP Browser icon */
  public static final ResourceIcon FILE_BROWSER_ICON = new ResourceIcon(
      ShiftSessionPanel.class,
      "sftpsession.png");
  final static ResourceIcon ADD_FAVOURITE_ICON = new ResourceIcon(ShiftSessionPanel.class, "largeaddfavourite.png");
  SftpClient sftp;
  JScrollPane scrollPane = new JScrollPane();
  JTable listing = new JTable();
  DirectoryListingTableModel model;
  JPanel navigation = new JPanel(new GridBagLayout());
  JLabel address = new JLabel("Address");
  JComboBox addressDropdown = new JComboBox(); //(new javax.swing.tree.DefaultTreeModel(new FileNode("c:\\", new java.io.FilenameFilter() {
  Stack backStack = new Stack();
  Stack forwardStack = new Stack();
  CopyFromAction copyFrom;
  CopyToAction copyTo;
  DeleteAction delete;
  NewFolderAction newFolder;
  UpAction up;
  BackAction back;
  ForwardAction forward;
  HomeAction home;
  RootAction root;
  RefreshAction refresh;
  RenameAction rename;
  PropertiesAction properties;
  AddFavouriteAction favorite;
  StatusBar statusBar;
  StandardAction cd;
  JPopupMenu popupMenu = new JPopupMenu();
  String lastDownloadPath = new String();
  String lastUploadPath = new String();
  Object lock = new Object();
  int weight = 10;  // Stores the last weight value for the favorites menu
  boolean isUpdating = false;
  ChannelEventListener eventListener;

  Vector activeOperations = new Vector();

  /**
   * Creates a new ShiftSessionPanel object.
   */
  public ShiftSessionPanel() {
    super(new BorderLayout());
  }

  /**
   *
   *
   * @return
   */
  public SshToolsConnectionTab[] getAdditionalConnectionTabs() {
    return null;
  }

  public boolean requiresConfiguration() {
    return false;
  }

  public String getId() {
    return "shift";
  }

  public void addEventListener(ChannelEventListener eventListener) {
    this.eventListener = eventListener;
    if (sftp != null) {
      sftp.addEventListener(eventListener);
    }
  }

  /**
   *
   *
   * @return
   */
  public ResourceIcon getIcon() {
    return FILE_BROWSER_ICON;
  }

  /**
   *
   *
   * @param application
   *
   * @throws SshToolsApplicationException
   */
  public void init(SshToolsApplication application) throws
      SshToolsApplicationException {

    super.init(application);

    listing.setShowGrid(false);
    listing.setShowHorizontalLines(false);
    listing.setShowVerticalLines(false);
    listing.setIntercellSpacing(new Dimension(0, 0));
    listing.setColumnSelectionAllowed(false);

    model = new DirectoryListingTableModel(sftp);
    listing.setModel(model);

    scrollPane.getViewport().add(listing);
    scrollPane.getViewport().setBackground(Color.white);
    listing.setBackground(Color.white);

    // Set up our grid bag constraints
    Insets ins = new Insets(8, 2, 2, 2);
    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.anchor = GridBagConstraints.NORTH;
    gbc1.insets = ins;

    JPanel addresspanel = new JPanel();
    addresspanel.setLayout(new GridBagLayout());
    JButton go = new JButton("Go", new ResourceIcon(ShiftSessionPanel.class, "go.png")) {
      public void actionPerformed(ActionEvent ev) {
        changeDirectory(addressDropdown.getEditor().toString(), false);
      }
    };

    go.setBorder(BorderFactory.createEmptyBorder());
    gbc1.weightx = 0.0;
    UIUtil.jGridBagAdd(navigation, address, gbc1,
                       GridBagConstraints.WEST);
    gbc1.weightx = 1.0;
    gbc1.insets = new Insets(2, 2, 2, 2);
    UIUtil.jGridBagAdd(addresspanel, addressDropdown, gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 0.0;
    gbc1.insets = new Insets(2, 2, 2, 2);
    UIUtil.jGridBagAdd(addresspanel, go, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 2.0;
    UIUtil.jGridBagAdd(navigation, addresspanel, gbc1,
                       GridBagConstraints.REMAINDER);

    statusBar = new StatusBar();
    this.add(navigation, BorderLayout.NORTH);
    this.add(scrollPane, BorderLayout.CENTER);
    this.add(statusBar, BorderLayout.SOUTH);

    //  Create the action menu groups
    initActions();

    addressDropdown.setRenderer(new DefaultListCellRenderer() {
      public Component getListCellRendererComponent(
          JList list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        setText(value.toString());
        setBackground(isSelected ? Color.black : Color.white);
        setForeground(isSelected ? Color.white : Color.black);
        setIcon(FILE_BROWSER_ICON);
        return this;
      }
    });


    // Add the drag and drop listener to the scrollpane
    new FileDrop(scrollPane, null, new
                 FileDrop.Listener() {
      public void filesDropped(final java.io.File[] files) {
        Thread thread = new Thread(new Runnable() {
          public void run() {
            Vector localfiles = new Vector();
            Vector localdirs = new Vector();

            for (int i = 0; i < files.length; i++) {
              if (files[i].isDirectory()) {
                localdirs.add(files[i]);
              }
              else {
                localfiles.add(files[i]);
              }
            }

            if (localdirs.size() > 0) {
              copyLocalDirectories(localdirs, localfiles);
            }
            else if (files.length > 0) {
              copyLocalFiles(files);
            }

            try {
              model.refresh();
            }
            catch (IOException ex) {
            }
          }
        });

        thread.start();
      }
    });

    // Add drag and drop listener to the JTable
    new FileDrop(listing, null, new
                 FileDrop.Listener() {
      public void filesDropped(final java.io.File[] files) {
        Thread thread = new Thread(new Runnable() {
          public void run() {
            Vector localfiles = new Vector();
            Vector localdirs = new Vector();

            for (int i = 0; i < files.length; i++) {
              if (files[i].isDirectory()) {
                localdirs.add(files[i]);
              }
              else {
                localfiles.add(files[i]);
              }
            }

            if (localdirs.size() > 0) {
              copyLocalDirectories(localdirs, localfiles);
            }
            else if (files.length > 0) {
              copyLocalFiles(files);
            }

            try {
              model.refresh();
            }
            catch (IOException ex) {
            }
          }
        });

        thread.start();
      }
    });

    // Remove stuff we dont want
    deregisterAction(getAction("Options"));
    setActionVisible("New Window", false);
    setActionVisible("About", false);

  }

  protected void initActions() {

    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("File",
        "File", 'f', 0));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Edit",
        "Edit", 'e', 10));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Navigate",
        "Navigate", 'n', 20));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Favorites",
        "Favorites", 'v', 30));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Help",
        "Help", 'h', 90));

    ActionMap map = new ActionMap();

    listing.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if ( (evt.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
          getContextMenu().show(listing, evt.getX(), evt.getY());
        }
      }
    });

    home = new HomeAction() {
      public void actionPerformed(ActionEvent e) {
        changeDirectory("", true);
      }
    };

    registerAction(home);
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), home);

    refresh = new RefreshAction() {
      public void actionPerformed(ActionEvent e) {
        try {
          model.refresh();
        }
        catch (IOException ex) {
          SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
              "The operation failed",
              "Refresh Contents",
              ex);
        }
      }
    };
    registerAction(refresh);
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), refresh);

    registerAction(newFolder = new NewFolderAction() {
      public void actionPerformed(ActionEvent e) {
        createFolder();
      }
    });

    registerAction(rename = new RenameAction() {
      public void actionPerformed(ActionEvent e) {
        rename();
      }
    });

    up = new UpAction() {
      public void actionPerformed(ActionEvent e) {
        moveToParent();
        setAvailableActions();
      }
    };
    registerAction(up);
    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.ALT_MASK), up);

    registerAction(properties = new PropertiesAction() {
      public void actionPerformed(ActionEvent e) {
        PropertiesDialog dialog = new PropertiesDialog(
            model.getFileAttributes(listing.getSelectedRow()),
            sftp,
            model.getFilename(listing.getSelectedRow()),
            model);

        dialog.show();
      }
    });

    registerAction(copyFrom = new CopyFromAction() {
      public void actionPerformed(ActionEvent e) {
        copyLocalFiles();
      }
    });

    registerAction(copyTo = new CopyToAction() {
      public void actionPerformed(ActionEvent e) {
        copyRemoteFiles();
      }
    });

    registerAction(delete = new DeleteAction() {
      public void actionPerformed(ActionEvent e) {
        removeSelected();
      }
    });

    loadFavorites();

    registerAction(favorite = new AddFavouriteAction() {
      public void actionPerformed(ActionEvent e) {
        Object[] options = new Object[] {
            "Add", "Cancel"};
        Object opt = JOptionPane.showInputDialog(ShiftSessionPanel.this,
                                                 "Do you want add " +
                                                 sftp.pwd().toString()
                                                 + " as a favorite? ",
                                                 "Add Favorite",
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 ADD_FAVOURITE_ICON, null,
                                                 sftp.pwd().toString());

        if (opt != null) {
          getCurrentConnectionProfile().
              setSftpFavorite(opt.toString(), sftp.pwd());

          FavoriteAction action;
          action = new FavoriteAction(opt.toString(), weight, sftp.pwd()) {
            public void actionPerformed(ActionEvent e) {
              changeDirectory(this.directory, true);
            }
          };

          // Add it to the menu bar
          addFavorite(action);

          // Save the profile
          try {
            getCurrentConnectionProfile().save();
          }
          catch (InvalidProfileFileException ex) {
            ex.printStackTrace();
          }

          // Increment the weight value for the next favorite
          weight = weight + 10;
        }
      }
    });

    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.ALT_MASK), favorite);


    listing.setActionMap(map);

    int i = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

    listing.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false),
                               "Enter");
    listing.getActionMap().put("Enter", new StandardAction() {
      public void actionPerformed(ActionEvent e) {
        if (model.isDirectory(listing.getSelectedRow()) &&
            listing.getSelectedRowCount() == 1) {
          changeDirectory(model.getFilename(listing.getSelectedRow()), false);
          setAvailableActions();
        }
      }
    });

    listing.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
        KeyEvent.ALT_MASK, false), "Root");
    registerAction(root = new RootAction() {
      public void actionPerformed(ActionEvent e) {
        changeDirectory("/", true);
      }
    });
    //registerAction(root);
    listing.getActionMap().put("Root", root);

    listing.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                               "Up");
    listing.getActionMap().put("Up", new StandardAction() {
      public void actionPerformed(ActionEvent e) {
        if (listing.getSelectedRow() > 0) {
          int row = listing.getSelectedRow();
          listing.getSelectionModel().clearSelection();
          listing.getSelectionModel().setSelectionInterval(row - 1, row - 1);
          listing.repaint();
          setAvailableActions();
        }
        else if (listing.getSelectedRowCount() == 0) {
          // if none selected, select last in list
          listing.getSelectionModel().setSelectionInterval(listing.getRowCount(),
              listing.getRowCount());
          listing.repaint();
          setAvailableActions();
        }
      }
    });

    listing.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                               "Down");
    listing.getActionMap().put("Down", new StandardAction() {
      public void actionPerformed(ActionEvent e) {
        if (listing.getSelectedRow() < listing.getRowCount()) {
          int row = listing.getSelectedRow();
          listing.getSelectionModel().clearSelection();
          listing.getSelectionModel().setSelectionInterval(row + 1, row + 1);
          listing.repaint();
          setAvailableActions();
        }
      }
    });

    listing.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
        KeyEvent.ALT_MASK, false), "Back");
    back = new BackAction() {
      public void actionPerformed(ActionEvent e) {
        if (backStack.size() > 0) {
          changeDirectory( (String) backStack.pop(), false);
          setAvailableActions();
        }
      }
    };
    registerAction(back);
    listing.getActionMap().put("Back", back);

    listing.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
        KeyEvent.ALT_MASK, false), "Forward");
    forward = new ForwardAction() {
      public void actionPerformed(ActionEvent e) {
        if (forwardStack.size() > 0) {
          String cwd = (String) forwardStack.pop();
          changeDirectory(cwd, true);
          setAvailableActions();
        }
      }
    };
    registerAction(forward);
    listing.getActionMap().put("Forward", forward);

  }

  private void loadFavorites() {
    try {
      Map map = getCurrentConnectionProfile().getSftpFavorites();
      Iterator it = map.entrySet().iterator();
      Map.Entry entry;

      while (it.hasNext()) {
        entry = (Map.Entry) it.next();
        final String name = (String) entry.getKey();
        final String directory = (String) entry.getValue();
        final FavoriteAction action;

        action = new FavoriteAction(name, weight, directory) {
          public void actionPerformed(ActionEvent e) {
              changeDirectory(this.directory, true);
          }
        };
        // Register the action
        registerAction(action);
        weight = weight + 10;
      }
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  private void copyLocalFiles() {

    Thread thread = new Thread(new Runnable() {

      public void run() {

        // Select the files using a JFileChooser
        JFileChooser chooser = new JFileChooser(
            System.getProperty("user.home"));
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select files to upload");
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (lastUploadPath != null && !lastUploadPath.equals("")) {
          File file = new File(lastUploadPath);
          if (file.exists()) {
            chooser.setCurrentDirectory(new File(lastUploadPath));
          }
        }

        if (chooser.showDialog(ShiftSessionPanel.this, "Upload") ==
            JFileChooser.APPROVE_OPTION) {
          // Download the files
          File[] files = chooser.getSelectedFiles();

          lastUploadPath = chooser.getCurrentDirectory().getAbsolutePath();

          Vector localfiles = new Vector();
          Vector localdirs = new Vector();

          for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
              localdirs.add(files[i]);
            }
            else {
              localfiles.add(files[i]);
            }
          }

          if (localdirs.size() > 0) {
            copyLocalDirectories(localdirs, localfiles);
          }
          else if (files.length > 0) {
            copyLocalFiles(files);
          }
        }
      }
    });

    thread.start();
  }

  private void copyRemoteFiles() {
    Thread thread = new Thread(new Runnable() {

      public void run() {
        // Select the location to download the files to
        Vector remotefiles = new Vector();
        Vector remotedirs = new Vector();

        int[] rows = listing.getSelectedRows();

        for (int i = 0; i < rows.length; i++) {
          if (model.isDirectory(rows[i])) {
            remotedirs.add(model.getFilename(rows[i]));
          }
          else {
            remotefiles.add(model.getFilename(rows[i]));
          }
        }

        // Select the copy location
        JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
        chooser.setMultiSelectionEnabled(false);

        chooser.setDialogTitle("Select destination location");

        if (lastDownloadPath != null && !lastDownloadPath.equals("")) {
          File file = new File(lastDownloadPath);
          if (file.exists()) {
            chooser.setCurrentDirectory(new File(lastDownloadPath));
          }
        }

        chooser.setFileSelectionMode(
            JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showDialog(ShiftSessionPanel.this, "Copy") ==
            JFileChooser.APPROVE_OPTION) {
          // Download the files
          try {
            sftp.lcd(chooser.getSelectedFile().getAbsolutePath());
            lastDownloadPath = chooser.getSelectedFile().getAbsolutePath();

            if (remotefiles.size() > 0 && remotedirs.size() == 0) {
              copyRemoteFiles(remotefiles);
            }
            else {
              copyRemoteDirectories(remotedirs, remotefiles,
                                    chooser.getSelectedFile());
            }
          }
          catch (IOException ex) {
            SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
                "An error occured copying the remote files!",
                "Copy files",
                ex);
          }
        }
      }
    });

    thread.start();
  }

  /**
   *
   */
  protected void moveToParent() {
    try {
      String cwd = model.getCurrentDirectory();
      int idx;

      if ( ( (idx = cwd.lastIndexOf("/")) >= 0) && (cwd.length() > 1)) {
        changeDirectory(cwd.substring(0, idx + 1), true);
      }
      else {
        Toolkit.getDefaultToolkit().beep();
      }
    }
    catch (IOException ex) {
    }
  }

  /**
   *
   *
   * @throws java.io.IOException
   */
  public boolean onOpenSession() throws java.io.IOException {
    sftp = manager.openSftpClient();

    // Add the callers eventlistener if any
    if (eventListener != null) {
      sftp.addEventListener(eventListener);
      // Add an event listener for the flashing lights
    }
    sftp.addEventListener(new DataNotificationListener(statusBar));

    statusBar.setHost("Connected to " + manager.getProfile().getHost());
    statusBar.setConnected(true);

    listing.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          if (listing.getSelectedRowCount() == 1) {
            if (model.isDirectory(listing.getSelectedRow())) {
              // Change directory
              changeDirectory(model.getFilename(
                  listing.getSelectedRow()), true);
            }
            else {
              // Download the file to the default location
              Vector l = new Vector();
              l.add(model.getFilename(
                  listing.getSelectedRow()));
              copyRemoteFiles(l);

            }
          }
        }
        setAvailableActions();
      }

      public void mouseReleased(MouseEvent e) {
        setAvailableActions();
      }
    });

    listing.addMouseListener(new PopupListener(popupMenu));
    listing.setModel(model = new DirectoryListingTableModel(sftp));
    listing.setAutoCreateColumnsFromModel(true);
    model.refresh();
    updateAddress(model.getCurrentDirectory());

    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
    listing.getColumn("Name").setCellRenderer(new SftpTableCellRenderer());
    listing.getColumn("Size").setCellRenderer(new FileSizeTableCellRenderer());
    listing.setBackground(Color.white);

    addressDropdown.setEditable(true);
    addressDropdown.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          synchronized (lock) {
            if (!isUpdating) {
              if (e.getItem()instanceof TokenizedPathElement) {
                changeDirectory( ( (TokenizedPathElement) e
                                  .getItem()).path, true);
              }
              else if (e.getItem()instanceof String) {
                changeDirectory(e.getItem().toString(), true);
              }
            }
          }
        }
      }
    });

    listing.getColumnModel().getColumn(0).setPreferredWidth(200);

    return true;
  }

  /**
   *
   *
   * @param localfiles
   */
  protected void copyLocalFiles(final File[] localfiles) {
    Runnable r = new Runnable() {
      public void run() {
        FileTransferDialog transfer = null;

        try {
          Vector files = new Vector();

          for (int i = 0; i < localfiles.length; i++) {
            files.add(localfiles[i].getName());
          }

          transfer = new FileTransferDialog( (Frame) SwingUtilities
                                            .getAncestorOfClass(Frame.class,
              ShiftSessionPanel.this), "Uploading",
                                            files.size());

          synchronized (activeOperations) {
            activeOperations.add(transfer);
          }
          transfer.putFiles(sftp,
                            localfiles[0].getParentFile(), files);

          transfer.waitForCompletion();
          synchronized (activeOperations) {
            activeOperations.remove(transfer);
          }

          System.out.println();
          model.refresh();
        }
        catch (final Exception ex) {
          if ( (transfer != null) && !transfer.isCancelled()) {
            try {
              SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
                      "Failed to download files!",
                      "SFTP Browser",
                      ex);
                }
              });
            }
            catch (Exception ex1) {
              log.info("Failed to invoke message box through SwingUtilities",
                       ex1);
            }
          }
        }
      }
    };
    Thread t = new Thread(r);
    t.start();

  }

  /**
   *
   *
   * @param remotefiles
   */
  protected void copyRemoteFiles(final java.util.List remotefiles) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        FileTransferDialog transfer = null;

        try {
          long totalBytes = 0;

          FileAttributes attrs;

          for (int i = 0; i < remotefiles.size(); i++) {
            attrs = sftp.stat( (String) remotefiles.get(i));
            totalBytes += attrs.getSize().longValue();
          }

          transfer = new FileTransferDialog( (Frame) SwingUtilities
                                            .getAncestorOfClass(Frame.class,
              ShiftSessionPanel.this), "Downloading", 1);

          transfer.getRemoteFiles(sftp, remotefiles,
                                  totalBytes);

          synchronized (activeOperations) {
            activeOperations.add(transfer);
          }
          transfer.waitForCompletion();
          synchronized (activeOperations) {
            activeOperations.remove(transfer);
          }

        }
        catch (final Exception ex) {
          if ( (transfer != null) && !transfer.isCancelled()) {
            try {
              SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
                      "Failed to download files!",
                      "SFTP Browser",
                      ex);
                }
              });
            }
            catch (Exception ex1) {
              log.info("Failed to invoke message box through SwingUtilities",
                       ex1);
            }
          }
        }
      }
    }

    , "Downloading files");

    thread.start();
  }

  protected void copyRemoteDirectories(final java.util.List remotedirs,
                                       final java.util.List remotefiles,
                                       final File localdir) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        FileTransferDialog transfer = null;

        try {
          int result = JOptionPane.showOptionDialog(ShiftSessionPanel.this,
              "How would you like to download the contents of "
              +
              (remotedirs.size() > 1 ? "these directories?" : "this directory?"),
                                                  "Copy Directory",
                                                  JOptionPane.DEFAULT_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null,
                                                  new String[] {"Synchronize",
                                                  "Recurse Copy", "Flat Copy",
                                                  "Cancel"}
                                                  ,
                                                  "Recurse");

          if (result == 3) {
            return;
          }

          if (result == 0) {
            if (localdir.isDirectory() && localdir.listFiles().length > 0) {
              result = JOptionPane.showConfirmDialog(ShiftSessionPanel.this,
                  "The directory you have chosen is not empty!  \nThe synchronize " +
                  "option will clear the contents of this directory!  \n\nAre you " +
                  "sure you wish to proceed?", "Confirm Synchronize Operation",
                  JOptionPane.YES_NO_OPTION);

              if (result == JOptionPane.NO_OPTION) {
                return;
              }
            }
          }

          statusBar.setRemoteId("");

          transfer = new FileTransferDialog( (Frame) SwingUtilities
                                            .getAncestorOfClass(Frame.class,
              ShiftSessionPanel.this), "Downloading", 1);

          transfer.copyRemoteDirectory(sftp,
                                       remotedirs,
                                       remotefiles,
                                       localdir.getAbsolutePath(),
                                       result == 1 || result == 0,
                                       result == 0);

          synchronized (activeOperations) {
            activeOperations.add(transfer);
          }
          transfer.waitForCompletion();
          synchronized (activeOperations) {
            activeOperations.remove(transfer);
          }

        }
        catch (final Exception ex) {
          if ( (transfer != null) && !transfer.isCancelled()) {
            try {
              SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
                      "Failed to download directories!",
                      "SFTP Browser",
                      ex);
                }
              });
            }
            catch (Exception ex1) {
              log.info("Failed to invoke message box through SwingUtilities",
                       ex1);
            }
          }
        }
      }
    }

    , "Downloading directories");

    thread.start();
  }

  protected void copyLocalDirectories(final java.util.List localdirs,
                                      final java.util.List localfiles) {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        FileTransferDialog transfer = null;

        try {
          int result = JOptionPane.showOptionDialog(ShiftSessionPanel.this,
              "How would you like to upload the contents of "
              +
              (localdirs.size() > 1 ? "these directories?" : "this directory?"),
                                                  "Copy Directory",
                                                  JOptionPane.DEFAULT_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE,
                                                  null,
                                                  new String[] {"Synchronize",
                                                  "Recurse Copy", "Flat Copy",
                                                  "Cancel"}
                                                  ,
                                                  "Recurse");

          if (result == 3) {
            return;
          }

          if (result == 0) {

            result = JOptionPane.showConfirmDialog(ShiftSessionPanel.this,
                "The synchronize " +
                "option will clear the contents of this directory!  \n\nAre you " +
                "sure you wish to proceed?", "Confirm Synchronize Operation",
                JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.NO_OPTION) {
              return;
            }

          }

          statusBar.setRemoteId("");

          transfer = new FileTransferDialog( (Frame) SwingUtilities
                                            .getAncestorOfClass(Frame.class,
              ShiftSessionPanel.this), "Uploading", 1);

          transfer.copyLocalDirectory(sftp,
                                      localfiles,
                                      localdirs,
                                      sftp.pwd(),
                                      result == 1 || result == 0,
                                      result == 0);

          synchronized (activeOperations) {
            activeOperations.add(transfer);
          }
          transfer.waitForCompletion();
          synchronized (activeOperations) {
            activeOperations.remove(transfer);
          }

          model.refresh();
        }
        catch (final Exception ex) {
          if ( (transfer != null) && !transfer.isCancelled()) {
            try {
              SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                  SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
                      "Failed to upload directories!",
                      "SFTP Browser",
                      ex);
                }
              });
            }
            catch (Exception ex1) {
              log.info("Failed to invoke message box through SwingUtilities",
                       ex1);
            }
          }
        }
      }
    }

    , "Uploading directories");

    thread.start();
  }

  /**
   *
   */
  protected void createFolder() {
    Runnable r = new Runnable() {
      public void run() {
        try {
          statusBar.setStatusText("Creating folder");
          String folder = JOptionPane.showInputDialog(ShiftSessionPanel.this,
              "Enter new folder name",
              "Create Folder",
              JOptionPane.OK_CANCEL_OPTION);
          if (folder != null && !folder.trim().equals("")) {
            model.createFolder(folder);
          }
          setAvailableActions();
          statusBar.setStatusText("Ready...");
        }
        catch (IOException ex) {
          statusBar.setStatusText("Ready..."); ;
          SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
              "Failed to create directory!",
              "SFTP Browser",
              ex);
        }
      }
    };
    Thread t = new Thread(r);
    t.start();

  }

  protected void rename() {
    Runnable r = new Runnable() {
      public void run() {
        try {
          statusBar.setStatusText("Renaming...");
          String newname = JOptionPane.showInputDialog(ShiftSessionPanel.this,
              "Enter new name",
              "Rename",
              JOptionPane.OK_CANCEL_OPTION);

          if (newname != null && !newname.trim().equals("")) {
            sftp.rename(model.getFilename(listing.getSelectedRow()), newname);
            model.refresh();
          }
          setAvailableActions();
          statusBar.setStatusText("Ready..."); ;
        }
        catch (IOException ex) {
          statusBar.setStatusText("Ready..."); ;
          SshToolsApplicationPanel.showErrorMessage(ShiftSessionPanel.this,
              "Failed to rename!",
              "SFTP Browser",
              ex);
        }
      }
    };
    Thread t = new Thread(r);
    t.start();
  }

  /**
   *
   *
   * @param directory
   * @param forwards
   */
  protected void changeDirectory(final String directory, final boolean forwards) {

    listing.getSelectionModel().clearSelection();
    setAvailableActions();

    Runnable r = new Runnable() {
      public void run() {
        statusBar.setStatusText("Listing directory contents... ");

        try {
          if (forwards) {
            backStack.add(model.getCurrentDirectory());
          }
          else {
            forwardStack.add(model.getCurrentDirectory());
          }
          model.changeDirectory(directory);
          updateAddress(model.getCurrentDirectory());

          statusBar.setStatusText("Ready..."); ;
          setAvailableActions();
        }
        catch (final IOException ex) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JOptionPane.showMessageDialog(ShiftSessionPanel.this,
                                            "Failed to change directory!\n" +
                                            ex.getMessage(),
                                            "SFTP Browser",
                                            JOptionPane.ERROR_MESSAGE);
            }
          });
        }
      }
    };
    Thread t = new Thread(r);
    t.start();
  }

  /**
   *
   */
  protected void removeSelected() {
    try {

      String message;

      if (listing.getSelectedRowCount() == 1) {
        if (model.isDirectory(listing.getSelectedRow())) {
          message =
              "Are you sure you wish to remove the selected folder and all its contents?";
        }
        else {
          message = "Are you sure you wish to remove this file?";
        }

        if (JOptionPane.showConfirmDialog(ShiftSessionPanel.this,
                                          message,
                                          "Confirm Delete",
                                          JOptionPane.YES_NO_OPTION) ==
            JOptionPane.YES_OPTION) {
          statusBar.setStatusText("Deleting...");
          model.removeFile(listing.getSelectedRows());
        }
      }

      if (listing.getSelectedRowCount() > 1) {

        boolean containsFolder = false;
        int[] rows = listing.getSelectedRows();

        for (int i = 0; i < rows.length; i++) {
          if (model.isDirectory(rows[i])) {
            containsFolder = true;
            break;
          }
        }

        if (containsFolder) {
          message = "Are you sure you wish to delete these "
              + String.valueOf(listing.getSelectedRowCount())
              + " items and their contents?";
        }
        else {
          message = "Are you sure you wish to delete these "
              + String.valueOf(listing.getSelectedRowCount())
              + " items?";
        }

        if (JOptionPane.showConfirmDialog(ShiftSessionPanel.this,
                                          message,
                                          "Confirm Multiple File Delete",
                                          JOptionPane.YES_NO_OPTION) ==
            JOptionPane.YES_OPTION) {

          model.removeFile(listing.getSelectedRows());
          setAvailableActions();
        }
      }
      statusBar.setStatusText("Ready...");
    }
    catch (final IOException ex) {
      statusBar.setStatusText("Ready...");
      JOptionPane.showMessageDialog(ShiftSessionPanel.this,
                                    "Failed to change directory!\n" +
                                    ex.getMessage(),
                                    "SFTP Browser", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void updateAddress(String address) throws IOException {
    synchronized (lock) {
      if (address.startsWith("/")) {
        isUpdating = true;

        StringTokenizer tokens = new StringTokenizer(address, "/");
        String path = "/";
        addressDropdown.removeAllItems();
        addressDropdown.addItem(new TokenizedPathElement("/", path));

        String tab = "  ";
        String name;

        while (tokens.hasMoreElements()) {
          name = (String) tokens.nextElement();
          addressDropdown.addItem(new TokenizedPathElement(tab + name,
              (path += ( ( (path.trim().length() > 1) ? "/" : "")
                        + name))));
          tab += tab;
        }

        addressDropdown.setSelectedIndex(addressDropdown.getModel().getSize() -
                                         1);
        isUpdating = false;
      }
      else {
        throw new IOException("Path must be absolute!!!");
      }
    }
  }

  /**
   *
   *
   * @return
   */
  public boolean postConnection() {
    return true;
  }

  /**
   *
   */
  public void setAvailableActions() {

    if (listing.getSelectedRowCount() == 0) {
      delete.setEnabled(false);
      properties.setEnabled(false);
      rename.setEnabled(false);
    }
    else {
      delete.setEnabled(true);
      properties.setEnabled(true);
      rename.setEnabled(true);
    }

    copyTo.setEnabled(true);
    copyFrom.setEnabled(true);
    up.setEnabled(true);
    newFolder.setEnabled(true);

    if (backStack.size() > 0) {
      back.setEnabled(true);
    }
    else {
      back.setEnabled(false);
    }

    if (forwardStack.size() > 0) {
      forward.setEnabled(true);
    }
    else {
      forward.setEnabled(false);
    }
  }

  /**
   *
   */
  public void close() {
    if ( (sftp != null) && !sftp.isClosed()) {
      try {
        sftp.quit();
      }
      catch (Exception ex) {
      }
      finally {
        manager.requestDisconnect();
      }
    }
  }

  /**
   *
   *
   * @return
   */
  public boolean canClose() {

    synchronized (activeOperations) {
      if ( (sftp != null) && !sftp.isClosed()) {

        if (activeOperations.size() > 0) {
          if (JOptionPane.showConfirmDialog(this,
                                            "There are "
                                            +
                                            String.valueOf(activeOperations.
              size())
                                            +
              " active file operations in progress!\n"
              +
              "Do you want to cancel these operations and close the session?",
              "Terminate Operations",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.YES_OPTION) {

            FileTransferDialog dialog;
            for (Iterator it = activeOperations.iterator();
                 it.hasNext(); ) {
              dialog = (FileTransferDialog) it.next();
              dialog.cancelOperation();
            }

            return true;
          }

          return false;
        }
        else {
          if (JOptionPane.showConfirmDialog(this,
                                            "Close the current session?",
                                            "Close Session",
                                            JOptionPane.YES_NO_OPTION,
                                            JOptionPane.QUESTION_MESSAGE) ==
              JOptionPane.NO_OPTION) {
            return false;
          }
        }

      }

      return true;
    }
  }

  class PopupListener
      extends MouseAdapter {
    JPopupMenu popup;

    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      // Display popup menu
      if (e.isPopupTrigger()) {
        back.setEnabled(backStack.size() > 0);
        forward.setEnabled(forwardStack.size() > 0);
        up.setEnabled(true);

        if (listing.getSelectedRowCount() > 0) {
          delete.setEnabled(true);
        }
        else {
          delete.setEnabled(false);
        }

        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  public class InputStreamMonitor
      extends InputStream
      implements ActionListener {
    javax.swing.Timer timer;
    InputStream in;

    InputStreamMonitor(InputStream in) {
      this.in = in;
      timer = new javax.swing.Timer(500, this);
      timer.setRepeats(false);
    }

    public void actionPerformed(ActionEvent evt) {
      statusBar.setReceiving(false);
    }

    public int read() throws IOException {
      int i = in.read();
      dataRead(1);

      return i;
    }

    public int read(byte[] b) throws IOException {
      int i = in.read(b);
      dataRead(i);

      return i;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      int i = in.read(b, off, len);
      dataRead(i);

      return i;
    }

    void dataRead(int i) {
      if (!timer.isRunning()) {
        statusBar.setReceiving(true);
        timer.start();
      }
    }
  }

  public class OutputStreamMonitor
      extends OutputStream
      implements ActionListener {
    javax.swing.Timer timer;
    OutputStream out;

    OutputStreamMonitor(OutputStream out) {
      this.out = out;
      timer = new javax.swing.Timer(500, this);
      timer.setRepeats(false);
    }

    public void write(int b) throws IOException {
      out.write(b);
      dataWritten(1);
    }

    public void write(byte[] b) throws IOException {
      out.write(b);
      dataWritten(b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
      out.write(b, off, len);
      dataWritten(len);
    }

    public void actionPerformed(ActionEvent evt) {
      statusBar.setSending(false);
    }

    void dataWritten(int i) {
      if (!timer.isRunning()) {
        statusBar.setSending(true);
        timer.start();
      }
    }
  }

  class TokenizedPathElement {
    String name;
    String path;

    TokenizedPathElement(String name, String path) {
      this.name = name;
      this.path = path;
    }

    public String toString() {
      return path;
    }
  }


}
