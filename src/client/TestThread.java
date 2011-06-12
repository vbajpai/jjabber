package client;

import gui.ChatWindow;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;

import jabber.*;
import log.Log;
public class TestThread extends Thread {
	ChatWindow chatWindow;
	Boolean disconnect=false;
	HashMap openChat=new HashMap();//list of people with whom you have chat windows open
	public void removeOpenChatEntry(String user)
	{
		openChat.remove(user);
	}
	public void addOpenChatEntry(String user,ChatWindow cw)
	{
		openChat.put(user, cw);
	}
	public Object openChatContains(String user)
	{
		return openChat.get(user);
	}
	public void setDisconnect(boolean disconnect)
	{
		this.disconnect=disconnect;
	}
	public boolean getDisconnect(){return disconnect;}
	PacketQueue packetQueue = new PacketQueue();
	public PacketQueue getQueue() { return packetQueue; }
	HashMap packetListeners = new HashMap();
	public boolean addListener(PacketListener listener, String element){
    if (listener == null || element == null){
      return false;
    }
    packetListeners.put(listener,element);
    return true;
	}
	public boolean removeListener(PacketListener listener){
    packetListeners.remove(listener);
    return true;
	}
	public void run(){
	  try{
	  // model.connect();
       //change
       //model.register();
       model.authenticate();
       do {
           notifyHandlers(packetQueue.pull());
       } while (model.getSessionStatus() != Session.AUTHENTICATED);
       model.sendRosterGet();
         while (disconnect==false) {
             
             Packet packet = waitFor("message", null);
             if(openChat.containsKey(packet.getFrom())==true)
             {
            	 chatWindow=(ChatWindow)openChat.get(packet.getFrom());
            	 chatWindow.jTextArea1.append(packet.getFrom() + ": " + packet.getChildValue("body") + "\n");
 
             }
             else
             {
            	 //System.out.println(packet.getFrom()+"in testhread run");
            	 chatWindow=new ChatWindow(packet.getTo(),packet.getFrom(),this);
            	 openChat.put(packet.getFrom(),chatWindow );
            	 chatWindow.jTextArea1.append(packet.getFrom() + ": " + packet.getChildValue("body") + "\n");
            	 chatWindow.setVisible(true);
             }
           
       }

       
       model.disconnect();
     } catch (Exception ex){
       ex.printStackTrace();
     }

	  
  } 
	JabberModel model;
	public void setModel(JabberModel newModel){
    model = newModel;
  }
	public JabberModel getModel(){return model;}
	public Packet waitFor(String element, String type){
    for( Packet packet = packetQueue.pull();
         packet != null;
         packet = packetQueue.pull()) {
      notifyHandlers(packet);
      if (packet.getElement().equals(element)){
        if (type != null){
          if (packet.getType().equals(type)){
            return packet;
          }
        } else {
          return packet;
        }
      }
    }
    return null;
  }
	void notifyHandlers(Packet packet){
    try {
    Packet child;
    String matchString;
    if (packet.getElement().equals("iq")){
      child = packet.getFirstChild("query");
      if (child == null){
        matchString = "iq";
      } else {
        matchString = child.getNamespace();
      }
    } else {
      matchString = packet.getElement();
    }

    synchronized(packetListeners){
        Iterator iter = packetListeners.keySet().iterator();
        while (iter.hasNext()){
        PacketListener listener = (PacketListener)iter.next();
        String listenerString = (String)packetListeners.get(listener);
        if (listenerString.equals(matchString)){
            listener.notify(packet);
        } 
        } 
    } 
    } catch (Exception ex){
      Log.error("TestThread: ", ex);     }
  }
}
