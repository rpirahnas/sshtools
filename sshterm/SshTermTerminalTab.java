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

import java.util.Iterator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.ColorComboBox;
import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.sshterm.emulation.TerminalEmulation;
import com.sshtools.sshterm.emulation.TerminalPanel;

public class SshTermTerminalTab
    extends JPanel
    implements SshToolsConnectionTab,
    ActionListener {
  //
  final static String DEFAULT = "<Default>";
  final static String TERM_ICON = "/com/sshtools/sshterm/largeterminal.png";
  //
  protected SshToolsConnectionProfile profile;
  protected JComboBox jComboTerm = new JComboBox();
  protected JComboBox jComboResizeStrategy = new JComboBox(new String[] {
      "None", "Font", "Screen"
  });
  protected JComboBox jComboEOL = new JComboBox(new String[] {
                                                "Default", "CR+LF", "CR"
  });
  protected JComboBox jComboScreenSize = new JComboBox(new String[] {
      "640x480", "800x600", "1024x768"
  });
  protected JComboBox fontSize = new JComboBox(new String[] {
                                               "6", "7", "8", "9", "10", "11",
                                               "12", "13", "14", "16", "18",
                                               "20", "22", "24", "26", "28",
                                               "32", "36", "40", "48", "56",
                                               "64",
                                               "8", "72"
  });
  protected JCheckBox antialias = new JCheckBox("Antialias (may be slow)");
  protected ColorComboBox backgroundColor = new ColorComboBox();
  protected ColorComboBox foregroundColor = new ColorComboBox();
  protected JLabel fontLabel;
  protected JCheckBox colorPrinting = new JCheckBox("Color printing");
  public SshTermTerminalTab() {
    super();
    Insets ins = new Insets(2, 2, 2, 2);
    //  Colors
    JPanel colorPanel = new JPanel(new GridBagLayout());
    colorPanel.setBorder(BorderFactory.createTitledBorder("Colors"));
    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.anchor = GridBagConstraints.NORTH;
    gbc1.insets = ins;
    gbc1.weightx = 1.0;
    gbc1.weightx = 0.0;
    UIUtil.jGridBagAdd(colorPanel, new JLabel("Background Color"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    UIUtil.jGridBagAdd(colorPanel, backgroundColor, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 0.0;
    UIUtil.jGridBagAdd(colorPanel, new JLabel("Foreground Color"), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;
    UIUtil.jGridBagAdd(colorPanel, foregroundColor, gbc1,
                       GridBagConstraints.REMAINDER);
    gbc1.weightx = 2.0;
    gbc1.weighty = 1.0;
    gbc1.anchor = GridBagConstraints.CENTER;
    UIUtil.jGridBagAdd(colorPanel, colorPrinting, gbc1,
                       GridBagConstraints.REMAINDER);
    //  Terminal tab
    JPanel terminalPanel = new JPanel(new GridBagLayout());
    terminalPanel.setBorder(BorderFactory.createTitledBorder("General"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = ins;
    gbc.weightx = 1.0;
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(terminalPanel, new JLabel("Terminal type"), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(terminalPanel, jComboTerm, gbc,
                       GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(terminalPanel, new JLabel("Resize strategy"), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(terminalPanel, jComboResizeStrategy, gbc,
                       GridBagConstraints.REMAINDER);
    jComboResizeStrategy.addActionListener(this);
    gbc.weightx = 0.0;
    gbc.insets = new Insets(2, 26, 2, 2);
    fontLabel = new JLabel("Default font size");
    UIUtil.jGridBagAdd(terminalPanel, fontLabel, gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    gbc.insets = ins;
    fontSize.setEditable(true);
    UIUtil.jGridBagAdd(terminalPanel, fontSize, gbc,
                       GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(terminalPanel, new JLabel("EOL Type"), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(terminalPanel, jComboEOL, gbc,
                       GridBagConstraints.REMAINDER);

    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(terminalPanel, new JLabel("Screen Size"), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(terminalPanel, jComboScreenSize, gbc,
                       GridBagConstraints.REMAINDER);

    antialias.setMnemonic('c');
    gbc.weighty = 1.0;
    gbc.weightx = 2.0;
    gbc.insets = new Insets(18, 26, 2, 2);
    UIUtil.jGridBagAdd(terminalPanel, antialias, gbc,
                       GridBagConstraints.REMAINDER);
    //
    JPanel main = new JPanel(new BorderLayout());
    main.add(terminalPanel, BorderLayout.CENTER);
    main.add(colorPanel, BorderLayout.SOUTH);
    //
    IconWrapperPanel iconTerminalPanel = new IconWrapperPanel(new ResourceIcon(
        TERM_ICON), main);
    //  This tab
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    add(iconTerminalPanel, BorderLayout.CENTER);
    //
    loadList(TerminalEmulation.getSupportedEmulations(), jComboTerm, false);
    setAvailableActions();
  }

  private void setAvailableActions() {
    fontLabel.setEnabled(jComboResizeStrategy.getSelectedIndex() ==
                         TerminalPanel.RESIZE_SCREEN);
    fontSize.setEnabled(jComboResizeStrategy.getSelectedIndex() ==
                        TerminalPanel.RESIZE_SCREEN);
  }

  public void actionPerformed(ActionEvent evt) {
    setAvailableActions();
  }

  private void loadList(java.util.List list, JComboBox combo,
                        boolean addDefault) {
    Iterator it = list.iterator();
    if (addDefault) {
      combo.addItem(DEFAULT);
    }
    while (it.hasNext()) {
      combo.addItem(it.next());
    }
  }

  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;
    String term = profile.getApplicationProperty(SshTerminalPanel.
                                                 PROFILE_PROPERTY_TERMINAL,
                                                 TerminalEmulation.VT100);
    for (int i = 0; i < jComboTerm.getItemCount(); i++) {
      if (jComboTerm.getItemAt(i).equals(term)) {
        jComboTerm.setSelectedIndex(i);
        break;
      }
    }
    jComboResizeStrategy.setSelectedIndex(profile.getApplicationPropertyInt(
        SshTerminalPanel.PROFILE_PROPERTY_RESIZE_STRATEGY,
        TerminalPanel.RESIZE_SCREEN));

    jComboEOL.setSelectedIndex(profile.getApplicationPropertyInt(
        SshTerminalPanel.PROFILE_PROPERTY_EOL,
        TerminalEmulation.EOL_DEFAULT));

    jComboScreenSize.setSelectedIndex(profile.getApplicationPropertyInt(
        SshTerminalPanel.PROFILE_PROPERTY_SCREEN_SIZE,
        SshTerminalPanel.PROFILE_SCREEN_800_600));

    int sz = profile.getApplicationPropertyInt(SshTerminalPanel.
                                               PROFILE_PROPERTY_FONT_SIZE,
                                               12);
    for (int i = 0; i < fontSize.getItemCount(); i++) {
      if (fontSize.getItemAt(i).equals(String.valueOf(sz))) {
        fontSize.setSelectedIndex(i);
        break;
      }
    }
    antialias.setSelected(profile.getApplicationPropertyBoolean(
        SshTerminalPanel.PROFILE_PROPERTY_ANTIALIAS, false));
    backgroundColor.setColor(profile.getApplicationPropertyColor(
        SshTerminalPanel.PROFILE_PROPERTY_BACKGROUND_COLOR, Color.black));
    foregroundColor.setColor(profile.getApplicationPropertyColor(
        SshTerminalPanel.PROFILE_PROPERTY_FOREGROUND_COLOR, Color.white));
    colorPrinting.setSelected(profile.getApplicationPropertyBoolean(
        SshTerminalPanel.PROFILE_PROPERTY_COLOR_PRINTING, false));
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
    return "Terminal";
  }

  public String getTabToolTipText() {
    return "Terminal related properties.";
  }

  public int getTabMnemonic() {
    return 't';
  }

  public Component getTabComponent() {
    return this;
  }

  public boolean validateTab() {
    return true;
  }

  public void applyTab() {

    profile.setApplicationProperty("sshterm.configured", true);

    profile.setApplicationProperty(SshTerminalPanel.PROFILE_PROPERTY_TERMINAL,
                                   (String) jComboTerm.getSelectedItem());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PROFILE_PROPERTY_RESIZE_STRATEGY,
                                   jComboResizeStrategy.getSelectedIndex());
    profile.setApplicationProperty(SshTerminalPanel.PROFILE_PROPERTY_EOL,
                                   jComboEOL.getSelectedIndex());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PROFILE_PROPERTY_SCREEN_SIZE,
                                   jComboScreenSize.getSelectedIndex());
    try {
      profile.setApplicationProperty(SshTerminalPanel.
                                     PROFILE_PROPERTY_FONT_SIZE,
                                     Integer.parseInt(String.valueOf(fontSize.
          getSelectedItem())));
    }
    catch (NumberFormatException nfe) {
      profile.setApplicationProperty(SshTerminalPanel.
                                     PROFILE_PROPERTY_FONT_SIZE,
                                     12);
    }
    profile.setApplicationProperty(SshTerminalPanel.PROFILE_PROPERTY_ANTIALIAS,
                                   antialias.isSelected());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PROFILE_PROPERTY_BACKGROUND_COLOR,
                                   backgroundColor.getColor());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PROFILE_PROPERTY_FOREGROUND_COLOR,
                                   foregroundColor.getColor());
    profile.setApplicationProperty(SshTerminalPanel.
                                   PROFILE_PROPERTY_COLOR_PRINTING,
                                   colorPrinting.isSelected());
  }

  public void tabSelected() {
  }
}