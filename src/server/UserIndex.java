package server;

import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import jabber.*;
public class UserIndex {
  // user (String) -> User
  Hashtable userIndex = new Hashtable();

  // Session -> User
  Hashtable sessionIndex = new Hashtable();

  public User addUser(String name){
    User user = getUser(name);
    if (user == null){
      user = new User(name);
    }
    userIndex.put(name,user);
    return user;
  }

  public User getUser(String name){
    return (User)userIndex.get(name);
  }

  public User getUser(Session session){
    return (User)sessionIndex.get(session);
  }

  public void removeUser(String name){
    userIndex.remove(name);
  }

  public Writer getWriter(String jabberID) throws IOException {
    return getWriter(new JabberID(jabberID));
  }

  public Writer getWriter(JabberID jabberID) throws IOException {
    return getUser(jabberID.getUser()).getWriter(jabberID.getResource());
  }

  public void removeSession(Session session){
    sessionIndex.remove(session);
    if (session.getJID() == null){
      return;
    }
    getUser(session.getJID().getUser()).removeSession(session);
  }

  public void addSession(Session session){
    User user = getUser(session.getJID().getUser());
    user.addSession(session);
    sessionIndex.put(session,user);
  }
}