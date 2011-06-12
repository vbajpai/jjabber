package client;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import jabber.*;

public class RosterHandler implements PacketListener {
	JFrame frame;
	Packet child;
	LinkedList list;
	TestThread clientThread;
	HashMap buddyList=new HashMap();
	public RosterHandler(JFrame frame,TestThread clientThread)
{
	this.frame=frame;
	this.clientThread=clientThread;
}
  // handle incoming presence and roster packets
  public void notify(Packet packet) {
    System.out.print("roster: ");
    System.out.println(packet.toString());
    if (packet.getElement().equals("iq")){
        child = packet.getFirstChild("query");
        if (child != null){
        	list=child.getChildren();
        	Iterator iter=list.iterator();
        	while(iter.hasNext())
        	{
        		String id=iter.next().toString();
        		if(id.equals("  ")==false)
        			id=id.substring(11,id.lastIndexOf("'"));
        		
        		if(buddyList.get(id)==null)
        		{	if(id.equals("  ")==false)
        		{
        			JButton button=new JButton(id);
        			button.addActionListener(new ChatWindowButtonHandler(button,clientThread));
        			System.out.println("new Entry made to buddy list in roaster handler"+id+ id.length());
        			buddyList.put(id, button);
        			frame.add(button);
        			frame.hide();
        			frame.show();
        		}	
        		}
        	}
        	
        	System.out.println(child);
        }
        else
        {
        	System.out.println(child+" else case");
             	
        }
        }
        
}
}