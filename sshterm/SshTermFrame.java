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
package com.sshtools.sshterm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsApplication;
import com.sshtools.common.ui.SshToolsApplicationContainer;
import com.sshtools.common.ui.SshToolsApplicationException;
import com.sshtools.common.ui.SshToolsApplicationFrame;
import com.sshtools.common.ui.SshToolsApplicationPanel;
import com.sshtools.common.ui.StandardAction;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

public class SshTermFrame
    extends SshToolsApplicationFrame {
  // Logger
  protected static Log log = LogFactory.getLog(SshTermFrame.class);
  private StandardAction fullScreenAction;
  public SshTermFrame() {
    super();
    setIconImage(new ResourceIcon("/com/sshtools/sshterm/sshframeicon.gif")
                 .getImage());
  }

  public void init(SshToolsApplication application,
                   SshToolsApplicationPanel panel) throws
      SshToolsApplicationException {
    super.init(application, panel);
    GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice();

    log.debug("Testing if " + device.getIDstring() + " can go full screen");
    try {
      Method m = device.getClass().getMethod("isFullScreenSupported", new Class[] {});
      if ( ( (Boolean) m.invoke(device, new Object[] {})).booleanValue()
          ||
          ConfigurationLoader.checkAndGetProperty("sshterm.enableFullScreen",
                                                  "true").
          equals("true")) {
        log.info(device.getIDstring() + " can go full screen");
        addFullScreenActions();
      }
    }
    catch (SecurityException e) {
    }
    catch (NoSuchMethodException e) {
    }
    catch (IllegalArgumentException e) {
    }
    catch (IllegalAccessException e) {
    }
    catch (InvocationTargetException e) {
    }
  }

  private void addFullScreenActions() {
    getApplicationPanel().registerAction(fullScreenAction = new
                                         FullScreenActionImpl(
        getApplication(), this));
    getApplicationPanel().rebuildActionComponents();
  }

  public void closeContainer() {
    getApplicationPanel().deregisterAction(fullScreenAction);
    super.closeContainer();
  }

  class FullScreenActionImpl
      extends FullScreenAction {
    public FullScreenActionImpl(SshToolsApplication application,
                                SshToolsApplicationContainer container) {
      super(application, container);
      putValue(StandardAction.ON_CONTEXT_MENU, Boolean.TRUE);
      putValue(StandardAction.CONTEXT_MENU_GROUP, new Integer(50));
      putValue(StandardAction.CONTEXT_MENU_WEIGHT, new Integer(30));
    }
  }
}
