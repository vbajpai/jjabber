package client;

import gui.ChatWindow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class ChatWindowButtonHandler implements ActionListener {
	JButton button;
	TestThread clientThread;
	public ChatWindowButtonHandler(JButton button,TestThread clientThread)
	{
		this.button=button;
		this.clientThread=clientThread;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		String msgBeingSentTo=button.getText()+"@"+clientThread.getModel().getServerName()+"/"+clientThread.getModel().getResource();
		ChatWindow chatWindow;
		if(clientThread.openChatContains(msgBeingSentTo)!=null)
        {
       	 chatWindow=(ChatWindow)clientThread.openChatContains(msgBeingSentTo);
       	 chatWindow.show();
        }
        else
        {
       	 
       	 chatWindow=new ChatWindow(clientThread.getModel().getJabberID(),msgBeingSentTo,clientThread);
       	 chatWindow.show();
       	 clientThread.addOpenChatEntry(msgBeingSentTo, chatWindow);

        }
	}

}
