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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.daemon.platform.NativeAuthenticationProvider;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.SshMsgUserAuthPKOK;
import com.sshtools.j2ssh.authentication.SshMsgUserAuthRequest;
import com.sshtools.j2ssh.io.ByteArrayReader;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.15 $
 */
public class PublicKeyAuthenticationServer
    extends SshAuthenticationServer {
  private static Class pkv = AuthorizationFileVerification.class;
  private Log log = LogFactory.getLog(PublicKeyAuthenticationServer.class);

  /**
   * Creates a new PublicKeyAuthenticationServer object.
   */
  public PublicKeyAuthenticationServer() {
  }

  /**
   *
   *
   * @return
   */
  public String getMethodName() {
    return "publickey";
  }

  /**
   *
   *
   * @param pkv
   */
  public static void setVerificationImpl(Class pkv) {
    PublicKeyAuthenticationServer.pkv = pkv;
  }

  /**
   *
   *
   * @param authentication
   * @param msg
   *
   * @return
   *
   * @throws IOException
   */
  public int authenticate(AuthenticationProtocolServer authentication,
                          SshMsgUserAuthRequest msg) throws IOException { //, Map nativeSettings)

    ByteArrayReader bar = new ByteArrayReader(msg.getRequestData());

    // If check == 0 then authenticate, otherwise just inform that
    // the authentication can continue with the key supplied
    int check = bar.read();
    String algorithm = bar.readString();
    byte[] encoded = bar.readBinaryString();
    byte[] signature = null;

    try {
      PublicKeyVerification verify = (PublicKeyVerification) pkv
          .newInstance();

      if (check == 0) {
        // Verify that the public key can be used for authenticaiton
        //boolean ok = SshKeyPairFactory.supportsKey(algorithm);
        // Send the reply
        if (verify.acceptKey(msg.getUsername(), algorithm, encoded)) {
          SshMsgUserAuthPKOK reply = new SshMsgUserAuthPKOK(
              algorithm, encoded);
          authentication.sendMessage(reply);

          return AuthenticationProtocolState.READY;
        }
        else {
          // We return cancelled to the server so that we dont increment the
          // failure count
          return AuthenticationProtocolState.CANCELLED;
        }
      }
      else {
        signature = bar.readBinaryString();

        NativeAuthenticationProvider authProv = NativeAuthenticationProvider
            .getInstance();

        if (authProv == null) {
          log.error(
              "Authentication failed because no native authentication provider is available");

          return AuthenticationProtocolState.FAILED;
        }

        if (!authProv.logonUser(msg.getUsername())) { //, nativeSettings)) {
          log.info("Authentication failed because "
                   + msg.getUsername() + " is not a valid username");

          return AuthenticationProtocolState.FAILED;
        }

        try {
          if (verify.verifyKeySignature(msg.getUsername(), algorithm,
                                        encoded, msg.getServiceName(),
                                        authentication.getSessionIdentifier(),
                                        signature)) {
            return AuthenticationProtocolState.COMPLETE;
          }
        }
        catch (Exception ex) {
          log.error(
              "Failed to create an instance of the verification implementation",
              ex);
        }
      }
    }
    catch (Exception e) {
    }

    return AuthenticationProtocolState.FAILED;
  }
}
