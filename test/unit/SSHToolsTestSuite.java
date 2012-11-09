package com.sshtools.test.unit;

import com.sshtools.test.unit.daemon.windows.WindowsAuthenticationTestCase;
import com.sshtools.test.unit.daemon.windows.WindowsProcessTestCase;
import com.sshtools.test.unit.j2ssh.ByteArrayReaderTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class SSHToolsTestSuite {

  public static void main(String[] args) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.sshtools.test.unit");
    //$JUnit-BEGIN$
    suite.addTest(new TestSuite(ByteArrayReaderTestCase.class));
    suite.addTest(new TestSuite(WindowsAuthenticationTestCase.class));
    suite.addTest(new TestSuite(WindowsProcessTestCase.class));
    //$JUnit-END$
    return suite;
  }
}
