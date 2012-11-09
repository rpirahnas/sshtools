/*
 *  Sshtools - SSHTerm
 *
 *  The contents of this package have been derived from the Java
 *  Telnet/SSH Applet from http://javassh.org. The files have been
 *  modified and are supplied under the terms of the original license.
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
package com.sshtools.sshterm.emulation;

import javax.swing.event.EventListenerList;

public class VDUBuffer {
  public final static int debug = 0;
  public final static boolean SCROLL_UP = false;
  public final static boolean SCROLL_DOWN = true;
  public final static int NORMAL = 0x00;
  public final static int BOLD = 0x01;
  public final static int UNDERLINE = 0x02;
  public final static int INVERT = 0x04;
  public final static int LOW = 0x08;
  public final static int COLOR = 0xff0;
  public final static int COLOR_FG = 0xf0;
  public final static int COLOR_BG = 0xf00;
  private EventListenerList listenerList = new EventListenerList();
  public int height;
  public int width;
  /*
   *  rows and columns
   */
  public boolean[] update;
  /*
   *  contains the lines that need update
   */
  public char[][] charArray;
  /*
   *  contains the characters
   */
  public int[][] charAttributes;
  /*
   *  contains character attrs
   */
  public int bufSize;
  public int maxBufSize;
  /*
   *  buffer sizes
   */
  public int screenBase;
  /*
   *  the actual screen start
   */
  public int windowBase;
  /*
   *  where the start displaying
   */
  public int scrollMarker;
  /*
   *  marks the last line inserted
   */
  private int topMargin;
  /*
   *  top scroll margin
   */
  private int bottomMargin;
  /*
   *  bottom scroll margin
   */
  // cursor variables
  protected boolean showcursor = true;
  protected int cursorX;
  protected int cursorY;
  protected VDUDisplay display;
  public VDUBuffer(int width, int height) {
    // set the display screen size
    setScreenSize(width, height);
  }

  public VDUBuffer() {
    this(80, 24);
  }

  public void addTerminalListener(TerminalListener l) {
    listenerList.add(TerminalListener.class, l);
  }

  public void removeTerminalListener(TerminalListener l) {
    listenerList.remove(TerminalListener.class, l);
  }

  public void putChar(int c, int l, char ch) {
    putChar(c, l, ch, NORMAL);
  }

  public void putChar(int c, int l, char ch, int attributes) {
    // Lets see what were printing
    //        System.out.print(ch);
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    charArray[screenBase + l][c] = ch;
    charAttributes[screenBase + l][c] = attributes;
    markLine(l, 1);
  }

  public char getChar(int c, int l) {
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    return charArray[screenBase + l][c];
  }

  public String getLine(int l) {
    l = checkBounds(l, 0, height - 1);
    return new String(charArray[screenBase + l]);
  }

  public int getAttributes(int c, int l) {
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    return charAttributes[screenBase + l][c];
  }

  public void insertChar(int c, int l, char ch, int attributes) {
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    System.arraycopy(charArray[screenBase + l], c,
                     charArray[screenBase + l], c + 1, width - c - 1);
    System.arraycopy(charAttributes[screenBase + l], c,
                     charAttributes[screenBase + l], c + 1, width - c - 1);
    putChar(c, l, ch, attributes);
  }

  public void deleteChar(int c, int l) {
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    if (c < (width - 1)) {
      System.arraycopy(charArray[screenBase + l], c + 1,
                       charArray[screenBase + l], c, width - c - 1);
      System.arraycopy(charAttributes[screenBase + l], c + 1,
                       charAttributes[screenBase + l], c, width - c - 1);
    }
    putChar(width - 1, l, (char) 0);
  }

  public void putString(int c, int l, String s) {
    putString(c, l, s, NORMAL);
  }

  public void putString(int c, int l, String s, int attributes) {
    for (int i = 0; (i < s.length()) && ( (c + i) < width); i++) {
      putChar(c + i, l, s.charAt(i), attributes);
    }
  }

  public void insertLine(int l) {
    insertLine(l, 1, SCROLL_UP);
  }

  public void insertLine(int l, int n) {
    insertLine(l, n, SCROLL_UP);
  }

  public void insertLine(int l, boolean scrollDown) {
    insertLine(l, 1, scrollDown);
  }

  public synchronized void insertLine(int l, int n, boolean scrollDown) {
    l = checkBounds(l, 0, height - 1);
    char[][] cbuf = null;
    int[][] abuf = null;
    int offset = 0;
    int oldBase = screenBase;
    if (l > bottomMargin) {
      /*
       *  We do not scroll below bottom margin (below the scrolling region).
       */
      return;
    }
    int top = ( (l < topMargin) ? 0
               : ( (l > bottomMargin)
                  ?
                  ( ( (bottomMargin + 1) < height) ? (bottomMargin + 1) :
                   (height - 1))
                  : topMargin));
    int bottom = ( (l > bottomMargin) ? (height - 1)
                  : ( (l < topMargin)
                     ? ( (topMargin > 0) ? (topMargin - 1) : 0) : bottomMargin));
    // System.out.println("l is "+l+", top is "+top+", bottom is "+bottom+", bottomargin is "+bottomMargin+", topMargin is "+topMargin);
    if (scrollDown) {
      if (n > (bottom - top)) {
        n = (bottom - top);
      }
      cbuf = new char[bottom - l - (n - 1)][width];
      abuf = new int[bottom - l - (n - 1)][width];
      System.arraycopy(charArray, oldBase + l, cbuf, 0,
                       bottom - l - (n - 1));
      System.arraycopy(charAttributes, oldBase + l, abuf, 0,
                       bottom - l - (n - 1));
      System.arraycopy(cbuf, 0, charArray, oldBase + l + n,
                       bottom - l - (n - 1));
      System.arraycopy(abuf, 0, charAttributes, oldBase + l + n,
                       bottom - l - (n - 1));
      cbuf = charArray;
      abuf = charAttributes;
    }
    else {
      try {
        if (n > ( (bottom - top) + 1)) {
          n = (bottom - top) + 1;
        }
        if (bufSize < maxBufSize) {
          if ( (bufSize + n) > maxBufSize) {
            offset = n - (maxBufSize - bufSize);
            scrollMarker += offset;
            bufSize = maxBufSize;
            screenBase = maxBufSize - height - 1;
            windowBase = screenBase;
          }
          else {
            scrollMarker += n;
            screenBase += n;
            windowBase += n;
            bufSize += n;
          }
          cbuf = new char[bufSize][width];
          abuf = new int[bufSize][width];
        }
        else {
          offset = n;
          cbuf = charArray;
          abuf = charAttributes;
        }
        // copy anything from the top of the buffer (+offset) to the new top
        // up to the screenBase.
        if (oldBase > 0) {
          System.arraycopy(charArray, offset, cbuf, 0,
                           oldBase - offset);
          System.arraycopy(charAttributes, offset, abuf, 0,
                           oldBase - offset);
        }
        // copy anything from the top of the screen (screenBase) up to the
        // topMargin to the new screen
        if (top > 0) {
          System.arraycopy(charArray, oldBase, cbuf, screenBase, top);
          System.arraycopy(charAttributes, oldBase, abuf, screenBase,
                           top);
        }
        // copy anything from the topMargin up to the amount of lines inserted
        // to the gap left over between scrollback buffer and screenBase
        if (oldBase > 0) {
          System.arraycopy(charArray, oldBase + top, cbuf,
                           oldBase - offset, n);
          System.arraycopy(charAttributes, oldBase + top, abuf,
                           oldBase - offset, n);
        }
        // copy anything from topMargin + n up to the line linserted to the
        // topMargin
        System.arraycopy(charArray, oldBase + top + n, cbuf,
                         screenBase + top, l - top - (n - 1));
        System.arraycopy(charAttributes, oldBase + top + n, abuf,
                         screenBase + top, l - top - (n - 1));
        //
        // copy the all lines next to the inserted to the new buffer
        if (l < (height - 1)) {
          System.arraycopy(charArray, oldBase + l + 1, cbuf,
                           screenBase + l + 1, (height - 1) - l);
          System.arraycopy(charAttributes, oldBase + l + 1, abuf,
                           screenBase + l + 1, (height - 1) - l);
        }
      }
      catch (ArrayIndexOutOfBoundsException e) {
        // this should not happen anymore, but I will leave the code
        // here in case something happens anyway. That code above is
        // so complex I always have a hard time understanding what
        // I did, even though there are comments
        System.err.println("*** Error while scrolling up:");
        System.err.println("--- BEGIN STACK TRACE ---");
        e.printStackTrace();
        System.err.println("--- END STACK TRACE ---");
        System.err.println("bufSize=" + bufSize + ", maxBufSize="
                           + maxBufSize);
        System.err.println("top=" + top + ", bottom=" + bottom);
        System.err.println("n=" + n + ", l=" + l);
        System.err.println("screenBase=" + screenBase + ", windowBase="
                           + windowBase);
        System.err.println("oldBase=" + oldBase);
        System.err.println("size.width=" + width + ", size.height="
                           + height);
        System.err.println("abuf.length=" + abuf.length
                           + ", cbuf.length=" + cbuf.length);
        System.err.println("*** done dumping debug information");
      }
    }
    // this is a little helper to mark the scrolling
    scrollMarker -= n;
    for (int i = 0; i < n; i++) {
      cbuf[ (screenBase + l) + (scrollDown ? i : ( -i))] = new char[width];
      abuf[ (screenBase + l) + (scrollDown ? i : ( -i))] = new int[width];
    }
    charArray = cbuf;
    charAttributes = abuf;
    if (scrollDown) {
      markLine(l, bottom - l + 1);
    }
    else {
      markLine(top, l - top + 1);
    }
    /*
     *  FIXME: needs to be in VDU
     *  if(display.scrollBar != null)
     *  scrollBar.setValues(windowBase, height, 0, bufSize);
     */
    if ( (display != null) && (display.getScrollBar() != null)) {
      display.getScrollBar().setValues(windowBase, height, 0, bufSize - 1);
    }
  }

  public void deleteLine(int l) {
    l = checkBounds(l, 0, height - 1);
    int bottom = ( (l > bottomMargin) ? (height - 1)
                  : ( (l < topMargin) ? topMargin
                     : (bottomMargin + 1)));
    System.arraycopy(charArray, screenBase + l + 1, charArray,
                     screenBase + l, bottom - l - 1);
    System.arraycopy(charAttributes, screenBase + l + 1, charAttributes,
                     screenBase + l, bottom - l - 1);
    charArray[ (screenBase + bottom) - 1] = new char[width];
    charAttributes[ (screenBase + bottom) - 1] = new int[width];
    markLine(l, bottom - l);
  }

  public void deleteArea(int c, int l, int w, int h, int curAttr) {
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    char[] cbuf = new char[w];
    int[] abuf = new int[w];
    for (int i = 0; i < w; i++) {
      abuf[i] = curAttr;
    }
    for (int i = 0; (i < h) && ( (l + i) < height); i++) {
      System.arraycopy(cbuf, 0, charArray[screenBase + l + i], c, w);
      System.arraycopy(abuf, 0, charAttributes[screenBase + l + i], c, w);
    }
    markLine(l, h);
  }

  public void deleteArea(int c, int l, int w, int h) {
    c = checkBounds(c, 0, width - 1);
    l = checkBounds(l, 0, height - 1);
    char[] cbuf = new char[w];
    int[] abuf = new int[w];
    for (int i = 0; (i < h) && ( (l + i) < height); i++) {
      System.arraycopy(cbuf, 0, charArray[screenBase + l + i], c, w);
      System.arraycopy(abuf, 0, charAttributes[screenBase + l + i], c, w);
    }
    markLine(l, h);
  }

  public void showCursor(boolean doshow) {
    if (doshow != showcursor) {
      markLine(cursorY, 1);
    }
    showcursor = doshow;
  }

  public void setCursorPosition(int c, int l) {
    cursorX = checkBounds(c, 0, width - 1);
    cursorY = checkBounds(l, 0, height - 1);
    markLine(cursorY, 1);
  }

  public int getCursorColumn() {
    return cursorX;
  }

  public int getCursorRow() {
    return cursorY;
  }

  public void setWindowBase(int line) {
    if (line > screenBase) {
      line = screenBase;
    }
    else if (line < 0) {
      line = 0;
    }
    windowBase = line;
    update[0] = true;
    redraw();
  }

  public int getWindowBase() {
    return windowBase;
  }

  public void setTopMargin(int l) {
    if (l > bottomMargin) {
      topMargin = bottomMargin;
      bottomMargin = l;
    }
    else {
      topMargin = l;
    }
    if (topMargin < 0) {
      topMargin = 0;
    }
    if (bottomMargin > (height - 1)) {
      bottomMargin = height - 1;
    }
  }

  public int getTopMargin() {
    return topMargin;
  }

  public void setBottomMargin(int l) {
    if (l < topMargin) {
      bottomMargin = topMargin;
      topMargin = l;
    }
    else {
      bottomMargin = l;
    }
    if (topMargin < 0) {
      topMargin = 0;
    }
    if (bottomMargin > (height - 1)) {
      bottomMargin = height - 1;
    }
  }

  public int getBottomMargin() {
    return bottomMargin;
  }

  public void setBufferSize(int amount) {
    if (amount < height) {
      amount = height;
    }
    if (amount < maxBufSize) {
      char[][] cbuf = new char[amount][width];
      int[][] abuf = new int[amount][width];
      int copyStart = ( (bufSize - amount) < 0) ? 0 : (bufSize - amount);
      int copyCount = ( (bufSize - amount) < 0) ? bufSize : amount;
      if (charArray != null) {
        System.arraycopy(charArray, copyStart, cbuf, 0, copyCount);
      }
      if (charAttributes != null) {
        System.arraycopy(charAttributes, copyStart, abuf, 0, copyCount);
      }
      charArray = cbuf;
      charAttributes = abuf;
      bufSize = copyCount;
      screenBase = bufSize - height;
      windowBase = screenBase;
    }
    maxBufSize = amount;
    update[0] = true;
    redraw();
  }

  public int getBufferSize() {
    return bufSize;
  }

  public int getMaxBufferSize() {
    return maxBufSize;
  }

  public void setScreenSize(int w, int h) {
    char[][] cbuf;
    int[][] abuf;
    int bsize = bufSize;
    int oldHeight = height;
    if ( (w < 1) || (h < 1)) {
      return;
    }
    if (debug > 0) {
      System.err.println("VDU: screen size [" + w + "," + h + "]");
    }
    if (h > maxBufSize) {
      maxBufSize = h;
    }
    if (h > bufSize) {
      bufSize = h;
      screenBase = 0;
      windowBase = 0;
    }
    if ( (windowBase + h) >= bufSize) {
      windowBase = bufSize - h;
    }
    if ( (screenBase + h) >= bufSize) {
      screenBase = bufSize - h;
    }
    cbuf = new char[bufSize][w];
    abuf = new int[bufSize][w];
    if ( (charArray != null) && (charAttributes != null)) {
      for (int i = 0; (i < bsize) && (i < bufSize); i++) {
        System.arraycopy(charArray[i], 0, cbuf[i], 0,
                         (w < width) ? w : width);
        System.arraycopy(charAttributes[i], 0, abuf[i], 0,
                         (w < width) ? w : width);
      }
    }
    charArray = cbuf;
    charAttributes = abuf;
    width = w;
    height = h;
    topMargin = 0;
    bottomMargin = h - 1;
    update = new boolean[h + 1];
    update[0] = true;
    Object[] l = listenerList.getListeners(TerminalListener.class);
    for (int i = l.length - 1; i >= 0; i--) {
      ( (TerminalListener) l[i]).screenResized(w, h);
    }
    if ( (display != null) && (display.getScrollBar() != null)) {
      display.getScrollBar().setValue(bufSize - 1);
    }
    if (height > oldHeight) {
      cursorY += (height - oldHeight);
    }
    /*
     *  FIXME: ???
     *  if(resizeStrategy == RESIZE_FONT)
     *  setBounds(getBounds());
     */
  }

  public int getRows() {
    return height;
  }

  public int getColumns() {
    return width;
  }

  public void markLine(int l, int n) {
    l = checkBounds(l, 0, height - 1);
    for (int i = 0; (i < n) && ( (l + i) < height); i++) {
      update[l + i + 1] = true;
    }
  }

  private int checkBounds(int value, int lower, int upper) {
    if (value < lower) {
      return lower;
    }
    if (value > upper) {
      return upper;
    }
    return value;
  }

  public void setDisplay(VDUDisplay display) {
    this.display = display;
  }

  protected void redraw() {
    if (display != null) {
      display.redraw();
    }
  }
}
