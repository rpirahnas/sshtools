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
package com.sshtools.tunnel;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;

public class TunnelEditorPane
    extends JPanel
    implements ActionListener {
  private static int id = 1000;
  private NumericTextField bindPort;
  private NumericTextField connectPort;
  private JComboBox bindAddress;
  private XTextField connectHost;
  private XTextField name;
  private CardLayout bindLayout;
  private JPanel bindPanel;
  private XTextField bindRemoteAddress;
  private boolean isLocal = true;

  public TunnelEditorPane() {
    super(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    Insets normal = new Insets(2, 2, 2, 2);
    Insets indented = new Insets(2, 26, 2, 2);
    gbc.insets = normal;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    ButtonGroup type = new ButtonGroup();
    gbc.weightx = 2.0;
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Name "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this, name = new XTextField(getNextAutoId(), 10),
                       gbc, GridBagConstraints.REMAINDER);

    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Connect port "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this,
                       connectPort = new NumericTextField(new Integer(0),
        new Integer(65535), new Integer(0)), gbc,
                       GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Connect host "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this, connectHost = new XTextField("localhost", 10), gbc,
                       GridBagConstraints.REMAINDER);

    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Bind port "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this,
                       bindPort = new NumericTextField(new Integer(0),
        new Integer(65535), new Integer(0)), gbc,
                       GridBagConstraints.REMAINDER);

    //	Build a list of interfaces to bind to
    Vector interaceList = new Vector();

    try {
      interaceList.add("0.0.0.0");
      interaceList.add(InetAddress.getByName("127.0.0.1"));
      interaceList.add(InetAddress.getLocalHost());
    }
    catch (UnknownHostException ex) {
    }

    try {

      // Try to get a list of network interfaces from reflection
      Method method = getClass().forName(
          "java.net.NetworkInterface").getMethod(
          "getNetworkInterfaces", null);

      Enumeration e = (Enumeration) method.invoke(null, null);

      while (e.hasMoreElements()) {
        Object ni = e.nextElement();
        Method method2 = ni.getClass().getMethod("getInetAddresses", null);
        for (Enumeration z = (Enumeration) method2.invoke(ni, null);
             z.hasMoreElements(); ) {
          Object obj = z.nextElement();
          if (!interaceList.contains(obj)) {
            interaceList.add(obj);
          }
        }
      }
    }
    catch (Exception e) {
    }

    gbc.weighty = 1.0;
    gbc.weightx = 0.0;

    bindAddress = new JComboBox(interaceList);
    bindAddress.setEditable(false);
    bindRemoteAddress = new XTextField("0.0.0.0", 10);
    bindPanel = new JPanel();
    bindLayout = new CardLayout();
    bindPanel.setLayout(bindLayout);
    bindPanel.add("local", bindAddress);
    bindPanel.add("remote", bindRemoteAddress);
    UIUtil.jGridBagAdd(this, new JLabel("Bind address "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this, bindPanel, gbc, GridBagConstraints.REMAINDER);
    bindAddress.setRenderer(new InetAddressRenderer());
    setBindPanel();
  }

  public void setMode(boolean isLocal) {
    this.isLocal = isLocal;
    setBindPanel();
  }

  public void actionPerformed(ActionEvent evt) {
    setBindPanel();
  }

  private void setBindPanel() {
    bindLayout.show(bindPanel, isLocal ? "local" : "remote");
  }

  protected String getNextAutoId() {
    return "#".concat(String.valueOf(++id));
  }

  /*public boolean isLocal() {
    return local.isSelected();
     }*/

  /*public boolean isRemote() {
    return remote.isSelected();
     }*/

  public String getForwardName() {
    return name.getText();
  }

  public void setForwardName(String name) {
    this.name.setText(name);
  }

  public int getPortToBind() {
    return ( (Integer) bindPort.getValue()).intValue();
  }

  public void setPortToBind(int port) {
    bindPort.setValue(new Integer(port));
  }

  public String getAddressToBind() {
    try {
      if (bindAddress.getSelectedItem()instanceof InetAddress) {
        return (bindAddress.getSelectedItem() == null)
            ? InetAddress.getLocalHost().getHostAddress()
            : ( (InetAddress) bindAddress.getSelectedItem()).getHostAddress();
      }
      else {
        return bindAddress.getSelectedItem().toString();
      }
    }
    catch (UnknownHostException uhe) {
      return "127.0.0.1";
    }
  }

  public void setAddressToBind(String address) {
    for (int i = 0; i < bindAddress.getModel().getSize(); i++) {
      if (bindAddress.getModel().getElementAt(i)instanceof InetAddress) {
        if ( ( (InetAddress) bindAddress.getModel().getElementAt(i)).
            getHostAddress().equals(address)) {
          bindAddress.setSelectedIndex(i);
        }
      }
      else if (bindAddress.getModel().getElementAt(i).equals(address)) {
        bindAddress.setSelectedIndex(i);
      }
    }

    if (bindAddress.getSelectedIndex() < 0) {
      bindAddress.setSelectedIndex(0);
    }
  }

  public int getPortToConnect() {
    return ( (Integer) connectPort.getValue()).intValue();
  }

  public void setPortToConnect(int port) {
    connectPort.setValue(new Integer(port));
  }

  public String getHostToConnect() {
    return connectHost.getText();
  }

  public void setHostToConnect(String host) {
    connectHost.setText(host);
  }

  public class InetAddressRenderer
      extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
                                         cellHasFocus);

      if (value instanceof InetAddress) {
        InetAddress ni = (InetAddress) value;
        /* makes the dialog too wide
                    StringBuffer buf = new StringBuffer();
                    buf.append(ni.getHostAddress());
                    buf.append(" (");
                    buf.append(ni.getHostName());
                    buf.append(")");
                    setText(buf.toString());
         */
        setText(ni.getHostAddress());
        setToolTipText(ni.getHostName());
      }
      else {
        setText(value.toString());
        setToolTipText(value.toString());
      }

      return this;
    }
  }

}
