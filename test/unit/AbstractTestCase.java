package com.sshtools.test.unit;

import java.io.FileNotFoundException;
import java.net.URL;

import junit.framework.TestCase;

public abstract class AbstractTestCase
    extends TestCase {
  public static final String TEST_USER = "testUser";
  public static final String TEST_PASSWORD = "test";
  public static String TEST_COMMAND;
  public static final String TEST_HOME_DIRECTORY =
      "c:\\Documents and Settings\\testUser";
  protected AbstractTestCase(String arg0) {
    super(arg0);
    System.getProperties().put("sshtools.home", ".");
    System.getProperties().put("sshtools.platform", ".\\conf\\windows.xml");
    TEST_COMMAND = getClass().getResource("/hellofail.bat").getFile();
    TEST_COMMAND = TEST_COMMAND.substring(1);
  }

  protected String getTestDataDirectory() throws FileNotFoundException {
    URL testDataDirectory = getClass().getResource("/test.properties");
    if (testDataDirectory == null) {
      throw new FileNotFoundException("test.properties");
    }
    String dir = testDataDirectory.getFile();
    int pos = dir.lastIndexOf("/");
    return dir.substring(0, pos);
  }
}
