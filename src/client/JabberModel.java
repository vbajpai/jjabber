package client;

import java.io.IOException;
import java.io.Writer;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jabber.*;
public class JabberModel {
TestThread clientThread;
public JFrame frame=new JFrame("Jabber Instant Messanger");//modified in Main
  public JabberModel(TestThread qThread) {
    packetQueue = qThread.getQueue();
    qThread.addListener(new OpenStreamHandler(),"stream:stream");
    qThread.addListener(new CloseStreamHandler(),"/stream:stream");
    qThread.addListener(new MessageHandler(),"message");
    qThread.addListener(authHandler,"jabber:iq:auth");
    qThread.addListener(new IQHandler(this),"iq");
    qThread.addListener(new RosterHandler(frame, qThread),"jabber:iq:roster");
    clientThread=qThread;
  }

  // Create the global queue and session everyone works with
  Session session = new Session();
  PacketQueue packetQueue;
  Authenticator authenticator = new Authenticator();
  public int getSessionStatus() {
    return session.getStatus();
  }

  String version = "v. 1.0 - ch. 4";
  public String getVersion(){ return version; }

  String sName;
  public String getServerName()               {return sName;}
  public void   setServerName(String name)    {sName = name;}

  String sAddress;
  public String getServerAddress()            {return sAddress;}
  public void   setServerAddress(String addr) {sAddress = addr;}

  String sPort;
  public String getPort()                     {return sPort;}
  public void   setPort(String port)          {sPort = port;}

  String user;
  public String getUser()                     {return user;}
  public void   setUser(String usr)           {user = usr; }

  String resource;
  public String getResource()                 {return resource;}
  public void   setResource(String res)       {resource = res;}
  
  String auth;
  public String getAuthMode() {return auth;}
  public void   setAuthMode(String mode) { auth = mode; }
  
  String password;
  public String getPassword() {return password;}
  public void   setPassword(String pass) {password = pass;}
  String jabberID;
  public String getJabberID() {return user+"@"+sName+"/"+resource;}
  
  
  
  public void addStatusListener(StatusListener listener){
    // Create the sesssion and get it setup
    session.addStatusListener(listener);
  }

  public void removeStatusListener(StatusListener listener){
    session.removeStatusListener(listener);
  }

  PacketListener authHandler = new AuthHandler(this);
  Hashtable resultHandlers = new Hashtable();
  public void addResultHandler(String id_code,PacketListener listener){
    resultHandlers.put(id_code,listener);
  }
  public PacketListener removeResultHandler(String id_code){
    return (PacketListener)resultHandlers.remove(id_code);
  }

  public void connect() throws IOException {

    // Create a socket
    session.setSocket(new Socket(sAddress,Integer.parseInt(sPort)));
    session.setStatus(Session.CONNECTED);

    // Process incoming messages
    (new ProcessThread(packetQueue,session)).start();

    // Send our own "open stream" packet with the server name
    Writer out = session.getWriter();
    session.setJID(new JabberID(user,sName,resource));
    out.write("<?xml version='1.0' encoding='UTF-8' ?><stream:stream to='");
    out.write(sName);
    out.write("' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'>");
    out.flush();
  }

  // Send close stream fragment
  public void disconnect() throws IOException {
	System.out.println("disconnecting");
	 Writer out =session.getWriter();
	  out.write("/stream:stream");
	  out.flush();
	System.out.println("Disconnected");
	session.closeStream();
    clientThread.disconnect=true;
    out.write("/stream:stream>");
  }

  // Register as user with given password
  public void register() throws IOException {
    if (auth.equals("0k")){
      register0k();
    } else {
      registerPlain();
    }
  }

  void registerPlain() throws IOException {
    Writer out = session.getWriter();
    out.write("<iq type='set' id='reg_id'><query xmlns='jabber:iq:register'><username>");
    out.write(this.user);
    out.write("</username><password>");
    out.write(this.password);
    out.write("</password></query></iq>");
    out.flush();
    addResultHandler("reg_id",new RegisterHandler(this));
  }

  void register0k() throws IOException {
    String token = authenticator.randomToken();
    String hash = authenticator.getZeroKHash(100,
                                             token.getBytes(),
                                             password.getBytes());
    Writer out = session.getWriter();
    out.write("<iq type='set' id='reg_id'><query xmlns='jabber:iq:register'><username>");
    out.write(this.user);
    out.write("</username><sequence>");
    out.write("100");
    out.write("</sequence><token>");
    out.write(token);
    out.write("</token><hash>");
    out.write(hash);
    out.write("</hash></query></iq>");
    out.flush();
    // Notice password is never sent or stored on server
    addResultHandler("reg_id",new RegisterHandler(this));
  }

  // Authenticate as user with given password
  int counter; // used to generate auth id's
  public void authenticate() throws IOException {
    if (auth.equals("0k")){
      authenticate0k();
    } else if (auth.equals("digest")){
      authenticateDigest();
    } else {
      authenticatePlain();
    }
  }

  // Authenticate as user with given password
  void authenticatePlain() throws IOException {
    addResultHandler("plain_auth_" + Integer.toString(counter),authHandler);
    Writer out = session.getWriter();
    out.write("<iq type='set' id='plain_auth_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(this.user);
    out.write("</username><resource>");
    out.write(this.resource);
    out.write("</resource><password>");
    out.write(this.password);
    out.write("</password></query></iq>");
    out.flush();
  }

  // Authenticate as user with given password
  void authenticateDigest() throws IOException {
    addResultHandler("digest_auth_" + Integer.toString(counter),authHandler);
    Writer out = session.getWriter();
    out.write("<iq type='set' id='digest_auth_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(this.user);
    out.write("</username><resource>");
    out.write(this.resource);
    out.write("</resource><digest>");
    out.write(authenticator.getDigest(session.getStreamID(),password));
    out.write("</digest></query></iq>");
    out.flush();
  }

  // Authenticate as user with given password
  void authenticate0k() throws IOException {
    // Initiate authentication here... it is finished in AuthHandler
    Writer out = session.getWriter();
    out.write("<iq type='get' id='auth_get_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(this.user);
    out.write("</username></query></iq>");
    out.flush();
  }

  public void sendMessage(String recipient,
                          String subject,
                          String thread,
                          String type,
                          String id,
                          String body) throws IOException {
    Packet packet = new Packet("message");

    if (recipient != null){
      packet.setTo(recipient);
    }
    if (id != null){
      packet.setID(id);
    }
    if (type != null){
      packet.setType(type);
    }
    if (subject != null){
      packet.getChildren().add(new Packet("subject",subject));
    }
    if (thread != null){
      packet.getChildren().add(new Packet("thread",thread));
    }
    if (body != null){
      packet.getChildren().add(new Packet("body",body));
    }
    packet.writeXML(session.getWriter());
  }

  public void sendRosterGet() throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("get");
    packet.setID("roster_get");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    packet.writeXML(session.getWriter());
  }

  public void sendRosterRemove(String jid) throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("set");
    packet.setID("roster_remove");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    Packet item = new Packet("item");
    item.setAttribute("subscription","remove");
    item.setAttribute("jid",jid);
    item.setParent(query);
    packet.writeXML(session.getWriter());
  }

  public void sendRosterSet(String jid,
                            String name,
                            Iterator groups) throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("set");
    packet.setID("roster_set");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    Packet item = new Packet("item");
    item.setAttribute("jid",jid);
    item.setAttribute("name",name);
    item.setParent(query);
    while (groups.hasNext()){
      new Packet("group",(String)groups.next()).setParent(item);
    }
    packet.writeXML(session.getWriter());
  }

  public void sendPresence(String recipient,
                           String type,
                           String show,
                           String status,
                           String priority) throws IOException {
    Packet packet = new Packet("presence");

    if (recipient != null){
      packet.setTo(recipient);
    }
    if (type != null){
      packet.setType(type);
    }
    if (show != null){
      packet.getChildren().add(new Packet("show",show));
    }
    if (status != null){
      packet.getChildren().add(new Packet("status",status));
    }
    if (priority != null){
      packet.getChildren().add(new Packet("priority",priority));
    }
    packet.writeXML(session.getWriter());
  }
}