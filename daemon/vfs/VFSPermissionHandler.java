/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.daemon.vfs;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Lee David Painter
 * @version $Id: VFSPermissionHandler.java,v 1.7 2003/09/22 15:58:05 martianx Exp $
 */
import java.io.FileNotFoundException;
import java.io.IOException;

import com.sshtools.daemon.platform.PermissionDeniedException;

public interface VFSPermissionHandler {

  public void verifyPermissions(String username, String path,
                                String permissions) throws
      PermissionDeniedException, FileNotFoundException, IOException;

  public String getVFSHomeDirectory(String username) throws
      FileNotFoundException;

}