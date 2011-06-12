package server;

import java.io.Writer;
import java.util.Iterator;
import jabber.*;
import log.Log;

public class MessageHandler implements PacketListener {

  static UserIndex userIndex;
  GroupChatManager chatMan = GroupChatManager.getManager();
  public MessageHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet){
    if (packet.getSession().getStatus() != Session.AUTHENTICATED){
      Log.trace("Dropping packet (no auth): " + packet.toString());
      return;
    }
    String recipient = packet.getTo();

    if (recipient == null){ // to server
      Log.trace("Dropping packet: " + packet.toString());
      return;
    }

    if (recipient.equalsIgnoreCase(Server.SERVER_NAME)){ // to server
      Log.trace("Dropping packet: " + packet.toString());
      return;
    }

    // Fill in sender as resource that sent message (anti-spoofing)
    packet.setFrom(packet.getSession().getJID().toString());

    if (packet.getAttribute("type").equals("groupchat")){
      if (chatMan.isChatPacket(packet)){
        chatMan.handleChatMessage(packet);
      } else {
        Log.trace("Dropping packet: " + packet.toString());
      }
      return;
    }

    deliverPacket(packet);
  }

  static public void deliverPacketToAll(String username, Packet packet){
    packet.setTo(null); // clear recipient
    User user = userIndex.getUser(username);
    Iterator sessions = user.getSessions();
    while (sessions.hasNext()){
      Session session = (Session)sessions.next();
      if (session.getPriority() >= 0){
        packet.setSession(session);
        deliverPacket(packet);
      }
    }
  }

  static public void deliverPacket(Packet packet){
    try {
      String recipient = packet.getTo();
      Writer out;

      if (recipient == null){
        out = packet.getSession().getWriter();
        if (out == null){
          Log.info("Undeliverable packet " + packet.toString());
          return;
        }
      } else {
        out = userIndex.getWriter(recipient);
      }
      if (out != null){
        Log.trace("Delivering packet: " + packet.toString());
        packet.writeXML(out);
      } else {
        Log.info("Store & forward: " + packet.toString());
        User user = userIndex.getUser(new JabberID(recipient).getUser());
        user.storeMessage(packet);
      }
    } catch (Exception ex){
      Log.error("MessageHandler: ", ex);
    }
  }
}