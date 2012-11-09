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

package com.sshtools.daemon.windows;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.15 $
 */
public class WindowsProcessOutputStream
    extends OutputStream {
  static {
    System.loadLibrary("sshtools-daemon-win32");
  }

  int handle;

  /**
   * Creates a new WindowsProcessOutputStream object.
   *
   * @param handle
   */
  public WindowsProcessOutputStream(int handle) {
    this.handle = handle;
  }

  /**
   *
   */
  public void flush() {
    flush(handle);
  }

  /**
   *
   *
   * @param b
   * @param off
   * @param len
   */
  public void write(byte[] b, int off, int len) {
    writeBytes(handle, b, off, len);
    flush();
  }

  /**
   *
   *
   * @param b
   *
   * @throws IOException
   */
  public void write(int b) throws IOException {
    writeByte(handle, b);
  }

  private native void flush(int handle);

  private native void writeByte(int handle, int b);

  private native void writeBytes(int handle, byte[] b, int off, int len);
}
