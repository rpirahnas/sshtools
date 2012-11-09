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
import javax.swing.JRadioButton;

import com.sshtools.common.ui.NumericTextField;
import com.sshtools.common.ui.UIUtil;
import com.sshtools.common.ui.XTextField;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;

public class PortForwardEditorPane
    extends JPanel
    implements ActionListener {
  private static int id = 1000;
  //  Private instance variables
  private JRadioButton local;
  //  Private instance variables
  private JRadioButton remote;
  private NumericTextField localPort;
  private NumericTextField remotePort;
  private JComboBox bindAddress;
  private XTextField host;
  private XTextField name;
  private CardLayout bindLayout;
  private JPanel bindPanel;
  private XTextField bindRemoteAddress;
  private ForwardingConfiguration config;

  public PortForwardEditorPane() {
    super(new GridBagLayout());
    init();
  }

  public PortForwardEditorPane(ForwardingConfiguration config) {
    super(new GridBagLayout());
    this.config = config;
    init();

    // Populate the textfields from the ForwardingConfiguration object
    name.setText(config.getName());
    localPort.setValue(new Integer(config.getPortToBind()));
    remotePort.setValue(new Integer(config.getPortToConnect()));
    host.setText(config.getHostToConnect());
    bindAddress.setSelectedItem(config.getAddressToBind());
  }

  void init() {

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    Insets normal = new Insets(2, 2, 2, 2);
    Insets indented = new Insets(2, 26, 2, 2);
    gbc.insets = normal;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    ButtonGroup type = new ButtonGroup();
    local = new JRadioButton("Outgoing", true);
    local.setMnemonic('o');
    local.addActionListener(this);
    gbc.weightx = 2.0;
    type.add(local);
    UIUtil.jGridBagAdd(this, local, gbc, GridBagConstraints.REMAINDER);
    remote = new JRadioButton("Incoming");
    remote.setMnemonic('i');
    remote.addActionListener(this);
    type.add(remote);
    UIUtil.jGridBagAdd(this, remote, gbc, GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Name "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this, name = new XTextField(getNextAutoId(), 10),
                       gbc, GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Local port "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this,
                       localPort = new NumericTextField(new Integer(0),
        new Integer(65535), new Integer(0)), gbc,
                       GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Remote port "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this,
                       remotePort = new NumericTextField(new Integer(0),
        new Integer(65535), new Integer(0)), gbc,
                       GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(this, new JLabel("Destination Host "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this, host = new XTextField("localhost", 10), gbc,
                       GridBagConstraints.REMAINDER);
    //	Build a list of interfaces to bind to
    Vector interaceList = new Vector();
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
          interaceList.add(z.nextElement());
        }
      }
    }
    catch (Exception e) {
      try {
        interaceList.add(InetAddress.getLocalHost());
        interaceList.add(InetAddress.getByName("127.0.0.1"));
      }
      catch (UnknownHostException ex) {
      }
    }

    gbc.weighty = 1.0;
    gbc.weightx = 0.0;
    bindAddress = new JComboBox(interaceList);
    bindRemoteAddress = new XTextField("0.0.0.0", 10);
    bindPanel = new JPanel();
    bindLayout = new CardLayout();
    bindPanel.setLayout(bindLayout);
    bindPanel.add("local", bindAddress);
    bindPanel.add("remote", bindRemoteAddress);
    UIUtil.jGridBagAdd(this, new JLabel("Bind to "), gbc,
                       GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(this, bindPanel, gbc, GridBagConstraints.REMAINDER);
    bindAddress.setRenderer(new InetAddressRenderer());
    setBindPanel();
  }

  public void actionPerformed(ActionEvent evt) {
    setBindPanel();
  }

  private void setBindPanel() {
    bindLayout.show(bindPanel, local.isSelected() ? "local" : "remote");
  }

  protected String getNextAutoId() {
    return "#".concat(String.valueOf(++id));
  }

  public boolean isLocal() {
    return local.isSelected();
  }

  public boolean isRemote() {
    return remote.isSelected();
  }

  public String getForwardName() {
    return name.getText();
  }

  public int getLocalPort() {
    return ( (Integer) localPort.getValue()).intValue();
  }

  public String getBindAddress() {
    try {
      return (bindAddress.getSelectedItem() == null)
          ? InetAddress.getLocalHost().getHostName()
          : ( (InetAddress) bindAddress.getSelectedItem()).getHostName();
    }
    catch (UnknownHostException uhe) {
      return "127.0.0.1";
    }
  }

  public int getRemotePort() {
    return ( (Integer) remotePort.getValue()).intValue();
  }

  public String getHost() {
    return host.getText();
  }

  public class InetAddressRenderer
      extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
                                                  int index, boolean isSelected,
                                                  boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
                                         cellHasFocus);
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
      return this;
    }
  }
}
