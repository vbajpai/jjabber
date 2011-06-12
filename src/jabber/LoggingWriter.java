package jabber;

import java.io.*;

public class LoggingWriter extends FilterWriter {
  
  Writer logWriter;
  public LoggingWriter(Writer out, Writer logWriter){
    super(out);
    this.logWriter = logWriter;
  }
  
  public void write (int c) throws IOException {
    out.write(c);
    logWriter.write(c);
  }
  
  public void write (char [] text, int offset, int length) throws IOException {
    out.write(text,offset,length);
    logWriter.write(text,offset,length);
  }
  
  public void write (String s, int offset, int length) throws IOException {
    out.write(s,offset,length);
    logWriter.write(s,offset,length);
  }
  
  public void flush() throws IOException {
    out.flush();
    logWriter.flush();
  }
  
}
