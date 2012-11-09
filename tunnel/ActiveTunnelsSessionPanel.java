package com.sshtools.tunnel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsApplication;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.ui.SshToolsApplicationPanel.ActionMenu;
import com.sshtools.common.ui.SshToolsApplicationSessionPanel;
import com.sshtools.common.ui.SshToolsConnectionTab;
import com.sshtools.common.ui.StandardAction;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.forwarding.ForwardingClient;
import com.sshtools.j2ssh.forwarding.ForwardingConfiguration;

public class ActiveTunnelsSessionPanel
    extends SshToolsApplicationSessionPanel {

  private TunnelingPropertiesPanel settings = new TunnelingPropertiesPanel();
  private PortForwardingPane pf = new PortForwardingPane(this);
  private ToggleActiveChannelsAction toggleActiveAction;
  private EditTunnelsAction editTunnelsAction;
  private NewTunnelAction newTunnelAction;
  private RemoveAction removeAction;
  private ResourceIcon icon = new
      ResourceIcon(ActiveTunnelsSessionPanel.class, "forward.png");
  private SshToolsConnectionTab[] tabs = new SshToolsConnectionTab[] {
      settings};
  protected StartAction startAction = new StartAction();
  protected StopAction stopAction = new StopAction();
  protected StopImmediatelyAction stopImmediatelyAction = new StopImmediatelyAction();

  public ActiveTunnelsSessionPanel() {
    super(new BorderLayout());
  }

  public String getId() {
    return "tunneling";
  }

  public boolean requiresConfiguration() {
    return false;
  }

  public void init(SshToolsApplication application) throws
      SshToolsApplicationException {

    super.init(application);

    add(pf, BorderLayout.CENTER);

    //  Create the action menu groups
    initActions();

    // Remove stuff we dont want
    deregisterAction(getAction("Options"));
    setActionVisible("New Window", false);
    setActionVisible("About", false);

    pf.table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if ( (evt.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
          setAvailableActions();
          getContextMenu().show(pf.table, evt.getX(), evt.getY());
        }

        setAvailableActions();
      }

      public void mouseReleased(MouseEvent evt) {
        setAvailableActions();
      }
    });

    setAvailableActions();
  }

  protected void initActions() {
    int i = JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

    registerActionMenu(new ActionMenu("Tunnel", "Tunnel", 't', 30));

    editTunnelsAction = new EditTunnelsAction();
    newTunnelAction = new NewTunnelAction();
    removeAction = new RemoveAction();
    toggleActiveAction = new ToggleActiveChannelsAction();

    pf.table.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_E,
        KeyEvent.ALT_MASK, false), "Edit");
    pf.table.getActionMap().put("Edit", editTunnelsAction);

    pf.table.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
        KeyEvent.ALT_MASK, false), "Stop");
    pf.table.getActionMap().put("Stop", stopAction);

    pf.table.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_T,
        KeyEvent.ALT_MASK, false), "Start");
    pf.table.getActionMap().put("Start", startAction);

    pf.table.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_N,
        KeyEvent.ALT_MASK, false), "New Tunnel");
    pf.table.getActionMap().put("New Tunnel", newTunnelAction);

    pf.table.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_R,
        KeyEvent.ALT_MASK, false), "Remove Tunnel");
    pf.table.getActionMap().put("Remove Tunnel", removeAction);

    pf.table.getInputMap(i).put(KeyStroke.getKeyStroke(KeyEvent.VK_C,
        KeyEvent.ALT_MASK, false), "Close Tunnel");
    pf.table.getActionMap().put("Close Tunnel", removeAction);


    registerAction(newTunnelAction);
    registerAction(editTunnelsAction);
    registerAction(startAction);
    registerAction(stopAction);
    registerAction(removeAction);
    registerAction(toggleActiveAction);
    registerAction(stopImmediatelyAction);
  }

  public SshToolsConnectionTab[] getAdditionalConnectionTabs() {
    return tabs;
  }

  public void addEventListener(ChannelEventListener eventListener) {

  }

  public void setAvailableActions() {

    if (pf.table.getSelectedRowCount() == 0 || pf.table.getRowCount() == 0) {
      startAction.setEnabled(false);
      stopAction.setEnabled(false);
      stopImmediatelyAction.setEnabled(false);
      removeAction.setEnabled(false);
      editTunnelsAction.setEnabled(false);
    }
    else {
      if (pf.table.getSelectedRowCount() > 1) {
        // If multiple selection then enable start/stop buttons as necessary
        for (int i = 0; i < pf.table.getSelectedRowCount(); i++) {
          if (pf.model.getForwardingConfigurationAt(i).isForwarding()) {
            stopAction.setEnabled(true);
            stopImmediatelyAction.setEnabled(true);
          }
          else {
            startAction.setEnabled(true);
          }
        }
      }
      else {
        startAction.setEnabled(!pf.isSelectedTunnelStarted());
        stopAction.setEnabled(pf.isSelectedTunnelStarted());
        stopImmediatelyAction.setEnabled(pf.isSelectedTunnelStarted());
        removeAction.setEnabled(!pf.isSelectedTunnelStarted());
        editTunnelsAction.setEnabled(!pf.isSelectedTunnelStarted());
      }
    }
  }

  public void close() {
    // DO NOT DISCONNECT SINCE THIS PANEL IS ONLY INFORMATIONAL
  }

  public ResourceIcon getIcon() {
    return icon;
  }

  public boolean canClose() {
    return true;
  }

  public boolean onOpenSession() throws java.io.IOException {
    // Get the forwarding client and list the forwardings in some panels
    ForwardingClient fwd = manager.getForwardingClient();

    // Now lets setup the forwarding configurations from the profile if
    // they dont exist already
    pf.setClient(fwd);

    return true;
  }

  private void saveProfile() {
    pf.applyForwardingToProfile(getCurrentConnectionProfile());
    manager.applyProfileChanges(getCurrentConnectionProfile());
  }

  //  Actions
  class StartAction
      extends StandardAction {
    StartAction() {
      putValue(Action.NAME, "Start");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class, "start.png"));
      putValue(Action.SHORT_DESCRIPTION, "Start Tunnel");
      putValue(Action.LONG_DESCRIPTION,
               "Start tunneling on the selected port");
      putValue(Action.MNEMONIC_KEY, new Integer('s'));
      putValue(Action.ACTION_COMMAND_KEY, "start");

      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(45));

      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "Tunnel");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(100));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(10));
      putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(10));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(45));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(10));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(5));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(true));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
      pf.startForwarding();
    }
  }

  class RemoveAction
      extends StandardAction {
    RemoveAction() {
      putValue(Action.NAME, "Delete Tunnel");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class, "remove.png"));
      putValue(Action.SHORT_DESCRIPTION, "Delete tunnel");
      putValue(Action.LONG_DESCRIPTION, "Delete a tunnel");
      putValue(Action.MNEMONIC_KEY, new Integer('r'));
      putValue(Action.ACTION_COMMAND_KEY, "delete");
      putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(15));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(10));
      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "Tunnel");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(150));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(20));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(20));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(20));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(true));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {

      int response = JOptionPane.showConfirmDialog(ActiveTunnelsSessionPanel.this,
          "Are you sure you wish to delete this tunnel?",
          "Confirm Delete",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE);

      if (response != JOptionPane.YES_OPTION) {
        return;
      }

      int r = pf.table.getSelectedRow();

      ForwardingConfiguration conf = pf.model.getForwardingConfigurationAt(r);

      try {
        if (conf instanceof ForwardingClient.ClientForwardingListener) {
          pf.client.removeLocalForwarding(conf
                                          .getName());
        }
        else {
          pf.client.removeRemoteForwarding(conf
                                           .getName());
        }
        saveProfile();
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(ActiveTunnelsSessionPanel.this,
                                      e.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
      finally {
        //pf.updateActiveDisplay();
        pf.model.refresh();
        setAvailableActions();
      }
    }
  }

  class StopAction
      extends StandardAction {
    StopAction() {
      putValue(Action.NAME, "Stop");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class, "stop.png"));
      putValue(Action.SHORT_DESCRIPTION, "Stop Tunnel");
      putValue(Action.LONG_DESCRIPTION,
               "Stop tunneling on the selected port");
      putValue(Action.MNEMONIC_KEY, new Integer('t'));
      putValue(Action.ACTION_COMMAND_KEY, "stop");

      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(45));

      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "Tunnel");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(100));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(10));
      putValue(StandardAction.ON_TOOLBAR, new Boolean(false));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(10));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(45));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(10));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(10));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(true));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {

      int response = JOptionPane.showConfirmDialog(ActiveTunnelsSessionPanel.this,
          "This option will prevent any further tunnel(s) from being created for the selected protocol(s)\n\n" +
          "Are you sure you want to proceed?",
          "Confirm Stop Tunnel",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE);

      if (response == JOptionPane.YES_OPTION) {
        pf.stopForwarding();
      }

    }
  }

  class StopImmediatelyAction
      extends StandardAction {
    StopImmediatelyAction() {
      putValue(Action.NAME, "Close Tunnel");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class, "stop.png"));
      putValue(Action.SHORT_DESCRIPTION, "Close Tunnel");
      putValue(Action.LONG_DESCRIPTION,
               "Stops tunneling immediately on the selected port");
      putValue(Action.MNEMONIC_KEY, new Integer('c'));
      putValue(Action.ACTION_COMMAND_KEY, "stop-now");

      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(50));

      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "Tunnel");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(100));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(15));
      putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(10));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(50));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(10));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(15));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(true));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
      int response = JOptionPane.showConfirmDialog(ActiveTunnelsSessionPanel.this,
          "This will immediately disconnect the selected tunnel(s).\n\n" +
          "Are you sure you want to proceed?",
          "Confirm Close Tunnel",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE);

      if (response == JOptionPane.YES_OPTION) {
        pf.closeForwardingChannel();
      }
    }
  }


  class EditTunnelsAction
      extends StandardAction {
    /**
     * Creates a new ConnectionPropertiesAction object.
     */
    public EditTunnelsAction() {
      putValue(Action.NAME, "Edit Tunnel");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class,
                                "properties.png"));
      putValue(Action.SHORT_DESCRIPTION, "Edit Tunnel");
      putValue(Action.LONG_DESCRIPTION,
               "Edit secure tunneling settings");
      putValue(Action.MNEMONIC_KEY, new Integer('t'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK));
      putValue(Action.ACTION_COMMAND_KEY, "edit-tunnel-command");
      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "Tunnel");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(150));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(10));
      putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(15));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(10));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(true));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(20));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(10));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
      pf.editPortForward();
      saveProfile();
    }
  }

  class NewTunnelAction
      extends StandardAction {
    /**
     * Creates a new ConnectionPropertiesAction object.
     */
    public NewTunnelAction() {
      putValue(Action.NAME, "New Tunnel");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class,
                                "add.png"));
      putValue(Action.SHORT_DESCRIPTION, "New Tunnel");
      putValue(Action.LONG_DESCRIPTION,
               "Create a new secure tunnel");
      putValue(Action.MNEMONIC_KEY, new Integer('n'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK));
      putValue(Action.ACTION_COMMAND_KEY, "new-tunnel-command");
      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "Tunnel");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(50));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(10));
      putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(5));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(5));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(false));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(true));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(5));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(5));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
      pf.addPortForward();
      saveProfile();
    }
  }

  class ToggleActiveChannelsAction
      extends StandardAction {
    /**
     * Creates a new ConnectionPropertiesAction object.
     */
    public ToggleActiveChannelsAction() {
      putValue(Action.NAME, "Show Connections");
      putValue(Action.SMALL_ICON,
               new ResourceIcon(ActiveTunnelsSessionPanel.class,
                                "drilldown.png"));
      putValue(Action.SHORT_DESCRIPTION, "Toggle Display");
      putValue(Action.LONG_DESCRIPTION,
               "Toggles the active channel display");
      putValue(Action.MNEMONIC_KEY, new Integer('a'));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.ALT_MASK));
      putValue(Action.ACTION_COMMAND_KEY, "toggle-active-command");
      putValue(StandardAction.ON_MENUBAR, new Boolean(true));
      putValue(StandardAction.MENU_NAME, "View");
      putValue(StandardAction.MENU_ITEM_GROUP, new Integer(40));
      putValue(StandardAction.MENU_ITEM_WEIGHT, new Integer(10));
      putValue(StandardAction.ON_TOOLBAR, new Boolean(true));
      putValue(StandardAction.TOOLBAR_GROUP, new Integer(25));
      putValue(StandardAction.TOOLBAR_WEIGHT, new Integer(5));
      putValue(StandardAction.ON_CONTEXT_MENU, new Boolean(false));
      putValue(StandardAction.HIDE_TOOLBAR_TEXT, new Boolean(false));
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(5));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(5));
      putValue(StandardAction.IS_TOGGLE_BUTTON, new Boolean(true));
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
      if (String.valueOf(this.getValue(Action.NAME)).equals("Show Connections")) {
        putValue(Action.NAME, "Hide Connections");
      }
      else {
        putValue(Action.NAME, "Show Connections");
      }
      pf.toggleActiveChannelDisplay();
    }
  }
}