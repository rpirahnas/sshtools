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

//import java.awt.event.MouseWheelEvent;
//import java.awt.event.MouseWheelListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import java.awt.AWTEvent;
import java.awt.AWTPermission;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.configuration.InvalidProfileFileException;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.mru.MRUAction;
import com.sshtools.common.mru.MRUListModel;
import com.sshtools.common.mru.MRUMenu;
import com.sshtools.common.ui.*;
import com.sshtools.common.util.X11Util;
import com.sshtools.j2ssh.SshEventAdapter;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.connection.ChannelEventAdapter;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.j2ssh.transport.TransportProtocol;
import com.sshtools.j2ssh.transport.TransportProtocolCommon;
import com.sshtools.sshterm.emulation.TerminalEmulation;
import com.sshtools.sshterm.emulation.TerminalListener;
import com.sshtools.sshterm.emulation.TerminalPanel;

//import com.sshtools.sshterm.forward.*;

public class SshTerminalPanel
    extends SshToolsApplicationClientPanel
    implements ActionListener, ClipboardOwner, TerminalListener,
    MouseMotionListener
/*, MouseWheelListener*/{
  // Preferenc names
  public final static String PREF_SHIFT_GEOMETRY = "sshterm.shift.geometry";
  public final static String PREF_PORT_FORWARDING_GEOMETRY =
      "sshterm.portForwarding.geometry";
  public final static String PREF_PORT_FORWARDING_DIVIDER_LOCATION =
      "sshterm.portForwarding.dividerLocation";
  public final static String PREF_PORT_FORWARDING_FORWARDS_TABLE_METRICS =
      "sshterm.portForwarding.forwards.tableMetrics";
  public final static String PREF_PORT_FORWARDING_ACTIVE_TABLE_METRICS =
      "sshterm.portForwarding.active.tableMetrics";
  public final static String PREF_PAGE_FORMAT_IMAGEABLE_X =
      "sshterm.pageFormat.imageable.x";
  public final static String PREF_PAGE_FORMAT_IMAGEABLE_Y =
      "sshterm.pageFormat.imageable.y";
  public final static String PREF_PAGE_FORMAT_IMAGEABLE_W =
      "sshterm.pageFormat.imageable.w";
  public final static String PREF_PAGE_FORMAT_IMAGEABLE_H =
      "sshterm.pageFormat.imageable.h";
  public final static String PREF_PAGE_FORMAT_SIZE_W =
      "sshterm.pageFormat.size.w";
  public final static String PREF_PAGE_FORMAT_SIZE_H =
      "sshterm.pageFormat.size.h";
  public final static String PREF_PAGE_FORMAT_ORIENTATION =
      "sshterm.pageFormat.orientation";
  public final static String PREF_X11_FORWARDING = "sshterm.xForwarding";
  public final static String PREF_X11_FORWARDING_LOCAL_DISPLAY =
      "sshterm.xForwarding.localDisplay";
  public final static String PREF_X11_FORWARDING_REMOTE_DISPLAY_NUMBER =
      "sshterm.xForwarding.remoteDisplayNumber";
  public final static String PREF_MOUSE_WHEEL_INCREMENT =
      "sshterm.mouseWheelIncrement";

  // Profile properties
  public final static String PROFILE_PROPERTY_TERMINAL = "TERM";
  public final static String PROFILE_PROPERTY_EOL = "EOL";
  public final static String PROFILE_PROPERTY_SCREEN_SIZE = "TERM_SCREEN_SIZE";
  public final static String PROFILE_PROPERTY_RESIZE_STRATEGY =
      "RESIZE_STRATEGY";
  public final static String PROFILE_PROPERTY_ANTIALIAS = "ANTIALIAS";
  public final static String PROFILE_PROPERTY_FONT_SIZE = "FONT_SIZE";
  public final static String PROFILE_PROPERTY_BACKGROUND_COLOR =
      "BACKGROUND_COLOR";
  public final static String PROFILE_PROPERTY_FOREGROUND_COLOR =
      "FOREGROUND_COLOR";
  public final static String PROFILE_PROPERTY_COLOR_PRINTING = "COLOR_PRINTING";

  // Screen size properties
  public final static int PROFILE_SCREEN_640_480 = 0;
  public final static int PROFILE_SCREEN_800_600 = 1;
  public final static int PROFILE_SCREEN_1024_768 = 2;

  private final static long VDU_EVENTS = AWTEvent.KEY_EVENT_MASK
      | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.ACTION_EVENT_MASK
      | AWTEvent.WINDOW_EVENT_MASK /*| AWTEvent.WINDOW_FOCUS_EVENT_MASK*/
      | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
      ;

  //
  final static String ABOUT_ICON = "/com/sshtools/sshterm/largessh.png";

  // Logger
  protected static Log log = LogFactory.getLog(SshTerminalPanel.class);

  // The scroll bar
  private JScrollBar scrollBar;

  // The shell client
  private SessionChannelClient session;

  // The profile EOL setting
  private int eol;

  // The terminal
  private TerminalPanel terminal;

  // Key generation frame
  private com.sshtools.common.keygen.Main keygenFrame;

  // Port forwarding pane
  //private PortForwardingPane portForwardingPane;

  // Action menus
  private Vector actionMenus;

  private DataNotificationListener dataListener;

  // Port forwarding dialog
  //private JDialog portForwardingDialog;
  //private SessionProviderFrame tunnelingFrame;

  // Session frames
  private Vector sessionFrames = new Vector();
  private HashMap sessionActions = new HashMap();

  // Connected status
  protected TerminalEmulation emulation;

  // Actions
  private NewAction newAction;
  private PrintPreviewAction printPreviewAction;
  private PrintAction printAction;
  private CloseAction closeAction;
  private CopyAction copyAction;
  private PasteAction pasteAction;
  private KeygenAction keygenAction;
  private ClearAction clearAction;
  private RefreshAction refreshAction;
  private PlayAction playAction;
  private StopAction stopAction;
  private RecordAction recordAction;
  private StandardAction shiftAction;
  private StandardAction tunnelingAction;
  private Vector actions;
  private PageFormat pageFormat;
  private OpenAction openAction;
  private EditAction editAction;
  private SaveAction saveAction;
  private SaveAsAction saveAsAction;
  private MRUActionImpl mruAction;
  private ConnectionPropertiesAction connectionPropertiesAction;
  //private CloneTerminalAction cloneTerminalAction;

  // The current connection properties
  private JSeparator toolSeparator;

  // The content pane
  JPanel contentPane;

  //
  private OutputStream recordingOutputStream;
  private File recordingFile;

  //
  private StatusBar statusBar;

  // Our borders
  private TitledBorder titledBorder1;
  private BorderLayout borderLayout1 = new BorderLayout();

  // What can the user change for the connection?
  private boolean anyHost = true;

  // What can the user change for the connection?
  private boolean anyUser = true;

  // What can the user change for the connection?
  private boolean anyPort = true;
  private boolean dialogActionsEnabled;

  // Full screen mode
  private boolean fullScreenMode;

  // Auto hide
  private boolean autoHideTools;
  private SshToolsConnectionTab[] additionalTabs;

  public static final ResourceIcon SSHTERM_ICON = new ResourceIcon(
      SshTerminalPanel.class,
      "sshframeicon.gif");

  public SshTerminalPanel() {
  }

  public void init(SshToolsApplication application) throws
      SshToolsApplicationException {
    super.init(application);

    //  Additional connection tabs
    additionalTabs = new SshToolsConnectionTab[] {
        new SshTermCommandTab(), new SshTermTerminalTab() /*,
        new XForwardingTab()*/
    };

    //
    //portForwardingPane = new PortForwardingPane();

    //  Printing page format
    try {
      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new RuntimePermission(
            "queuePrintJob"));
      }

      try {
        PrinterJob job = PrinterJob.getPrinterJob();

        if (job == null) {
          throw new IOException("Could not get print page format.");
        }

        pageFormat = job.defaultPage();

        if (PreferencesStore.preferenceExists(
            PREF_PAGE_FORMAT_ORIENTATION)) {
          pageFormat.setOrientation(PreferencesStore.getInt(
              PREF_PAGE_FORMAT_ORIENTATION, PageFormat.LANDSCAPE));

          Paper paper = new Paper();
          paper.setImageableArea(PreferencesStore.getDouble(
              PREF_PAGE_FORMAT_IMAGEABLE_X, 0),
                                 PreferencesStore.getDouble(
              PREF_PAGE_FORMAT_IMAGEABLE_Y, 0),
                                 PreferencesStore.getDouble(
              PREF_PAGE_FORMAT_IMAGEABLE_W, 0),
                                 PreferencesStore.getDouble(
              PREF_PAGE_FORMAT_IMAGEABLE_H, 0));
          paper.setSize(PreferencesStore.getDouble(
              PREF_PAGE_FORMAT_SIZE_W, 0),
                        PreferencesStore.getDouble(PREF_PAGE_FORMAT_SIZE_H, 0));
          pageFormat.setPaper(paper);
        }
      }
      catch (Exception e) {
        showExceptionMessage("Error", e.getMessage());
      }
    }
    catch (AccessControlException ace) {
      ace.printStackTrace();
    }

    enableEvents(VDU_EVENTS);

    // Set up the actions
    initActions();

    // Create the status bar
    statusBar = new StatusBar();

    dataListener = new DataNotificationListener(statusBar);

    // Create our terminal emulation object
    try {
      emulation = createEmulation();
    }
    catch (IOException ioe) {
      throw new SshToolsApplicationException(ioe);
    }

    emulation.addTerminalListener(this);

    // Set a scrollbar for the terminal - doesn't seem to be as simple as this
    scrollBar = new JScrollBar(JScrollBar.VERTICAL);
    emulation.setBufferSize(1000);

    // Create our swing terminal and add it to the main frame
    terminal = new TerminalPanel(emulation) {
      public void processEvent(AWTEvent evt) {
        /** We can't add a MouseWheelListener because it was not available in 1.3, so direct processing of events is necessary */
        if (evt instanceof MouseEvent && evt.getID() == 507) {
          try {
            Method m = evt.getClass().getMethod("getWheelRotation", new Class[] {});
            SshTerminalPanel.this.scrollBar.setValue(SshTerminalPanel.this.
                scrollBar.getValue() +
                (SshTerminalPanel.this.scrollBar.getUnitIncrement() *
                 ( (Integer) m.invoke(evt, new Object[] {})).intValue() *
                 PreferencesStore.getInt(PREF_MOUSE_WHEEL_INCREMENT, 1)));

          }
          catch (Throwable t) {
            //	In theory, this should never happen
          }
        }
        else {
          super.processEvent(evt);
        }
      }
    };
    terminal.requestFocus();
    terminal.setScrollbar(scrollBar);
    terminal.addMouseMotionListener(this);

    //terminal.addMouseWheelListener(this);
    // Center panel with terminal and scrollbar
    JPanel center = new JPanel(new BorderLayout());
    center.setBackground(Color.red);
    center.add(terminal, BorderLayout.CENTER);
    center.add(scrollBar, BorderLayout.EAST);

    // Show the context menu on mouse button 3 (right click)
    terminal.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if ( (evt.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
          getContextMenu().setLabel( (getCurrentConnectionFile() == null)
                                    ? getApplication().getApplicationName()
                                    : getCurrentConnectionFile().getName());
          getContextMenu().show(terminal, evt.getX(), evt.getY());
        }
      }
    });

    //
    //        JPanel top = new JPanel(new BorderLayout());
    //        top.add(getJMenuBar(), BorderLayout.NORTH);
    //        top.add(north, BorderLayout.SOUTH);
    setLayout(new BorderLayout());
    add(center, BorderLayout.CENTER);

    //        add(top, BorderLayout.NORTH);
    // Make sure that the swing terminal has focus
    terminal.requestFocus();
  }

  /*public void mouseWheelMoved(MouseWheelEvent e) {
    scrollBar.setValue(scrollBar.getValue()
                       +
       (scrollBar.getUnitIncrement() * e.getWheelRotation() * PreferencesStore
                        .getInt(PREF_MOUSE_WHEEL_INCREMENT, 1)));
       }*/
  public StatusBar getStatusBar() {
    return statusBar;
  }

  public boolean setAutoHideTools(boolean autoHideTools) {
    if (this.autoHideTools != autoHideTools) {
      setToolBarVisible(!autoHideTools);
      setMenuBarVisible(!autoHideTools);
      setStatusBarVisible(!autoHideTools);
      setScrollBarVisible(!autoHideTools);
      this.autoHideTools = autoHideTools;

      return true;
    }

    return false;
  }

  public boolean isAutoHideTools() {
    return autoHideTools;
  }

  public boolean isScrollBarVisible() {
    return (scrollBar != null) && scrollBar.isVisible();
  }

  public void setScrollBarVisible(boolean visible) {
    if ( (scrollBar != null) && (scrollBar.isVisible() != visible)) {
      scrollBar.setVisible(visible);
    }
  }

  public void mouseMoved(MouseEvent e) {
    if (isAutoHideTools()) {
      if (!isToolsVisible() && (e.getY() < 4)) {
        setToolsVisible(true);
      }
      else if (isToolsVisible() && (e.getY() > 12)) {
        setToolsVisible(false);
      }

      if (!scrollBar.isVisible() && (e.getX() > (getWidth() - 4))) {
        setScrollBarVisible(true);
      }
      else if (scrollBar.isVisible() && (e.getX() < (getWidth() - 12))) {
        setScrollBarVisible(false);
      }
    }
  }

  public void mouseDragged(MouseEvent evt) {
  }

  public void setFullScreenMode(final boolean full) {
    if (fullScreenMode != full) {
      try {
        if (!full) {
          application.convertContainer(getContainer(),
                                       SshTermFrame.class);
        }
        else {
          application.convertContainer(getContainer(),
                                       SshTermFullScreenWindowContainer.class);
        }

        requestFocus();
        fullScreenMode = full;
      }
      catch (SshToolsApplicationException sshte) {
        sshte.printStackTrace();
        log.error(sshte);
      }

      setAvailableActions();
    }
  }

  public ResourceIcon getIcon() {
    return SSHTERM_ICON;
  }

  public boolean isFullScreenMode() {
    return fullScreenMode;
  }

  public void requestFocus() {
    terminal.requestFocus();
  }

  public void registerAction(StandardAction action) {
    super.registerAction(action);
    action.addActionListener(this);
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) {
  }

  public void setAnyHost(boolean anyHost) {
    this.anyHost = anyHost;
  }

  public void setAnyUser(boolean anyUser) {
    this.anyUser = anyUser;
  }

  public void setAnyPort(boolean anyPort) {
    this.anyPort = anyPort;
  }

  public void screenResized(int w, int h) {
    try {
      if (session != null) {
        session.changeTerminalDimensions(emulation);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void setToolsVisible(boolean visible) {
    super.setToolsVisible(visible);
    setScrollBarVisible(visible);
  }

  public void actionPerformed(ActionEvent event) {
    // Get the name of the action command
    String command = event.getActionCommand();

    if (sessionActions.containsKey(event.getActionCommand())) {
      SessionProviderAction action = (SessionProviderAction) sessionActions.get(
          event.getActionCommand());
      SessionProviderFrame frame;
      // Do we have an existing frame?
      for (Iterator it = sessionFrames.iterator();
           it.hasNext(); ) {
        frame = (SessionProviderFrame) it.next();
        if (action.getProvider().getProviderClass().isInstance(frame)) {
          frame.show();
          return;
        }
      }

      try {
        frame = new SessionProviderFrame(getCurrentConnectionProfile(),
                                         ssh,
                                         action.getProvider());
        if (frame.initFrame(getApplication())) {
          frame.show();
          sessionFrames.add(frame);
        }

      }
      catch (Throwable ex) {
        ex.printStackTrace();
      }
    }
    /*if ((shiftAction != null)
        && command.equals(shiftAction.getActionCommand())) {
      //boolean error = false;
        try {
          SessionProviderFrame browserFrame = new SessionProviderFrame(
         getCurrentConnectionProfile(),
                                        ssh,
         SessionProviderFactory.getInstance().getProvider("shift"));
          if (PreferencesStore.preferenceExists(PREF_SHIFT_GEOMETRY)) {
            browserFrame.setBounds(PreferencesStore.getRectangle(
                PREF_SHIFT_GEOMETRY, browserFrame.getBounds()));
          }
          else {
            browserFrame.setLocation(40, 40);
          }
          browserFrame.init(getApplication());
          browserFrame.show();
          browserFrames.add(browserFrame);
        }
        catch (Exception e) {
          showExceptionMessage("Error", e.getMessage());
        }
      }
      /*if (browserFrame != null) {
         //  We need to go back to windowed mode if in full screen mode
         if (!browserFrame.isVisible()) {
           setFullScreenMode(false);
         }
         browserFrame.setVisible(!browserFrame.isVisible());
       }*/

     /* if ( (tunnelingAction != null)
          && command.equals(tunnelingAction.getActionCommand())) {
          try {
            if(tunnelingFrame==null) {
            tunnelingFrame = new SessionProviderFrame(
          getCurrentConnectionProfile(),
                                          ssh,
          SessionProviderFactory.getInstance().getProvider("tunneling"));
            /*if (PreferencesStore.preferenceExists(PREF_SHIFT_GEOMETRY)) {
               tunnelingFrame.setBounds(PreferencesStore.getRectangle(
                   PREF_SHIFT_GEOMETRY, tunnelingFrame.getBounds()));
             }
             else {
               tunnelingFrame.setLocation(40, 40);
             }*/

      /*      }
            tunnelingFrame.init(getApplication());
            tunnelingFrame.show();
          }
          catch (Exception e) {
            showExceptionMessage("Error", e.getMessage());
          }*/

      /*if (portForwardingDialog == null) {
           Window parent = (Window) SwingUtilities.getAncestorOfClass(Window.class,
             this);
         if (parent instanceof JFrame) {
           portForwardingDialog = new JDialog( (JFrame) parent,
                                              "Port Forwarding", false);
         }
         else if (parent instanceof JDialog) {
           portForwardingDialog = new JDialog( (JDialog) parent,
                                              "Port Forwarding", false);
         }
         else {
           portForwardingDialog = new JDialog( (JFrame)null,
                                              "Port Forwarding", false);
         }
         portForwardingDialog.getContentPane().setLayout(new BorderLayout());
         portForwardingDialog.getContentPane().add(portForwardingPane,
                                                   BorderLayout.CENTER);
         portForwardingDialog.pack();
         if (PreferencesStore.preferenceExists(
             PREF_PORT_FORWARDING_GEOMETRY)) {
           portForwardingDialog.setBounds(PreferencesStore
                                          .getRectangle(
               PREF_PORT_FORWARDING_GEOMETRY,
               portForwardingDialog.getBounds()));
         }
         else {
           portForwardingDialog.setLocation(40, 40);
           portForwardingDialog.setSize(new Dimension(260, 420));
         }
         PreferencesStore.restoreTableMetrics(portForwardingPane
                                              .getPortForwardingTable(),
             PREF_PORT_FORWARDING_FORWARDS_TABLE_METRICS,
             new int[] {68, 62, 44, 54, 98, 78});
         PreferencesStore.restoreTableMetrics(portForwardingPane.
                                              getActiveChannelPane()
                                              .getActiveChannelTable(),
             PREF_PORT_FORWARDING_ACTIVE_TABLE_METRICS,
             new int[] {22, 22, 92, 264});
         if (PreferencesStore.preferenceExists(
             PREF_PORT_FORWARDING_DIVIDER_LOCATION)) {
           portForwardingPane.setDividerLocation(PreferencesStore
                                                 .getInt(
               PREF_PORT_FORWARDING_DIVIDER_LOCATION, 100));
         }
         else {
           portForwardingPane.setDividerLocation(0.75d);
         }
       }
       //  We need to go back to windowed mode if in full screen mode
       if (!portForwardingDialog.isVisible()) {
         setFullScreenMode(false);
       }
       portForwardingDialog.setVisible(!portForwardingDialog.isVisible());*/
      //}

      if ( (stopAction != null)
          && command.equals(stopAction.getActionCommand())) {
        stopRecording();
      }

    if ( (recordAction != null)
        && command.equals(recordAction.getActionCommand())) {
      //  We need to go back to windowed mode if in full screen mode
      setFullScreenMode(false);

      // Select the file to record to
      JFileChooser fileDialog = new JFileChooser(System.getProperty(
          "user.home"));
      int ret = fileDialog.showSaveDialog(this);

      if (ret == fileDialog.APPROVE_OPTION) {
        recordingFile = fileDialog.getSelectedFile();

        if (recordingFile.exists()
            && (JOptionPane.showConfirmDialog(this,
                                              "File exists. Are you sure?",
                                              "File exists",
                                              JOptionPane.YES_NO_OPTION,
                                              JOptionPane.WARNING_MESSAGE) ==
                JOptionPane.NO_OPTION)) {
          return;
        }

        try {
          recordingOutputStream = new FileOutputStream(recordingFile);
          statusBar.setStatusText("Recording to "
                                  + recordingFile.getName());
          setAvailableActions();
        }
        catch (IOException ioe) {
          showExceptionMessage("Error",
                               "Could not open file for recording\n\n"
                               + ioe.getMessage());
        }
      }
    }

    if ( (playAction != null)
        && command.equals(playAction.getActionCommand())) {
      //  We need to go back to windowed mode if in full screen mode
      setFullScreenMode(false);

      // Select the file to record to
      JFileChooser fileDialog = new JFileChooser(System.getProperty(
          "user.home"));
      int ret = fileDialog.showOpenDialog(this);

      if (ret == fileDialog.APPROVE_OPTION) {
        File playingFile = fileDialog.getSelectedFile();
        InputStream in = null;

        try {
          statusBar.setStatusText("Playing from "
                                  + playingFile.getName());
          in = new FileInputStream(playingFile);

          byte[] b = null;
          int a = 0;

          while (true) {
            a = in.available();

            if (a == -1) {
              break;
            }

            if (a == 0) {
              a = 1;
            }

            b = new byte[a];
            a = in.read(b);

            if (a == -1) {
              break;
            }

            //emulation.write(b);
            emulation.getOutputStream().write(b);
          }

          statusBar.setStatusText("Finished playing "
                                  + playingFile.getName());
          setAvailableActions();
        }
        catch (IOException ioe) {
          statusBar.setStatusText("Error playing "
                                  + playingFile.getName());
          showExceptionMessage("Error",
                               "Could not open file for playback\n\n"
                               + ioe.getMessage());
        }
        finally {
          if (in != null) {
            try {
              in.close();
            }
            catch (IOException ioe) {
              log.error(ioe);
            }
          }
        }
      }
    }

    if ( (newAction != null) && command.equals(newAction.getActionCommand())) {
      setFullScreenMode(false);

      // Clear the screen
      emulation.clearScreen();
      emulation.setCursorPosition(0, 0);

      // Force a repaint
      terminal.refresh();

      SshToolsConnectionProfile p = SshToolsConnectionPanel
          .showConnectionDialog(this, getCurrentConnectionProfile(),
                                getAdditionalConnectionTabs());

      if (p != null) {
        setContainerTitle(null);
        setNeedSave(true);
        connect(p, true);
      }
      else {
        log.info("New connection cancelled");
      }

      return;
    }

    if ( (closeAction != null)
        && command.equals(closeAction.getActionCommand())) {
      if (ssh != null) {
        if (performVerifiedDisconnect(true)) {
          ssh.disconnect();
        }
      }
    }

    if ( (openAction != null)
        && command.equals(openAction.getActionCommand())) {
      open();
    }

    if ( (saveAction != null)
        && command.equals(saveAction.getActionCommand())) {
      // Make sure we dont have a null connection object
      saveConnection(false);
    }

    if ( (saveAsAction != null)
        && command.equals(saveAsAction.getActionCommand())) {
      saveConnection(true);
    }

    //  Keygen
    if ( (keygenAction != null)
        && event.getActionCommand().equals(keygenAction
                                           .getActionCommand())) {
      if (keygenFrame == null) {
        keygenFrame = new com.sshtools.common.keygen.Main();
        keygenFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        keygenFrame.pack();
        UIUtil.positionComponent(SwingConstants.CENTER, keygenFrame);
      }

      if (!keygenFrame.isVisible()) {
        setFullScreenMode(false);
      }

      keygenFrame.setVisible(!keygenFrame.isVisible());
    }
  }

  private boolean performVerifiedDisconnect(boolean force) {
    // Lets examine the profile to see if we need to close the connection
    SshToolsConnectionProfile profile = getCurrentConnectionProfile();

    if (profile.disconnectOnSessionClose()) {
      // Yes we should, lets ask the user about any forwarding channels
      if (ssh.getForwardingClient().hasActiveConfigurations()) {
        return (JOptionPane.showConfirmDialog(SshTerminalPanel.this,
            "There are currently active forwarding channels!\n\n"
            +
            "The profile is configured to disconnect when the session is closed. Closing\n"
            + "the connection will terminate these forwarding channels with unexpected results.\n\n"
            + "Do you want to disconnect now?", "Auto disconnect",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
      }
      else {
        return true;
      }
    }

    return force;
  }

  public void open() {
    setFullScreenMode(false);
    super.open();
  }

  public void open(File f) {
    if (log.isDebugEnabled()) {
      log.debug("Opening connection file " + f);
    }

    // Make sure a connection is not already open
    if (isConnected()) {
      Option optNew = new Option("New", "New create a new terminal", 'n');
      Option optClose = new Option("Close", "Close current connection",
                                   'l');
      Option optCancel = new Option("Cancel",
                                    "Cancel the opening of this connection",
                                    'c');
      OptionsDialog dialog = OptionsDialog.createOptionDialog(this,
          new Option[] {optNew, optClose, optCancel}
          ,
          "You already have a connection open. Select\n"
          + "Close to close the current connection, New\n"
          + "to create a new terminal or Cancel to abort.",
          "Existing connection", optNew, null,
          UIManager.getIcon("OptionPane.warningIcon"));
      UIUtil.positionComponent(SwingConstants.CENTER, dialog);
      dialog.setVisible(true);

      Option opt = dialog.getSelectedOption();

      if ( (opt == null) || (opt == optCancel)) {
        return;
      }
      else if (opt == optNew) {
        try {
          SshToolsApplicationContainer c = (SshToolsApplicationContainer)
              application
              .newContainer();
          SshTerminalPanel term = (SshTerminalPanel) c
              .getApplicationPanel();
          term.open(f);

          return;
        }
        catch (SshToolsApplicationException stae) {
          log.error(stae);
        }
      }
      else {
        closeConnection(true);
      }
    }

    // Save to MRU
    if (getApplication()instanceof SshTerm
        && ( ( (SshTerm) getApplication()).getMRUModel() != null)) {
      ( (SshTerm) getApplication()).getMRUModel().add(f);
    }

    // Make sure its not invalid
    if (f != null) {
      // Create a new connection properties object
      SshToolsConnectionProfile profile = new SshToolsConnectionProfile();

      try {
        // Open the file
        profile.open(f.getAbsolutePath());
        setNeedSave(false);
        setCurrentConnectionFile(f);
        setContainerTitle(f);

        // Connect with the new details.
        connect(profile, false);
      }
      catch (InvalidProfileFileException fnfe) {
        showExceptionMessage("Open Connection", fnfe.getMessage());
      }
      catch (SshException e) {
        e.printStackTrace();
        showExceptionMessage("Open Connection",
                             "An unexpected error occured!");
      }
    }
    else {
      showExceptionMessage("Open Connection", "Invalid file specified");
    }
  }

  public void stopRecording() {
    try {
      recordingOutputStream.flush();
      recordingOutputStream.close();
      statusBar.setStatusText("Stopped recording to "
                              + recordingFile.getName());
    }
    catch (IOException ioe) {
    }

    recordingOutputStream = null;
    setAvailableActions();
  }

  public boolean isConnected() {
    return (session != null) && super.isConnected();
  }

  public void closeConnection(boolean doDisconnect) {
    /*if (portForwardingPane != null) {
      portForwardingPane.setClient(null);
         }*/

    if (isConnected()) {
      super.closeConnection(doDisconnect);
    }

    if (doDisconnect) {
      // We should disconnect the session
      try {
        if (session != null) {
          session.close();
        }

        if ( (ssh != null) && ssh.isConnected()) {
          ssh.disconnect();
        }
      }
      catch (Exception se) {
        se.printStackTrace();
        showExceptionMessage("Disconnect",
                             "An unexpected error occured!\n\n" + se.getMessage());
      }
    }

    //  Stop recording
    if (recordingOutputStream != null) {
      stopRecording();
    }

    // Reset the terminal emulation
    emulation.reset();

    // Reset the menu items
    setAvailableActions();
    statusBar.setStatusText("Disconnected");
    statusBar.setHost("");
    statusBar.setUser("");
    statusBar.setRemoteId("");
    statusBar.setConnected(false);

    // Null the session and current properties
    ssh = null;
    session = null;

    SessionProviderFrame browserFrame;
    for (Iterator it = sessionFrames.iterator();
         it.hasNext(); ) {
      try {
        browserFrame = (SessionProviderFrame) it.next();
        browserFrame.exit();
        browserFrame.dispose();
      }
      catch (Throwable ex) {
      }
    }

    sessionFrames.clear();

    /* if(tunnelingFrame!=null) {
       tunnelingFrame.exit();
       tunnelingFrame.dispose();
       tunnelingFrame = null;
     }*/
    /*if (browserFrame != null) {
      ( (SessionProviderFrame) browserFrame).exit();
      browserFrame.dispose();
      browserFrame = null;
         }*/

    //
    setCurrentConnectionProfile(null);
    setNeedSave(false);
    setCurrentConnectionFile(null);
    setContainerTitle(getCurrentConnectionFile());
  }

  public File saveConnection(boolean saveAs, File file,
                             SshToolsConnectionProfile profile) {
    if ( (profile != null) && ( (file == null) || saveAs)) {
      setFullScreenMode(false);
    }

    return super.saveConnection(saveAs, file, profile);
  }

  private void saveConnection(boolean saveAs) {

    //portForwardingPane.applyForwardingToProfile(getCurrentConnectionProfile());
    /*if(tunnelingFrame!=null)
      tunnelingFrame.applyProfileChanges(getCurrentConnectionProfile());*/

    File f = saveConnection(saveAs, getCurrentConnectionFile(),
                            getCurrentConnectionProfile());

    if (f != null) {
      ( (SshTerm) getApplication()).getMRUModel().add(f);
      setCurrentConnectionFile(f);
      setContainerTitle(f);
    }
  }

  public SshToolsConnectionTab[] getAdditionalConnectionTabs() {
    return additionalTabs;
  }

  public void editConnection() {
    setFullScreenMode(false);
    super.editConnection();
  }

  public boolean canClose() {
    if (session != null) {
      setFullScreenMode(false);

      if (JOptionPane.showConfirmDialog(this,
                                        "Close the current session and exit?",
                                        "Exit Application",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE) ==
          JOptionPane.NO_OPTION) {
        return false;
      }

      /*if (browserFrame != null) {
        if (! ( (SessionProviderFrame) browserFrame).canExit()) {
          return false;
        }
             }*/
    }

    return true;
  }

  public void printScreen() {
    try {
      PrinterJob job = PrinterJob.getPrinterJob();
      job.setPrintable(terminal, pageFormat);

      if (job.printDialog()) {
        setCursor(Cursor.getPredefinedCursor(3));
        job.print();
        setCursor(Cursor.getPredefinedCursor(0));
      }
    }
    catch (PrinterException pe) {
      JOptionPane.showMessageDialog(this, pe.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  public void connect(final SshToolsConnectionProfile profile,
                      boolean newProfile) {

    emulation.clearScreen();
    emulation.setCursorPosition(0, 0);
    terminal.refresh();

    /*if (portForwardingPane != null) {
      portForwardingPane.setClient(null);
         }*/

    setTerminalProperties(profile);
    super.connect(profile, newProfile);
  }

  public boolean postConnection() {
    Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class,
        SshTerminalPanel.this);

    if (w != null) {
      w.toFront();
    }

    terminal.requestFocus();

    return true;
  }

  public void authenticationComplete(boolean newProfile) throws SshException,
      IOException {
    SshToolsConnectionProfile profile = getCurrentConnectionProfile();

    com.sshtools.j2ssh.io.ByteArrayWriter b =
        new com.sshtools.j2ssh.io.ByteArrayWriter();
    b.writeString("127.0.0.1");
    b.writeInt(8085);
    ssh.sendGlobalRequest("tcpip-forward", true, b.toByteArray());

    //portForwardingPane.setClient(ssh);
    //portForwardingPane.setProfile(profile);

    /*if (profile.getApplicationPropertyBoolean(PREF_X11_FORWARDING, false)) {
      String d = profile.getApplicationProperty(
          PREF_X11_FORWARDING_LOCAL_DISPLAY,
          "");
      if (d.equals("")) {
        d = ConfigurationLoader.checkAndGetProperty(
            "sshterm.xForwarding.localDisplay",
            "localhost:0");
      }
      XDisplay display = new XDisplay(d);
      portForwardingPane.enableX11Forwarding(display);
         }*/

    // We are now connected
    statusBar.setStatusText("Connected");
    statusBar.setConnected(true);

    // Set the connection status
    setAvailableActions();

    // Make sure the terminal has focus
    terminal.requestFocus();
    ssh.addEventHandler(new SshEventAdapter() {

      public void onDisconnect(TransportProtocol transport) {
        log.info("The connection has disconnected cleaning up");
        closeConnection(false);
      }
    });


    //If the eol setting is EOL_DEFAULT, then use the
    // value guessed by j2ssh
    if (eol == TerminalEmulation.EOL_DEFAULT) {
      if (ssh.getRemoteEOL() == TransportProtocolCommon.EOL_CRLF) {
        emulation.setEOL(TerminalEmulation.EOL_CR_LF);
      }
      else {
        emulation.setEOL(TerminalEmulation.EOL_CR);
      }
    }

    if (profile.getOnceAuthenticatedCommand() !=
        SshToolsConnectionProfile.DO_NOTHING) {
      if (profile.getOnceAuthenticatedCommand() ==
          SshToolsConnectionProfile.EXECUTE_COMMANDS) {
        BufferedReader reader = new BufferedReader(new StringReader(profile
            .getCommandsToExecute() + "\n"));

        String cmd;

        while ( (cmd = reader.readLine()) != null) {
          if (cmd.trim().length() > 0) {
            log.info("Executing " + cmd);
            session = createNewSession(false);

            if (session.executeCommand(cmd)) {
              session.bindInputStream(/*new InputStreamMonitor(*/
                  emulation.getTerminalInputStream()/*)*/);
              session.bindOutputStream(/*new OutputStreamMonitor(*/
                  emulation.getTerminalOutputStream()/*)*/);

              try {
                session.getState().waitForState(ChannelState.CHANNEL_CLOSED);
              }
              catch (InterruptedException ex) {
                JOptionPane.showMessageDialog(this,
                                              "The command was interrupted!",
                                              "Interrupted Exception",
                                              JOptionPane.OK_OPTION);
              }
            }
          }
        }

        if (profile.disconnectOnSessionClose()) {
          ssh.disconnect();
        }
      }
      else {
        // Start the users shell
        session = createNewSession(true);

        if (session.startShell()) {
          session.bindInputStream(/*new InputStreamMonitor(*/
              emulation.getTerminalInputStream()/*)*/);
          session.bindOutputStream(/*new OutputStreamMonitor(*/
              emulation.getTerminalOutputStream()/*)*/);
        }
      }
    }
  }

  private SessionChannelClient createNewSession(boolean addEventListener) throws
      IOException {
    SessionChannelClient session = ssh.openSessionChannel();
    session.addEventListener(dataListener);
    if (addEventListener) {
      session.addEventListener(new ChannelEventAdapter() {
        public void onChannelClose(Channel channel) {
          if (ssh != null) {
            if (performVerifiedDisconnect(false)) {
              ssh.disconnect();
            }
          }
        }
      });
    }

    if (getCurrentConnectionProfile().getApplicationPropertyBoolean(
        PREF_X11_FORWARDING,
        false)) {
      int displayNumber = getCurrentConnectionProfile()
          .getApplicationPropertyInt(SshTerminalPanel.
                                     PREF_X11_FORWARDING_LOCAL_DISPLAY,
                                     0);
      String cookie = X11Util.getCookie(displayNumber);

      if (!session.requestX11Forwarding(displayNumber, cookie)) {
        JOptionPane.showMessageDialog(SshTerminalPanel.this,
            "The server refused to start start X11 forwarding!",
            "Start X11 Forwarding", JOptionPane.OK_OPTION);
      }
    }

    if (getCurrentConnectionProfile().getAllowAgentForwarding()) {
      if (!session.requestAgentForwarding()) {
        JOptionPane.showMessageDialog(SshTerminalPanel.this,
            "The server failed to open an agent listener",
            "Allow Agent Forwarding", JOptionPane.OK_OPTION,
            (Icon) UIManager.get("OptionPane.informationIcon"));
      }
    }

    // Request a pseudo terminal
    if (getCurrentConnectionProfile().requiresPseudoTerminal()) {
      if (!session.requestPseudoTerminal(emulation)) {
        JOptionPane.showMessageDialog(SshTerminalPanel.this,
            "The server refused to allocate a pseudo terminal!",
            "Request Pseudo Terminal", JOptionPane.OK_OPTION);
      }
    }

    return session;
  }

  private void setTerminalProperties(SshToolsConnectionProfile profile) {
    //  EOL
    eol = profile.getApplicationPropertyInt(PROFILE_PROPERTY_EOL,
                                            TerminalEmulation.EOL_DEFAULT);

    if (eol != TerminalEmulation.EOL_DEFAULT) {
      emulation.setEOL(eol);
    }

    // Set the terminal type
    emulation.setTerminalType(profile.getApplicationProperty(
        PROFILE_PROPERTY_TERMINAL, TerminalEmulation.VT100));

    // Set the colors
    terminal.setBackground(profile.getApplicationPropertyColor(
        PROFILE_PROPERTY_BACKGROUND_COLOR, Color.black));
    terminal.setForeground(profile.getApplicationPropertyColor(
        PROFILE_PROPERTY_FOREGROUND_COLOR, Color.white));

    //  Color printing
    terminal.setColorPrinting(profile.getApplicationPropertyBoolean(
        PROFILE_PROPERTY_COLOR_PRINTING, false));

    // Set the antialias
    terminal.setAntialias(profile.getApplicationPropertyBoolean(
        PROFILE_PROPERTY_ANTIALIAS, false));

    // Set the resize strategy, fonts and sizes
    int rsz = profile.getApplicationPropertyInt(
        PROFILE_PROPERTY_RESIZE_STRATEGY,
        TerminalPanel.RESIZE_SCREEN);
    terminal.setResizeStrategy(rsz);

    if (rsz == TerminalPanel.RESIZE_SCREEN) {
      terminal.setFont(terminal.getFont().deriveFont( (float) (profile
          .getApplicationPropertyInt(PROFILE_PROPERTY_FONT_SIZE, 12))));
    }
  }

  private void initActions() {
    //  Create the action menu groups
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("File",
        "File", 'f', 0));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Edit",
        "Edit", 'e', 10));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("View",
        "View", 'v', 20));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Help",
        "Help", 'h', 90));
    actions = new Vector();

    // MRU
    if (getApplication().getMRUModel() != null) {
      registerAction(mruAction = new MRUActionImpl(
          getApplication().getMRUModel()));
    }

    //
    connectionPropertiesAction = new ConnectionPropertiesActionImpl();
    registerAction(connectionPropertiesAction);
    newAction = new NewAction();
    registerAction(newAction);

    //  Only allow opening of files if allowed by the security manager
    try {
      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new FilePermission(
            "<<ALL FILES>>", "read"));
      }

      openAction = new OpenAction();
      registerAction(openAction);
      playAction = new PlayAction();
      registerAction(playAction);
    }
    catch (AccessControlException ace) {
      log.warn("File reading actions are not available");
    }

    //  Only allow saving of files if allowed by the security manager
    try {
      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new FilePermission(
            "<<ALL FILES>>", "write"));
      }

      saveAction = new SaveAction();
      registerAction(saveAction);
      saveAsAction = new SaveAsAction();
      registerAction(saveAsAction);
      recordAction = new RecordAction();
      registerAction(recordAction);
      stopAction = new StopAction();
      registerAction(stopAction);
    }
    catch (AccessControlException ace) {
      log.warn("File write actions are not available");
    }

    //  Only allow editing of connection file if read / write is allowed
    try {
      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new FilePermission(
            "<<ALL FILES>>", "write"));
      }

      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new FilePermission(
            "<<ALL FILES>>", "read"));
      }

      editAction = new EditActionImpl();
      registerAction(editAction);
    }
    catch (AccessControlException ace) {
      log.warn("Read / write actions are not available");
    }

    //  Checking if printing is allowed
    if (pageFormat != null) {
      try {
        if (System.getSecurityManager() != null) {
          AccessController.checkPermission(new RuntimePermission(
              "queuePrintJob"));
        }

        printAction = new PrintActionImpl();
        registerAction(printAction);
        printPreviewAction = new PrintPreviewActionImpl();
        registerAction(printPreviewAction);
      }
      catch (AccessControlException ace) {
        log.warn("Print actions are not available");
      }
    }

    //  Always allow refreshing of terminal
    refreshAction = new RefreshActionImpl();
    registerAction(refreshAction);

    //  Always allow closing of connect
    closeAction = new CloseAction();
    registerAction(closeAction);

    //  Copy / Paste
    try {
      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new AWTPermission(
            "accessClipboard"));
      }

      copyAction = new CopyActionImpl();
      registerAction(copyAction);
      pasteAction = new PasteActionImpl();
      registerAction(pasteAction);
    }
    catch (AccessControlException ace) {
    }

    //  Theres no point in having the keygen action if we can't write to local file
    try {
      if (System.getSecurityManager() != null) {
        AccessController.checkPermission(new FilePermission(
            "<<ALL FILES>>", "write"));
      }

      keygenAction = new KeygenAction();
      registerAction(keygenAction);
    }
    catch (AccessControlException ace) {
      log.warn("Keygen actions is not available");
    }

    //  Clear action
    clearAction = new ClearActionImpl();
    registerAction(clearAction);

    // Secure Tunneling
    /*try {
      SessionProvider provider = SessionProviderFactory.getInstance().getProvider("tunneling");
      if(provider!=null) {
        tunnelingAction = (StandardAction)new SessionProviderAction(
            provider);
        registerAction(tunnelingAction);
      }
         }
         catch (Throwable t) {
      log.info(
          "Secure Tunneling not available on CLASSPATH");
         }
         //  ShiFT action
         try {
      SessionProvider provider = SessionProviderFactory.getInstance().getProvider("shift");
      if(provider!=null) {
        shiftAction = (StandardAction)new SessionProviderAction(
            provider);
        registerAction(shiftAction);
      }
         }
         catch (Throwable t) {
      log.info(
          "ShiFT not available on CLASSPATH");
         }*/

    java.util.List providers = SessionProviderFactory.getInstance().
        getSessionProviders();
    SessionProvider provider;
    SessionProviderAction action;
    for (Iterator it = providers.iterator();
         it.hasNext(); ) {
      provider = (SessionProvider) it.next();
      action = new SessionProviderAction(provider);
      sessionActions.put(action.getActionCommand(), action);
      registerAction(action);
    }
  }

  public void setDialogActionsEnabled(boolean enable) {
    dialogActionsEnabled = enable;
    setAvailableActions();
  }

  public void setAvailableActions() {
    boolean connected = isConnected();

    SessionProviderAction action;
    for (Iterator it = sessionActions.values().iterator();
         it.hasNext(); ) {
      action = (SessionProviderAction) it.next();
      action.setEnabled(connected);
    }

    if (newAction != null) {
      newAction.setEnabled(!connected);
    }

    if (openAction != null) {
      openAction.setEnabled(true);
    }

    if (connectionPropertiesAction != null) {
      connectionPropertiesAction.setEnabled(connected);
    }

    if (closeAction != null) {
      closeAction.setEnabled(connected);
    }

    if (saveAction != null) {
      saveAction.setEnabled(connected && isNeedSave());
    }

    if (saveAsAction != null) {
      saveAsAction.setEnabled(connected);
    }

    if (recordAction != null) {
      recordAction.setEnabled(connected
                              && (recordingOutputStream == null));
    }

    if (recordAction != null) {
      playAction.setEnabled(connected);
    }

    if (recordAction != null) {
      stopAction.setEnabled(recordingOutputStream != null);
    }

    if (shiftAction != null) {
      shiftAction.setEnabled(connected);
    }

    if (tunnelingAction != null) {
      tunnelingAction.setEnabled(connected);
    }
  }

  private TerminalEmulation createEmulation() throws IOException {
    return new TerminalEmulation(TerminalEmulation.VT320);
  }

  public void showExceptionMessage(String title, String message) {
    setFullScreenMode(false);
    super.showExceptionMessage(title, message);
  }

  public void close() {

    /*if(tunnelingFrame!=null) {
         }*/
    /* if (portForwardingDialog != null) {
      PreferencesStore.putRectangle(PREF_PORT_FORWARDING_GEOMETRY,
                                    portForwardingDialog.getBounds());
      PreferencesStore.putInt(PREF_PORT_FORWARDING_DIVIDER_LOCATION,
                              portForwardingPane.getDividerLocation());
      PreferencesStore.saveTableMetrics(portForwardingPane
                                        .getPortForwardingTable(),
          PREF_PORT_FORWARDING_FORWARDS_TABLE_METRICS);
         PreferencesStore.saveTableMetrics(portForwardingPane.getActiveChannelPane()
                                        .getActiveChannelTable(),
          PREF_PORT_FORWARDING_ACTIVE_TABLE_METRICS);
      portForwardingDialog.dispose();
         }*/

    //  Store the page format
    if (pageFormat != null) {
      PreferencesStore.putDouble(PREF_PAGE_FORMAT_IMAGEABLE_X,
                                 pageFormat.getImageableX());
      PreferencesStore.putDouble(PREF_PAGE_FORMAT_IMAGEABLE_Y,
                                 pageFormat.getImageableY());
      PreferencesStore.putDouble(PREF_PAGE_FORMAT_IMAGEABLE_W,
                                 pageFormat.getImageableWidth());
      PreferencesStore.putDouble(PREF_PAGE_FORMAT_IMAGEABLE_H,
                                 pageFormat.getImageableHeight());
      PreferencesStore.putDouble(PREF_PAGE_FORMAT_SIZE_W,
                                 pageFormat.getWidth());
      PreferencesStore.putDouble(PREF_PAGE_FORMAT_SIZE_H,
                                 pageFormat.getHeight());
      PreferencesStore.putInt(PREF_PAGE_FORMAT_ORIENTATION,
                              pageFormat.getOrientation());
    }

    if (mruAction != null) {
      mruAction.cleanUp();
    }

    if ( (ssh != null) && ssh.isConnected()) {
      ssh.disconnect();
    }
  }

  /*class InputStreamMonitor
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

      if (recordingOutputStream != null) {
        recordingOutputStream.write(i);
      }

      return i;
    }

    public int read(byte[] b) throws IOException {
      int i = in.read(b);
      dataRead(i);

      if (recordingOutputStream != null) {
        recordingOutputStream.write(b);
      }

      return i;
    }

    public int read(byte[] b, int off, int len) throws IOException {
      int i = in.read(b, off, len);
      dataRead(i);

      if (recordingOutputStream != null) {
        recordingOutputStream.write(b, off, len);
      }

      return i;
    }

    void dataRead(int i) {
      if (!timer.isRunning()) {
        statusBar.setReceiving(true);
        timer.start();
      }
    }
  }*/

  /*class OutputStreamMonitor
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
  }*/

  //  Concrete actions
  class PrintPreviewActionImpl
      extends PrintPreviewAction {
    public void actionPerformed(ActionEvent evt) {
      PrintPreview pv = new PrintPreview(terminal, pageFormat);
      final Option print = new Option("Print", "Print the screen", 'p');
      final Option close = new Option("Close", "Close this dialog", 'c');
      OptionsDialog od = OptionsDialog.createOptionDialog(SshTerminalPanel.this,
          new Option[] {print, close}
          , pv, "Print Preview", close,
          null, null);
      od.pack();
      UIUtil.positionComponent(SwingConstants.CENTER, od);
      od.setVisible(true);

      if (od.getSelectedOption() == print) {
        printScreen();
      }
    }
  }

  class MRUActionImpl
      extends MRUAction {
    public MRUActionImpl(MRUListModel model) {
      super(model);
    }

    public void cleanUp() {
      ( (MRUMenu) getValue(MenuAction.MENU)).cleanUp();
    }

    public void actionPerformed(ActionEvent evt) {
      open(new File(evt.getActionCommand()));
    }
  }

  class PrintActionImpl
      extends PrintAction {
    public void actionPerformed(ActionEvent evt) {
      printScreen();
    }
  }

  class EditActionImpl
      extends EditAction {
    public void actionPerformed(ActionEvent evt) {
      editConnection();
    }
  }

  class ConnectionPropertiesActionImpl
      extends ConnectionPropertiesAction {
    public ConnectionPropertiesActionImpl() {
      super();
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(45));
    }

    public void actionPerformed(ActionEvent evt) {
      if (editConnection(getCurrentConnectionProfile())) {
        setTerminalProperties(getCurrentConnectionProfile());
      }
    }
  }

  class CopyActionImpl
      extends CopyAction {
    CopyActionImpl() {
      super();
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(10));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(0));
    }

    public void actionPerformed(ActionEvent evt) {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new
          StringSelection(
          terminal.getSelection()), SshTerminalPanel.this);
    }
  }

  class PasteActionImpl
      extends PasteAction {
    PasteActionImpl() {
      super();
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(10));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(10));
    }

    public void actionPerformed(ActionEvent evt) {
      Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
          .getContents(SshTerminalPanel.this);

      if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        try {
          emulation.getOutputStream().write(t.getTransferData(
              DataFlavor.stringFlavor).toString().getBytes());
        }
        catch (Exception e) {
        }
      }
    }
  }

  class ClearActionImpl
      extends ClearAction {
    ClearActionImpl() {
      super();
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(20));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(0));
    }

    public void actionPerformed(ActionEvent evt) {
      emulation.clearScreen();
      emulation.setCursorPosition(0, 0);
      terminal.refresh();
    }
  }

  class RefreshActionImpl
      extends RefreshAction {
    RefreshActionImpl() {
      super();
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(20));
    }

    public void actionPerformed(ActionEvent evt) {
      emulation.update[0] = true;
      terminal.redraw();
    }
  }
}
