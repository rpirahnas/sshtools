/*
 *  Sshtools - SshVNC
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.sshvnc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.common.ui.ResourceIcon;
import com.sshtools.common.ui.SshToolsApplicationFrame;

public class SshVNCFrame
    extends SshToolsApplicationFrame {
  // Logger
  protected static Log log = LogFactory.getLog(SshVNCFrame.class);

  public SshVNCFrame() {
    super();
    setIconImage(new ResourceIcon("/com/sshtools/sshvnc/sshvncframeicon.gif").
                 getImage());
  }
}
