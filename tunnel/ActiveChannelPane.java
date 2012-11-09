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

import java.util.Iterator;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.j2ssh.forwarding.ForwardingChannel;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;
import com.sshtools.j2ssh.forwarding.ForwardingConfigurationListener;
import com.sshtools.j2ssh.forwarding.ForwardingSocketChannel;
import com.sshtools.sshterm.FolderBar;

public class ActiveChannelPane
    extends JPanel
    implements ForwardingConfigurationListener {
  //  Private instance variables
  private ActiveChannelTable table;
  private ActiveChannelModel model;
  private Vector configurationsVector = new Vector();

  public ActiveChannelPane() {
    super(new BorderLayout());
    add(new FolderBar("Active Connections",
                      new ResourceIcon(ActiveChannelPane.class, "forward.png")),
        BorderLayout.NORTH);
    table = new ActiveChannelTable(model = new ActiveChannelModel());
    JScrollPane scroller = new JScrollPane(table) {
      public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 128);
      }
    };
    add(scroller, BorderLayout.CENTER);
    table.getColumnModel().getColumn(0).setMaxWidth(25);
    table.getColumnModel().getColumn(0).setMinWidth(25);
    table.getColumnModel().getColumn(1).setMaxWidth(25);
    table.getColumnModel().getColumn(1).setMinWidth(25);
    table.getColumnModel().getColumn(2).setMaxWidth(100);
    table.getColumnModel().getColumn(2).setMinWidth(50);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
  }

  public ActiveChannelTable getActiveChannelTable() {
    return table;
  }

  public void opened(ForwardingConfiguration config,
                     final ForwardingChannel channel) {
    model.addActiveChannel(channel);
  }

  public void closed(ForwardingConfiguration config,
                     final ForwardingChannel channel) {
    model.removeActiveChannel(channel);
  }

  public void dataReceived(ForwardingConfiguration config,
                           final ForwardingChannel channel, final int bytes) {
    model.dataReceived(channel);
  }

  public void dataSent(ForwardingConfiguration config,
                       final ForwardingChannel channel, final int bytes) {
    model.dataSent(channel);
  }

  public void removeConfiguration(ForwardingConfiguration conf) {

    if (configurationsVector.contains(conf)) {
      java.util.List active = conf.getActiveForwardingSocketChannels();
      conf.removeForwardingConfigurationListener(this);

      for (Iterator i = active.iterator(); i.hasNext(); ) {
        ForwardingSocketChannel channel = (ForwardingSocketChannel) i
            .next();
        model.removeActiveChannel(channel);
      }

      conf.removeForwardingConfigurationListener(this);
      configurationsVector.remove(conf);
    }

  }

  public void addConfiguration(ForwardingConfiguration conf) {
    if (!configurationsVector.contains(conf)) {
      conf.addForwardingConfigurationListener(this);
      configurationsVector.add(conf);
    }
  }

  public void setConfiguration(Vector configurations) {

    configurationsVector.clear();
    model.clear();

    if (configurations == null) {
      return;
    }

    Iterator it = configurations.iterator();

    while (it.hasNext()) {
      ForwardingConfiguration f = (ForwardingConfiguration) it.next();

      java.util.List active = f.getActiveForwardingSocketChannels();
      f.addForwardingConfigurationListener(this);

      for (Iterator i = active.iterator(); i.hasNext(); ) {
        ForwardingSocketChannel channel = (ForwardingSocketChannel) i
            .next();
        model.addActiveChannel(channel);
      }

      configurationsVector.add(f);
    }
  }
}
