package jabber;

import org.apache.xerces.readers.DefaultReaderFactory;
import org.apache.xerces.readers.StreamingCharReader;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.ChunkyByteArray;
import org.apache.xerces.utils.StringPool;
import org.xml.sax.InputSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Stack;

public class StreamingCharFactory extends DefaultReaderFactory {
  public XMLEntityHandler.EntityReader createCharReader(XMLEntityHandler  entityHandler,
                                                        XMLErrorReporter errorReporter,
                                                        boolean sendCharDataAsCharArray,
                                                        Reader reader,
                                                        StringPool stringPool)
  throws Exception {
      return new StreamingCharReader(entityHandler,
                                     errorReporter,
                                     sendCharDataAsCharArray,
                                     reader,
                                     stringPool);
  }

  public XMLEntityHandler.EntityReader createUTF8Reader(XMLEntityHandler entityHandler, 
                                                        XMLErrorReporter errorReporter,
                                                        boolean sendCharDataAsCharArray,
                                                        InputStream data,
                                                        StringPool stringPool)
  throws Exception {
      XMLEntityHandler.EntityReader reader;
      reader = new StreamingCharReader(entityHandler,
                                       errorReporter,
                                       sendCharDataAsCharArray,
                                       new InputStreamReader(data, "UTF8"),
                                       stringPool);
      return reader;
  }
}

