/*
 *  SSHTools - Java SSH2 API
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
package com.sshtools.sshterm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.UIUtil;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class SshTermCommandTab
    extends JPanel
    implements SshToolsConnectionTab {
  /**  */
  public final static String COMMANDS_ICON =
      "/com/sshtools/common/ui/commands.png";

  /**  */
  protected JCheckBox requestPseudoTerminal = new JCheckBox(
      "Don't allocate a pseudo terminal");

  /**  */
  protected JCheckBox disconnectOnSessionClose = new JCheckBox(
      "Disconnect when session is closed");

  /**  */
  protected JLabel onceAuthenticated = new JLabel("Once authenticated..");

  /**  */
  protected JRadioButton doNothing = new JRadioButton("Do nothing");

  /**  */
  protected JRadioButton startShell = new JRadioButton(
      "Start the user's shell");

  /**  */
  protected JRadioButton executeCommands = new JRadioButton(
      "Execute the following commands:");

  /**  */
  protected ButtonGroup group = new ButtonGroup();

  /**  */
  protected JTextArea commands = new JTextArea();

  /**  */
  protected SshToolsConnectionProfile profile;

  /**  */
  protected Log log = LogFactory.getLog(SshTermCommandTab.class);

  /**
   * Creates a new SshToolsConnectionCommandTab object.
   */
  public SshTermCommandTab() {
    super();

    JPanel main = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(0, 2, 2, 2);

    Insets ins2 = new Insets(2, 24, 2, 2);
    gbc.weightx = 1.0;
    requestPseudoTerminal.getModel().setSelected(false);
    disconnectOnSessionClose.getModel().setSelected(true);
    UIUtil.jGridBagAdd(main, requestPseudoTerminal, gbc,
                       GridBagConstraints.REMAINDER);
    UIUtil.jGridBagAdd(main, disconnectOnSessionClose, gbc,
                       GridBagConstraints.REMAINDER);
    UIUtil.jGridBagAdd(main, new JSeparator(JSeparator.HORIZONTAL), gbc,
                       GridBagConstraints.REMAINDER);
    UIUtil.jGridBagAdd(main, onceAuthenticated, gbc,
                       GridBagConstraints.REMAINDER);
    group.add(doNothing);
    group.add(startShell);
    group.add(executeCommands);
    startShell.setSelected(true);
    UIUtil.jGridBagAdd(main, doNothing, gbc, GridBagConstraints.REMAINDER);
    UIUtil.jGridBagAdd(main, startShell, gbc, GridBagConstraints.REMAINDER);
    UIUtil.jGridBagAdd(main, executeCommands, gbc,
                       GridBagConstraints.REMAINDER);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = ins2;
    gbc.weighty = 1.0;

    //commands.setLineWrap(true);
    commands.setBorder(BorderFactory.createEtchedBorder());

    JScrollPane scroll = new JScrollPane(commands);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scroll.setHorizontalScrollBarPolicy(JScrollPane.
                                        HORIZONTAL_SCROLLBAR_AS_NEEDED);
    UIUtil.jGridBagAdd(main, scroll, gbc, GridBagConstraints.REMAINDER);

    IconWrapperPanel iconProxyDetailsPanel = new IconWrapperPanel(new
        ResourceIcon(
        COMMANDS_ICON), main);
    commands.setRows(8);

    //  This panel
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.weightx = 1.0;
    add(iconProxyDetailsPanel, BorderLayout.NORTH);
  }

  /**
   *
   *
   * @param profile
   */
  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;
    requestPseudoTerminal.getModel().setSelected(!profile
                                                 .requiresPseudoTerminal());
    disconnectOnSessionClose.getModel().setSelected(profile
        .disconnectOnSessionClose());

    if (profile.getOnceAuthenticatedCommand() ==
        SshToolsConnectionProfile.DO_NOTHING) {
      doNothing.setSelected(true);
    }
    else if (profile.getOnceAuthenticatedCommand() ==
             SshToolsConnectionProfile.EXECUTE_COMMANDS) {
      executeCommands.setSelected(true);
      commands.setText(profile.getCommandsToExecute());
    }
    else {
      startShell.setSelected(true);
    }
  }

  /**
   *
   *
   * @return
   */
  public SshToolsConnectionProfile getConnectionProfile() {
    return profile;
  }

  /**
   *
   *
   * @return
   */
  public String getTabContext() {
    return "Connection";
  }

  /**
   *
   *
   * @return
   */
  public Icon getTabIcon() {
    return null;
  }

  /**
   *
   *
   * @return
   */
  public String getTabTitle() {
    return "Commands";
  }

  /**
   *
   *
   * @return
   */
  public String getTabToolTipText() {
    return "Configure the commands to be executed";
  }

  /**
   *
   *
   * @return
   */
  public int getTabMnemonic() {
    return 'p';
  }

  /**
   *
   *
   * @return
   */
  public Component getTabComponent() {
    return this;
  }

  /**
   *
   *
   * @return
   */
  public boolean validateTab() {
    return true;
  }

  /**
   *
   */
  public void applyTab() {
    profile.setRequiresPseudoTerminal(!requestPseudoTerminal.getModel()
                                      .isSelected());

    if (!doNothing.isSelected()) {
      profile.setDisconnectOnSessionClose(disconnectOnSessionClose.getModel()
                                          .isSelected());
    }
    else {
      profile.setDisconnectOnSessionClose(false);
    }

    if (doNothing.isSelected()) {
      profile.setOnceAuthenticatedCommand(SshToolsConnectionProfile.DO_NOTHING);
    }
    else if (executeCommands.isSelected()) {
      profile.setOnceAuthenticatedCommand(SshToolsConnectionProfile.
                                          EXECUTE_COMMANDS);
      profile.setCommandsToExecute(commands.getText());
    }
    else {
      profile.setOnceAuthenticatedCommand(SshToolsConnectionProfile.START_SHELL);
    }
  }

  /**
   *
   */
  public void tabSelected() {
  }
}
