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
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.StandardAction;

class AuthenticationFrame
    extends JDialog
    implements ActionListener {

  JPasswordField password;
  JButton okButton, cancelButton;
  boolean cancelled;

  //
  // Constructor.
  //

  AuthenticationFrame(Frame parent) {
    super(parent, "VNC Authentication", true);
    init();
  }

  //
  // Constructor.
  //

  AuthenticationFrame(Dialog parent) {
    super(parent, "VNC Authentication", true);
    init();
  }

  //
  // Constructor.
  //

  AuthenticationFrame() {
    super( (Frame)null, "VNC Authentication", true);
    init();
  }

  private void init() {

    JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
    b.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    okButton = new JButton("Ok");
    okButton.setMnemonic('o');
    okButton.addActionListener(this);
    okButton.setDefaultCapable(true);
    b.add(okButton);

    cancelButton = new JButton("Cancel");
    cancelButton.setMnemonic('c');
    cancelButton.addActionListener(this);
    b.add(cancelButton);

    JPanel t = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
    t.add(new JLabel("Password "));
    t.add(password = new JPasswordField(15));

    // Set the default action for the hitting of the return key
    password.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "Enter");
    password.getActionMap().put("Enter", new SendPasswordAction());

    IconWrapperPanel w = new IconWrapperPanel(
        new ResourceIcon("/com/sshtools/sshvnc/largevnc.png"), t);
    w.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(w, BorderLayout.CENTER);
    getContentPane().add(b, BorderLayout.SOUTH);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    pack();
  }

  public char[] doAuthentication() {
    cancelled = true;
    setVisible(true);
    return getPassword();
  }

  private char[] getPassword() {
    return cancelled ? null : password.getPassword();
  }

  public JButton getDefaultButton() {
    return okButton;
  }

  public void actionPerformed(ActionEvent evt) {
    cancelled = evt.getSource() == cancelButton;
    setVisible(false);
  }

  class SendPasswordAction
      extends StandardAction {

    private final static String NAME_CONFIG = "OK";

    public SendPasswordAction() {
      putValue(Action.NAME, NAME_CONFIG);
    }

    public void actionPerformed(ActionEvent evt) {
      cancelled = evt.getSource() == cancelButton;
      setVisible(false);
    }
  }
}
