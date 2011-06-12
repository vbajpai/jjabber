package server;

import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import jabber.*;

public class User {

  public User(String username) { roster = new Roster(username); }
  String pass;
  public void setPassword(String password){ pass = password; }
  public String getPassword(){ return pass;}

  String hash;
  public void   setHash(String value) { hash = value; }
  public String getHash()             { return hash;  }

  String sequence;
  public void   setSequence(String value) { sequence = value;}
  public String getSequence()             { return sequence; }

  String token;
  public void   setToken(String value){ token = value;}
  public String getToken() {return token;}

  Roster roster;
  public Roster getRoster() { return roster; }

  // Stored messages for later delivery (store & forward)
  LinkedList messageStore = new LinkedList();
  public void storeMessage(Packet msg){ messageStore.add(msg); }
    // Deliver stored messages
  public void deliverMessages(){
    while (messageStore.size() > 0){
      Packet storedMsg = (Packet)messageStore.removeFirst();
      storedMsg.setSession(activeSession);
      storedMsg.setTo(null);
      MessageHandler.deliverPacket(storedMsg);
     }
  }
// Resource (String) to Session object
  Hashtable resources = new Hashtable();
public Iterator getSessions(){
    return resources.values().iterator();
  }

  // Primary session
  Session activeSession;
public void changePriority(Session session){
    if (activeSession.getPriority() < session.getPriority()){
      activeSession = session;
    }
  }

  public void addSession(Session session){
    resources.put(session.getJID().getResource(),session);
    if (activeSession == null){
      activeSession = session;
    } else if (activeSession.getPriority() < session.getPriority()){
      activeSession = session;
    }
    
    deliverMessages();
  }

  public void removeSession(Session session){
    resources.remove(session.getJID().getResource());
    activeSession = null;

    Iterator sessionIterator = resources.values().iterator();
    if (sessionIterator.hasNext()){
      activeSession = (Session)sessionIterator.next();
      while (sessionIterator.hasNext()){
        Session sess = (Session)sessionIterator.next();
        if (sess.getPriority() > activeSession.getPriority()){
          activeSession = sess;
        }
      }
    }
  }

  public Writer getWriter(String resource) throws IOException {
    Session session;
    if (resource == null){
      session = activeSession;
    } else if (resource.length() == 0){
      session = activeSession;
    } else {
      session = (Session)resources.get(resource);
    }
    if (session == null){
      return null;
    }
    return session.getWriter();
  }
}