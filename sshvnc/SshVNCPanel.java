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

import java.io.EOFException;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.authentication.KBIRequestHandlerDialog;
import com.sshtools.common.authentication.PasswordAuthenticationDialog;
import com.sshtools.common.authentication.PasswordChange;
import com.sshtools.common.authentication.PublicKeyAuthenticationPrompt;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.mru.MRUAction;
import com.sshtools.common.mru.MRUListModel;
import com.sshtools.common.mru.MRUMenu;
import com.sshtools.common.ui.*;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.authentication.KBIAuthenticationClient;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.PublicKeyAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.forwarding.ForwardingChannel;
import com.sshtools.j2ssh.forwarding.ForwardingClient;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;
import com.sshtools.j2ssh.forwarding.ForwardingConfigurationListener;
import com.sshtools.j2ssh.forwarding.ForwardingIOChannel;
import com.sshtools.j2ssh.forwarding.XDisplay;
import com.sshtools.j2ssh.io.IOStreamConnector;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.sshterm.emulation.TerminalEmulation;
import com.sshtools.sshterm.emulation.TerminalPanel;

public class SshVNCPanel

    extends SshToolsApplicationClientPanel

    implements ActionListener {

  public final static String PROFILE_PROPERTY_VNC_HOST = "VNC_HOST";

  public final static String PROFILE_PROPERTY_VNC_DISPLAY = "VNC_DISPLAY";

  public final static String PROFILE_PRE_VNC_COMMAND = "PRE_VNC_COMMAND";

  public final static String PROFILE_POST_VNC_COMMAND = "POST_VNC_COMMAND";

  public final static String PROFILE_PROPERTY_PREFERRED_ENCODING =

      "PREFERRED_ENCODING";

  public final static String PROFILE_PROPERTY_USE_COPY_RECT = "USE_COPY_RECT";

  public final static String PROFILE_PROPERTY_COMPRESS_LEVEL =

      "COMPRESS_LEVEL";

  public final static String PROFILE_PROPERTY_JPEG_QUALITY = "JPEG_QUALITY";

  public final static String PROFILE_PROPERTY_CURSOR_UPDATES =

      "CURSOR_UPDATES";

  public final static String PROFILE_PROPERTY_EIGHT_BIT_COLORS =

      "EIGHT_BIT_COLORS";

  public final static String PROFILE_PROPERTY_REVERSE_MOUSE_BUTTONS_2_AND_3 =

      "REVERSE_MOUSE_BUTTONS_2_AND_3";

  public final static String PROFILE_PROPERTY_SHARE_DESKTOP = "SHARE_DESKTOP";

  public final static String PROFILE_PROPERTY_VIEW_ONLY = "VIEW_ONLY";

  public final static String PROFILE_PROPERTY_ENCRYPTED_VNC_PASSWORD =

      "ENCRYPTED_VNC_PASSWORD";

  public final static String PROFILE_PROPERTY_SSH_PASSWORD =

      "SSH_PASSWORD";

  public final static String PROFILE_PROPERTY_SCREEN_SIZE_POLICY =
      "VNC_SCREEN_SIZE";

  public final static String PROFILE_PROPERTY_VNC_SERVER_OS =
      "VNC_SERVER_OS";

  // Screen size properties
  public final static int PROFILE_SCREEN_NO_CHANGE = 0;
  public final static int PROFILE_SCREEN_REMOTE_DESKTOP = 1;

  // Server operating system properties
  public final static int PROFILE_VNC_SERVER_OS_WINDOWSMAC = 0;
  public final static int PROFILE_VNC_SERVER_OS_LINUX = 1;

  /*public final static int PROFILE_SCREEN_640_480 = 0;
     public final static int PROFILE_SCREEN_800_600 = 1;
     public final static int PROFILE_SCREEN_1024_768 = 2;*/

  private TerminalPanel terminal;
  protected TerminalEmulation emulation;

  private Vector sessionFrames = new Vector();
  private HashMap sessionActions = new HashMap();

  // Logger

  protected static Log log = LogFactory.getLog(SshVNC.class);

  //  Private instance variables

  private SshVNCViewer vnc;

  private StandardAction newAction;

  private StandardAction closeAction;

  private StandardAction refreshAction;

  private StandardAction clipboardAction;

  private StandardAction ctrlAltDelAction;

  private StandardAction recordAction;

  private StandardAction stopAction;

  private Thread rfbThread;

  private JFileChooser recordingFileChooser;

  private VNCTab vncTab;

  private StatusBar statusBar;

  private javax.swing.Timer sendTimer, receiveTimer;

  private ForwardingClient.ClientForwardingListener forwardingConfig;

  private SshToolsConnectionTab[] additionalTabs;

  private OpenAction openAction;

  private EditAction editAction;

  private SaveAction saveAction;

  private SaveAsAction saveAsAction;

  private MRUActionImpl mruAction;

  private ConnectionPropertiesAction connectionPropertiesAction;

  private boolean closing = false;

  ForwardingIOChannel channel;

  // Full screen mode

  private boolean fullScreenMode;

  public static final ResourceIcon SSHVNC_ICON = new ResourceIcon(SshVNCPanel.class,

      "sshvncframeicon.gif");

  //  Private static instance variables

  private static boolean debug = true;

  public void init(SshToolsApplication application) throws

      SshToolsApplicationException {

    super.init(application);

    setLayout(new BorderLayout());

    sendTimer = new javax.swing.Timer(500, this);

    sendTimer.setRepeats(false);

    receiveTimer = new javax.swing.Timer(500, this);

    receiveTimer.setRepeats(false);

    statusBar = new StatusBar();

    statusBar.setUser("");

    statusBar.setHost("");

    statusBar.setStatusText("Disconnected");

    statusBar.setConnected(false);

    setLayout(new BorderLayout());

    // Create our terminal emulation object
    try {
      emulation = createEmulation();
    }
    catch (IOException ioe) {
      throw new SshToolsApplicationException(ioe);
    }

    // Set a scrollbar for the terminal - doesn't seem to be as simple as this

    emulation.setBufferSize(1000);

    // Create our swing terminal and add it to the main frame
    terminal = new TerminalPanel(emulation);

    terminal.requestFocus();

    //

    try {

      vnc = new SshVNCViewer();

      //  Additional connection tabs

      additionalTabs = new SshToolsConnectionTab[] {
          new VNCTab( /*vnc*/),
          new VNCAdvancedTab( /*vnc*/)};

      add(vnc, BorderLayout.CENTER);

      initActions();

    }

    catch (Throwable t) {

      StringWriter sw = new StringWriter();

      t.printStackTrace(new PrintWriter(sw));

      IconWrapperPanel p =

          new IconWrapperPanel(

          UIManager.getIcon("OptionPane.errorIcon"),

          new ErrorTextBox(

          ( (t.getMessage() == null)

           ? "<no message supplied>"

           : t.getMessage())

          + (debug

             ? ("\n\n" + sw.getBuffer().toString())

             : "")));

      p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      add(p, BorderLayout.CENTER);

      throw new SshToolsApplicationException(

          "Failed to start SshVNC. ",

          t);

    }

  }

  public ResourceIcon getIcon() {

    return SSHVNC_ICON;

  }

  public boolean postConnection() {

    return true;

  }

  public SshToolsConnectionTab[] getAdditionalConnectionTabs() {

    return additionalTabs;

  }

  public void actionPerformed(ActionEvent evt) {

    if (evt.getSource() == receiveTimer) {

      statusBar.setReceiving(false);

    }

    else if (evt.getSource() == sendTimer) {

      statusBar.setSending(false);

    }

    if (sessionActions.containsKey(evt.getActionCommand())) {
      SessionProviderAction action = (SessionProviderAction) sessionActions.get(
          evt.getActionCommand());
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
      }
    }

  }

  public void setFrameResizeable(boolean frameResizeable) {

    vnc.setFrameResizeable(frameResizeable);

  }


  public StatusBar getStatusBar() {

    return statusBar;

  }

  public JToolBar getToolBar() {

    return toolBar;

  }

  public void initVNC(

      InputStream in,

      OutputStream out,

      SshVNCOptions options) {

    log.info("Initialising VNC");

    try {

      vnc.init(in, out, this, options);

    }

    catch (EOFException e) {

      if (isConnected() && !closing) {
        showErrorMessage(this, "The server closed the connection.", "Error", null);
      }
    }
    catch (Exception e) {
      if (isConnected() && !closing) {
        showErrorMessage(this, "Error", e);
      }
    }
    finally {
      // DO NOT CLOSE the connection since other session providers may
      // be open - closeConnection(true);
    }
  }

  public void closeVNC() {
    vnc.closeConnection();
    setAvailableActions();
  }

  public synchronized void closeConnection(boolean doDisconnect) {

    statusBar.setStatusText("Closing VNC");

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

    if (ssh != null && ssh.isConnected()) {
      super.closeConnection(doDisconnect);

      if (doDisconnect) {
        closeVNC();
      }

      try {
        String command = getCurrentConnectionProfile().getApplicationProperty(
            PROFILE_POST_VNC_COMMAND, null);

        if (command != null && command.trim().length() > 0) {

          statusBar.setStatusText("Executing command: " + command);

          remove(vnc);
          add(terminal, BorderLayout.CENTER);
          emulation.reset();
          emulation.clearScreen();
          emulation.setCursorPosition(0, 0);
          terminal.refresh();
          log.debug("Executing post VNC command " + command);
          SessionChannelClient session = ssh.openSessionChannel();
          session.requestPseudoTerminal("vt100", 80, 24, 0, 0, "");
          if (session.executeCommand(command)) {
            session.bindInputStream(
                emulation.getTerminalInputStream());
            session.bindOutputStream(
                emulation.getTerminalOutputStream());
          }

          try {
            session.getState().waitForState(ChannelState.CHANNEL_CLOSED);

          }
          catch (InterruptedException ex) {
          }
          finally {
            remove(terminal);
            add(vnc, BorderLayout.CENTER);
          }
        }
      }
      catch (Exception e) {

      }

      try {
        if (channel != null) {
          channel.close();
        }
      }
      catch (IOException ex) {
      }

      statusBar.setUser("");
      statusBar.setHost("");
      statusBar.setStatusText("Disconnected");
      statusBar.setConnected(false);
      // Reset the menu items
      setAvailableActions();
      // Null the session and current properties
      ssh = null;
      forwardingConfig = null;
      //
      //setCurrentConnectionProfile(null);
      setNeedSave(false);
      //setCurrentConnectionFile(null);
      setContainerTitle(getCurrentConnectionFile());
      setAvailableActions();
    }
  }

  protected int showAuthenticationPrompt(SshAuthenticationClient instance) throws

      IOException {

    // Get password from profile, if any ppp

    String profPw = getCurrentConnectionProfile().getApplicationProperty(
        SshVNCPanel.PROFILE_PROPERTY_SSH_PASSWORD, "");

    if (!profPw.equals("")) {

      // SSH_PASSWORD is hexascii-encoded. Decode.

      ( (PasswordAuthenticationClient) instance).setPassword(HexASCII.ToString(
          profPw));

    }

    instance.setUsername(getCurrentConnectionProfile().getUsername());

    if (instance instanceof PasswordAuthenticationClient) {

      PasswordAuthenticationDialog dialog =

          new PasswordAuthenticationDialog(

          (Frame) SwingUtilities.getAncestorOfClass(

          Frame.class, SshVNCPanel.this));

      instance.setAuthenticationPrompt(dialog);

      ( (PasswordAuthenticationClient) instance).setPasswordChangePrompt(

          PasswordChange.getInstance());

      PasswordChange.getInstance().setParentComponent(

          SshVNCPanel.this);

    }

    else if (instance instanceof PublicKeyAuthenticationClient) {

      PublicKeyAuthenticationPrompt prompt = new PublicKeyAuthenticationPrompt(

          SshVNCPanel.this);

      instance.setAuthenticationPrompt(prompt);

    }

    else if (instance instanceof KBIAuthenticationClient) {

      KBIAuthenticationClient kbi = new KBIAuthenticationClient();

      ( (KBIAuthenticationClient) instance).setKBIRequestHandler(

          new KBIRequestHandlerDialog(

          (Frame) SwingUtilities.getAncestorOfClass(

          Frame.class, SshVNCPanel.this)));

    }

    return ssh.authenticate(instance);

  }

  public void authenticationComplete(boolean newProfile) throws SshException,

      IOException {

    statusBar.setStatusText("User authenticated");

    int localPort = 0;

    String host = getCurrentConnectionProfile()
        .getApplicationProperty(
        PROFILE_PROPERTY_VNC_HOST,
        "localhost");
    String port = getCurrentConnectionProfile()
        .getApplicationProperty(
        PROFILE_PROPERTY_VNC_DISPLAY,
        "5900");

    final XDisplay display = new XDisplay(host + ":" + port, 5900);

    final String addr = "0.0.0.0";

    String command = getCurrentConnectionProfile().getApplicationProperty(

        PROFILE_PRE_VNC_COMMAND, null);

    if (command != null && command.trim().length() > 0) {

      statusBar.setStatusText("Executing command: " + command);
      remove(vnc);
      add(terminal, BorderLayout.CENTER);

      emulation.reset();
      emulation.clearScreen();
      emulation.setCursorPosition(0, 0);
      terminal.refresh();

      log.debug("Executing pre VNC command" + command);

      SessionChannelClient session = ssh.openSessionChannel();

      session.requestPseudoTerminal("vt100", 80, 24, 0, 0, "");

      if (session.executeCommand(command)) {

        session.bindInputStream(
            emulation.getTerminalInputStream());
        session.bindOutputStream(
            emulation.getTerminalOutputStream());
      }

      try {

        session.getState().waitForState(ChannelState.CHANNEL_CLOSED);

      }

      catch (InterruptedException ex) {

      }
      finally {

        remove(terminal);
        add(vnc, BorderLayout.CENTER);

      }

    }

    statusBar.setStatusText("Setting up VNC forwarding");

    if (log.isDebugEnabled()) {

      log.debug("Setting up forwarding on " + addr

                + " (" + localPort + ") to " + display.getHost()

                + ":" + display.getPort());

    }

    final SshVNCOptions options =

        new SshVNCOptions(getCurrentConnectionProfile());

    statusBar.setStatusText("Initialising VNC");

    ForwardingConfiguration config =

        new ForwardingConfiguration(

        "VNC",

        "forwarded-channel",

        0,

        display.getHost(),

        display.getPort());

    channel =

        new ForwardingIOChannel(

        ForwardingIOChannel.LOCAL_FORWARDING_CHANNEL,

        "VNC",

        config.getHostToConnect(),

        config.getPortToConnect(),

        addr,

        display.getPort());

    if (ssh.openChannel(channel)) {

      // The forwarding channel is open so forward to the
      // VNC protocol

      channel.addEventListener(new DataNotificationListener(statusBar));

      if (newProfile) {

        setNeedSave(true);

      }

      new SshThread(new Runnable() {

        public void run() {

          initVNC(

              channel.getInputStream(),

              channel.getOutputStream(),

              options);

        }

      }

      , "VNC", true).start();

    }

    else {

      // We need to close the connection and inform the user

      // that the forwarding failed to start

      try {

        SwingUtilities.invokeAndWait(new Runnable() {

          public void run() {

            JOptionPane.showMessageDialog(

                SshVNCPanel.this,

                "SSHVnc failed to open a forwarding channel to "

                + display.toString(),

                "SSHVnc",

                JOptionPane.OK_OPTION);

          }

        });

      }

      catch (Exception ex) {

        statusBar.setStatusText(

            "Could not connect to local forwarding server");

      }

      finally {

        closeConnection(true);

      }

    }

  }

  private void initActions() {

    //  Create the action menu groups

    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("File", "File",

        'f', 0));

    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Edit", "Edit",

        'e', 10));

    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("View", "View",

        'v', 20));

    vncTab = new VNCTab( /*vnc*/);

    newAction = new NewAction() {

      public void actionPerformed(ActionEvent evt) {

        SshToolsConnectionProfile newProfile = newConnectionProfile(null);

        if (newProfile != null) {

          connect(newProfile, true);

        }

        else {

          log.info("New connection cancelled");

        }

      }

    };

    registerAction(newAction);

    closeAction = new CloseAction() {

      public void actionPerformed(ActionEvent evt) {

        closing = true;
        // Close on a thread to avoid blocking the event queue
        Thread thread = new Thread() {
          public void run() {
            closeConnection(true);
          }
        };

        thread.start();

      }

    };

    registerAction(closeAction);

    refreshAction = new RefreshAction() {

      public void actionPerformed(ActionEvent evt) {

        refresh();

      }

    };

    registerAction(refreshAction);

    ctrlAltDelAction = new CtrlAltDelAction() {

      public void actionPerformed(ActionEvent evt) {

        try {

          vnc.sendCtrlAltDel();

        }

        catch (IOException ioe) {

          closeConnection(true);

          showErrorMessage(SshVNCPanel.this, "Error", ioe);

        }

      }

    };

    registerAction(ctrlAltDelAction);

    clipboardAction = new ClipboardAction() {

      public void actionPerformed(ActionEvent evt) {

        vnc.setClipboardVisible(!vnc.isClipboardVisible());

      }

    };

    registerAction(clipboardAction);

    if (getApplication().getMRUModel() != null) {

      registerAction(

          mruAction = new MRUActionImpl(getApplication().getMRUModel()));

    }

    connectionPropertiesAction = new ConnectionPropertiesAction() {

      public void actionPerformed(ActionEvent evt) {

        editConnection(getCurrentConnectionProfile());

      }

    };

    registerAction(connectionPropertiesAction);

    //  Only allow opening of files if allowed by the security manager

    try {

      if (System.getSecurityManager() != null) {

        AccessController.checkPermission(

            new FilePermission("<<ALL FILES>>", "read"));

      }

      openAction = new OpenAction() {

        public void actionPerformed(ActionEvent evt) {

          open();

        }

      };

      registerAction(openAction);

    }

    catch (AccessControlException ace) {

      ace.printStackTrace();

    }

    //  Only allow saving of files if allowed by the security manager

    try {

      if (System.getSecurityManager() != null) {

        AccessController.checkPermission(

            new FilePermission("<<ALL FILES>>", "write"));

      }

      saveAction = new SaveAction() {

        public void actionPerformed(ActionEvent evt) {

          saveConnection(false, getCurrentConnectionFile(),

                         getCurrentConnectionProfile());

        }

      };

      registerAction(saveAction);

      saveAsAction = new SaveAsAction() {

        public void actionPerformed(ActionEvent evt) {

          saveConnection(true, getCurrentConnectionFile(),

                         getCurrentConnectionProfile());

        }

      };

      registerAction(saveAsAction);

      recordAction = new RecordAction() {

        public void actionPerformed(ActionEvent evt) {

          startRecording();

        }

      };

      registerAction(recordAction);

      stopAction = new StopAction() {

        public void actionPerformed(ActionEvent evt) {

          stopRecording();

        }

      };

      registerAction(stopAction);

    }

    catch (AccessControlException ace) {

      ace.printStackTrace();

    }

    //  Only allow editing of connection file if read / write is allowed

    try {

      if (System.getSecurityManager() != null) {

        AccessController.checkPermission(

            new FilePermission("<<ALL FILES>>", "write"));

      }

      if (System.getSecurityManager() != null) {

        AccessController.checkPermission(

            new FilePermission("<<ALL FILES>>", "read"));

      }

      editAction = new EditAction() {

        public void actionPerformed(ActionEvent evt) {

          editConnection();

        }

      };

      registerAction(editAction);

    }

    catch (AccessControlException ace) {

      ace.printStackTrace();

    }

    java.util.List providers = SessionProviderFactory.getInstance().
        getSessionProviders();
    SessionProvider provider;
    SessionProviderAction action;
    for (Iterator it = providers.iterator();
         it.hasNext(); ) {
      provider = (SessionProvider) it.next();
      action = new SessionProviderAction(provider);
      sessionActions.put(action.getActionCommand(), action);
      action.addActionListener(this);
      registerAction(action);
    }

  }

  private void startRecording() {

    if (recordingFileChooser == null) {

      recordingFileChooser =

          new JFileChooser(System.getProperty("user.home"));

    }

    int ret = recordingFileChooser.showSaveDialog(this);

    int i = 1;

    File file = null;

    while (file == null) {

      File f =

          new File(

          recordingFileChooser.getCurrentDirectory(),

          "vncsession.fbs." + i);

      if (!f.exists()) {

        file = f;

      }

      i++;

    }

    recordingFileChooser.setSelectedFile(file);

    if (ret == recordingFileChooser.APPROVE_OPTION) {

      file = recordingFileChooser.getSelectedFile();

      if (file.exists()

          && JOptionPane.showConfirmDialog(

          this,

          "File " + file.getName() + " already exists. Are you sure?",

          "File exists",

          JOptionPane.YES_NO_OPTION,

          JOptionPane.WARNING_MESSAGE)

          == JOptionPane.NO_OPTION) {

        return;

      }

      vnc.setRecordingStatus(file.getAbsolutePath());

      setAvailableActions();

    }

  }

  private void stopRecording() {

    vnc.setRecordingStatus(null);

    setAvailableActions();

  }

  public void refresh() {

    try {

      vnc.refresh();

    }

    catch (IOException ioe) {

      closeConnection(true);

      showErrorMessage(this, "Error", ioe);

    }

  }

  public void setAvailableActions() {

    boolean connected = isConnected();

    newAction.setEnabled(!connected);

    closeAction.setEnabled(connected);

    refreshAction.setEnabled(connected);

    ctrlAltDelAction.setEnabled(connected);

    clipboardAction.setEnabled(connected);

    String sessionFile = vnc.getRecordingStatus();

    SessionProviderAction action;
    for (Iterator it = sessionActions.values().iterator();
         it.hasNext(); ) {
      action = (SessionProviderAction) it.next();
      action.setEnabled(connected);
    }

    if (stopAction != null) {

      stopAction.setEnabled(connected && sessionFile != null);

    }

    if (recordAction != null) {

      recordAction.setEnabled(connected && sessionFile == null);

    }

    if (openAction != null) {

      openAction.setEnabled(true);

    }

    connectionPropertiesAction.setEnabled(connected);

    closeAction.setEnabled(connected);

    if (saveAction != null) {

      saveAction.setEnabled(connected && isNeedSave());

    }

    if (saveAsAction != null) {

      saveAsAction.setEnabled(connected);

    }

  }

  public void close() {
    closeConnection(true);
  }

  private TerminalEmulation createEmulation() throws IOException {
    return new TerminalEmulation(TerminalEmulation.VT320);
  }

  public boolean canClose() {

    if (isConnected()) {
      if (JOptionPane
          .showConfirmDialog(
          this,
          "Close the current session and exit?",
          "Exit Application",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.QUESTION_MESSAGE)
          == JOptionPane.NO_OPTION) {
        return false;
      }
    }
    return true;

  }

  //  Supporting classes
  class ErrorTextBox
      extends JTextArea {
    ErrorTextBox(String text) {
      super(text);
      setOpaque(false);
      setWrapStyleWord(true);
      setLineWrap(true);
      setEditable(false);
    }
  }

  class IOConnector
      extends Thread {
    InputStream in;
    OutputStream out;
    IOConnector(InputStream in, OutputStream out) {
      super("IOConnector");
      this.in = in;
      this.out = out;
      start();
    }

    public void run() {
      IOStreamConnector con = new IOStreamConnector(in, out);
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

}