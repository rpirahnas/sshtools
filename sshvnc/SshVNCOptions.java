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

import com.sshtools.common.configuration.SshToolsConnectionProfile;

public class SshVNCOptions
    implements Cloneable {
  private int[] encodings = new int[20];
  private int nEncodings;
  private int preferredEncoding;
  private int compressLevel;
  private int cursorUpdates;
  private int jpegQuality;
  private int screenSizePolicy;
  private boolean useCopyRect;
  private boolean eightBitColors;
  private boolean requestCursorUpdates;
  private boolean ignoreCursorUpdates;
  private boolean reverseMouseButtons2And3;
  private boolean viewOnly;
  private boolean shareDesktop;
  private String encryptedPassword;

  public SshVNCOptions() {
    this(null);
  }

  public SshVNCOptions(SshToolsConnectionProfile profile) {
    setFromProfile(profile);
  }

  public boolean isReverseMouseButtons2And3() {
    return reverseMouseButtons2And3;
  }

  public boolean isViewOnly() {
    return viewOnly;
  }

  public boolean isShareDesktop() {
    return shareDesktop;
  }

  public String getEncryptedPassword() {
    return encryptedPassword;
  }

  public int[] getEncodings() {
    return encodings;
  }

  public int getNumberOfEncodings() {
    return nEncodings;
  }

  public int getCompressLevel() {
    return compressLevel;
  }

  public int getJPEGQuality() {
    return jpegQuality;
  }

  public boolean isUseCopyRect() {
    return useCopyRect;
  }

  public void setEightBitColors(boolean eightBitColors) {
    this.eightBitColors = eightBitColors;
  }

  public boolean isEightBitColors() {
    return eightBitColors;
  }

  public boolean isRequestCursorUpdates() {
    return requestCursorUpdates;
  }

  public void setRequestCursorUpdates(boolean requestCursorUpdates) {
    this.requestCursorUpdates = requestCursorUpdates;
  }

  public boolean isIgnoreCursorUpdates() {
    return ignoreCursorUpdates;
  }

  public int getScreenSizePolicy() {
    return screenSizePolicy;
  }

  public void setEncodings() {
    nEncodings = 0;
    if (useCopyRect) {
      encodings[nEncodings++] = RfbProto.EncodingCopyRect;
    }

    boolean enableCompressLevel = preferredEncoding == RfbProto.EncodingZlib ||
        preferredEncoding == RfbProto.EncodingTight;

    encodings[nEncodings++] = preferredEncoding;
    if (preferredEncoding != RfbProto.EncodingHextile) {
      encodings[nEncodings++] = RfbProto.EncodingHextile;
    }
    if (preferredEncoding != RfbProto.EncodingTight) {
      encodings[nEncodings++] = RfbProto.EncodingTight;
    }
    if (preferredEncoding != RfbProto.EncodingZlib) {
      encodings[nEncodings++] = RfbProto.EncodingZlib;
    }
    if (preferredEncoding != RfbProto.EncodingCoRRE) {
      encodings[nEncodings++] = RfbProto.EncodingCoRRE;
    }
    if (preferredEncoding != RfbProto.EncodingRRE) {
      encodings[nEncodings++] = RfbProto.EncodingRRE;
    }

    // Handle compression level setting.

    if (enableCompressLevel) {
      if (compressLevel >= 1 && compressLevel <= 9) {
        encodings[nEncodings++] =
            RfbProto.EncodingCompressLevel0 + compressLevel;
      }
      else {
        compressLevel = -1;
      }
    }

    // Handle JPEG quality setting.

    if (preferredEncoding == RfbProto.EncodingTight && !eightBitColors) {
      if (jpegQuality >= 0 && jpegQuality <= 9) {
        encodings[nEncodings++] =
            RfbProto.EncodingQualityLevel0 + jpegQuality;
      }
      else {
        jpegQuality = -1;
      }
    }

    // Request cursor shape updates if necessary.

    requestCursorUpdates = cursorUpdates != 2;

    if (requestCursorUpdates) {
      encodings[nEncodings++] = RfbProto.EncodingXCursor;
      encodings[nEncodings++] = RfbProto.EncodingRichCursor;
      ignoreCursorUpdates = cursorUpdates == 1;
      if (!ignoreCursorUpdates) {
        encodings[nEncodings++] = RfbProto.EncodingPointerPos;
      }
    }

    encodings[nEncodings++] = RfbProto.EncodingLastRect;
    encodings[nEncodings++] = RfbProto.EncodingNewFBSize;

  }

  public void setFromProfile(SshToolsConnectionProfile profile) {
    preferredEncoding = profile == null ? RfbProto.EncodingTight :
        profile.getApplicationPropertyInt(
        SshVNCPanel.PROFILE_PROPERTY_PREFERRED_ENCODING, RfbProto.EncodingTight);
    useCopyRect = profile == null ? true :
        profile.getApplicationPropertyBoolean(
        SshVNCPanel.PROFILE_PROPERTY_USE_COPY_RECT, true);
    compressLevel = profile == null ? 0 : profile.getApplicationPropertyInt(
        SshVNCPanel.PROFILE_PROPERTY_COMPRESS_LEVEL, 0);
    jpegQuality = profile == null ? 7 : profile.getApplicationPropertyInt(
        SshVNCPanel.PROFILE_PROPERTY_JPEG_QUALITY, 7);
    cursorUpdates = profile == null ? 0 : profile.getApplicationPropertyInt(
        SshVNCPanel.PROFILE_PROPERTY_CURSOR_UPDATES, 0);
    screenSizePolicy = profile == null ? 0 : profile.getApplicationPropertyInt(
        SshVNCPanel.PROFILE_PROPERTY_SCREEN_SIZE_POLICY, 0);
    eightBitColors = profile == null ? false :
        profile.getApplicationPropertyBoolean(
        SshVNCPanel.PROFILE_PROPERTY_EIGHT_BIT_COLORS, false);
    reverseMouseButtons2And3 = profile == null ? false :
        profile.getApplicationPropertyBoolean(
        SshVNCPanel.PROFILE_PROPERTY_REVERSE_MOUSE_BUTTONS_2_AND_3, false);
    viewOnly = profile == null ? false : profile.getApplicationPropertyBoolean(
        SshVNCPanel.PROFILE_PROPERTY_VIEW_ONLY, false);
    shareDesktop = profile == null ? false :
        profile.getApplicationPropertyBoolean(
        SshVNCPanel.PROFILE_PROPERTY_SHARE_DESKTOP, false);
    encryptedPassword = profile == null ? "" :
        profile.getApplicationProperty(SshVNCPanel.
                                       PROFILE_PROPERTY_ENCRYPTED_VNC_PASSWORD,
                                       "");

    //
    setEncodings();
  }

  public Object clone() {
    SshVNCOptions opt = new SshVNCOptions();

    opt.encodings = encodings;
    opt.nEncodings = nEncodings;
    opt.preferredEncoding = preferredEncoding;
    opt.compressLevel = compressLevel;
    opt.cursorUpdates = cursorUpdates;
    opt.jpegQuality = jpegQuality;
    opt.useCopyRect = useCopyRect;
    opt.eightBitColors = eightBitColors;
    opt.requestCursorUpdates = requestCursorUpdates;
    opt.ignoreCursorUpdates = ignoreCursorUpdates;
    opt.reverseMouseButtons2And3 = reverseMouseButtons2And3;
    opt.viewOnly = viewOnly;
    opt.shareDesktop = shareDesktop;
    opt.screenSizePolicy = screenSizePolicy;

    return opt;
  }
}
