package server;

import java.io.Writer;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import jabber.*;
import log.Log;
public class GroupChatManager {

  private GroupChatManager(){}

  static GroupChatManager man;
  static public GroupChatManager getManager(){
    if (man == null){
      man = new GroupChatManager();
    }
    return man;
  }

  // Group id's on this server take the form:
  // [group name].group@[server name]/[nickname]
  public boolean isChatPacket(Packet packet){
    JabberID recipient = new JabberID(packet.getTo());
    if (recipient == null){
      Log.trace("ChatMan: is NOT chat packet: " + recipient.toString());
      return false; // bad style to exit method in so many places...
    }
    String user = recipient.getUser();
    if (user == null){
      Log.trace("ChatMan: is NOT chat packet: " + recipient.toString());
      return false;
    }
    boolean rc = user.endsWith(".group")
           && recipient.equalsDomain(Server.SERVER_NAME);
    if (rc){
      Log.trace("ChatMan: is chat packet: " + recipient.toString());
    } else {
      Log.trace("ChatMan: is NOT chat packet: " + recipient.toString());
    }
    return rc;
  }

  public void handleChatPresence(Packet packet){
    JabberID recipient = new JabberID(packet.getTo());
    if (!isChatPacket(packet)){
      return;
    }

    Group group = getGroup(recipient.getUser());
    String nick = recipient.getResource();

    Log.trace("ChatMan: handling presence for " + nick + " in " + group.jid); 
    if (group.nick2jid.containsKey(nick)){
      if (group.nick2jid.get(nick).equals(packet.getFrom())){
        // user updating presence
        updatePresence(group, packet);
      } else {
        // new user registering but nick taken
        sendConflictingNicknameError(packet);
      }
    } else {
      // new user joining
      joinGroup(group,packet);
    }
  }

  public void handleChatMessage(Packet packet) {
    // TODO: should make sure sender is in group
    JabberID recipient = new JabberID(packet.getTo());
    Group group = getGroup(recipient.getUser());

    // Convert sender address to group nickname
    packet.setFrom(group.getJabberID() + "/" + (String)group.jid2nick.get(packet.getAttribute("from")));

    if (recipient.getResource() == null){
      // addressed to group
      deliverToGroup(group,packet);
    } else {
      // addressed to member
      packet.setTo((String)group.nick2jid.get(recipient.getResource()));
      Log.trace("ChatMan: delivering " + packet.toString());
      MessageHandler.deliverPacket(packet);
    }
  }

  void joinGroup(Group group, Packet packet){
    JabberID gid = new JabberID(packet.getTo());
    String sender = packet.getFrom();
    Log.trace("ChatMan: user " + sender + " joining " + packet.getTo());
    group.jid2nick.put(sender,gid.getResource());
    group.nick2jid.put(gid.getResource(),sender);
    updatePresence(group,packet);

    Iterator presencePackets = group.jid2presence.values().iterator();
    while (presencePackets.hasNext()){
      Packet p = (Packet)presencePackets.next();
      // Skip sending new user's update to user again
      if (!p.getFrom().equals(gid.toString())){
        p.setTo(sender);
        Log.trace("ChatMan: delivering " + p.toString());
        MessageHandler.deliverPacket(p);
      }
    }
    serverMessage(group, gid.getResource() + " has joined the group");
  }

  void updatePresence(Group group, Packet packet){

    Log.trace("ChatMan: updating presence " + packet.toString());
    String sender = packet.getFrom();

    // Convert sender address to group nickname
    packet.setFrom(group.getJabberID() + "/" + (String)group.jid2nick.get(sender));

    deliverToGroup(group,packet);
    group.jid2presence.put(sender,packet);

    if (packet.getType() == null){
      return;
    }

    if (packet.getAttribute("type").equals("unavailable")){
      removeUser(group,sender);
    }
  }

  public void removeUser(Group group, String jabberID){
    String nick = (String)group.jid2nick.get(jabberID);
    if (nick == null){
      return;
    }
    Log.trace("ChatMan: removing user " + jabberID);
    group.jid2nick.remove(jabberID);
    group.jid2presence.remove(jabberID);
    group.nick2jid.remove(nick);
    if (group.jid2nick.size() == 0){
      Log.trace("ChatMan: removing group " + group.getJabberID());
      groups.remove(group.getJabberID());
    } else {
      serverMessage(group,nick + " has left");
    }
  }

  public void removeUser(String jabberID){
    Iterator grps = groups.values().iterator();
    while (grps.hasNext()){
      removeUser((Group)grps.next(),jabberID);
    }
  }

  void serverMessage(Group group, String msg){
    Packet packet = new Packet("message");
    packet.setFrom(group.getJabberID());
    packet.setType("groupchat");
    Packet body = new Packet("body",msg);
    body.setParent(packet);
    deliverToGroup(group,packet);
  }

  void deliverToGroup(Group group, Packet packet){

    Enumeration members = group.jid2nick.keys();
    while(members.hasMoreElements()){
      String rec = (String)members.nextElement();
      packet.setAttribute("to",rec);
      Log.trace("ChatMan: delivering to " + rec + " " + packet.toString());
      MessageHandler.deliverPacket(packet);
    }
  }

  Group getGroup(String name){
    if (groups.containsKey(name)){
      return (Group)groups.get(name);
    } else {
      Group activeGroup = new Group(name + "@" + Server.SERVER_NAME);
      groups.put(name,activeGroup);
      return activeGroup;
    }
  }

  void sendConflictingNicknameError(Packet packet){
    try {
      Packet presence = new Packet("presence");
      presence.setFrom(packet.getFrom());
      presence.setTo(packet.getTo());
      Packet ePacket = new Packet("error");
      ePacket.setAttribute("code",Integer.toString(409));
      ePacket.getChildren().add("Conflict: nickname taken");
      ePacket.writeXML();
      ePacket.setParent(presence);
      presence.writeXML(packet.getSession().getWriter());
    }catch (Exception ex){
      Log.error("GroupChatManager: ", ex);
    }
  }

  Hashtable groups = new Hashtable();

  class Group {
    String jid;
    Group(String jabberID){ jid = jabberID; }

    String getJabberID() {return jid;}

    Hashtable nick2jid = new Hashtable();
    Hashtable jid2presence = new Hashtable();
    Hashtable jid2nick = new Hashtable();
  }
}