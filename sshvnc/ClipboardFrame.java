//
//  Copyright (C) 2001 HorizonLive.com, Inc.  All Rights Reserved.
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
// Clipboard frame.
//

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sshtools.common.ui.IconWrapperPanel;
import com.sshtools.common.ui.ResourceIcon;

class ClipboardFrame
    extends JDialog
    implements WindowListener, ActionListener, DocumentListener {

  JTextArea textArea;
  JButton clearButton, closeButton;
  String selection;
  SshVNCViewer viewer;

  //
  // Constructor.
  //

  ClipboardFrame(Frame parent, SshVNCViewer v) {
    super(parent, "VNC Clipboard", false);
    init(v);
  }

  //
  // Constructor.
  //

  ClipboardFrame(Dialog parent, SshVNCViewer v) {
    super(parent, "VNC Clipboard", false);
    init(v);
  }

  //
  // Constructor.
  //

  ClipboardFrame(SshVNCViewer v) {
    super( (Frame)null, "VNC Clipboard", false);
    init(v);
  }

  private void init(SshVNCViewer v) {
    viewer = v;

    JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
    b.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    closeButton = new JButton("Close");
    closeButton.setMnemonic('c');
    closeButton.addActionListener(this);
    closeButton.setDefaultCapable(true);
    b.add(closeButton);

    clearButton = new JButton("Clear");
    clearButton.setMnemonic('l');
    clearButton.addActionListener(this);
    b.add(clearButton);

    JPanel t = new JPanel(new BorderLayout());
    textArea = new JTextArea(8, 30);
    textArea.getDocument().addDocumentListener(this);
    t.add(new JScrollPane(textArea), BorderLayout.CENTER);

    IconWrapperPanel w = new IconWrapperPanel(
        new ResourceIcon("/com/sshtools/sshvnc/largeclipboard.png"), t);
    w.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(w, BorderLayout.CENTER);
    getContentPane().add(b, BorderLayout.SOUTH);

    pack();
    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    addWindowListener(this);
  }

  public void insertUpdate(DocumentEvent e) {
    updateClipboard();
  }

  public void removeUpdate(DocumentEvent e) {
    updateClipboard();
  }

  public void changedUpdate(DocumentEvent e) {
    updateClipboard();
  }

  private void updateClipboard() {
    if (selection != null && !selection.equals(textArea.getText())) {
      selection = textArea.getText();
      viewer.setCutText(selection);
    }
  }

  public void setCutText(String text) {
    selection = text;
    textArea.setText(text);
    if (isVisible()) {
      textArea.selectAll();
    }
  }

  public JButton getDefaultButton() {
    return closeButton;
  }

  public void windowClosing(WindowEvent evt) {
    setVisible(false);
  }

  public void windowDeactivated(WindowEvent evt) {}

  public void windowActivated(WindowEvent evt) {}

  public void windowOpened(WindowEvent evt) {}

  public void windowClosed(WindowEvent evt) {}

  public void windowIconified(WindowEvent evt) {}

  public void windowDeiconified(WindowEvent evt) {}

  //
  // Respond to button presses
  //

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == clearButton) {
      textArea.setText("");
    }
    else if (evt.getSource() == closeButton) {
      setVisible(false);
    }
  }
}