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
import java.util.Map;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.sshtools.j2ssh.forwarding.ForwardingClient;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;
import com.sshtools.j2ssh.util.StartStopState;

public class PortForwardingModel
    extends AbstractTableModel {
  //  Private instance variables
  private Vector forwards;
  private ForwardingClient client;
  public PortForwardingModel() {
    forwards = new Vector();
  }

  public Class getColumnClass(int c) {
    switch (c) {
      case 0:
        return StartStopState.class;
      case 1:
        return String.class;
      case 2:
        return String.class;
      case 3:
        return String.class;
      case 4:
        return String.class;
      default:
        return null; //Integer.class;
    }
  }

  public void setClient(ForwardingClient client) {
    this.client = client;
    refresh();
  }

  public void refresh() {
    forwards.removeAllElements();

    // Add the forwards and sort by status
    if (client != null) {
      for (int it = 0; it < 3; it++) {
        Map map = client.getLocalForwardings();
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
          String key = (String) i.next();
          ForwardingConfiguration f = (ForwardingConfiguration) map.get(key);
          if (f.getState().getValue() == StartStopState.STARTED && it == 0) {
            forwards.addElement(map.get(key));
          }
          if (f.getState().getValue() == StartStopState.STOPPED && it == 1) {
            forwards.addElement(map.get(key));
          }
          if (f.getState().getValue() == StartStopState.FAILED && it == 2) {
            forwards.addElement(map.get(key));
          }
        }
        map = client.getRemoteForwardings();
        for (Iterator i = map.keySet().iterator(); i.hasNext(); ) {
          String key = (String) i.next();
          ForwardingConfiguration f = (ForwardingConfiguration) map.get(key);
          if (f.getState().getValue() == StartStopState.STARTED && it == 0) {
            forwards.addElement(map.get(key));
          }
          if (f.getState().getValue() == StartStopState.STOPPED && it == 1) {
            forwards.addElement(map.get(key));
          }
          if (f.getState().getValue() == StartStopState.FAILED && it == 2) {
            forwards.addElement(map.get(key));
          }
        }
        ForwardingConfiguration x11 = client.getX11ForwardingConfiguration();
        if (x11 != null) {
          forwards.addElement(x11);
        }
      }
    }
    fireTableDataChanged();
  }

  public String getColumnName(int c) {
    switch (c) {
      case 0:
        return " "; //Status
      case 1:
        return "Name";
      case 2:
        return "Type";
      case 3:
        return "Listening On";
      case 4:
        return "Forwarded To";
      default:
        return null;
    }
  }

  public int getColumnCount() {
    return 5;
  }

  public int getRowCount() {
    return forwards.size();
  }

  public ForwardingConfiguration getForwardingConfigurationAt(int r) {
    return (ForwardingConfiguration) forwards.elementAt(r);
  }

  public Object getValueAt(int r, int c) {
    ForwardingConfiguration conf = getForwardingConfigurationAt(r);
    switch (c) {
      case 0:
        return conf.getState();
      case 1:
        return conf.getName();
      case 2:
        if (conf instanceof ForwardingClient.ClientForwardingListener) {
          return "Outgoing";
        }
        else {
          return "Incoming";
        }
      case 3:
        return new String(conf.getAddressToBind() + ":" +
                          String.valueOf(conf.getPortToBind()));
      case 4:
        return new String(conf.getHostToConnect() + ":" +
                          String.valueOf(conf.getPortToConnect()));
      default:
        return null;
    }
  }
}
