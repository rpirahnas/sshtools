package com.sshtools.test.unit.daemon.windows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sshtools.daemon.windows.WindowsAuthentication;
import com.sshtools.test.unit.AbstractTestCase;

public class WindowsAuthenticationTestCase
    extends AbstractTestCase {
  private WindowsAuthentication authentication;
  private Map tokens = new HashMap();

  public WindowsAuthenticationTestCase(String arg0) {
    super(arg0);
    authentication = new WindowsAuthentication();
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(WindowsAuthenticationTestCase.class);
  }

  public void testGetHomeDirectory() {
    try {
      String dir = authentication.getHomeDirectory(TEST_USER); //, tokens);
      assertEquals("Wrong home directory for " + TEST_USER,
                   TEST_HOME_DIRECTORY, dir);
    }
    catch (IOException ex) {
    }
  }

  public void testLogonUser() {
    try {
      boolean result = authentication.logonUser(TEST_USER,
                                                TEST_PASSWORD); //, tokens);
      assertTrue("Failed to logonUser.", result);
      System.out.println("Expected failure...");
      result = authentication.logonUser(TEST_USER, "x"); //, tokens);
      //assertFalse("logonUser should have failed.", result);
      result = authentication.logonUser(TEST_USER); //, tokens);
      assertTrue("Failed to logonUser.", result);
    }
    catch (IOException ex) {
    }
  }
}
