/*
 *  Sshtools - SshVNC
 *
 *  Copyright (C) 2003 Lee David Painter.
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

public class HexASCII {
  public static byte[] ToByteArray(String s) {
    byte[] c = {
        0, 0, 0, 0, 0, 0, 0, 0};
    int len = s.length() / 2;
    if (len > 8) {
      len = 8;
    }
    for (int i = 0; i < len; i++) {
      String hex = s.substring(i * 2, i * 2 + 2);
      Integer x = new Integer(Integer.parseInt(hex, 16));
      c[i] = x.byteValue();
    }
    return c;
  }

  public static String ToString(String s) {
    return new String(HexASCII.ToByteArray(s));
  }
}
