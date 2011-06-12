package client;

import javax.swing.JOptionPane;
import jabber.*;

public class MessageHandler implements PacketListener {

  public void notify(Packet packet){
    String type = packet.getType() == null ? "normal" : packet.getType();
    System.out.println("Received " + type + " message: " 
                       + packet.getChildValue("body"));
    System.out.println("    To: " + packet.getTo());
    System.out.println("  From: " + packet.getFrom());
  }
}