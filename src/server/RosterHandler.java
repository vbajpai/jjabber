package server;

import jabber.*;
import log.Log;

public class RosterHandler implements PacketListener {

  UserIndex userIndex;
  public RosterHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet) {
    packet.setTo(null);
    packet.setFrom(null);

    if (packet.getSession().getStatus() != Session.AUTHENTICATED){
      Log.info("RosterHandler: Not authenticated" + packet.toString());
      ErrorTool.setError(packet,401,"You must be authenticated to send roster update");
      MessageHandler.deliverPacket(packet);
      return;
    }

    User user = userIndex.getUser(packet.getSession());
    if (packet.getType().equals("set")){
      user.getRoster().updateRoster(packet);
      return;
    }

    if (packet.getType().equals("get")){
      Log.trace("RosterHandler: get roster dump");
      packet.setType("result");
      packet.getChildren().clear();
      user.getRoster().getPacket().setParent(packet);
      MessageHandler.deliverPacket(packet);
      return;
    }

    Log.info("RosterHandler: Unknown packet" + packet.toString());
    ErrorTool.setError(packet,400,"What kind of IQ is this?");
    MessageHandler.deliverPacket(packet);
  }
}