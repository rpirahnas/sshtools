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

package com.sshtools.daemon.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.SshThread;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolException;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.SshMsgUserAuthBanner;
import com.sshtools.j2ssh.authentication.SshMsgUserAuthFailure;
import com.sshtools.j2ssh.authentication.SshMsgUserAuthRequest;
import com.sshtools.j2ssh.authentication.SshMsgUserAuthSuccess;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.transport.AsyncService;
import com.sshtools.j2ssh.transport.Service;
import com.sshtools.j2ssh.transport.SshMessage;
import com.sshtools.j2ssh.transport.SshMessageStore;
import com.sshtools.j2ssh.transport.TransportProtocolState;
import com.sshtools.j2ssh.SshEventAdapter;
import com.sshtools.j2ssh.transport.TransportProtocol;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.15 $
 */
public class AuthenticationProtocolServer
    extends AsyncService {
  private static Log log = LogFactory.getLog(AuthenticationProtocolServer.class);
  private List completedAuthentications = new ArrayList();
  private Map acceptServices = new HashMap();
  private List availableAuths;
  private String serviceToStart;
  private int[] messageFilter = new int[1];
  private SshMessageStore methodMessages = new SshMessageStore();
  private int attempts = 0;
  private boolean completed = false;

  /**
   * Creates a new AuthenticationProtocolServer object.
   */
  public AuthenticationProtocolServer() {
    super("ssh-userauth");
    messageFilter[0] = SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST;
  }

  /**
   *
   *
   * @throws java.io.IOException
   */
  protected void onServiceAccept() throws java.io.IOException {
  }

  protected void onStart() {

  }

  protected void onStop() {

  }

  /**
   *
   *
   * @param startMode
   *
   * @throws java.io.IOException
   */
  protected void onServiceInit(int startMode) throws java.io.IOException {
    // Register the required messages
    transport.getMessageStore().registerMessage(SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST,
                                 SshMsgUserAuthRequest.class, this);
    //transport.addMessageStore(methodMessages);
  }

  /**
   *
   *
   * @return
   */
  public byte[] getSessionIdentifier() {
    return transport.getSessionIdentifier();
  }

  /**
   *
   *
   * @return
   */
  public TransportProtocolState getConnectionState() {
    return transport.getState();
  }

  /**
   *
   *
   * @param msg
   *
   * @throws IOException
   */
  public void sendMessage(SshMessage msg) throws IOException {
    transport.sendMessage(msg, this);
  }

  /**
   *
   *
   * @return
   *
   * @throws IOException
   * @throws SshException
   */
  public SshMessage readMessage() throws IOException {
    try {
      return methodMessages.nextMessage();
    }
    catch (InterruptedException ex) {
      throw new SshException("The thread was interrupted");
    }
  }

  /**
   *
   *
   * @param messageId
   * @param cls
   */
  public void registerMessage(int messageId, Class cls) {
    methodMessages.registerMessage(messageId, cls);
  }

  /**
   *
   *
   * @throws java.io.IOException
   * @throws AuthenticationProtocolException
   */
  protected void onServiceRequest() throws java.io.IOException {
    // Send a user auth banner if configured
    ServerConfiguration server = (ServerConfiguration) ConfigurationLoader
        .getConfiguration(ServerConfiguration.class);

    if (server == null) {
      throw new AuthenticationProtocolException(
          "Server configuration unavailable");
    }

    availableAuths = new ArrayList();

    Iterator it = SshAuthenticationServerFactory.getSupportedMethods()
        .iterator();
    String method;
    List allowed = server.getAllowedAuthentications();

    while (it.hasNext()) {
      method = (String) it.next();

      if (allowed.contains(method)) {
        availableAuths.add(method);
      }
    }

    if (availableAuths.size() <= 0) {
      throw new AuthenticationProtocolException(
          "No valid authentication methods have been specified");
    }

    // Accept the service request
    sendServiceAccept();

    String bannerFile = server.getAuthenticationBanner();

    if (bannerFile != null) {
      if (bannerFile.length() > 0) {
        InputStream in = ConfigurationLoader.loadFile(bannerFile);

        if (in != null) {
          byte[] data = new byte[in.available()];
          in.read(data);
          in.close();

          SshMsgUserAuthBanner bannerMsg = new SshMsgUserAuthBanner(new String(
              data));
          transport.sendMessage(bannerMsg, this);
        }
        else {
          log.info("The banner file '" + bannerFile
                   + "' was not found");
        }
      }
    }
  }

  /**
   *
   *
   * @param msg
   *
   * @throws java.io.IOException
   * @throws AuthenticationProtocolException
   */
  public void onMessageReceived(SshMessage msg) throws java.io.IOException {
    switch (msg.getMessageId()) {
      case SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST: {
        onMsgUserAuthRequest( (SshMsgUserAuthRequest) msg);

        break;
      }

      default:
        throw new AuthenticationProtocolException(
            "Unregistered message received!");
    }
  }

  /**
   *
   *
   * @return
   */
  protected int[] getAsyncMessageFilter() {
    return messageFilter;
  }

  /**
   *
   *
   * @param service
   */
  public void acceptService(Service service) {
    acceptServices.put(service.getServiceName(), service);
  }

  private void sendUserAuthFailure(boolean success) throws IOException {
    Iterator it = availableAuths.iterator();
    String auths = null;

    while (it.hasNext()) {
      auths = ( (auths == null) ? "" : (auths + ",")) + (String) it.next();
    }

    SshMsgUserAuthFailure reply = new SshMsgUserAuthFailure(auths, success);
    transport.sendMessage(reply, this);
  }

  /**
   *
   */
 /* protected void onStop() {
    try {
      // If authentication succeeded then wait for the
      // disconnect and logoff the user
      if (completed) {
        try {
          transport.getState().waitForState(TransportProtocolState.DISCONNECTED);
        }
        catch (InterruptedException ex) {
          log.warn("The authentication service was interrupted");
        }


      }
    }
    catch (IOException ex) {
      log.warn("Failed to logoff " + SshThread.getCurrentThreadUser());
    }
  }*/

  private void sendUserAuthSuccess() throws IOException {
    SshMsgUserAuthSuccess msg = new SshMsgUserAuthSuccess();
    Service service = (Service) acceptServices.get(serviceToStart);
    service.init(Service.ACCEPTING_SERVICE, transport); //, nativeSettings);
    service.start();
    transport.sendMessage(msg, this);
    transport.addService(service);
    transport.addEventHandler(new SshEventAdapter() {
      public void onDisconnect(TransportProtocol transport) {
        try {
          NativeAuthenticationProvider nap = NativeAuthenticationProvider
              .getInstance();
          nap.logoffUser();
        }
        catch (IOException ex) {
        }
      }
    });
    completed = true;
    stop();
  }


  private void onMsgUserAuthRequest(SshMsgUserAuthRequest msg) throws
      IOException {
    if (msg.getMethodName().equals("none")) {
      sendUserAuthFailure(false);
    }
    else {
      if (attempts >= ( (ServerConfiguration) ConfigurationLoader
                       .getConfiguration(ServerConfiguration.class))
          .getMaxAuthentications()) {
        // Too many authentication attempts
        transport.disconnect("Too many failed authentication attempts");
      }
      else {
        int result = AuthenticationProtocolState.FAILED;

        // If the service is supported then perfrom the authentication
        if (acceptServices.containsKey(msg.getServiceName())) {
          String method = msg.getMethodName();

          if (availableAuths.contains(method)) {
            SshAuthenticationServer auth = SshAuthenticationServerFactory
                .newInstance(method);
            serviceToStart = msg.getServiceName();

            //auth.setUsername(msg.getUsername());
            result = auth.authenticate(this, msg); //, nativeSettings);

            if (result == AuthenticationProtocolState.FAILED
                || result==AuthenticationProtocolState.CANCELLED) {
              sendUserAuthFailure(false);
            }
            else if (result == AuthenticationProtocolState.COMPLETE) {
              completedAuthentications.add(auth.getMethodName());

              ServerConfiguration sc = (ServerConfiguration)
                  ConfigurationLoader
                  .getConfiguration(ServerConfiguration.class);
              Iterator it = sc.getRequiredAuthentications()
                  .iterator();

              while (it.hasNext()) {
                if (!completedAuthentications.contains(
                    it.next())) {
                  sendUserAuthFailure(true);

                  return;
                }
              }

              SshThread.getCurrentThread().setUsername(msg.getUsername());
              //thread.setUsername(msg.getUsername());
              sendUserAuthSuccess();
            }
            else {
              // Authentication probably returned READY as no completion
              // evaluation was needed
            }
          }
          else {
            sendUserAuthFailure(false);
          }
        }
        else {
          sendUserAuthFailure(false);
        }

        if(result==AuthenticationProtocolState.FAILED)
          attempts++;
      }
    }
  }
}
