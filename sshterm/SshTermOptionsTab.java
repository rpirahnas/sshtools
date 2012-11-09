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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.OptionsTab;
import com.sshtools.common.ui.PreferencesStore;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.UIUtil;

public class SshTermOptionsTab
    extends JPanel
    implements OptionsTab {
  public final static String TERMINAL_ICON =
      "/com/sshtools/sshterm/largeterminal.png";

  //
  private JComponent mouseWheelIncrement;

  public SshTermOptionsTab() {
    super();

    Insets ins = new Insets(2, 2, 2, 2);

    JPanel s = new JPanel(new GridBagLayout());
    GridBagConstraints gbc1 = new GridBagConstraints();
    gbc1.weighty = 1.0;
    gbc1.insets = ins;
    gbc1.anchor = GridBagConstraints.NORTHWEST;
    gbc1.fill = GridBagConstraints.HORIZONTAL;
    gbc1.weightx = 0.0;
    UIUtil.jGridBagAdd(s, new JLabel("Mouse wheel increment "), gbc1,
                       GridBagConstraints.RELATIVE);
    gbc1.weightx = 1.0;

    /* Mouse wheel support only available in 1.4+. Spinner is also only available in 1.4
     * so this can be used to determine if to support mouse wheels.
     */
    try {
      Class spinnerModelClass = Class.forName("javax.swing.SpinnerModel");
      Class spinnerNumberModelClass = Class.forName(
          "javax.swing.SpinnerNumberModel");
      Constructor spinnerNumberModelConstructor = spinnerNumberModelClass.
          getConstructor(
          new Class[] {int.class, int.class, int.class, int.class});
      Object spinnerNumberModel = spinnerNumberModelConstructor.newInstance(
          new Object[] {new Integer(1), new Integer(1), new Integer(999),
          new Integer(1)});
      Class spinnerClass = Class.forName("javax.swing.JSpinner");
      Constructor spinnerConstructor = spinnerClass.getConstructor(new Class[] {
          spinnerModelClass});
      mouseWheelIncrement = (JComponent) spinnerConstructor.newInstance(new
          Object[] {spinnerNumberModel});
      UIUtil.jGridBagAdd(s, mouseWheelIncrement, gbc1,
                         GridBagConstraints.REMAINDER);
    }
    catch (Throwable t) {
      throw new RuntimeException(
          "Not 1.4?. This exception should be removed if and when new options that " +
          "don't depend on 1.4 are added to this panel");
    }

    IconWrapperPanel w = new IconWrapperPanel(new ResourceIcon(
        TERMINAL_ICON), s);

    //  This tab
    setLayout(new BorderLayout());
    add(w, BorderLayout.CENTER);
    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    reset();
  }

  public void reset() {
    if (mouseWheelIncrement != null) {
      try {
        Method m = mouseWheelIncrement.getClass().getMethod("setValue",
            new Class[] {Integer.class});
        Integer i = new Integer(PreferencesStore.getInt(
            SshTerminalPanel.PREF_MOUSE_WHEEL_INCREMENT, 1));
        m.invoke(mouseWheelIncrement, new Object[] {i});
      }
      catch (Throwable t) {
      }
    }
  }

  public String getTabContext() {
    return "Options";
  }

  public Icon getTabIcon() {
    return null;
  }

  public String getTabTitle() {
    return "Terminal";
  }

  public String getTabToolTipText() {
    return "Terminal options.";
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
    if (mouseWheelIncrement != null) {
      try {
        Method m = mouseWheelIncrement.getClass().getMethod("getValue",
            new Class[] {});
        Integer i = (Integer) m.invoke(mouseWheelIncrement, new Object[] {});
        PreferencesStore.putInt(SshTerminalPanel.PREF_MOUSE_WHEEL_INCREMENT,
                                i.intValue());
      }
      catch (Throwable t) {
      }
    }
  }

  public void tabSelected() {
  }

  class LAFRenderer
      extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
                                         cellHasFocus);
      setText( ( (UIManager.LookAndFeelInfo) value).getName());

      return this;
    }
  }
}