package client;
import jabber.*;
import log.Log;

public class IQHandler implements PacketListener {

  JabberModel jabberModel;
  
  public IQHandler(JabberModel model){
    jabberModel = model;
  }  

  public void notify(Packet packet) {
    if (packet.getID() != null){
      PacketListener listener = jabberModel.removeResultHandler(packet.getID());
      if (listener != null){
        listener.notify(packet);
        return;
      }
    }
    Log.trace("Dropping : " + packet.toString());
  }
}