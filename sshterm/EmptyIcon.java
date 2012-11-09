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

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

public class EmptyIcon
    implements Icon {
  private int w;
  private int h;
  public EmptyIcon(int w, int h) {
    this.w = w;
    this.h = h;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
  }

  public int getIconWidth() {
    return w;
  }

  public int getIconHeight() {
    return h;
  }
}
