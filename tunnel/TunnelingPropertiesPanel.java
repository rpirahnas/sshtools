/*
 *  My3SP
 *
 *  Copyright (C) 2003 3SP LTD. All Rights Reserved
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.tunnel;

import java.util.Iterator;
import java.util.Vector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import com.sshtools.common.configuration.SshToolsConnectionProfile;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;

/**
 * The panel where the user edit the list of incoming and outgoing tunnel. This
 * is the main panel under the tab Tunneling in the settings dialog.
 *
 * @author Yanick Belanger
 */
public class TunnelingPropertiesPanel
    extends JPanel
    implements SshToolsConnectionTab {
  final static int INCOMING = 0;
  final static int OUTGOING = 1;

  private SshToolsConnectionProfile profile = null;
  private TunnelsListPanel outgoingPanel = null;
  private TunnelsListPanel incomingPanel = null;

  private ResourceIcon icon = new ResourceIcon(TunnelingPropertiesPanel.class,
                                               "");
  private boolean initialized = false;

  private Vector outgoingTunnels = new Vector();
  private Vector incomingTunnels = new Vector();

  /**
   * Creates a new TunnelingPanel object.
   *
   * @param profile
   * @param wizard
   */
  public TunnelingPropertiesPanel() {
    init();
  }

  /**
   *
   */
  public void init() {
    setLayout(new GridLayout(2, 1));

    outgoingPanel = new TunnelsListPanel(OUTGOING, outgoingTunnels,
                                         "Outgoing tunnels");
    outgoingPanel.init();
    add(outgoingPanel);

    incomingPanel = new TunnelsListPanel(INCOMING, incomingTunnels,
                                         "Incoming tunnels");
    incomingPanel.init();
    add(incomingPanel);

    this.setPreferredSize(new Dimension(400, 300));
    initialized = true;
  }

  /**
   *
   *
   * @return
   */
  public Insets getInsets() {
    return new Insets(5, 5, 5, 5);
  }

  /**
   *
   *
   * @return
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   *
   */
  public void refreshControls() {
    outgoingPanel.refreshControls();
    incomingPanel.refreshControls();
  }

  /**
   *
   */
  public void populateControls() {
    refreshControls();
  }

  /**
   *
   */
  public void populateProperties() {
  }

  /**
   *
   *
   * @return
   */
  public String validateProperties() {
    return null;
  }

  /**
   * This is a generic tunnel list panel that can handle both incoming and
   * outgoing tunnel list.
   *
   * @author Yanick Belanger
   */
  public class TunnelsListPanel
      extends JPanel {
    private int direction;
    private String stringDirection;
    private String title;
    private JTable tunnelTable;
    private java.util.List tunnels;
    JScrollPane tunnelScrollPane;
    JButton addBtn;
    JButton editBtn;
    JButton removeBtn;

    public TunnelsListPanel(int direction, java.util.List tunnels, String title) {
      this.direction = direction;
      this.tunnels = tunnels;

      if (direction == OUTGOING) {
        stringDirection = "outgoing";
      }
      else {
        stringDirection = "incoming";
      }

      if (title != null) {
        this.title = title;
      }
      else {
        this.title = "Tunneling";
      }
    }

    public void init() {
      setLayout(new BorderLayout());
      setBorder(new TitledBorder(BorderFactory.createEtchedBorder(), title));

      tunnelTable = new JTable(new TunnelsModel());

      TableColumnModel colModel = tunnelTable.getColumnModel();

      colModel.getColumn(0).setPreferredWidth(140);
      colModel.getColumn(1).setPreferredWidth(100);
      colModel.getColumn(2).setPreferredWidth(200);

      tunnelTable.setShowGrid(false);
      tunnelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      tunnelTable.getSelectionModel().addListSelectionListener(new
          ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          refreshButtons();
        }
      });

      tunnelTable.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          if ( (e.getClickCount() == 2)
              /*& (e.getButton() == MouseEvent.BUTTON1)*/) {
            doEditBtn();
          }
        }
      });

      tunnelScrollPane = new JScrollPane();
      tunnelScrollPane.getViewport().add(tunnelTable, null);
      add(tunnelScrollPane, BorderLayout.CENTER);

      JPanel eastPanel = new JPanel() {
        public Insets getInsets() {
          return new Insets(0, 5, 0, 0);
        }
      };

      eastPanel.setLayout(new BorderLayout());

      JPanel actionPanel = new JPanel();
      eastPanel.add(actionPanel, BorderLayout.NORTH);

      actionPanel.setLayout(new GridLayout(3, 1, 2, 2));

      addBtn = new JButton("Add...");
      addBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          doAddBtn();
        }
      });
      actionPanel.add(addBtn);

      editBtn = new JButton("Edit...");
      editBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          doEditBtn();
        }
      });
      actionPanel.add(editBtn);

      removeBtn = new JButton("Remove");
      removeBtn.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          doRemoveBtn();
        }
      });
      actionPanel.add(removeBtn);

      add(eastPanel, BorderLayout.EAST);

      refreshButtons();
    }

    public Insets getInsets() {
      return new Insets(20, 14, 14, 14);
    }

    public void refreshControls() {
      tunnelTable.updateUI();
      refreshButtons();
    }

    public void doAddBtn() {

      TunnelEditorPane editor = new TunnelEditorPane();
      editor.setMode(direction == TunnelingPropertiesPanel.OUTGOING);

      int option = JOptionPane.showConfirmDialog(this, editor,
                                                 "Add " +
                                                 (direction ==
                                                  TunnelingPropertiesPanel.OUTGOING ?
                                                  "Outgoing" : "Incoming") +
                                                 " Tunnel",
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE);

      if (option != JOptionPane.CANCEL_OPTION) {
        tunnels.add(new ForwardingConfiguration(editor.getForwardName(),
                                                editor.getAddressToBind(),
                                                editor.getPortToBind(),
                                                editor.getHostToConnect(),
                                                editor.getPortToConnect()));

        refreshControls();
      }
    }

    public void doEditBtn() {

      ForwardingConfiguration cf = getSelectedTunnel();

      TunnelEditorPane editor = new TunnelEditorPane();

      editor.setMode(direction == TunnelingPropertiesPanel.OUTGOING);
      editor.setForwardName(cf.getName());
      editor.setHostToConnect(cf.getHostToConnect());
      editor.setPortToConnect(cf.getPortToConnect());
      editor.setAddressToBind(cf.getAddressToBind());
      editor.setPortToBind(cf.getPortToBind());

      int option = JOptionPane.showConfirmDialog(this, editor,
                                                 "Edit " +
                                                 (direction ==
                                                  TunnelingPropertiesPanel.OUTGOING ?
                                                  "Outgoing" : "Incoming") +
                                                 " Tunnel",
                                                 JOptionPane.OK_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE);

      if (option != JOptionPane.CANCEL_OPTION) {
        tunnels.add(new ForwardingConfiguration(editor.getForwardName(),
                                                editor.getAddressToBind(),
                                                editor.getPortToBind(),
                                                editor.getHostToConnect(),
                                                editor.getPortToConnect()));

        refreshControls();
      }

    }

    public void doRemoveBtn() {

      int selectedRow = tunnelTable.getSelectedRow();
      tunnels.remove(getSelectedTunnel());
      tunnelTable.updateUI();
      tunnelTable.changeSelection(selectedRow - 1, 0, false, false);
      refreshButtons();

    }

    public ForwardingConfiguration getSelectedTunnel() {
      int selectedRow = tunnelTable.getSelectedRow();
      return (ForwardingConfiguration) tunnelTable.getModel().getValueAt(
          selectedRow,
          3);
    }

    public void refreshButtons() {
      boolean hasRowSelected = (tunnelTable.getSelectedRow() >= 0)
          && (tunnelTable.getModel().getRowCount() > 0);
      editBtn.setEnabled(hasRowSelected);
      removeBtn.setEnabled(hasRowSelected);
    }

    private class TunnelsModel
        extends AbstractTableModel {
      final String[] columnNames = {
          "Name", "Listening on", "Forwarded to"};

      public int getColumnCount() {
        return columnNames.length;
      }

      public int getRowCount() {
        return tunnels.size();
      }

      public String getColumnName(int col) {
        return columnNames[col];
      }

      public Object getValueAt(int row, int col) {
        ForwardingConfiguration fc = getTunnelAt(row);

        switch (col) {
          case 0:
            return fc.getName();

          case 1:
            return fc.getAddressToBind() + ":" +
                String.valueOf(fc.getPortToBind());

          case 2:
            return fc.getHostToConnect() + ":"
                + fc.getPortToConnect();

          case 3:
            return fc;
        }

        return null;
      }

      // Returns the tunnel for the selected row
      private ForwardingConfiguration getTunnelAt(int row) {
        return (ForwardingConfiguration) tunnels.get(row);
      }

    }
  }

  public void setConnectionProfile(SshToolsConnectionProfile profile) {
    this.profile = profile;

    // Clear the existing tunnels
    outgoingTunnels.clear();
    incomingTunnels.clear();

    // Get the existing tunnels from the profile
    Iterator it;

    // Add the local forwardings
    it = profile.getLocalForwardings().values().iterator();
    while (it.hasNext()) {
      ForwardingConfiguration config = (ForwardingConfiguration) it
          .next();
      outgoingTunnels.add(config);
    }

    // Add the remote forwardings
    it = profile.getRemoteForwardings().values().iterator();
    while (it.hasNext()) {
      ForwardingConfiguration config = (ForwardingConfiguration) it
          .next();
      incomingTunnels.add(config);
    }
  }

  public SshToolsConnectionProfile getConnectionProfile() {
    return profile;
  }

  public String getTabContext() {
    return "Options";
  }

  public Icon getTabIcon() {
    return icon;
  }

  public String getTabTitle() {
    return "Secure Tunneling";
  }

  public String getTabToolTipText() {
    return "Here you may configure secure TCP/IP tunneling properties to allow " +
        "you to make use of SSH port forwarding";
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

    profile.setApplicationProperty("tunneling.configured", true);

    // Clear out the profiles tunnels
    profile.removeAllForwardings();

    for (Iterator it = outgoingTunnels.iterator(); it.hasNext(); ) {
      profile.addLocalForwarding( (ForwardingConfiguration) it.next());
    }
    for (Iterator it = incomingTunnels.iterator(); it.hasNext(); ) {
      profile.addRemoteForwarding( (ForwardingConfiguration) it.next());
    }

  }

  public void tabSelected() {

  }
}
