package com.sshtools.ext.putty;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.cipher.SshCipher;
import com.sshtools.j2ssh.transport.publickey.InvalidSshKeyException;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKeyFormat;
import com.sshtools.j2ssh.util.Base64;
import com.sshtools.j2ssh.util.Hash;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Lee David Painter
 * @version $Id: PuttyPrivateKeyFormat.java,v 1.5 2003/09/22 15:58:06 martianx Exp $
 */

public class PuttyPrivateKeyFormat
    implements SshPrivateKeyFormat {
  public PuttyPrivateKeyFormat() {
  }

  public boolean isPassphraseProtected(byte[] formattedKey) {
    BufferedReader reader = new BufferedReader(
        new InputStreamReader(
        new ByteArrayInputStream(formattedKey)));

    try {
      String line = reader.readLine();

      if (line != null
          && (line.startsWith("PuTTY-User-Key-File-2:")
              || line.equals("PuTTY-User-Key-File-1:"))) {
        line = reader.readLine();
        if (line != null && line.startsWith("Encryption:")) {
          String encryption = line.substring(line.indexOf(":") + 1).trim();
          if (encryption.equals("aes256-cbc")) {

            ConfigurationLoader.getExtensionClass(
                "com.sshtools.ext.bouncycastle.cipher.AES256Cbc");
            return true;
          }
        }
      }
    }
    catch (Exception ex) {}

    return false;

  }

  public boolean isFormatted(byte[] formattedKey) {

    BufferedReader reader = new BufferedReader(
        new InputStreamReader(
        new ByteArrayInputStream(formattedKey)));

    try {
      String line = reader.readLine();

      return (line != null
              && (line.startsWith("PuTTY-User-Key-File-2:")
                  || line.equals("PuTTY-User-Key-File-1:")));
    }
    catch (IOException ex) {
      return false;
    }

  }

  public byte[] decryptKeyblob(byte[] formattedKey, String passphrase) throws
      InvalidSshKeyException {

    BufferedReader reader = new BufferedReader(new InputStreamReader(new
        ByteArrayInputStream(formattedKey)));

    try {
      String line = reader.readLine();

      if (line != null && (line.startsWith("PuTTY-User-Key-File-2:")
                           || line.equals("PuTTY-User-Key-File-1:"))) {

        int format = line.startsWith("PuTTY-User-Key-File-2:") ? 2 : 1;
        String type = line.substring(line.indexOf(":") + 1).trim();

        line = reader.readLine();

        if (line != null && line.startsWith("Encryption:")) {
          String encryption = line.substring(line.indexOf(":") + 1).trim();

          line = reader.readLine();

          if (line != null && line.startsWith("Comment:")) {
            String comment = line.substring(line.indexOf(":") + 1).trim();

            line = reader.readLine();

            if (line != null && line.startsWith("Public-Lines:")) {

              try {

                int publiclines = Integer.parseInt(line.substring(line.indexOf(
                    ":") + 1).trim());

                String publickey = "";
                for (int i = 0; i < publiclines; i++) {
                  line = reader.readLine();
                  if (line != null) {
                    publickey += line;
                  }
                  else {
                    throw new InvalidSshKeyException();
                  }
                }

                ByteArrayReader pub = new ByteArrayReader(Base64.decode(
                    publickey));

                line = reader.readLine();

                if (line != null && line.startsWith("Private-Lines:")) {
                  int privatelines = Integer.parseInt(line.substring(line.
                      indexOf(":") + 1).trim());

                  String privatekey = "";
                  for (int i = 0; i < privatelines; i++) {
                    line = reader.readLine();
                    if (line != null) {
                      privatekey += line;
                    }
                    else {
                      throw new InvalidSshKeyException();
                    }
                  }

                  byte[] blob = Base64.decode(privatekey);

                  if (encryption.equals("aes256-cbc")) {
                    // Unencrypt the private key
                    Class cls =
                        ConfigurationLoader.getExtensionClass(
                        "com.sshtools.ext.bouncycastle.cipher.AES256Cbc");

                    SshCipher cipher = (SshCipher) cls.newInstance();

                    byte[] iv = new byte[40];
                    byte[] key = new byte[40];

                    try {
                      Hash hash = new Hash("SHA");
                      hash.putInt(0);
                      hash.putBytes(passphrase.getBytes());
                      byte[] key1 = hash.doFinal();

                      hash.putInt(1);
                      hash.putBytes(passphrase.getBytes());
                      byte[] key2 = hash.doFinal();

                      System.arraycopy(key1, 0, key, 0, 20);
                      System.arraycopy(key2, 0, key, 20, 20);

                      cipher.init(SshCipher.DECRYPT_MODE, iv, key);

                      blob = cipher.transform(blob);

                    }
                    catch (NoSuchAlgorithmException ex1) {
                      throw new InvalidSshKeyException();
                    }

                  }

                  // Read the private key data
                  ByteArrayReader bar = new ByteArrayReader(blob);
                  ByteArrayWriter baw = new ByteArrayWriter();

                  // Convert the private key into the format requried by J2SSH
                  if (type.equals("ssh-dss")) {

                    // Read the required variables from the public key
                    pub.readString(); // Ignore sice we already have it
                    BigInteger p = pub.readBigInteger();
                    BigInteger q = pub.readBigInteger();
                    BigInteger g = pub.readBigInteger();

                    /* And for "ssh-dss", it will be composed of
                     *
                     *    mpint  x                  (the private key parameter)
                         *  [ string hash   20-byte hash of mpints p || q || g   only in old format ]*/

                    // now read the private exponent from the private key
                    BigInteger x = bar.readBigInteger();

                    if (format == 1) {

                    }

                    // Combine into the format required by J2SSH
                    baw.writeString("ssh-dss");
                    baw.writeBigInteger(p);
                    baw.writeBigInteger(q);
                    baw.writeBigInteger(g);
                    baw.writeBigInteger(x);

                    return baw.toByteArray();

                  }
                  else {

                    // Read the requried variables from the public key
                    pub.readString(); // Ignore since we already have it
                    BigInteger e = pub.readBigInteger();
                    BigInteger n = pub.readBigInteger();

                    /*    mpint  private_exponent
                         *    mpint  p                  (the larger of the two primes)
                     *    mpint  q                  (the smaller prime)
                     *    mpint  iqmp               (the inverse of q modulo p)
                         *    data   padding            (to reach a multiple of the cipher block size)
                     */

                    // Read the private key variables from putty file
                    BigInteger p = bar.readBigInteger();

                    // Transform into the format requried by J2SSH
                    baw.writeString("ssh-rsa");
                    baw.writeBigInteger(e);
                    baw.writeBigInteger(n);
                    baw.writeBigInteger(p);

                    return baw.toByteArray();
                  }

                }

              }
              catch (NumberFormatException ex) {}

            }
          }
        }

      }
    }
    catch (Exception ex) {}

    throw new InvalidSshKeyException();

  }

  public byte[] encryptKeyblob(byte[] keyblob, String passphrase) throws
      InvalidSshKeyException {
    return null;
  }

  public boolean supportsAlgorithm(String algorithm) {
    return algorithm.equals("ssh-dss") || algorithm.equals("ssh-rsa");
  }

  public String getFormatType() {
    return "Putty-PrivateKey-File";
  }

}