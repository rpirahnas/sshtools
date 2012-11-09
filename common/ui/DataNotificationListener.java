package com.sshtools.common.ui;

import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.connection.ChannelEventAdapter;
import com.sshtools.j2ssh.connection.Channel;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DataNotificationListener
      extends ChannelEventAdapter {

    Timer timerReceiving;
    Timer timerSending;
    StatusBar bar;
    public DataNotificationListener(StatusBar bar) {
      this.bar = bar;
      timerReceiving = new Timer(500, new ReceivingActionListener());
      timerReceiving.setRepeats(false);
      timerSending = new Timer(500, new SendingActionListener());
      timerSending.setRepeats(false);
    }

    public void actionPerformed(ActionEvent evt) {
      bar.setReceiving(false);
    }

    public void onDataReceived(Channel channel, byte[] data) {

      if (!timerReceiving.isRunning()) {
        bar.setReceiving(true);
        timerReceiving.start();
      }

    }

    public void onDataSent(Channel channel, byte[] data) {
      if (!timerSending.isRunning()) {
        bar.setSending(true);
        timerSending.start();
      }

    }

    class SendingActionListener
        implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        bar.setSending(false);
      }
    }

    class ReceivingActionListener
        implements ActionListener {
      public void actionPerformed(ActionEvent evt) {
        bar.setReceiving(false);
      }
    }

  }
