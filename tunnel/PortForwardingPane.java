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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import com.sshtools.j2ssh.connection.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.j2ssh.forwarding.*;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;
import com.sshtools.j2ssh.forwarding.ForwardingConfigurationException;
import com.sshtools.j2ssh.util.StartStopState;

public class PortForwardingPane
    extends JPanel {

  protected final static Log log = LogFactory.getLog(PortForwardingPane.class);
  SshToolsConnectionProfile profile;
  protected PortForwardingTable table;
  protected ForwardingClient client;
  protected PortForwardingModel model;
  private boolean activeChannelDisplay = true;
  private ActiveChannelPane active;
  private JSplitPane split;
  private ActiveTunnelsSessionPanel sessionPanel;

  public PortForwardingPane(ActiveTunnelsSessionPanel sessionPanel) {
    super(new BorderLayout());

    this.sessionPanel = sessionPanel;

    JPanel north = new JPanel(new BorderLayout());
    table = new PortForwardingTable(model = new PortForwardingModel());

    table.setShowGrid(false);
    table.setShowHorizontalLines(false);
    table.setShowVerticalLines(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setColumnSelectionAllowed(false);

    table.getColumnModel().getColumn(0).setMaxWidth(25);
    table.getColumnModel().getColumn(0).setMinWidth(25);
    table.getColumnModel().getColumn(2).setMaxWidth(60);
    table.getColumnModel().getColumn(2).setMinWidth(60);
    table.getColumnModel().getColumn(3).setMaxWidth(150);
    table.getColumnModel().getColumn(3).setMinWidth(80);
    table.getColumnModel().getColumn(4).setMaxWidth(150);
    table.getColumnModel().getColumn(4).setMinWidth(80);

    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

    //  Scroller for table
    JScrollPane scroller = new JScrollPane(table) {
      public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 164);
      }
    };

    //  Create the active forward pane
    active = new ActiveChannelPane();
    //  Create the top panel
    JPanel top = new JPanel(new BorderLayout());
    top.add(north, BorderLayout.NORTH);
    top.add(scroller, BorderLayout.CENTER);
    //  Create the split pane for forward selection / active channels
    split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, active);
    split.setOneTouchExpandable(true);
    split.setDividerSize(7);
    //  Create the panel
    add(split, BorderLayout.CENTER);

    // Hide the active channel display initially
    toggleActiveChannelDisplay();

    //  Initialise
    setClient(null);
  }

  protected boolean isSelectedTunnelStarted() {
    if (table.getSelectedRow() > -1) {
      try {
        ForwardingConfiguration conf = model.getForwardingConfigurationAt(table.
            getSelectedRow());
        if (conf.isForwarding()) {
          return true;
        }
        else {
          return false;
        }
      }
      catch (Exception e) {
      }
    }
    return false;
  }

  public void applyForwardingToProfile(SshToolsConnectionProfile profile) {

    Iterator it = client.getLocalForwardings().values().iterator();
    profile.removeAllForwardings();

    while (it.hasNext()) {
      ForwardingConfiguration config = (ForwardingConfiguration) it.next();
      profile.addLocalForwarding(config);
    }

    it = client.getRemoteForwardings().values().iterator();
    while (it.hasNext()) {
      ForwardingConfiguration config = (ForwardingConfiguration) it.next();
      if (!config.getName().equals("X")) {
        profile.addRemoteForwarding(config);
      }
    }

    //updateActiveDisplay();
  }

  public void setDividerLocation(double div) {
    split.setDividerLocation(div);
  }

  public void setDividerLocation(int div) {
    split.setDividerLocation(div);
  }

  public int getDividerLocation() {
    return split.getDividerLocation();
  }

  public ActiveChannelPane getActiveChannelPane() {
    return active;
  }

  public PortForwardingTable getPortForwardingTable() {
    return table;
  }

  public void setClient(ForwardingClient client) {
    this.client = client;
    model.setClient( (client == null) ? null : client);
    table.repaint();
    active.setConfiguration(null);
    setAvailableActions();
    updateActiveDisplay();
  }

  public void refreshTable() {
    model.refresh();
    table.repaint();
    updateActiveDisplay();
  }

  /*
   * Set what actions are available applicable to the current state
   */
  private void setAvailableActions() {

  }

  protected void toggleActiveChannelDisplay() {
    if (activeChannelDisplay) {
      split.remove(active);
      activeChannelDisplay = false;
    }
    else {
      split.add(active);
      split.setDividerLocation(sessionPanel.getHeight() / 2);
      activeChannelDisplay = true;
    }
  }

  protected void addPortForward() {
    PortForwardEditorPane editor = new PortForwardEditorPane();
    int option =  JOptionPane.showConfirmDialog(this, editor,
                                               "Add New Tunnel",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.QUESTION_MESSAGE);
    if (option != JOptionPane.CANCEL_OPTION) {
      try {
        ForwardingClient forwardingClient = client; //.getForwardingClient();
        String id = editor.getForwardName();
        if (id.equals("x11")) {
          throw new Exception("The id of x11 is reserved.");
        }
        int i = model.getRowCount();
        if (editor.isLocal()) {
          ForwardingConfiguration f = forwardingClient.addLocalForwarding(id,
                                              editor.getBindAddress(),
                                              editor.getLocalPort(),
                                              editor.getHost(),
                                              editor.getRemotePort());
          forwardingClient.startLocalForwarding(id);
          active.addConfiguration(f);
        }
        else {
          forwardingClient.addRemoteForwarding(id,
                                               editor.getBindAddress(),
                                               editor.getLocalPort(),
                                               editor.getHost(),
                                               editor.getRemotePort());
          forwardingClient.startRemoteForwarding(id);
          //active.addConfiguration(f);
        }

        if (i < model.getRowCount()) {
          table.getSelectionModel().addSelectionInterval(i, i);
        }
      }
      catch (Exception e) {
        sessionPanel.setAvailableActions();
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
      finally {
        model.refresh();
        sessionPanel.setAvailableActions();
      }
    }
  }

  protected void editPortForward() {

    ForwardingConfiguration config = model.getForwardingConfigurationAt(table.
        getSelectedRow());
    PortForwardEditorPane editor = new PortForwardEditorPane(config);

    int option = JOptionPane.showConfirmDialog(this, editor,
                                               "Edit Tunnel",
                                               JOptionPane.OK_CANCEL_OPTION,
                                               JOptionPane.QUESTION_MESSAGE);

    if (option != JOptionPane.CANCEL_OPTION) {
      try {
        ForwardingClient forwardingClient = client;
        String id = editor.getForwardName();
        if (id.equals("x11")) {
          throw new Exception("The id of x11 is reserved.");
        }
        int i = model.getRowCount();
        if (editor.isLocal()) {
          forwardingClient.removeLocalForwarding(config.getName());
          forwardingClient.addLocalForwarding(id,
                                              editor.getBindAddress(),
                                              editor.getLocalPort(),
                                              editor.getHost(),
                                              editor.getRemotePort());
          forwardingClient.startLocalForwarding(id);
        }
        else {
          forwardingClient.removeRemoteForwarding(config.getName());
          forwardingClient.addRemoteForwarding(id,
                                               editor.getBindAddress(),
                                               editor.getLocalPort(),
                                               editor.getHost(),
                                               editor.getRemotePort());
          forwardingClient.startRemoteForwarding(id);
        }

        if (i < model.getRowCount()) {
          table.getSelectionModel().addSelectionInterval(i, i);
        }
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
      finally {
        model.refresh();
        sessionPanel.setAvailableActions();
      }
    }
  }

  public void updateActiveDisplay() {
    Vector v = new Vector();

    for (int i = 0; i < table.getRowCount(); i++) {
      v.add(model.getForwardingConfigurationAt(i));
    }

    active.setConfiguration(v);
  }

  public void startForwarding() {

    ForwardingConfiguration conf = null;
    int failCount = 0;
    //Starts forwarding for each selected row
    for (int i = 0; i < table.getRowCount(); i++) {
      if (table.isRowSelected(i)) {
        try {
          conf = model.getForwardingConfigurationAt(i);

          if (!conf.isForwarding()) {
            if (conf instanceof ForwardingClient.ClientForwardingListener) {
              client.startLocalForwarding(conf.getName());
            }
            else {
              client.startRemoteForwarding(conf.getName());
            }

            active.addConfiguration(conf);
          }
        }
        catch (ForwardingConfigurationException fce) {
          failCount++;
          sessionPanel.setAvailableActions();
          if (conf != null) {
            conf.getState().setValue(StartStopState.FAILED);
          }
        }
        catch (Exception e) {
          failCount++;
          sessionPanel.setAvailableActions();
          JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    }

    model.refresh();
    sessionPanel.setAvailableActions();

    // If one or more tunnels failed to start then error
    if (failCount > 0) {
      JOptionPane.showMessageDialog(PortForwardingPane.this,
                                    String.valueOf(failCount) +
                                    " tunnel(s) failed to start:  " +
                                    "This port is already in use!",
                                    "Tunneling Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  public void stopForwarding() {
    //Starts forwarding for each selected row
    for (int i = 0; i < table.getRowCount(); i++) {
      if (table.isRowSelected(i)) {
        try {
          ForwardingConfiguration conf = model.getForwardingConfigurationAt(i);
          if (conf.isForwarding()) {
            if (conf instanceof ForwardingClient.ClientForwardingListener) {
              client.stopLocalForwarding(conf.getName());
            }
            else {
              client.stopRemoteForwarding(conf.getName());
            }
          }
        }
        catch (Exception e) {
          sessionPanel.setAvailableActions();
          JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }

      }
    }
    model.refresh();
    sessionPanel.setAvailableActions();
  }

  public void closeForwardingChannel() {
    //Starts forwarding for each selected row
    for (int i = 0; i < table.getRowCount(); i++) {
      if (table.isRowSelected(i)) {
        try {
          ForwardingConfiguration conf = model.getForwardingConfigurationAt(i);

          // Close each channel manually so the forwarding stops immediatelly
          java.util.List activeChannels = conf.getActiveForwardingSocketChannels();
          Iterator it = activeChannels.iterator();

          while (it.hasNext()) {
            Channel channel = (Channel) it.next();

            if (!channel.isClosed()) {
              channel.close();
            }
          }

          // Shut off the forwarding client so new forwardings cannot be established
          if (conf.isForwarding()) {
            if (conf instanceof ForwardingClient.ClientForwardingListener) {
              client.stopLocalForwarding(conf.getName());
            }
            else {
              client.stopRemoteForwarding(conf.getName());
            }
          }
        }
        catch (Exception e) {
          sessionPanel.setAvailableActions();
          JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }

      }
    }
    model.refresh();
    sessionPanel.setAvailableActions();
  }


}