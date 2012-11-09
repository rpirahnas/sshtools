package com.sshtools.ext.jzlib;

import com.sshtools.j2ssh.transport.compression.SshCompression;

public class ZLibCompression implements SshCompression {

  public ZLibCompression() {
  stream=new ZStream();
  }


  static private final int BUF_SIZE=4096;

private int type;
private ZStream stream;
private byte[] tmpbuf=new byte[BUF_SIZE];


public void init(int type, int level){
  if(type==SshCompression.DEFLATER){
    stream.deflateInit(level);
    this.type=SshCompression.DEFLATER;
  }
  else if(type==SshCompression.INFLATER){
    stream.inflateInit();
    inflated_buf=new byte[BUF_SIZE];
    this.type=SshCompression.INFLATER;
  }
}
/*
static Compression getDeflater(int level){
  Compression foo=new Compression();
  foo.stream.deflateInit(level);
  foo.type=DEFLATER;
  return foo;
}
*/
private byte[] inflated_buf;
/*
static Compression getInflater(){
  Compression foo=new Compression();
  foo.stream.inflateInit();
  foo.inflated_buf=new byte[BUF_SIZE];
  foo.type=INFLATER;
  return foo;
}
*/

public byte[] compress(byte[] buf, int start, int len){
  stream.next_in=buf;
  stream.next_in_index=start;
  stream.avail_in=len-start;
  int status;
  int outputlen=start;

  do{
    stream.next_out=tmpbuf;
    stream.next_out_index=0;
    stream.avail_out=BUF_SIZE;
    status=stream.deflate(JZlib.Z_PARTIAL_FLUSH);
    switch(status){
      case JZlib.Z_OK:
          // Resize the buffer and re-allocate
          byte[] foo = new byte[start+outputlen+BUF_SIZE-stream.avail_out];
          if(start+outputlen > 0) {
            System.arraycopy(buf, 0, foo, start + outputlen,
                             BUF_SIZE - stream.avail_out);
          }
          buf = foo;
          System.arraycopy(tmpbuf, 0,
                           buf, outputlen,
                           BUF_SIZE-stream.avail_out);
          outputlen+=(BUF_SIZE-stream.avail_out);
          break;
      default:
          System.err.println("compress: deflate returnd "+status);
    }
  }
  while(stream.avail_out==0);

  return buf;
}

public byte[] uncompress(byte[] buffer, int start, int length){
  int inflated_end=0;

  stream.next_in=buffer;
  stream.next_in_index=start;
  stream.avail_in=length;

  while(true){
    stream.next_out=tmpbuf;
    stream.next_out_index=0;
    stream.avail_out=BUF_SIZE;
    int status=stream.inflate(JZlib.Z_PARTIAL_FLUSH);
    switch(status){
      case JZlib.Z_OK:
        if(inflated_buf.length<inflated_end+BUF_SIZE-stream.avail_out){
          byte[] foo=new byte[inflated_end+BUF_SIZE-stream.avail_out];
          System.arraycopy(inflated_buf, 0, foo, 0, inflated_end);
          inflated_buf=foo;
        }
        System.arraycopy(tmpbuf, 0,
                         inflated_buf, inflated_end,
                         BUF_SIZE-stream.avail_out);
        inflated_end+=(BUF_SIZE-stream.avail_out);
        length=inflated_end;
        break;
      case JZlib.Z_BUF_ERROR:
        if(inflated_end>buffer.length-start){
          byte[] foo=new byte[inflated_end+start];
          System.arraycopy(buffer, 0, foo, 0, start);
          System.arraycopy(inflated_buf, 0, foo, start, inflated_end);
          buffer=foo;
        }
        else{
          System.arraycopy(inflated_buf, 0, buffer, start, inflated_end);
        }
        length=inflated_end;
        return buffer;
      default:
        System.err.println("uncompress: inflate returnd "+status);
        return null;
    }
  }
}


}