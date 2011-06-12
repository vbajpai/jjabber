package jabber;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;


public class Packet {

  public Packet(String element){
    setElement(element);
  }

  public Packet(String element, String value){
    setElement(element);
    children.add(value);
  }

  public Packet(Packet parent, String element, String namespace, Attributes atts){
    setElement(element);
    setNamespace(namespace);
    setParent(parent);

    // Copy attributes into hashtable
    for (int i = 0; i < atts.getLength(); i++){
      attributes.put(atts.getQName(i), atts.getValue(i));
    }
  }

  Packet parent;
  public Packet getParent()             { return parent;        }
  public void   setParent(Packet parent){
    this.parent = parent;
    if (parent != null){
      parent.children.add(this);
    }
  }

  LinkedList children = new LinkedList();

  public LinkedList getChildren() {return children;}

  public Packet getFirstChild(String subelement){
    Iterator childIterator = children.iterator();
    while (childIterator.hasNext()){
      Object child = childIterator.next();
      if (child instanceof Packet){
        Packet childPacket = (Packet)child;
        if (childPacket.getElement().equals(subelement)) {
          return childPacket;
        }
      }
    }
    return null;
  }

  public String getChildValue(String subelement){
    Packet child = getFirstChild(subelement);
    if (child == null){
      return null;
    }
    return child.getValue();
  }

  public String getValue(){
    StringBuffer value = new StringBuffer();
    Iterator childIterator = children.iterator();
    while (childIterator.hasNext()){
      Object valueChild = childIterator.next();
      if (valueChild instanceof String){
        value.append((String)valueChild);
      }
    }
    return value.toString().trim();
  }

  Session session;
  public void     setSession(Session session) { this.session = session; }
  public Session  getSession() {
    if (session != null){
      return session;
    }
    if (parent != null){
      return parent.getSession();
    }
    return null;
  }

  String namespace;
  public void   setNamespace(String name) { namespace = name; }
  public String getNamespace()            { return namespace; }

  String element;
  public void   setElement(String element) { this.element = element; }
  public String getElement()               { return element;         }

  Hashtable attributes = new Hashtable();

  public String getAttribute(String attribute) {
    return (String)attributes.get(attribute);
  }

  public void  setAttribute(String attribute, String value) {
    if (value == null){
      removeAttribute(attribute);
    } else {
      attributes.put(attribute,value);
    }
  }

  public void removeAttribute(String attribute){
    attributes.remove(attribute);
  }

  public void clearAttributes(){
    attributes.clear();
  }

  public String getTo()                 { return (String)attributes.get("to");   }
  public void   setTo(String recipient) { setAttribute("to",recipient);}

  public String getFrom()               { return (String)attributes.get("from"); }
  public void   setFrom(String sender)  { setAttribute("from",sender); }

  public String getType()               { return (String)attributes.get("type"); }
  public void   setType(String type)    { setAttribute("type",type);   }

  public String getID()                 { return (String)attributes.get("id");   }
  public void   setID(String ID)        { setAttribute("id",ID);       }

  public void writeXML() throws IOException {
    writeXML(session.getWriter());
  }

  public void writeXML(Writer out) throws IOException{
    out.write("<");
    out.write(element);
    Enumeration keys = attributes.keys();
    while (keys.hasMoreElements()){
      String key = (String)keys.nextElement();
      out.write(" ");
      out.write(key);
      out.write("='");
      out.write((String)attributes.get(key));
      out.write("'");
    }

    if (children.size() == 0){
      out.write("/>");
      out.flush();
      return;
    }

    out.write(">");
    Iterator childIterator = children.iterator();
    while (childIterator.hasNext()){
      Object child = childIterator.next();
      if (child instanceof String){
        out.write((String)child);
      } else {
        ((Packet)child).writeXML(out);
      }
    }
    out.write("</");
    out.write(element);
    out.write(">");
    out.flush();
  }

  public String toString(){
    try {
      StringWriter reply = new StringWriter();
      writeXML(reply);
      return reply.toString();
    } catch (Exception ex){
    }
    return "<" + element + ">";
  }
}