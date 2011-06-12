package server;
import jabber.*;
import log.Log;

public class PresenceHandler implements PacketListener {

  UserIndex userIndex;
  GroupChatManager chatMan = GroupChatManager.getManager();
  public PresenceHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet){
    if (packet.getSession().getStatus() != Session.AUTHENTICATED){
      Log.info("PresenceHandler: Not authenticated" + packet.toString());
      packet.setTo(null);
      packet.setFrom(null);
      ErrorTool.setError(packet,401,"You must be authenticated to send presence");
      MessageHandler.deliverPacket(packet);
    } else if (chatMan.isChatPacket(packet)){
      Log.trace("PresenceHandler: groupchat presence");
      chatMan.handleChatPresence(packet);
    } else {
      Log.trace("PresenceHandler: user presence sending to user roster: " +
                packet.getSession().getJID().getUser());
      User user = userIndex.getUser(packet.getSession().getJID().getUser());
      user.getRoster().updatePresence(packet);
    }
  }
}