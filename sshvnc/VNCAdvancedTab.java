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

import java.io.File;
import java.io.FilenameFilter;

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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sshtools.common.configuration.InvalidProfileFileException;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

public class VNCAdvancedTab
    extends JPanel
    implements SshToolsConnectionTab, ActionListener {

  final static ResourceIcon vncicon = new ResourceIcon(VNCAdvancedTab.class,
      "largevnc.png");

  final static String[] ENCODINGS = {
      "Raw", "RRE", "CoRRE", "Hextile", "Zlib", "Tight"};
  final static String[] COMPRESSION_LEVEL = {
      "Default", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
  final static String[] JPEG_QUALITY = {
      "JPEG off", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
  final static String[] CURSOR_UPDATES = {
      "Enable", "Ignore", "Disable"};

  protected SshToolsConnectionProfile profile;
  protected JComboBox encoding, compressionLevel, jpegQuality, cursorUpdates,
      bandwidth;
  protected JCheckBox useCopyRect, restrictedColors, reverseMouseButtons2And3,
      viewOnly, shareDesktop;

  public VNCAdvancedTab( /*SshVNCViewer viewer*/) {
    super();

    Insets ins = new Insets(2, 24, 2, 2);

    JPanel s = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.insets = ins;
    gbc.weighty = 1.0;

    GridBagConstraints gbc2 = new GridBagConstraints();
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    gbc2.anchor = GridBagConstraints.NORTH;
    gbc2.insets = new Insets(2, 16, 2, 2);

    JPanel a = new JPanel(new GridBagLayout());
    a.setBorder(BorderFactory.createTitledBorder("Advanced Settings"));
    gbc2.fill = GridBagConstraints.HORIZONTAL;
    gbc2.anchor = GridBagConstraints.NORTH;
    gbc2.insets = ins;
    gbc2.weightx = 0.0;

    UIUtil.jGridBagAdd(a, new JLabel("Bandwidth Setting"), gbc2,
                       GridBagConstraints.RELATIVE);
    gbc2.weightx = 1.0;
    gbc2.insets = new Insets(2, 2, 2, 2);

    // Load any optimized profile settings
    bandwidth = new JComboBox();
    loadOptimizedProfiles();

    bandwidth.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        // Load the relevant profile here
        SshToolsConnectionProfile settingsprofile = new
            SshToolsConnectionProfile();
        try {
          settingsprofile.open(ConfigurationLoader.getConfigurationDirectory() +
                               bandwidth.getSelectedItem().toString() + ".opt");

          // Get the preferred encoding setting
          profile.setApplicationProperty(SshVNCPanel.
                                         PROFILE_PROPERTY_PREFERRED_ENCODING,
                                         settingsprofile.getApplicationProperty(
              SshVNCPanel.PROFILE_PROPERTY_PREFERRED_ENCODING, null));

          encoding.setSelectedIndex(settingsprofile.getApplicationPropertyInt(
              SshVNCPanel.PROFILE_PROPERTY_PREFERRED_ENCODING, 1));

          // Get the compression level setting
          profile.setApplicationProperty(SshVNCPanel.
                                         PROFILE_PROPERTY_COMPRESS_LEVEL,
                                         settingsprofile.getApplicationProperty(
              SshVNCPanel.PROFILE_PROPERTY_COMPRESS_LEVEL, null));

          compressionLevel.setSelectedIndex(settingsprofile.
                                            getApplicationPropertyInt(
              SshVNCPanel.PROFILE_PROPERTY_COMPRESS_LEVEL, 1));

          // Get the JPEG quality setting
          profile.setApplicationProperty(SshVNCPanel.
                                         PROFILE_PROPERTY_JPEG_QUALITY,
                                         settingsprofile.getApplicationProperty(
              SshVNCPanel.PROFILE_PROPERTY_JPEG_QUALITY, null));
          jpegQuality.setSelectedIndex(settingsprofile.
                                       getApplicationPropertyInt(
              SshVNCPanel.PROFILE_PROPERTY_JPEG_QUALITY, 1) + 1);

          // Get the 8bit color setting
          profile.setApplicationProperty(SshVNCPanel.
                                         PROFILE_PROPERTY_EIGHT_BIT_COLORS,
                                         settingsprofile.getApplicationProperty(
              SshVNCPanel.PROFILE_PROPERTY_EIGHT_BIT_COLORS, null));
          restrictedColors.setSelected(settingsprofile.
                                       getApplicationPropertyBoolean(
              SshVNCPanel.PROFILE_PROPERTY_EIGHT_BIT_COLORS, false));

          // Get the use copyrect setting
          profile.setApplicationProperty(SshVNCPanel.
                                         PROFILE_PROPERTY_USE_COPY_RECT,
                                         settingsprofile.getApplicationProperty(
              SshVNCPanel.PROFILE_PROPERTY_USE_COPY_RECT, null));
          useCopyRect.setSelected(settingsprofile.getApplicationPropertyBoolean(
              SshVNCPanel.PROFILE_PROPERTY_USE_COPY_RECT, false));

          setAvailableActions();
        }
        catch (InvalidProfileFileException ex) {
        }
      }
    });

    UIUtil.jGridBagAdd(a, bandwidth, gbc2,
                       GridBagConstraints.REMAINDER);

    gbc2.insets = new Insets(2, 24, 2, 2);

    UIUtil.jGridBagAdd(a, new JLabel("Encodings "), gbc2,
                       GridBagConstraints.RELATIVE);

    gbc2.weightx = 1.0;
    gbc2.insets = new Insets(2, 2, 2, 2);
    encoding = new JComboBox(ENCODINGS);
    encoding.addActionListener(this);
    UIUtil.jGridBagAdd(a, encoding, gbc2,
                       GridBagConstraints.REMAINDER);

    gbc2.weightx = 0.0;
    gbc2.insets = new Insets(2, 24, 2, 2);
    UIUtil.jGridBagAdd(a, new JLabel("Compression "), gbc2,
                       GridBagConstraints.RELATIVE);

    gbc2.weightx = 1.0;
    gbc2.insets = new Insets(2, 2, 2, 2);
    compressionLevel = new JComboBox(COMPRESSION_LEVEL);
    compressionLevel.addActionListener(this);
    UIUtil.jGridBagAdd(a, compressionLevel, gbc2,
                       GridBagConstraints.REMAINDER);

    gbc2.weightx = 0.0;
    gbc2.insets = new Insets(2, 24, 2, 2);
    UIUtil.jGridBagAdd(a, new JLabel("JPEG Quality "), gbc2,
                       GridBagConstraints.RELATIVE);

    gbc2.weightx = 1.0;
    gbc2.insets = new Insets(2, 2, 2, 2);
    jpegQuality = new JComboBox(JPEG_QUALITY);
    jpegQuality.addActionListener(this);
    UIUtil.jGridBagAdd(a, jpegQuality, gbc2,
                       GridBagConstraints.REMAINDER);
    gbc2.weightx = 0.0;
    gbc2.insets = new Insets(2, 24, 2, 2);
    UIUtil.jGridBagAdd(a, new JLabel("Cursor shape updates "), gbc2,
                       GridBagConstraints.RELATIVE);

    gbc2.weightx = 1.0;
    gbc2.insets = new Insets(2, 2, 2, 2);
    cursorUpdates = new JComboBox(CURSOR_UPDATES);
    cursorUpdates.addActionListener(this);
    UIUtil.jGridBagAdd(a, cursorUpdates, gbc2,
                       GridBagConstraints.REMAINDER);

    gbc2.weightx = 0.0;
    gbc2.insets = new Insets(2, 24, 2, 2);

    // Checkboxes ======================================================
    gbc2.weightx = 2.0;
    gbc2.insets = new Insets(8, 24, 2, 2);
    useCopyRect = new JCheckBox("Use CopyRect");
    useCopyRect.addActionListener(this);
    UIUtil.jGridBagAdd(a, useCopyRect, gbc2,
                       GridBagConstraints.RELATIVE);

    gbc2.insets = new Insets(8, 10, 2, 2);
    restrictedColors = new JCheckBox("Restricted Colors");
    restrictedColors.addActionListener(this);
    UIUtil.jGridBagAdd(a, restrictedColors, gbc2,
                       GridBagConstraints.REMAINDER);

    gbc2.insets = new Insets(4, 24, 2, 2);
    reverseMouseButtons2And3 = new JCheckBox("Reverse mouse buttons");
    reverseMouseButtons2And3.addActionListener(this);
    UIUtil.jGridBagAdd(a, reverseMouseButtons2And3, gbc2,
                       GridBagConstraints.RELATIVE);

    gbc2.insets = new Insets(4, 10, 2, 2);
    viewOnly = new JCheckBox("View only");
    viewOnly.addActionListener(this);
    UIUtil.jGridBagAdd(a, viewOnly, gbc2,
                       GridBagConstraints.REMAINDER);

    gbc2.insets = new Insets(4, 24, 2, 2);
    shareDesktop = new JCheckBox("Share desktop");
    shareDesktop.addActionListener(this);
    UIUtil.jGridBagAdd(a, shareDesktop, gbc2,
                       GridBagConstraints.REMAINDER);

    JPanel z = new JPanel(new BorderLayout());
    z.add(a, BorderLayout.NORTH);

    IconWrapperPanel w = new IconWrapperPanel(vncicon, z);

    //  This tab
    setLayout(new BorderLayout());
    add(w, BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    setConnectionProfile(null);
  }

  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;

    if (profile != null) {
      int idx = profile == null ? RfbProto.EncodingTight :
          profile.getApplicationPropertyInt(SshVNCPanel.
                                            PROFILE_PROPERTY_PREFERRED_ENCODING,
                                            RfbProto.EncodingTight);
      switch (idx) {
        case RfbProto.EncodingRRE:
          encoding.setSelectedIndex(1);
          break;
        case RfbProto.EncodingCoRRE:
          encoding.setSelectedIndex(2);
          break;
        case RfbProto.EncodingHextile:
          encoding.setSelectedIndex(3);
          break;
        case RfbProto.EncodingZlib:
          encoding.setSelectedIndex(4);
          break;
        case RfbProto.EncodingTight:
          encoding.setSelectedIndex(5);
          break;
        default:
          encoding.setSelectedIndex(0);
          break;
      }
      useCopyRect.setSelected(profile == null ? true :
                              profile.getApplicationPropertyBoolean(
          SshVNCPanel.PROFILE_PROPERTY_USE_COPY_RECT, true));
      compressionLevel.setSelectedIndex(profile == null ? 0 :
                                        profile.getApplicationPropertyInt(
          SshVNCPanel.PROFILE_PROPERTY_COMPRESS_LEVEL, 0));
      jpegQuality.setSelectedIndex(profile == null ? 7 :
                                   profile.getApplicationPropertyInt(
          SshVNCPanel.PROFILE_PROPERTY_JPEG_QUALITY, 7));
      cursorUpdates.setSelectedIndex(profile == null ? 0 :
                                     profile.getApplicationPropertyInt(
          SshVNCPanel.PROFILE_PROPERTY_CURSOR_UPDATES, 0));

      restrictedColors.setSelected(profile == null ? false :
                                   profile.getApplicationPropertyBoolean(
          SshVNCPanel.PROFILE_PROPERTY_EIGHT_BIT_COLORS, false));
      reverseMouseButtons2And3.setSelected(profile == null ? false :
                                           profile.
                                           getApplicationPropertyBoolean(
          SshVNCPanel.PROFILE_PROPERTY_REVERSE_MOUSE_BUTTONS_2_AND_3, false));
      viewOnly.setSelected(profile == null ? false :
                           profile.getApplicationPropertyBoolean(
          SshVNCPanel.PROFILE_PROPERTY_VIEW_ONLY, false));
      shareDesktop.setSelected(profile == null ? false :
                               profile.getApplicationPropertyBoolean(
          SshVNCPanel.PROFILE_PROPERTY_SHARE_DESKTOP, false));

    }

    setAvailableActions();
  }

  public void actionPerformed(ActionEvent evt) {
    setAvailableActions();
  }

  private void setAvailableActions() {
    compressionLevel.setEnabled(encoding.getSelectedIndex() == 4 ||
                                encoding.getSelectedIndex() == 5);
    jpegQuality.setEnabled(encoding.getSelectedIndex() == 5 &&
                           !restrictedColors.isSelected());

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
    return "VNC Advanced";
  }

  public String getTabToolTipText() {
    return "Here you may select advanced VNC settings such " +
        "as the encoding method, bandwidth settings and " +
        "other miscellaneous options";
  }

  public int getTabMnemonic() {
    return 'a';
  }

  public Component getTabComponent() {
    return this;
  }

  public boolean validateTab() {
    return true;
  }

  public void applyTab() {

    int idx = RfbProto.EncodingRaw;
    switch (encoding.getSelectedIndex()) {
      case 1:
        idx = RfbProto.EncodingRRE;
        break;
      case 2:
        idx = RfbProto.EncodingCoRRE;
        break;
      case 3:
        idx = RfbProto.EncodingHextile;
        break;
      case 4:
        idx = RfbProto.EncodingZlib;
        break;
      case 5:
        idx = RfbProto.EncodingTight;
        break;
    }
    profile.setApplicationProperty(SshVNCPanel.
                                   PROFILE_PROPERTY_PREFERRED_ENCODING, idx);
    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_USE_COPY_RECT,
                                   useCopyRect.isSelected());
    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_COMPRESS_LEVEL,
                                   compressionLevel.getSelectedIndex());
    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_JPEG_QUALITY,
                                   jpegQuality.getSelectedIndex());
    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_CURSOR_UPDATES,
                                   cursorUpdates.getSelectedIndex());
    profile.setApplicationProperty(SshVNCPanel.
                                   PROFILE_PROPERTY_EIGHT_BIT_COLORS,
                                   restrictedColors.isSelected());

    profile.setApplicationProperty(SshVNCPanel.
        PROFILE_PROPERTY_REVERSE_MOUSE_BUTTONS_2_AND_3,
        reverseMouseButtons2And3.isSelected());
    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_VIEW_ONLY,
                                   viewOnly.isSelected());
    profile.setApplicationProperty(SshVNCPanel.PROFILE_PROPERTY_SHARE_DESKTOP,
                                   shareDesktop.isSelected());

  }

  public void tabSelected() {
  }

  private void loadOptimizedProfiles() {

    File[] optimizedprofiles = recurseDir(new File(ConfigurationLoader.
        getConfigurationDirectory()));

    if (optimizedprofiles != null) {
      for (int j = 0; j < optimizedprofiles.length; j++) {
        try {
          SshToolsConnectionProfile profile = new SshToolsConnectionProfile();

          profile.open(optimizedprofiles[j].getAbsolutePath());

          if (profile != null) {
            bandwidth.addItem(profile.getApplicationProperty("PROFILE_NAME", null));
          }
        }
        catch (InvalidProfileFileException ex) {
        }
      }
    }
    if (bandwidth.getModel().getSize() == 0) {
      bandwidth.addItem("None found");
      bandwidth.setSelectedIndex(0);
      bandwidth.setEnabled(false);
    }
  }

  private File[] recurseDir(File directory) {

    return directory.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".opt");
      }
    });

  }

}