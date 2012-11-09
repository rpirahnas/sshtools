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
package com.sshtools.tunnel;

import javax.swing.Timer;

import com.sshtools.j2ssh.forwarding.ForwardingChannel;

public class ActiveChannelWrapper {
  //  Private instance variables
  private boolean sending;
  //  Private instance variables
  private boolean receiving;
  private ForwardingChannel channel;
  private javax.swing.Timer sentTimer;
  private javax.swing.Timer receivedTimer;
  public ActiveChannelWrapper(ForwardingChannel channel) {
    this.channel = channel;
  }

  public void setSending(boolean sending) {
    this.sending = sending;
  }

  public void setReceiving(boolean receiving) {
    this.receiving = receiving;
  }

  public void setSentTimer(Timer timer) {
    sentTimer = timer;
  }

  public void setReceivedTimer(Timer timer) {
    receivedTimer = timer;
  }

  public boolean isSending() {
    return sending;
  }

  public boolean isReceiving() {
    return receiving;
  }

  public Timer getSentTimer() {
    return sentTimer;
  }

  public Timer getReceivedTimer() {
    return receivedTimer;
  }

  public ForwardingChannel getChannel() {
    return channel;
  }
}
