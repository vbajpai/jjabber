package jabber;

import java.io.*;

public class LoggingReader extends FilterReader {
  
  Writer logWriter;
  
  public LoggingReader(Reader in, Writer logWriter){
    super(in);
    this.logWriter = logWriter;
  }
  
  public int read() throws IOException {
    int b = in.read();
    logWriter.write((char)b);
    return b;
  }
  
  public int read(char [] text, int offset, int length) throws IOException {
    int numRead = in.read(text,offset,length);
    logWriter.write(text,offset,numRead);
    return numRead;
  }
  
}
