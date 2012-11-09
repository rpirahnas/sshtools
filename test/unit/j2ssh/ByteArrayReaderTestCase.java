package com.sshtools.test.unit.j2ssh;

import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayReader;
import junit.framework.TestCase;

public class ByteArrayReaderTestCase
    extends TestCase {

  public ByteArrayReaderTestCase(String arg) {
    super(arg);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(ByteArrayReaderTestCase.class);
  }

  protected void setUp() throws Exception {
  }

  protected void tearDown() throws Exception {
  }

  public void testReadIntBArrayI() {
  }

  public void testReadInt() {
    byte[] data = {
        0x01, 0x01, 0x01, 0x01};
    ByteArrayReader reader = new ByteArrayReader(data);
    try {
      assertEquals("failed to retrieve integer.", 16843009, reader.readInt());
    }
    catch (IOException e) {
      e.printStackTrace();
      fail("failed to retrieve integer.");
    }
  }

  public void testReadStringBArrayI() {
  }

  public void testReadBigInteger() {
  }

  public void testReadBinaryString() {
  }

  public void testReadString() {
  }

}
