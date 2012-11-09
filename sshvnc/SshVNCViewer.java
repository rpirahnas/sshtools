//
//  Copyright (C) 2001,2002 HorizonLive.com, Inc.  All Rights Reserved.
//  Copyright (C) 2002 Constantin Kaplinsky.  All Rights Reserved.
//  Copyright (C) 1999 AT&T Laboratories Cambridge.  All Rights Reserved.
//
//  This is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//
//  This software is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this software; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
//  USA.
//
package com.sshtools.sshvnc;

//
// VncViewer.java - the VNC viewer applet.  This class mainly just sets up the
// user interface, leaving it to the VncCanvas to do the actual rendering of
// a VNC desktop.
//

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.ui.UIUtil;

public class SshVNCViewer
    extends JPanel {

  private String[] mainArgs;
  private RfbProto rfb;

  private JScrollPane desktopScrollPane;
  private GridBagLayout gridbag;
  private VncCanvas vc;
  private ClipboardFrame clipboard;
  private boolean protocolInitialised;

  // Control session recording.
  private Object recordingSync;
  private String sessionFileName;
  private boolean recordingActive;
  private boolean recordingStatusChanged;
  private String cursorUpdatesDef;
  private String eightBitColorsDef;

  // Variables read from parameter values.
  private String socketFactory;
  private boolean showOfflineDesktop;
  private int deferScreenUpdates;
  private int deferCursorUpdates;
  private int deferUpdateRequests;
  private SshToolsApplicationPanel application;
  private SshVNCOptions options;
  private SshVNCOptions oldOptions;
  private AuthenticationFrame authFrame;
  private boolean frameResizeable;

  public SshVNCViewer() {
    super();
    frameResizeable = true;
    showWelcomeScreen();

  }

  public boolean init(InputStream in, OutputStream out,
                      SshToolsApplicationPanel application,
                      SshVNCOptions options) throws Exception {

    this.options = options;
    this.application = application;

    recordingSync = new Object();

    Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
    if (w instanceof JFrame) {
      clipboard = new ClipboardFrame( (JFrame) w, this);
    }
    else if (w instanceof JDialog) {
      clipboard = new ClipboardFrame( (JDialog) w, this);
    }
    else {
      clipboard = new ClipboardFrame(this);
    }

    sessionFileName = null;
    recordingActive = false;
    recordingStatusChanged = false;
    cursorUpdatesDef = null;
    eightBitColorsDef = null;

    this.application = application;

    gridbag = new GridBagLayout();
    removeAll();
    setLayout(gridbag);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.anchor = GridBagConstraints.NORTHWEST;

    try {
      if (!connectAndAuthenticate(in, out)) {
        application.getStatusBar().setStatusText(
            "Failed to authenticate with VNC server");
        closeConnection();
        return false;
      }
      application.getStatusBar().setStatusText(
          "Initialising VNC protocol");
      doProtocolInitialisation();
      vc = new VncCanvas(this);

      // Disable the local cursor if required
      if (options.isRequestCursorUpdates() && !options.isIgnoreCursorUpdates()) {
        try {
//                    Image img = new ResourceIcon(
//                        "/com/sshtools/sshvnc/dotcursor.gif").getImage();
          Image img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
          if (img != null) {
            Cursor c = getToolkit().createCustomCursor(img,
                new Point(0, 0), "Dot");
            vc.setCursor(c);
          }
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }

      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gridbag.setConstraints(vc, gbc);

//            Panel canvasPanel = new Panel();
//            canvasPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
//            canvasPanel.add(vc);
      desktopScrollPane = new JScrollPane(vc);
      desktopScrollPane.setBorder(null);
      gbc.fill = GridBagConstraints.BOTH;
      gridbag.setConstraints(desktopScrollPane, gbc);
//            desktopScrollPane.add(vc);
      add(desktopScrollPane);
      validate();
      vc.resizeDesktopFrame();

      application.getStatusBar().setStatusText("Connected");
      vc.processNormalProtocol();

      return true;
    }
    catch (Exception e) {
      closeConnection();
      throw e;
    }
  }

  private void showWelcomeScreen() {
    synchronized (getTreeLock()) {
      removeAll();
      setLayout(new BorderLayout());
      JPanel p = new JPanel(new BorderLayout());
      p.setBackground(Color.black);
      p.setForeground(Color.white);
      JLabel welcomeLabel = new JLabel("SSHVnc", JLabel.CENTER);
      welcomeLabel.setForeground(Color.white);
      welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(72f).
                           deriveFont(Font.BOLD + Font.ITALIC));
      p.add(welcomeLabel, BorderLayout.CENTER);
      add(p, BorderLayout.CENTER);
      validate();
    }
  }

  public boolean isFrameResizeable() {
    return frameResizeable;
  }

  public void setFrameResizeable(boolean frameResizeable) {
    this.frameResizeable = frameResizeable;
  }

  public void setClipboardVisible(boolean visible) {
    if (clipboard != null) {
      clipboard.setVisible(visible);
    }
  }

  public int getScreenSizePolicy() {
    return options.getScreenSizePolicy();
  }

  public boolean isClipboardVisible() {
    return clipboard != null && clipboard.isVisible();
  }

  public void closeConnection() {
    protocolInitialised = false;
    if (vc != null) {
      vc.enableInput(false);
      vc.setCursor(Cursor.getDefaultCursor());
    }
    if (rfb != null && !rfb.closed()) {
      rfb.close();
    }
    System.out.println("Disconnect");

    if (rfb != null && !rfb.closed()) {
      rfb.close();
    }
    if (clipboard != null) {
      clipboard.dispose();
    }
    if (recordingActive) {
      setRecordingStatus(null);
      try {
        stopRecording();
      }
      catch (IOException ioe) {
        System.err.println("Could not stop recording sesion.");
        ioe.printStackTrace();
      }
    }
    showWelcomeScreen();
  }

  public String getSocketFactory() {
    return socketFactory;
  }

  public RfbProto getRFB() {
    return rfb;
  }

  public SshVNCOptions getOptions() {
    return options;
  }

  public int getDeferCursorUpdates() {
    return deferCursorUpdates;
  }

  public int getDeferUpdateRequests() {
    return deferUpdateRequests;
  }

  public int getDeferScreenUpdates() {
    return deferUpdateRequests;
  }

  public ClipboardFrame getClipboard() {
    return clipboard;
  }

  public VncCanvas getVNCCanvas() {
    return vc;
  }

  public JFrame getFrame() {
    return (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
  }

  public JScrollPane getScroller() {
    return desktopScrollPane;
  }

  //
  // Connect to the RFB server and authenticate the user.
  //

  private boolean connectAndAuthenticate(InputStream in, OutputStream out) throws
      Exception {
    protocolInitialised = false;

    if (authFrame == null) {
      Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
      if (w instanceof JFrame) {
        authFrame = new AuthenticationFrame( (JFrame) w);
      }
      else if (w instanceof JDialog) {
        authFrame = new AuthenticationFrame( (JDialog) w);
      }
      else {
        authFrame = new AuthenticationFrame();
      }
      UIUtil.positionComponent(SwingConstants.CENTER, authFrame);
    }

    rfb = new RfbProto(in, out, this);
    rfb.readVersionMsg();
    System.out.println("RFB server supports protocol version " +
                       rfb.serverMajor + "." + rfb.serverMinor);

    rfb.writeVersionMsg();

    int authScheme = rfb.readAuthScheme();
    System.out.println("Got auth scheme " + authScheme);

    while (true) {
      switch (authScheme) {

        case RfbProto.NoAuth:
          return true;
        case RfbProto.VncAuth:
          String pw = "";

          String encPw = options.getEncryptedPassword();
          if (!encPw.equals("")) {
            // Encrypted password is hexascii-encoded. Decode.
            byte[] c = HexASCII.ToByteArray(encPw);

            // Decrypt the password.
            byte[] key = {
                23, 82, 107, 6, 35, 78, 88, 7};
            DesCipher des = new DesCipher(key);
            des.decrypt(c, 0, c, 0);
            pw = new String(c);
          }
          else {
            // No supplied password, use GUI dialog
            char[] c = authFrame.doAuthentication();
            if (c == null) {
              return false;
            }
            pw = new String(c);
          }
          ;

          byte[] challenge = new byte[16];
          rfb.is.readFully(challenge);

          if (pw.length() > 8) {
            pw = pw.substring(0, 8); // Truncate to 8 chars

            // vncEncryptBytes in the UNIX libvncauth truncates password
            // after the first zero byte. We do to.
          }
          int firstZero = pw.indexOf(0);
          if (firstZero != -1) {
            pw = pw.substring(0, firstZero);

          }
          byte[] key = {
              0, 0, 0, 0, 0, 0, 0, 0};
          System.arraycopy(pw.getBytes(), 0, key, 0, pw.length());

          DesCipher des = new DesCipher(key);

          des.encrypt(challenge, 0, challenge, 0);
          des.encrypt(challenge, 8, challenge, 8);

          rfb.os.write(challenge);

          int authResult = rfb.is.readInt();

          switch (authResult) {
            case RfbProto.VncAuthOK:
              return true;
            case RfbProto.VncAuthFailed:
              break;
            case RfbProto.VncAuthTooMany:
              throw new Exception(
                  "VNC authentication failed - too many tries");
            default:
              throw new Exception(
                  "Unknown VNC authentication result " +
                  authResult);
          }
          break;

        default:
          throw new Exception("Unknown VNC authentication scheme " +
                              authScheme);
      }
    }
  }

  public boolean isProtocolInitialised() {
    return protocolInitialised;
  }

  //
  // Do the rest of the protocol initialisation.
  //

  void doProtocolInitialisation() throws IOException {

    rfb.writeClientInit();
    protocolInitialised = true;

    rfb.readServerInit();

    System.out.println("Desktop name is " + rfb.desktopName);
    System.out.println("Desktop size is " + rfb.framebufferWidth + " x " +
                       rfb.framebufferHeight);

    setEncodings();
  }

  //
  // Send current encoding list to the RFB server.
  //

  void setEncodings() {
    try {
      if (rfb != null && rfb.inNormalProtocol) {
        rfb.writeSetEncodings(options.getEncodings(),
                              options.getNumberOfEncodings());
        if (vc != null) {
          vc.softCursorFree();
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  //
  // setCutText() - send the given cut text to the RFB server.
  //

  void setCutText(String text) {
    try {
      if (rfb != null && rfb.inNormalProtocol) {
        rfb.writeClientCutText(text);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  //
  // Order change in session recording status. To stop recording, pass
  // null in place of the fname argument.
  //

  void setRecordingStatus(String fname) {
    synchronized (recordingSync) {
      sessionFileName = fname;
      recordingStatusChanged = true;
    }
  }

  public String getRecordingStatus() {
    return sessionFileName;
  }

  //
  // Start or stop session recording. Returns true if this method call
  // causes recording of a new session.
  //

  boolean checkRecordingStatus() throws IOException {
    synchronized (recordingSync) {
      if (recordingStatusChanged) {
        recordingStatusChanged = false;
        if (sessionFileName != null) {
          startRecording();
          return true;
        }
        else {
          stopRecording();
        }
      }
    }
    return false;
  }

  //
  // Start session recording.
  //

  protected void startRecording() throws IOException {
    synchronized (recordingSync) {
      if (!recordingActive) {
        try {
          oldOptions = (SshVNCOptions) options.clone();
        }
        catch (Throwable t) {

        }
        options.setEightBitColors(false);
        options.setRequestCursorUpdates(false);
        options.setEncodings();
        setEncodings();
      }
      else {
        rfb.closeSession();
      }

      System.out.println("Recording the session in " + sessionFileName);
      rfb.startSession(sessionFileName);
      recordingActive = true;
    }
  }

  //
  // Stop session recording.
  //

  protected void stopRecording() throws IOException {
    synchronized (recordingSync) {
      if (recordingActive) {
        options = oldOptions;
        setEncodings();
        rfb.closeSession();
        System.out.println("Session recording stopped.");
      }
      sessionFileName = null;
      recordingActive = false;
    }
  }

  public void refresh() throws IOException {
    rfb.writeFramebufferUpdateRequest(0, 0, rfb.framebufferWidth,
                                      rfb.framebufferHeight, false);
  }

  public void sendCtrlAltDel() throws IOException {
    final int modifiers = InputEvent.CTRL_MASK | InputEvent.ALT_MASK;
    KeyEvent ctrlAltDelEvent =
        new KeyEvent(this, KeyEvent.KEY_PRESSED, 0, modifiers,
                     KeyEvent.VK_DELETE, KeyEvent.CHAR_UNDEFINED);
    getRFB().writeKeyEvent(ctrlAltDelEvent);
    ctrlAltDelEvent = new KeyEvent(this, KeyEvent.KEY_RELEASED, 0,
                                   modifiers, KeyEvent.VK_DELETE,
                                   KeyEvent.CHAR_UNDEFINED);
    getRFB().writeKeyEvent(ctrlAltDelEvent);
  }

  //
  // moveFocusToDesktop() - move keyboard focus either to the
  // VncCanvas or to the AuthPanel.
  //

  void moveFocusToDesktop() {
    if (vc != null && isAncestorOf(vc)) {
      vc.requestFocus();
    }
  }

  synchronized public void fatalError(String str, Exception e) {
    if (rfb != null) {
      if (rfb.closed()) {
        return;
      }
      rfb.close();
    }
    application.showErrorMessage(this, str, "VNC Error", e);
  }

  //
  // Show message text and optionally "Relogin" and "Close" buttons.
  //

  void showMessage(String msg) {
    removeAll();

    Label errLabel = new Label(msg, Label.CENTER);
    errLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
    setLayout(new FlowLayout(FlowLayout.LEFT, 30, 30));
    add(errLabel);
    validate();
  }

  class AuthenticationDialog
      extends JDialog {

  }
}
