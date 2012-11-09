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

import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import javax.swing.JComponent;
import javax.swing.JScrollBar;

public class TerminalPanel
    extends JComponent
    implements VDUDisplay,
    KeyListener, MouseListener, MouseMotionListener, Printable {
  private final static int debug = 0;
  private final static long VDU_EVENTS = AWTEvent.KEY_EVENT_MASK
      | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.ACTION_EVENT_MASK
      | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
      | 0x20000 /* We want mouse wheel support and 1.3 runtime and compile compatibility - so this mask unfortunately needs to be hard code*/
      ;
  public final static int RESIZE_NONE = 0;
  public final static int RESIZE_FONT = 1;
  public final static int RESIZE_SCREEN = 2;
  public final static int COLOR_BOLD = 8;
  public final static int COLOR_INVERT = 9;

  /*
   *  definitions of standards for the display unit
   */
  private final static int COLOR_FG_STD = 7;
  private final static int COLOR_BG_STD = 0;
  private VDUBuffer buffer;
  private Insets insets;

  /*
   *  size of the border
   */
  private boolean raised;

  /*
   *  indicator if the border is raised
   */
  private Font normalFont;

  /*
   *  normal font
   */
  private FontMetrics fm;

  /*
   *  current font metrics
   */
  private int charWidth;

  /*
   *  current width of a char
   */
  private int charHeight;

  /*
   *  current height of a char
   */
  private int charDescent;

  /*
   *  base line descent
   */
  private int resizeStrategy;

  /*
   *  current resizing strategy
   */
  private Point selectBegin;

  /*
   *  current resizing strategy
   */
  private Point selectEnd;

  /*
   *  selection coordinates
   */
  private String selection;

  /*
   *  contains the selected text
   */
  protected JScrollBar scrollBar;
  private SoftFont sf = new SoftFont();
  private boolean colorPrinting = false;

  /*
   *  print display in color
   */
  private Image backingStore = null;
  private boolean antialias;
  private Color[] color = {
      Color.black,
      // Background
      Color.red, Color.green, Color.yellow, Color.blue, Color.magenta,
      Color.cyan, Color.white,
      // Foreground
      null,
      // bold color
      null,
  };
  private Color cursorColorFG = null;
  private Color cursorColorBG = null;

  // lightweight component event handling
  private MouseListener mouseListener;
  private MouseMotionListener mouseMotionListener;

  //private MouseWheelListener mouseWheelListener;
  private KeyListener keyListener;
  FocusListener focusListener;

  public TerminalPanel(VDUBuffer buffer, Font font) {
    setVDUBuffer(buffer);

    addKeyListener(this);

    //  Escape events seem to get missed, there is probably a better way
    //  of doing this

    /*
        final KeyStroke ks = KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE, 0);
        ActionListener escLis = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                KeyEvent evt = new KeyEvent(
                    TerminalPanel.this, KeyEvent.KEY_PRESSED,
                    System.currentTimeMillis(), ks.getModifiers(),
                    ks.getKeyCode(), ks.getKeyChar());
                keyPressed(evt);
            }
        };
        registerKeyboardAction(escLis, ks, WHEN_IN_FOCUSED_WINDOW);
     */
    /*
     *  we have to make sure the tab key stays within the component
     */
    String version = System.getProperty("java.version");

    if (version.startsWith("1.4")) {
      try {
        Class[] params = new Class[] {
            boolean.class};
        TerminalPanel.class.getMethod("setFocusable", params).invoke(this,
            new Object[] {new Boolean(true)});
        TerminalPanel.class.getMethod("setFocusTraversalKeysEnabled",
                                      params).invoke(this,
            new Object[] {new Boolean(false)});
      }
      catch (Exception e) {
        System.err.println(
            "vt320: unable to reset focus handling for java version "
            + version);
        e.printStackTrace();
      }
    }

    // lightweight component handling
    enableEvents(VDU_EVENTS);

    // set the standard resize strategy
    setResizeStrategy(RESIZE_FONT);

    // set the normal font to use
    setFont(font);

    setForeground(Color.white);
    setBackground(Color.black);

    cursorColorFG = color[COLOR_FG_STD];
    cursorColorBG = color[COLOR_BG_STD];

    clearSelection();

    addMouseListener(this);
    addMouseMotionListener(this);

    selection = null;
  }

  public TerminalPanel(VDUBuffer buffer) {
    this(buffer, new Font("Monospaced", Font.PLAIN, 11));
  }

  private Color brighten(Color clr) {
    int r;
    int g;
    int b;

    r = (int) min(clr.getRed() * 1.2, 255.0);
    g = (int) min(clr.getGreen() * 1.2, 255.0);
    b = (int) min(clr.getBlue() * 1.2, 255.0);

    return new Color(r, g, b);
  }

  private Color darken(Color clr) {
    int r;
    int g;
    int b;

    r = (int) max(clr.getRed() * 0.8, 0.0);
    g = (int) max(clr.getGreen() * 0.8, 0.0);
    b = (int) max(clr.getBlue() * 0.8, 0.0);

    return new Color(r, g, b);
  }

  protected double max(double f1, double f2) {
    return (f1 < f2) ? f2 : f1;
  }

  protected double min(double f1, double f2) {
    return (f1 < f2) ? f1 : f2;
  }

  public void setAntialias(boolean antialias) {
    this.antialias = antialias;
    repaint();
  }

  public boolean isAntialias() {
    return antialias;
  }

  public void setVDUBuffer(VDUBuffer buffer) {
    this.buffer = buffer;
    buffer.setDisplay(this);
  }

  public VDUBuffer getVDUBuffer() {
    return buffer;
  }

  public void setColorSet(Color[] colorset) {
    System.arraycopy(colorset, 0, color, 0, 10);
    buffer.update[0] = true;
    redraw();
  }

  public Color[] getColorSet() {
    return color;
  }

  public void setFont(Font font) {
    super.setFont(normalFont = font);
    fm = getFontMetrics(font);

    if (fm != null) {
      charWidth = fm.charWidth('@');
      charHeight = fm.getHeight();
      charDescent = fm.getDescent();
    }

    if (buffer.update != null) {
      buffer.update[0] = true;
    }

    redraw();
  }

  public void setResizeStrategy(int strategy) {
    resizeStrategy = strategy;

    if (debug > 0) {
      System.out.println("VDU: Setting resize strategy to " + strategy);
    }

    setBounds(getBounds());
  }

  public void setBorder(int thickness, boolean raised) {
    if (thickness == 0) {
      insets = null;
    }
    else {
      insets = new Insets(thickness + 1, thickness + 1, thickness + 1,
                          thickness + 1);
    }

    this.raised = raised;
  }

  public void setScrollbar(JScrollBar scrollBar) {
    if (scrollBar == null) {
      return;
    }

    this.scrollBar = scrollBar;
    this.scrollBar.setValues(buffer.windowBase, buffer.height, 0,
                             buffer.bufSize - buffer.height - 1);
    this.scrollBar.addAdjustmentListener(new AdjustmentListener() {
      public void adjustmentValueChanged(AdjustmentEvent evt) {
        buffer.setWindowBase(evt.getValue());
      }
    });
  }

  public void redraw() {
    if (backingStore != null) {
      redraw(backingStore.getGraphics());
      repaint();
    }
  }

  protected void redraw(Graphics g1) {
    if (debug > 0) {
      System.err.println("redraw()");
    }

    Graphics2D g = (Graphics2D) g1;

    //  Antialiasing
    if (isAntialias()) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    }

    int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
    int yoffset = (super.getSize().height - (buffer.height * charHeight)) / 2;

    int selectStartLine = selectBegin.y - buffer.windowBase;
    int selectEndLine = selectEnd.y - buffer.windowBase;

    //        Color fg = darken(color[COLOR_FG_STD]);
    //        Color bg = darken(color[COLOR_BG_STD]);
    Color fg = color[COLOR_FG_STD];
    Color bg = color[COLOR_BG_STD];

    g.setFont(normalFont);

    /*
     *  for debug only
     *  if (update[0]) {
     *  System.err.println("Redrawing all");
     *  } else {
     *  for (int l = 1; l < size.height+1; l++) {
     *  if (update[l]) {
     *  for (int c = 0; c < size.height-l;c++) {
     *  if (!update[c+l]) {
     *  System.err.println("Redrawing "+(l-1)+" - "+(l+c-2));
     *  l=l+c;
     *  break;
     *  }
     *  }
     *  }
     *  }
     *  }
     */
    for (int l = 0; l < buffer.height; l++) {
      if (!buffer.update[0] && !buffer.update[l + 1]) {
        continue;
      }

      buffer.update[l + 1] = false;

      if (debug > 2) {
        System.err.println("redraw(): line " + l);
      }

      for (int c = 0; c < buffer.width; c++) {
        int addr = 0;
        int currAttr = buffer.charAttributes[buffer.windowBase + l][c];

        fg = getForeground();
        bg = getBackground();

        //                fg = darken(getForeground());
        //                bg = darken(getBackground());
        if ( (currAttr & buffer.COLOR_FG) != 0) {
          fg = darken(color[ ( (currAttr & buffer.COLOR_FG) >> 4) - 1]);
        }

        if ( (currAttr & buffer.COLOR_BG) != 0) {
          bg = darken(darken(color[ ( (currAttr & buffer.COLOR_BG) >> 8)
                             - 1]));
        }

        if ( (currAttr & VDUBuffer.BOLD) != 0) {
          g.setFont(new Font(normalFont.getName(), Font.BOLD,
                             normalFont.getSize()));

          // does not work with IE6: g.setFont(normalFont.deriveFont(Font.BOLD));
          if (null != color[COLOR_BOLD]) {
            fg = color[COLOR_BOLD];
          }

          /*
           *  if(fg.equals(Color.black)) {
           *  fg = Color.gray;
           *  } else {
           *  fg = brighten(fg);
           *  / bg = bg.brighter(); -- make some programs ugly
           *  }
           */
        }
        else {
          g.setFont(normalFont);
        }

        if ( (currAttr & VDUBuffer.LOW) != 0) {
          fg = darken(fg);
        }

        if ( (currAttr & VDUBuffer.INVERT) != 0) {
          if (null == color[COLOR_INVERT]) {
            Color swapc = bg;
            bg = fg;
            fg = swapc;
          }
          else {
            if (null == color[COLOR_BOLD]) {
              fg = bg;
            }
            else {
              fg = color[COLOR_BOLD];
            }

            bg = color[COLOR_INVERT];
          }
        }

        if (sf.inSoftFont(buffer.charArray[buffer.windowBase + l][c])) {
          g.setColor(bg);
          g.fillRect( (c * charWidth) + xoffset,
                     (l * charHeight) + yoffset, charWidth, charHeight);
          g.setColor(fg);
          sf.drawChar(g, buffer.charArray[buffer.windowBase + l][c],
                      xoffset + (c * charWidth), (l * charHeight) + yoffset,
                      charWidth, charHeight);

          if ( (currAttr & VDUBuffer.UNDERLINE) != 0) {
            g.drawLine( (c * charWidth) + xoffset,
                       ( (l + 1) * charHeight) - (charDescent / 2)
                       + yoffset, (c * charWidth) + charWidth + xoffset,
                       ( (l + 1) * charHeight) - (charDescent / 2)
                       + yoffset);
          }

          continue;
        }

        // determine the maximum of characters we can print in one go
        while ( ( (c + addr) < buffer.width)
               && ( (buffer.charArray[buffer.windowBase + l][c + addr] < ' ')
                   || (buffer.charAttributes[buffer.windowBase + l][c
                       + addr] == currAttr))
               && !sf.inSoftFont(
            buffer.charArray[buffer.windowBase + l][c + addr])) {
          if (buffer.charArray[buffer.windowBase + l][c + addr] < ' ') {
            buffer.charArray[buffer.windowBase + l][c + addr] = ' ';
            buffer.charAttributes[buffer.windowBase + l][c + addr] = 0;

            continue;
          }

          addr++;
        }

        // clear the part of the screen we want to change (fill rectangle)
        g.setColor(bg);
        g.fillRect( (c * charWidth) + xoffset,
                   (l * charHeight) + yoffset, addr * charWidth, charHeight);

        g.setColor(fg);

        // draw the characters
        g.drawChars(buffer.charArray[buffer.windowBase + l], c, addr,
                    (c * charWidth) + xoffset,
                    ( (l + 1) * charHeight) - charDescent + yoffset);

        if ( (currAttr & VDUBuffer.UNDERLINE) != 0) {
          g.drawLine( (c * charWidth) + xoffset,
                     ( (l + 1) * charHeight) - (charDescent / 2) + yoffset,
                     (c * charWidth) + (addr * charWidth) + xoffset,
                     ( (l + 1) * charHeight) - (charDescent / 2) + yoffset);
        }

        c += (addr - 1);
      }

      // selection code, highlites line or part of it when it was
      // selected previously
      if ( (l >= selectStartLine) && (l <= selectEndLine)) {
        int selectStartColumn = ( (l == selectStartLine) ? selectBegin.x
                                 : 0);
        int selectEndColumn = ( (l == selectEndLine)
                               ?
                               //                        (l == selectStartLine ? selectEnd.x - selectStartColumn :
                               ( (l == selectStartLine) ? selectEnd.x :
                                selectEnd.x)
                               : buffer.width);

        if (selectStartColumn != selectEndColumn) {
          if (debug > 0) {
            System.err.println("select(" + selectStartColumn + "-"
                               + selectEndColumn + ")");
          }

          g.setXORMode(bg);
          g.fillRect( (selectStartColumn * charWidth) + xoffset,
                     (l * charHeight) + yoffset,
                     (selectEndColumn - selectStartColumn) * charWidth,
                     charHeight);
          g.setPaintMode();
        }
      }
    }

    // draw cursor
    if (buffer.showcursor
        && ( ( (buffer.screenBase + buffer.cursorY) >= buffer.windowBase)
            && ( (buffer.screenBase + buffer.cursorY) < (buffer.windowBase
        + buffer.height)))) {
      g.setColor(cursorColorFG);
      g.setXORMode(cursorColorBG);
      g.fillRect( (buffer.cursorX * charWidth) + xoffset,
                 ( ( (buffer.cursorY + buffer.screenBase) - buffer.windowBase) *
                  charHeight)
                 + yoffset, charWidth, charHeight);
      g.setPaintMode();
      g.setColor(color[COLOR_FG_STD]);
    }

    // draw border
    if (insets != null) {
      g.setColor(getBackground());
      xoffset--;
      yoffset--;

      for (int i = insets.top - 1; i >= 0; i--) {
        g.draw3DRect(xoffset - i, yoffset - i,
                     (charWidth * buffer.width) + 1 + (i * 2),
                     (charHeight * buffer.height) + 1 + (i * 2), raised);
      }
    }

    buffer.update[0] = false;
  }

  public void paint(Graphics g1) {
    Graphics2D g = (Graphics2D) g1;

    //  Antialiasing
    if (isAntialias()) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
    }

    if (backingStore == null) {
      Dimension size = super.getSize();
      backingStore = createImage(size.width, size.height);
      buffer.update[0] = true;
      redraw();

      Graphics g2 = backingStore.getGraphics();
      g2.setColor(getBackground());
      g2.fillRect(0, 0, super.getSize().width, super.getSize().height);
    }

    if (debug > 1) {
      System.err.println("Clip region: " + g.getClipBounds());
    }

    g.drawImage(backingStore, 0, 0, this);
  }

  /*
       *  public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
   *  if(pi >= 1) {
   *  return Printable.NO_SUCH_PAGE;
   *  }
   *  paint(g);
   *  return Printable.PAGE_EXISTS;
   *  }
   */
  public void setColorPrinting(boolean colorPrint) {
    colorPrinting = colorPrint;
  }

  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws
      PrinterException {
    print(graphics);

    return (pageIndex == 0) ? Printable.PAGE_EXISTS : NO_SUCH_PAGE;
  }

  public void print(Graphics g) {
    if (debug > 0) {
      System.err.println("DEBUG: print()");
    }

    for (int i = 0; i <= buffer.height; i++) {
      buffer.update[i] = true;
    }

    Color fg = null;
    Color bg = null;
    Color[] colorSave = null;

    if (!colorPrinting) {
      fg = getForeground();
      bg = getBackground();
      setForeground(Color.black);
      setBackground(Color.white);
      colorSave = color;
      color = new Color[] {
          Color.black, Color.black, Color.black, Color.black,
          Color.black, Color.black, Color.black, Color.white, null,
          null,
      };
    }

    redraw(g);

    if (!colorPrinting) {
      color = colorSave;
      setForeground(fg);
      setBackground(bg);
    }
  }

  public Point mouseGetPos(Point evtpt) {
    Point mousepos;

    mousepos = new Point(0, 0);

    int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
    int yoffset = (super.getSize().height - (buffer.height * charHeight)) / 2;

    mousepos.x = (evtpt.x - xoffset) / charWidth;

    if (mousepos.x < 0) {
      mousepos.x = 0;
    }

    if (mousepos.x >= buffer.width) {
      mousepos.x = buffer.width - 1;
    }

    mousepos.y = (evtpt.y - yoffset) / charHeight;

    if (mousepos.y < 0) {
      mousepos.y = 0;
    }

    if (mousepos.y >= buffer.height) {
      mousepos.y = buffer.height - 1;
    }

    return mousepos;
  }

  public void setCursorColors(Color fg, Color bg) {
    if (fg == null) {
      cursorColorFG = color[COLOR_FG_STD];
    }
    else {
      cursorColorFG = fg;
    }

    if (bg == null) {
      cursorColorBG = color[COLOR_BG_STD];
    }
    else {
      cursorColorBG = bg;
    }

    repaint();
  }

  public void setBounds(int x, int y, int w, int h) {
    if (debug > 0) {
      System.err.println("VDU: setBounds(" + x + "," + y + "," + w + ","
                         + h + ")");
    }

    super.setBounds(x, y, w, h);

    // ignore zero bounds
    if ( (x == 00) && (y == 0) && (w == 0) && (h == 0)) {
      return;
    }

    if (insets != null) {
      w -= (insets.left + insets.right);
      h -= (insets.top + insets.bottom);
    }

    if (debug > 0) {
      System.err.println("VDU: looking for better match for "
                         + normalFont);
    }

    Font tmpFont = normalFont;
    String fontName = tmpFont.getName();
    int fontStyle = tmpFont.getStyle();
    fm = getFontMetrics(normalFont);

    if (fm != null) {
      charWidth = fm.charWidth('@');
      charHeight = fm.getHeight();
    }

    switch (resizeStrategy) {
      case RESIZE_SCREEN:

        if (debug > 0) {
          System.err.println("VDU: resizing screen");
        }

        //                buffer.setScreenSize(w / charWidth, buffer.height = h / charHeight);
        buffer.setScreenSize(w / charWidth, h / charHeight);

        break;

      case RESIZE_FONT:

        if (debug > 0) {
          System.err.println("VDU: resizing font");
        }

        int height = h / buffer.height;
        int width = w / buffer.width;

        fm = getFontMetrics(normalFont = new Font(fontName, fontStyle,
                                                  charHeight));

        // adapt current font size (from small up to best fit)
        if ( (fm.getHeight() < height) || (fm.charWidth('@') < width)) {
          do {
            fm = getFontMetrics(normalFont = new Font(fontName,
                fontStyle, ++charHeight));
          }
          while ( (fm.getHeight() < height)
                 || (fm.charWidth('@') < width));
        }

        // now check if we got a font that is too large
        if ( (fm.getHeight() > height) || (fm.charWidth('@') > width)) {
          do {
            fm = getFontMetrics(normalFont = new Font(fontName,
                fontStyle, --charHeight));
          }
          while ( (charHeight > 1)
                 && ( (fm.getHeight() > height)
                     || (fm.charWidth('@') > width)));
        }

        if (charHeight <= 1) {
          System.err.println(
              "VDU: error during resize, resetting to default");
          normalFont = new Font(fontName, fontStyle, 10);
          System.err.println("VDU: disabling font/screen resize");
        }

        setFont(normalFont);
        fm = getFontMetrics(normalFont);
        charWidth = fm.charWidth('@');
        charHeight = fm.getHeight();
        charDescent = fm.getDescent();

        break;

      case RESIZE_NONE:

        if (debug > 0) {
          System.err.println("VDU: not resizing");
        }

      default:
        break;
    }

    if (debug > 0) {
      System.err.println("VDU: charWidth=" + charWidth + ", "
                         + "charHeight=" + charHeight + ", " + "charDescent="
                         + charDescent);
    }

    // delete the double buffer image and mark all lines
    //        backingStore = null;
    //        buffer.markLine(0, buffer.height);
    Dimension size = super.getSize();

    if (size.width <= 0) {
      size.width = 1;
    }

    if (size.height <= 0) {
      size.height = 1;
    }

    backingStore = createImage(size.width, size.height);

    if (backingStore != null) {
      Graphics g = backingStore.getGraphics();
      g.setColor(getBackground());
      g.fillRect(0, 0, size.width, size.height);
      buffer.update[0] = true;
      redraw();
    }
  }

  public void refresh() {
    backingStore = null;
    repaint();
  }

  public Dimension getSize() {
    int xborder = 0;
    int yborder = 0;

    if (insets != null) {
      xborder = insets.left + insets.right;
      yborder = insets.top + insets.bottom;
    }

    return new Dimension( (buffer.width * charWidth) + xborder,
                         (buffer.height * charHeight) + yborder);
  }

  public Dimension getPreferredSize() {
    return getSize();
  }

  public Insets getInsets() {
    return insets;
  }

  public void clearSelection() {
    selectBegin = new Point(0, 0);
    selectEnd = new Point(0, 0);
    selection = null;
  }

  public String getSelection() {
    return selection;
  }

  public JScrollBar getScrollBar() {
    return scrollBar;
  }

  private boolean buttonCheck(int modifiers, int mask) {
    return (modifiers & mask) == mask;
  }

  public void mouseMoved(MouseEvent evt) {
    /*
     *  nothing yet we do here
     */
  }

  private void selectWord(int x, int y) {
    int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
    int yoffset = (super.getSize().height - (buffer.height * charHeight)) / 2;
    x = (x - xoffset) / charWidth;
    y = ( (y - yoffset) / charHeight) + buffer.windowBase;

    char[] l = buffer.charArray[y];
    int i = x;

    for (; (i >= 0) && (l[i] != ' '); i--) {
      ;
    }

    if (i != x) {
      selectBegin.y = y;
      selectBegin.x = i + 1;
      selectEnd.y = y;

      int j = x;

      for (; (j < l.length) && (l[j] != ' '); j++) {
        ;
      }

      selectEnd.x = j;
      buildSelectionText();
      buffer.update[0] = true;
      redraw();
    }
  }

  private void selectLine(int x, int y) {
    int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
    int yoffset = (super.getSize().height - (buffer.height * charHeight)) / 2;
    x = (x - xoffset) / charWidth;
    y = ( (y - yoffset) / charHeight) + buffer.windowBase;

    char[] l = buffer.charArray[y];
    int i = 0;

    for (; (i < l.length) && (l[i] == ' '); i++) {
      ;
    }

    if (i < l.length) {
      int j = l.length - 1;
      selectBegin.y = y;
      selectEnd.y = y;
      selectBegin.x = i;

      for (; (j >= 0) && (l[j] == ' '); j--) {
        ;
      }

      selectEnd.x = j + 1;
      buildSelectionText();
      buffer.update[0] = true;
      redraw();
    }
  }

  public void mouseDragged(MouseEvent evt) {
    if (buttonCheck(evt.getModifiers(), MouseEvent.BUTTON1_MASK)
        || ( // Windows NT/95 etc: returns 0, which is a bug
        evt.getModifiers() == 0)) {
      int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
      int yoffset = (super.getSize().height
                     - (buffer.height * charHeight)) / 2;
      int x = (evt.getX() - xoffset) / charWidth;
      int y = ( (evt.getY() - yoffset) / charHeight) + buffer.windowBase;
      int oldx = selectEnd.x;
      int oldy = selectEnd.y;

      if ( ( (x <= selectBegin.x) && (y <= selectBegin.y) && (x >= 0))) {
        selectBegin.x = x;
        selectBegin.y = y;
      }
      else {
        if ( (x <= buffer.width) && (x >= 0)) {
          selectEnd.x = x;
        }

        selectEnd.y = y;
      }

      if ( (oldx != x) || (oldy != y)) {
        buffer.update[0] = true;

        if (debug > 0) {
          System.err.println("select([" + selectBegin.x + ","
                             + selectBegin.y + "]," + "[" + selectEnd.x + ","
                             + selectEnd.y + "])");
        }

        redraw();
      }
    }
  }

  public void mouseClicked(MouseEvent evt) {
    if (evt.getClickCount() == 2) {
      selectWord(evt.getX(), evt.getY());
    }
    else if (evt.getClickCount() == 3) {
      selectLine(evt.getX(), evt.getY());
    }
    else {
      requestFocus();
    }

    /*
     *  nothing yet we do here
     */
  }

  public void mouseEntered(MouseEvent evt) {
    /*
     *  nothing yet we do here
     */
  }

  public void mouseExited(MouseEvent evt) {
    /*
     *  nothing yet we do here
     */
  }

  public void mousePressed(MouseEvent evt) {
    requestFocus();

    int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
    int yoffset = (super.getSize().height - (buffer.height * charHeight)) / 2;

    if (buffer instanceof VDUInput) {
      ( (VDUInput) buffer).mousePressed(xoffset, yoffset,
                                        evt.getModifiers());
    }

    // looks like we get no modifiers here ... ... We do? -Marcus
    if (buttonCheck(evt.getModifiers(), MouseEvent.BUTTON1_MASK)) {
      selectBegin.x = (evt.getX() - xoffset) / charWidth;
      selectBegin.y = ( (evt.getY() - yoffset) / charHeight)
          + buffer.windowBase;
      selectEnd.x = selectBegin.x;
      selectEnd.y = selectBegin.y;
    }
  }

  public void mouseReleased(MouseEvent evt) {
    int xoffset = (super.getSize().width - (buffer.width * charWidth)) / 2;
    int yoffset = (super.getSize().height - (buffer.height * charHeight)) / 2;

    if (buffer instanceof VDUInput) {
      ( (VDUInput) buffer).mousePressed(xoffset, yoffset,
                                        evt.getModifiers());
    }

    if (buttonCheck(evt.getModifiers(), MouseEvent.BUTTON1_MASK)) {
      mouseDragged(evt);

      if ( (selectBegin.x == selectEnd.x)
          && (selectBegin.y == selectEnd.y)) {
        buffer.update[0] = true;
        redraw();

        return;
      }

      // fix end.x and end.y, they can get over the border
      if (selectEnd.x < 0) {
        selectEnd.x = 0;
      }

      if (selectEnd.y < 0) {
        selectEnd.y = 0;
      }

      if (selectEnd.y >= buffer.charArray.length) {
        selectEnd.y = buffer.charArray.length - 1;
      }

      if (selectEnd.x > buffer.charArray[0].length) {
        selectEnd.x = buffer.charArray[0].length;
      }

      buildSelectionText();
    }
  }

  private void buildSelectionText() {
    selection = "";

    for (int l = selectBegin.y; l <= selectEnd.y; l++) {
      int start;
      int end;
      start = ( (l == selectBegin.y) ? (start = selectBegin.x) : 0);
      end = ( (l == selectEnd.y) ? (end = selectEnd.x)
             : buffer.charArray[l].length);

      // Trim all spaces from end of line, like xterm does.
      selection += ("-"
                    + (new String(buffer.charArray[l])).substring(start, end)).
          trim()
          .substring(1);

      if (end == buffer.charArray[l].length) {
        selection += "\n";
      }
    }
  }

  public void keyTyped(KeyEvent e) {
    if (buffer != null) {
      ( (VDUInput) buffer).keyTyped(e.getKeyCode(), e.getKeyChar(),
                                    getModifiers(e));
    }
  }

  public void keyPressed(KeyEvent e) {
    if (buffer != null) {
      ( (VDUInput) buffer).keyPressed(e.getKeyCode(), e.getKeyChar(),
                                      getModifiers(e));
    }
  }

  public void keyReleased(KeyEvent e) {
    // ignore
  }

  public void addMouseListener(MouseListener listener) {
    mouseListener = AWTEventMulticaster.add(mouseListener, listener);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
  }

  public void removeMouseListener(MouseListener listener) {
    mouseListener = AWTEventMulticaster.remove(mouseListener, listener);
  }

  public void addMouseMotionListener(MouseMotionListener listener) {
    mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener,
                                                  listener);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
  }

  public void removeMouseMotionListener(MouseMotionListener listener) {
    mouseMotionListener = AWTEventMulticaster.remove(mouseMotionListener,
        listener);
  }

  /*public void addMouseWheelListener(MouseWheelListener listener) {
    mouseWheelListener = AWTEventMulticaster.add(mouseWheelListener,
                                                 listener);
    enableEvents(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
       }*/
  /*public void removeMouseWheelListener(MouseWheelListener listener) {
    mouseWheelListener = AWTEventMulticaster.remove(mouseWheelListener,
        listener);
       }*/
  public void processMouseEvent(MouseEvent evt) {
    // handle simple mouse events
    if (mouseListener != null) {
      switch (evt.getID()) {
        case MouseEvent.MOUSE_CLICKED:
          mouseListener.mouseClicked(evt);

          break;

        case MouseEvent.MOUSE_ENTERED:
          mouseListener.mouseEntered(evt);

          break;

        case MouseEvent.MOUSE_EXITED:
          mouseListener.mouseExited(evt);

          break;

        case MouseEvent.MOUSE_PRESSED:
          mouseListener.mousePressed(evt);

          break;

        case MouseEvent.MOUSE_RELEASED:
          mouseListener.mouseReleased(evt);

          break;
      }
    }

    super.processMouseEvent(evt);
  }

  public void processMouseMotionEvent(MouseEvent evt) {
    // handle mouse motion events
    if (mouseMotionListener != null) {
      switch (evt.getID()) {
        case MouseEvent.MOUSE_DRAGGED:
          mouseMotionListener.mouseDragged(evt);

          break;

        case MouseEvent.MOUSE_MOVED:
          mouseMotionListener.mouseMoved(evt);

          break;
      }
    }

    super.processMouseMotionEvent(evt);
  }

  /*public void processMouseWheelEvent(MouseWheelEvent evt) {
    if (mouseWheelListener != null) {
      mouseWheelListener.mouseWheelMoved(evt);
    }
    super.processMouseWheelEvent(evt);
       }*/
  public void addKeyListener(KeyListener listener) {
    keyListener = AWTEventMulticaster.add(keyListener, listener);
    enableEvents(AWTEvent.KEY_EVENT_MASK);
  }

  public void removeKeyListener(KeyListener listener) {
    keyListener = AWTEventMulticaster.remove(keyListener, listener);
  }

  public void processKeyEvent(KeyEvent evt) {
    if (keyListener != null) {
      switch (evt.getID()) {
        case KeyEvent.KEY_PRESSED:
          keyListener.keyPressed(evt);

          break;

        case KeyEvent.KEY_RELEASED:
          keyListener.keyReleased(evt);

          break;

        case KeyEvent.KEY_TYPED:
          keyListener.keyTyped(evt);

          break;
      }
    }

    // consume TAB keys if they originate from our component
    if ( (evt.getKeyCode() == KeyEvent.VK_TAB) && (evt.getSource() == this)) {
      evt.consume();
    }

    super.processKeyEvent(evt);
  }

  public void addFocusListener(FocusListener listener) {
    focusListener = AWTEventMulticaster.add(focusListener, listener);
  }

  public void removeFocusListener(FocusListener listener) {
    focusListener = AWTEventMulticaster.remove(focusListener, listener);
  }

  public void processFocusEvent(FocusEvent evt) {
    if (debug > 0) {
      System.out.println("VDU: focuse event " + evt);
    }

    if (focusListener != null) {
      switch (evt.getID()) {
        case FocusEvent.FOCUS_GAINED:
          focusListener.focusGained(evt);

          break;

        case FocusEvent.FOCUS_LOST:
          focusListener.focusLost(evt);

          break;
      }
    }

    super.processFocusEvent(evt);
  }

  private int getModifiers(KeyEvent e) {
    return (e.isControlDown() ? VDUInput.KEY_CONTROL : 0)
        | (e.isShiftDown() ? VDUInput.KEY_SHIFT : 0)
        | (e.isAltDown() ? VDUInput.KEY_ALT : 0)
        | (e.isActionKey() ? VDUInput.KEY_ACTION : 0);
  }
}
