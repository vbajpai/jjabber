package server;

import java.io.Writer;

import jabber.*;
import log.Log;

public class IQHandler implements PacketListener {

  UserIndex userIndex;
  public IQHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet){
    Log.trace("Delivering packet: " + packet.toString());
    String recipient = packet.getAttribute("to");
    if (recipient.equalsIgnoreCase(Server.SERVER_NAME) || recipient == null){
      Log.trace("Dropping packets for server: " + packet.toString());
      return;
    }
    try {
      Writer out = userIndex.getWriter(recipient);
      if (out != null){
        packet.writeXML(out);
      } else {
        Log.info("Could not deliver: " + packet.toString()); // Will eventually store&forward
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
 }
}