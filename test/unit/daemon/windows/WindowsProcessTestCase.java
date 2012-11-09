package com.sshtools.test.unit.daemon.windows;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.sshtools.daemon.windows.WindowsAuthentication;
import com.sshtools.daemon.windows.WindowsProcess;
import com.sshtools.test.unit.AbstractTestCase;

/**
 * This class is a test case for the WindowsProcess JNI call.
 * @author <A HREF="mailto:ben@benheath.com">Benjamin R Heath</A>
 */
public class WindowsProcessTestCase
    extends AbstractTestCase {

  public WindowsProcessTestCase(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(WindowsProcessTestCase.class);
  }

  /**
   * WindowsProcess really only exposes start(), getInputStream() and getOutputStream()
       * are used to get/put data to/from the process.  This tests a simple batch file.
   */
  public void testAll() {
    try {
      WindowsProcess process = new WindowsProcess();
      Map environment = new HashMap();
      Map nativeSettings = new HashMap();

      nativeSettings.put("Username", TEST_USER);
      WindowsAuthentication authentication = new
          WindowsAuthentication();
      authentication.logonUser(TEST_USER); //, nativeSettings);

      System.out.println("executing " + TEST_COMMAND);
      boolean result = process.createProcess(TEST_COMMAND, environment); //, nativeSettings);
      assertTrue("failed to start command.", result);
      InputStream in = process.getInputStream();
      OutputStream out = process.getOutputStream();
      assertNotNull("Failed to getInputStream().", in);
      assertNotNull("Failed to getOutputStream().", out);
      try {
        out.write("ben".getBytes());
        Thread.sleep(100); // wait for data
        int length = in.available();
        assertEquals("Invalid return length.", 11, length);
        byte[] buffer = new byte[length];
        in.read(buffer);
        in.close();
        assertEquals("Wrong output from process.", "Hello ben\r\n",
                     new String(buffer));
      }
      catch (Exception e) {
        e.printStackTrace();
        fail("Failed to read process stream.");
      }
    }
    catch (Exception ex) {
    }
  }

}
