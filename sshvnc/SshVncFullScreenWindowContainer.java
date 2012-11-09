package com.sshtools.sshvnc;

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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.ui.AboutAction;
import com.sshtools.common.ui.ExitAction;
import com.sshtools.common.ui.NewWindowAction;
import com.sshtools.common.ui.SshToolsApplication;
import com.sshtools.common.ui.SshToolsApplicationContainer;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationFrame;
import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.ui.StandardAction;

public class SshVncFullScreenWindowContainer
    extends JFrame
    implements SshToolsApplicationContainer {

  private final static long VDU_EVENTS = AWTEvent.KEY_EVENT_MASK
      | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.ACTION_EVENT_MASK
      | AWTEvent.WINDOW_EVENT_MASK /*| AWTEvent.WINDOW_FOCUS_EVENT_MASK*/
      | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK;
  protected StandardAction exitAction;
  protected StandardAction aboutAction;
  protected StandardAction newWindowAction;
  protected StandardAction fullScreenAction;
  protected Log log = LogFactory.getLog(SshToolsApplicationFrame.class);
  //
  private SshToolsApplicationPanel panel;
  private SshToolsApplication application;
  private JSeparator toolSeperator;

  public void init(final SshToolsApplication application,
                   SshToolsApplicationPanel panel) throws
      SshToolsApplicationException {
    getContentPane().invalidate();
    //setUndecorated(true);
    this.panel = panel;
    this.application = application;
    setJMenuBar( ( (SshVNCPanel) panel).getJMenuBar());
    // We dont want the status bar, menu bar or tool bar showing in full screen mode by default
    ( (SshVNCPanel) panel).setToolsVisible(false);
    panel.registerActionMenu(new SshToolsApplicationPanel.ActionMenu(
        "File", "File", 'f', 0));
    panel.registerAction(exitAction = new ExitAction(application, this));
    panel.registerAction(newWindowAction = new NewWindowAction(application));
    panel.registerAction(aboutAction = new AboutAction(this, application));
    panel.registerAction(fullScreenAction = new FullScreenActionImpl(
        application, this));

    getApplicationPanel().rebuildActionComponents();
    JPanel p = new JPanel(new BorderLayout());
    if (panel.getToolBar() != null) {
      JPanel t = new JPanel(new BorderLayout());
      t.add(panel.getToolBar(), BorderLayout.NORTH);
      t.add(toolSeperator = new JSeparator(JSeparator.HORIZONTAL),
            BorderLayout.SOUTH);
      final SshToolsApplicationPanel pnl = panel;
      panel.getToolBar().addComponentListener(new ComponentAdapter() {
        public void componentHidden(ComponentEvent evt) {
          log.debug("Tool separator is now "
                    + pnl.getToolBar().isVisible());
          toolSeperator.setVisible(pnl.getToolBar().isVisible());
        }
      });
      p.add(t, BorderLayout.NORTH);
    }
    p.add(panel, BorderLayout.CENTER);
    if (panel.getStatusBar() != null) {
      p.add(panel.getStatusBar(), BorderLayout.SOUTH);
    }
    getContentPane().setLayout(new GridLayout(1, 1));
    getContentPane().add(p);
    // Watch for the frame closing
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        application.closeContainer(SshVncFullScreenWindowContainer.this);
      }
    });
    getContentPane().validate();
  }

  public void setContainerTitle(String title) {
  }

  public void setContainerVisible(boolean visible) {
    if (visible && !isContainerVisible()) {
      try {
        setVisible(true);
        GraphicsDevice device = panel.getGraphicsConfiguration()
            .getDevice();
        //device.setFullScreenWindow(this);
        log.debug("Full screen container made visible");
        // ( (SshVNCPanel) panel).setAutoHideTools(true);
      }
      catch (Exception e) {
        e.printStackTrace();
        log.error(e);
      }
    }
    else if (!visible && isContainerVisible()) {
      try {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice();
        //device.setFullScreenWindow(null);
        setVisible(false);
        log.debug("Full screen container made invisible");
      }
      catch (Exception e) {
        e.printStackTrace();
        log.error(e);
      }
    }
  }

  public boolean isContainerVisible() {
    return isVisible();
  }

  public SshToolsApplicationPanel getApplicationPanel() {
    return panel;
  }

  public void closeContainer() {
    log.debug("Closing full screen container");
    //( (SshVNCPanel) panel).setAutoHideTools(false);
    getApplicationPanel().deregisterAction(newWindowAction);
    getApplicationPanel().deregisterAction(exitAction);
    getApplicationPanel().deregisterAction(aboutAction);
    getApplicationPanel().deregisterAction(fullScreenAction);
    getApplicationPanel().rebuildActionComponents();
    setContainerVisible(false);
    dispose();
  }

  class FullScreenActionImpl
      extends VncFullScreenAction {

    public FullScreenActionImpl(SshToolsApplication application,
                                SshToolsApplicationContainer container) {
      super(application, container);
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(30));
    }
  }
}
