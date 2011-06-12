package jabber;

import java.io.*;

public class XercesReader extends FilterReader {

  public XercesReader(InputStream in){
    super(new InputStreamReader(in));
  }

  private XercesReader(Reader in){
    super(in);
  }
  int sendBlank = 0;

  public int read() throws IOException {
    if (sendBlank > 0) {
      sendBlank--;
      return (int)' ';
    }
    int b = in.read();
    if (b == (int)'>'){
      sendBlank = 2;
    }
    return b;
  }

  public int read(char [] text, int offset, int length) throws IOException {
    int numRead = 0;
    for (int i = offset; i < offset + length; i++){
      int temp = this.read();
      if (temp == -1) break;
      text[i] = (char) temp;
      numRead++;
    }
    if (numRead == 0 && length != 0) numRead = -1;
    return numRead;
  }
 }
