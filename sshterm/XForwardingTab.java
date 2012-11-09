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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;

public class XForwardingTab
    extends JPanel
    implements SshToolsConnectionTab,
    ActionListener {
  //
  final static String X_ICON = "/com/sshtools/sshterm/x.png";
  //
  protected SshToolsConnectionProfile profile;
  protected JCheckBox enableXForwarding;
  protected XTextField localDisplay;
  protected NumericTextField remoteDisplayNumber;
  protected JLabel localDisplayLabel;
  protected JLabel remoteDisplayNumberLabel;
  public XForwardingTab() {
    super();
    Insets ins = new Insets(2, 2, 2, 2);
    Insets ins2 = new Insets(2, 24, 2, 2);
    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.anchor = GridBagConstraints.NORTH;
    gbc1.insets = ins;
    gbc1.weightx = 1.0;
    JPanel main = new JPanel(new GridBagLayout());
    gbc1.weightx = 2.0;
    UIUtil.jGridBagAdd(main,
                       enableXForwarding = new JCheckBox("Enable X Forwarding", true),
                       gbc1, GridBagConstraints.REMAINDER);
    enableXForwarding.addActionListener(this);
    gbc1.insets = ins2;
    gbc1.weightx = 0.0;
    UIUtil.jGridBagAdd(main,
                       localDisplayLabel = new JLabel("Local display "), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    UIUtil.jGridBagAdd(main, localDisplay = new XTextField(15), gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.insets = ins2;
    gbc1.weighty = 1.0;
    gbc1.weightx = 0.0;
    UIUtil.jGridBagAdd(main,
                       remoteDisplayNumberLabel = new JLabel(
        "Remote display number "),
                       gbc1, GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    UIUtil.jGridBagAdd(main,
                       remoteDisplayNumber = new NumericTextField(new Integer(0),
        new Integer(65536), new Integer(0)), gbc1,
                       GridBagConstraints.REMAINDER);
    //
    IconWrapperPanel iconTerminalPanel = new IconWrapperPanel(new ResourceIcon(
        X_ICON), main);
    //  This tab
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    add(iconTerminalPanel, BorderLayout.CENTER);
    //
    setAvailableActions();
  }

  private void setAvailableActions() {
    remoteDisplayNumber.setEnabled(enableXForwarding.isSelected());
    remoteDisplayNumberLabel.setEnabled(enableXForwarding.isSelected());
    localDisplay.setEnabled(enableXForwarding.isSelected());
    localDisplayLabel.setEnabled(enableXForwarding.isSelected());
  }

  public void actionPerformed(ActionEvent evt) {
    setAvailableActions();
  }

  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;
    enableXForwarding.setSelected(profile.getApplicationPropertyBoolean(
        SshTerminalPanel.PREF_X11_FORWARDING, false));
    localDisplay.setText(profile.getApplicationProperty(
        SshTerminalPanel.PREF_X11_FORWARDING_LOCAL_DISPLAY, ""));
    remoteDisplayNumber.setText(profile.getApplicationProperty(
        SshTerminalPanel.PREF_X11_FORWARDING_REMOTE_DISPLAY_NUMBER, ""));
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
    return "X";
  }

  public String getTabToolTipText() {
    return "X forwarding configuration.";
  }

  public int getTabMnemonic() {
    return 'x';
  }

  public Component getTabComponent() {
    return this;
  }

  public boolean validateTab() {
    return true;
  }

  public void applyTab() {
    profile.setApplicationProperty(SshTerminalPanel.PREF_X11_FORWARDING,
                                   enableXForwarding.isSelected());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PREF_X11_FORWARDING_LOCAL_DISPLAY,
                                   localDisplay.getText());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PREF_X11_FORWARDING_REMOTE_DISPLAY_NUMBER,
                                   remoteDisplayNumber.getText());
  }

  public void tabSelected() {
  }
}
