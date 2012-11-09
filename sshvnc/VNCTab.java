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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;

public class VNCTab
    extends JPanel
    implements SshToolsConnectionTab, ActionListener {

  final static String DEFAULT = "<Default>";
  final static ResourceIcon vncIcon = new ResourceIcon(VNCTab.class,
      "largevnc.png");
  final static String DEFAULT_HOST = "localhost";
  final static String DEFAULT_DISPLAY = "5900";

  protected SshToolsConnectionProfile profile;
  protected XTextField vncHost;
  protected JTextField startCommand;
  protected JTextField stopCommand;
  protected JRadioButton nix;
  protected JRadioButton windows;
  protected ButtonGroup group;
  protected JLabel display;
  protected NumericTextField port;
  protected JCheckBox jChkScreenSize;

  public VNCTab() {
    super();

    Insets ins = new Insets(2, 24, 2, 2);

    JPanel a = new JPanel(new GridBagLayout());
    a.setBorder(BorderFactory.createEtchedBorder());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = ins;
    gbc.weighty = 1.0;
    gbc.weightx = 1.0;

    //  Server Type tab
    JPanel serverType = new JPanel(new GridBagLayout());
    serverType.setBorder(BorderFactory.createTitledBorder(
        "The VNC server I want to connect to is a"));
    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    gbc2.anchor = GridBagConstraints.NORTH;
    gbc2.insets = ins;
    gbc2.weightx = 1.0;
    gbc2.weightx = 0.0;

    UIUtil.jGridBagAdd(serverType,
                       windows = new JRadioButton("Windows/Macintosh"), gbc2,
                       GridBagConstraints.RELATIVE);
    UIUtil.jGridBagAdd(serverType, nix = new JRadioButton("Linux/Unix Variant"),
                       gbc2,
                       GridBagConstraints.RELATIVE);

    //  Server Type tab
    JPanel hostPortPanel = new JPanel(new GridBagLayout());
    hostPortPanel.setBorder(BorderFactory.createTitledBorder(
        "Or manually specify the SSH forwarding settings"));
    gbc.insets = new Insets(2, 16, 2, 2);
    UIUtil.jGridBagAdd(hostPortPanel, new JLabel("Host "), gbc,
                       GridBagConstraints.RELATIVE);
    vncHost = new XTextField(15);
    gbc.insets = new Insets(2, 2, 2, 10);
    UIUtil.jGridBagAdd(hostPortPanel, vncHost, gbc,
                       GridBagConstraints.REMAINDER);
    gbc.insets = new Insets(2, 16, 2, 2);
    UIUtil.jGridBagAdd(hostPortPanel, display = new JLabel("Display "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.insets = new Insets(2, 2, 2, 10);
    UIUtil.jGridBagAdd(hostPortPanel, port = new NumericTextField(new Integer(0),
        new Integer(65535), new Integer(1)), gbc,
                       GridBagConstraints.REMAINDER);

    group = new ButtonGroup();
    group.add(nix);
    group.add(windows);
    ChangeListener listener = new ChangeListener() {
      public void stateChanged(ChangeEvent evt) {
        display.setText(windows.isSelected() ? "Port    " : "Display ");
        port.setValue(windows.isSelected() ? new Integer(5900) : new Integer(1));
      }
    };

    nix.addChangeListener(listener);
    windows.addChangeListener(listener);
    nix.setSelected(true);

    //  Server Type tab
    JPanel serverCommands = new JPanel(new GridBagLayout());
    serverCommands.setBorder(BorderFactory.createTitledBorder(
        "You may enter commands to start/stop your VNC server"));

    gbc.insets = new Insets(2, 16, 2, 2);
    UIUtil.jGridBagAdd(serverCommands, new JLabel("Start VNC Command"), gbc,
                       GridBagConstraints.RELATIVE);

    gbc.insets = new Insets(2, 2, 2, 10);
    UIUtil.jGridBagAdd(serverCommands, startCommand = new JTextField(), gbc,
                       GridBagConstraints.REMAINDER);

    gbc.insets = new Insets(2, 16, 2, 2);
    UIUtil.jGridBagAdd(serverCommands, new JLabel("Stop VNC Command"), gbc,
                       GridBagConstraints.RELATIVE);

    gbc.insets = new Insets(2, 2, 2, 10);
    UIUtil.jGridBagAdd(serverCommands, stopCommand = new JTextField(), gbc,
                       GridBagConstraints.REMAINDER);

    gbc.insets = new Insets(2, 16, 2, 2);
    UIUtil.jGridBagAdd(serverCommands,
                       jChkScreenSize = new JCheckBox(
        "Resize window to fit remote screen"), gbc,
                       GridBagConstraints.REMAINDER);

    Box box = new Box(BoxLayout.Y_AXIS);
    box.add(serverType);
    box.add(hostPortPanel);
    box.add(serverCommands);

    IconWrapperPanel w = new IconWrapperPanel(vncIcon, box);

    //  This tab
    setLayout(new BorderLayout());
    add(w, BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    setConnectionProfile(null);
  }

  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;

    String host = profile == null ? "" :
        profile.getApplicationProperty(SshVNCPanel.
                                       PROFILE_PROPERTY_VNC_HOST, "");

    String portStr = profile == null ? "" :
        profile.getApplicationProperty(SshVNCPanel.
                                       PROFILE_PROPERTY_VNC_DISPLAY, "");

    int os = profile == null ? 0 :
        profile.getApplicationPropertyInt(SshVNCPanel.
                                          PROFILE_PROPERTY_VNC_SERVER_OS, 0);
    if (os == 0) {
      windows.setSelected(true);
    }
    else {
      nix.setSelected(true);
    }

    if (host.equals("") || portStr.equals("")) {
      host = DEFAULT_HOST;
      portStr = DEFAULT_DISPLAY;
    }

    port.setText(portStr);
    vncHost.setText(host);

    if (profile != null) {
      startCommand.setText(profile.getApplicationProperty(SshVNCPanel.
          PROFILE_PRE_VNC_COMMAND, ""));
      stopCommand.setText(profile.getApplicationProperty(SshVNCPanel.
          PROFILE_POST_VNC_COMMAND, ""));
    }

    jChkScreenSize.setSelected(profile == null ? false :
                               profile.
                               getApplicationPropertyInt(SshVNCPanel.
        PROFILE_PROPERTY_SCREEN_SIZE_POLICY,
        SshVNCPanel.PROFILE_SCREEN_NO_CHANGE) ==
                               SshVNCPanel.PROFILE_SCREEN_REMOTE_DESKTOP);

    setAvailableActions();
  }

  public void actionPerformed(ActionEvent evt) {
    setAvailableActions();
  }

  private void setAvailableActions() {

  }

  public SshToolsConnectionProfile getConnectionProfile() {
    return profile;
  }

  public String getTabContext() {
    return "Connection";
  }

  public Icon getTabIcon() {
    return null;
  }

  public String getTabTitle() {
    return "VNC Settings";
  }

  public String getTabToolTipText() {
    return "Here you can specify your VNC settings for this connection.  " +
        "You can also enter commands to stop/start " +
        "your remote VNC server here.";
  }

  public int getTabMnemonic() {
    return 'v';
  }

  public Component getTabComponent() {
    return this;
  }

  public boolean validateTab() {
    return true;
  }

  public void applyTab() {

    profile.setApplicationProperty("sshvnc.configured", true);

    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_VNC_HOST,
                                   vncHost.getText().equals("") ?
                                   "localhost" : vncHost.getText());

    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_VNC_DISPLAY,
                                   port.getText().equals("") ?
                                   "5900" : port.getText());

    if (nix.isSelected()) {
      profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_VNC_SERVER_OS,
                                     SshVNCPanel.PROFILE_VNC_SERVER_OS_LINUX);
    }
    else if (windows.isSelected()) {
      profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_VNC_SERVER_OS,
                                     SshVNCPanel.
                                     PROFILE_VNC_SERVER_OS_WINDOWSMAC);
    }

    profile.setApplicationProperty(SshVNCPanel.PROFILE_PRE_VNC_COMMAND,
                                   startCommand.getText());
    profile.setApplicationProperty(SshVNCPanel.PROFILE_POST_VNC_COMMAND,
                                   stopCommand.getText());

    profile.setApplicationProperty(SshVNCPanel.
                                   PROFILE_PROPERTY_SCREEN_SIZE_POLICY,
                                   jChkScreenSize.isSelected() ?
                                   SshVNCPanel.PROFILE_SCREEN_REMOTE_DESKTOP
                                   : SshVNCPanel.PROFILE_SCREEN_NO_CHANGE);

  }

  public void tabSelected() {
  }
}