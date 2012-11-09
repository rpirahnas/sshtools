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
package com.sshtools.sshvnc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.ui.*;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.connection.Channel;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.forwarding.ForwardingChannel;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;
import com.sshtools.j2ssh.forwarding.ForwardingConfigurationListener;
import com.sshtools.j2ssh.forwarding.ForwardingIOChannel;
import com.sshtools.j2ssh.forwarding.XDisplay;
import com.sshtools.j2ssh.io.IOStreamConnector;
import com.sshtools.j2ssh.session.SessionChannelClient;
import com.sshtools.sshterm.emulation.TerminalEmulation;
import com.sshtools.sshterm.emulation.TerminalPanel;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.13 $
 */
public class SshVncSessionPanel
    extends SshToolsApplicationSessionPanel
    implements ActionListener {
  /**  */
  public final static String PROFILE_PROPERTY_VNC_HOST = "VNC_HOST";

  public final static String PROFILE_PROPERTY_VNC_DISPLAY = "VNC_DISPLAY";

  /**  */
  public final static String PROFILE_PROPERTY_PREFERRED_ENCODING =
      "PREFERRED_ENCODING";

  /**  */
  public final static String PROFILE_PROPERTY_USE_COPY_RECT = "USE_COPY_RECT";

  /**  */
  public final static String PROFILE_PROPERTY_COMPRESS_LEVEL = "COMPRESS_LEVEL";

  public final static String PROFILE_PRE_VNC_COMMAND = "PRE_VNC_COMMAND";

  public final static String PROFILE_POST_VNC_COMMAND = "POST_VNC_COMMAND";

  /**  */
  public final static String PROFILE_PROPERTY_JPEG_QUALITY = "JPEG_QUALITY";

  /**  */
  public final static String PROFILE_PROPERTY_CURSOR_UPDATES = "CURSOR_UPDATES";

  /**  */
  public final static String PROFILE_PROPERTY_EIGHT_BIT_COLORS =
      "EIGHT_BIT_COLORS";

  /**  */
  public final static String PROFILE_PROPERTY_REVERSE_MOUSE_BUTTONS_2_AND_3 =
      "REVERSE_MOUSE_BUTTONS_2_AND_3";

  /**  */
  public final static String PROFILE_PROPERTY_SHARE_DESKTOP = "SHARE_DESKTOP";

  /**  */
  public final static String PROFILE_PROPERTY_VIEW_ONLY = "VIEW_ONLY";

  // Logger

  /**  */
  protected static Log log = LogFactory.getLog(SshVNC.class);

  //  Private static instance variables
  private static boolean debug = true;

  /**  */
  public static final ResourceIcon MY3SP_VNC_ICON = new ResourceIcon(
      SshVncSessionPanel.class,
      "sshvncframeicon.gif");
  private boolean closing = false;

  //  Private instance variables
  private SshVNCViewer vnc;
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
  private javax.swing.Timer sendTimer;
  private javax.swing.Timer receiveTimer;
  private SessionChannelClient sessionCloserSession;
  private TerminalPanel terminal;
  protected TerminalEmulation emulation;

  // Full screen mode
  private boolean fullScreenMode;
  private SshToolsConnectionTab[] additionalTabs;
  private ConnectionPropertiesAction connectionPropertiesAction;
  private VncThread vncThread;

  /**  */
  protected SshClient ssh;

  private ChannelEventListener eventListener;
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
          new VNCTab(), new VNCAdvancedTab()};
      add(vnc, BorderLayout.CENTER);
      initActions();
    }
    catch (Throwable t) {
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));

      IconWrapperPanel p = new IconWrapperPanel(UIManager.getIcon(
          "OptionPane.errorIcon"),
                                                new ErrorTextBox( ( (t.
          getMessage() == null)
          ? "<no message supplied>" : t.getMessage())
          + (debug ? ("\n\n" + sw.getBuffer().toString()) : "")));
      p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      add(p, BorderLayout.CENTER);
      throw new SshToolsApplicationException("Failed to start SshVNC. ", t);
    }
  }

  /**
   *
   *
   * @return
   */
  public ResourceIcon getIcon() {
    return MY3SP_VNC_ICON;
  }

  public String getId() {
    return "sshvnc";
  }

  public void addEventListener(ChannelEventListener eventListener) {
    this.eventListener = eventListener;
    if (vncThread != null) {
      vncThread.getChannel().addEventListener(eventListener);
    }
  }

  /**
   *
   *
   * @return
   */
  public SshToolsConnectionTab[] getAdditionalConnectionTabs() {
    return additionalTabs;
  }

  /**
   *
   *
   * @param evt
   */
  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == receiveTimer) {
      statusBar.setReceiving(false);
    }
    else if (evt.getSource() == sendTimer) {
      statusBar.setSending(false);
    }
  }

  public boolean requiresConfiguration() {
    return true;
  }

  /**
   *
   *
   * @param frameResizeable
   */
  public void setFrameResizeable(boolean frameResizeable) {
    vnc.setFrameResizeable(frameResizeable);
  }

  /**
   *
   *
   * @param channel
   */
  public void opened(ForwardingConfiguration config, ForwardingChannel channel) {
  }

  /**
   *
   *
   * @param channel
   */
  public void closed(ForwardingConfiguration config, ForwardingChannel channel) {
  }

  /**
   *
   *
   * @param channel
   * @param bytes
   */
  public void dataReceived(ForwardingConfiguration config,
                           ForwardingChannel channel, int bytes) {
    if (!receiveTimer.isRunning()) {
      statusBar.setReceiving(true);
      receiveTimer.start();
    }
  }

  /**
   *
   *
   * @param full
   */
  public void setFullScreenMode(final boolean full) {
    if (fullScreenMode != full) {
      try {
        if (!full) {
          application.convertContainer(getContainer(),
                                       SshVncSessionPanel.class);
        }
        else {
          application.convertContainer(getContainer(),
                                       SshVncFullScreenWindowContainer.class);
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

  /**
   *
   *
   * @param channel
   * @param bytes
   */
  public void dataSent(ForwardingConfiguration config,
                       ForwardingChannel channel, int bytes) {
    if (!sendTimer.isRunning()) {
      statusBar.setSending(true);
      sendTimer.start();
    }
  }

  /**
   *
   *
   * @return
   */
  public StatusBar getStatusBar() {
    return statusBar;
  }

  /**
   *
   *
   * @return
   */
  public JToolBar getToolBar() {
    return toolBar;
  }

  /**
   *
   *
   * @param in
   * @param out
   * @param options
   */
  public void initVNC(InputStream in, OutputStream out, SshVNCOptions options) {
    log.info("Initialising VNC");

    try {
      vnc.init(in, out, this, options);
    }
    catch (EOFException e) {
      //if (isConnected() && !closing) {
      //    closeConnection(true);
      //    showErrorMessage(this, "The server closed the connection.",
      //        "Error", null);
      // }
    }
    catch (Exception e) {
      // if (isConnected() && !closing) {
      //     closeConnection(true);
      //     showErrorMessage(this, "Error", e);
      // }
    }
  }

  /**
   *
   */
  public void closeVNC() {
    vnc.closeConnection();
  }

  /**
   *
   */
  public void run() {
  }

  /**
   *
   *
   * @param doDisconnect
   */
  public synchronized void closeConnection(boolean doDisconnect) {

    closing = true;
    closeVNC();

    try {
      String command = getCurrentConnectionProfile().getApplicationProperty(
          PROFILE_POST_VNC_COMMAND, null);

      if (command != null && command.trim().length() > 0 && manager.isConnected()) {
        statusBar.setStatusText("Executing command: " + command);

        remove(vnc);
        add(terminal, BorderLayout.CENTER);
        emulation.reset();
        emulation.clearScreen();
        emulation.setCursorPosition(0, 0);
        terminal.refresh();
        log.debug("Executing post VNC command " + command);

        sessionCloserSession.requestPseudoTerminal("vt100", 80, 24, 0, 0, "");

        if (sessionCloserSession.executeCommand(command)) {
          sessionCloserSession.bindInputStream(
              emulation.getTerminalInputStream());
          sessionCloserSession.bindOutputStream(
              emulation.getTerminalOutputStream());
        }

        try {
          sessionCloserSession.getState().waitForState(ChannelState.
              CHANNEL_CLOSED);
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

    if (manager.isConnected()) {
      manager.requestDisconnect();
    }

    statusBar.setUser("");
    statusBar.setHost("");
    statusBar.setStatusText("Closed");
    statusBar.setConnected(false);

    // Reset the menu items
    setAvailableActions();

    closing = false;
  }

  /**
   *
   *
   * @throws IOException
   */
  public boolean onOpenSession() throws IOException {
    statusBar.setStatusText("User Authenticated");

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

    // Execute any pre-vnc command as we are now authenticated
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

      log.debug("Executing pre VNC command " + command);

      SessionChannelClient session = manager.openSession();

      /* Create another session.  This prevents the transport protocol from disconnecting
       when the preVNC commmand completes execution. This will happen in cases where the user
       has specified that they want to disconnect when the last session is closed. We will
       reuse this session later to execute the disconnect command. */
      sessionCloserSession = manager.openSession();

      session.requestPseudoTerminal("vt100", 80, 24, 0, 0, "");

      if (session.executeCommand(command)) {

        IOStreamConnector ios = new IOStreamConnector(session.
            getStderrInputStream(),
            emulation.getTerminalOutputStream());
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
      log.debug("Setting up forwarding on " + addr + " (" + localPort
                + ") to " + display.getHost() + ":" + display.getPort());
    }

    final SshVNCOptions options = new SshVNCOptions(getCurrentConnectionProfile());
    statusBar.setStatusText("Initialising VNC");

    /*ForwardingConfiguration config = new ForwardingConfiguration("VNC",
        "forwarded-channel", 0, display.getHost(), display.getPort());*/
    final ForwardingIOChannel channel = new ForwardingIOChannel(
        ForwardingIOChannel.LOCAL_FORWARDING_CHANNEL,
        "VNC",
        display.getHost(), display.getPort(), addr,
        display.getPort());

    if (manager.openChannel(channel)) {
      // The forwarding channel is open so forward to the
      // VNC protocol

      channel.addEventListener(new DataNotificationListener(statusBar));

      if (eventListener != null) {
        channel.addEventListener(eventListener);
      }
      vncThread = new VncThread(channel);
      vncThread.start();
      setAvailableActions();
    }
    else {
      // We need to close the connection and inform the user
      // that the forwarding failed to start
      closeConnection(true);
      throw new IOException("Could not connect to the vnc server");

      /*try {
        SwingUtilities.invokeAndWait(new DisplayErrorMessage());
             }
             catch (Exception ex) {
        statusBar.setStatusText(
            "Could not connect to local forwarding server");
             }
             finally {
        closeConnection(true);
             }*/
    }

    return true;

    //setFullScreenMode(true);
  }

  private void initActions() {
    //  Create the action menu groups
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("File",
        "File", 'f', 0));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("Edit",
        "Edit", 'e', 10));
    registerActionMenu(new SshToolsApplicationPanel.ActionMenu("View",
        "View", 'v', 20));
    vncTab = new VNCTab( /*vnc*/);

    closeAction = new VncCloseAction();

    registerAction(closeAction);
    refreshAction = new VncRefreshAction();
    registerAction(refreshAction);
    ctrlAltDelAction = new VncCtrlAltDelAction();
    registerAction(ctrlAltDelAction);
    clipboardAction = new VncClipboardAction();
    registerAction(clipboardAction);
    connectionPropertiesAction = new VncConnectionPropertiesAction();
    registerAction(connectionPropertiesAction);

    // Remove stuff we dont want
    deregisterAction(getAction("Options"));
    setActionVisible("New Window", false);
    setActionVisible("About", false);
  }

  private void startRecording() {
  }

  private void stopRecording() {
  }

  /**
   *
   */
  public void refresh() {
    try {
      vnc.refresh();
    }
    catch (IOException ioe) {
      closeConnection(true);
      showErrorMessage(this, "Error", ioe);
    }
  }

  /**
   *
   */
  public void setAvailableActions() {
    boolean connected = isConnected();

    closeAction.setEnabled(connected);
    refreshAction.setEnabled(connected);
    ctrlAltDelAction.setEnabled(connected);
    clipboardAction.setEnabled(connected);

    String sessionFile = vnc.getRecordingStatus();

    closeAction.setEnabled(connected);
  }

  /**
   *
   */
  public void close() {
    closeConnection(true);
  }

  private TerminalEmulation createEmulation() throws IOException {
    return new TerminalEmulation(TerminalEmulation.VT320);
  }

  /**
   *
   *
   * @return
   */
  public boolean canClose() {
    if (isConnected()) {
      if (JOptionPane.showConfirmDialog(this,
                                        "Close the current session and exit?",
                                        "Exit Application",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE) ==
          JOptionPane.NO_OPTION) {
        return false;
      }
    }

    return true;
  }

  //  Supporting classes
  public class ErrorTextBox
      extends JTextArea {
    public ErrorTextBox(String text) {
      super(text);
      setOpaque(false);
      setWrapStyleWord(true);
      setLineWrap(true);
      setEditable(false);
    }
  }

  public class IOConnector
      extends Thread {
    InputStream in;
    OutputStream out;

    public IOConnector(InputStream in, OutputStream out) {
      super("IOConnector");
      this.in = in;
      this.out = out;
      start();
    }

    public void run() {
      IOStreamConnector con = new IOStreamConnector(in, out);
    }
  }

  public class VncCloseAction
      extends CloseAction {

    public VncCloseAction() {}

    public void actionPerformed(ActionEvent evt) {
      closeConnection(true);
    }
  }

  public class VncRefreshAction
      extends RefreshAction {

    public VncRefreshAction() {}

    public void actionPerformed(ActionEvent evt) {
      refresh();
    }
  }

  public class VncCtrlAltDelAction
      extends CtrlAltDelAction {

    public VncCtrlAltDelAction() {}

    public void actionPerformed(ActionEvent evt) {
      try {
        vnc.sendCtrlAltDel();
      }
      catch (IOException ioe) {
        closeConnection(true);
        showErrorMessage(SshVncSessionPanel.this, "Error", ioe);
      }
    }
  }

  public class VncClipboardAction
      extends ClipboardAction {

    public VncClipboardAction() {}

    public void actionPerformed(ActionEvent evt) {
      vnc.setClipboardVisible(!vnc.isClipboardVisible());
    }
  }

  public class VncConnectionPropertiesAction
      extends ConnectionPropertiesAction {

    public VncConnectionPropertiesAction() {}

    public void actionPerformed(ActionEvent evt) {
      editSettings(getCurrentConnectionProfile());
    }
  }

  public class VncThread
      extends SshThread {

    ForwardingIOChannel channel;

    public VncThread(ForwardingIOChannel channel) {
      super("VNC", false);
      this.channel = channel;
    }

    public Channel getChannel() {
      return channel;
    }

    public void run() {
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
        try {
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
          session.getState().waitForState(ChannelState.CHANNEL_CLOSED);
        }
        catch (Exception iox) {
        }

        finally {
          remove(terminal);
          add(vnc, BorderLayout.CENTER);
        }
      }

      initVNC(channel.getInputStream(),
              channel.getOutputStream(),
              new SshVNCOptions(getCurrentConnectionProfile()));

    }
  }

  public class DisplayErrorMessage
      implements Runnable {
    public void run() {
      JOptionPane.showMessageDialog(SshVncSessionPanel.this,
          "SSHVnc failed to open a forwarding channel",
          "SSHVnc",
          JOptionPane.OK_OPTION);
    }
  }

}