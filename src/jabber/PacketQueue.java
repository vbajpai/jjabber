package jabber;

import java.util.LinkedList;

import log.Log;
public class PacketQueue {
  LinkedList queue = new LinkedList();
  
  public synchronized void push(Packet packet){
    Log.trace("[PQ] " + packet);
    queue.add(packet);
    notifyAll();
  }

  public synchronized Packet pull(){
    try {
      while (queue.isEmpty()) {
        wait();
      }
    } catch (InterruptedException e){
      return null;
    }
    return (Packet)queue.remove(0);
  }
}
