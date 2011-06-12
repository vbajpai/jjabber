package jabber;

import java.io.*;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;

import log.Log;
public class JabberInputHandler extends DefaultHandler {

  PacketQueue packetQ;
  Session session;
  public JabberInputHandler(PacketQueue packetQueue) {
    packetQ = packetQueue;
  }

  public void process(Session session)
  throws IOException, SAXException {
    SAXParser parser = new SAXParser();
    parser.setContentHandler(this);
    parser.setReaderFactory(new StreamingCharFactory());

    this.session = session;
    parser.parse(new InputSource(session.getReader()));
  }


  Packet packet;
  int depth = 0;
  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Attributes atts)
                    throws SAXException{
    Log.trace("[XS] URI: " + namespaceURI + " lName: " + localName + " qName: " + qName);
    switch (depth++){
    case 0:   // Only stream:stream allowed... all others is error
      if (qName.equals("stream:stream")){
        Packet openPacket = new Packet(null,qName,namespaceURI,atts);
        openPacket.setSession(session);
        packetQ.push(openPacket);
        return;
      }
      throw new SAXException("Root element must be <stream:stream>");
    case 1:   // Only message, presence, iq
      packet = new Packet(null,qName,namespaceURI,atts);
      packet.setSession(session);
      break;
    default:  // Inside packet
      Packet child = new Packet(packet,qName,namespaceURI,atts);
      packet = child;
    }
  }

  public void characters(char[] ch,
                     int start,
                     int length)
              throws SAXException{

    Log.trace("[XC] " + new String(ch,start,length));

    if (depth > 1){
      packet.getChildren().add(new String(ch,start,length));
    }
  }

  public void endElement(java.lang.String uri,
                         java.lang.String localName,
                         java.lang.String qName)
                  throws SAXException {

    Log.trace("[XE] finished with " + qName);

    switch(--depth){
    case 0:   // We're back at the end of the root
      Packet closePacket = new Packet("/stream:stream");
      closePacket.setSession(session);
      packetQ.push(closePacket);
      break;
    case 1:   // The Packet is finished
      packetQ.push(packet);
      break;
    default:  // Move back up the tree
      packet = packet.getParent();
    }
  }
}

