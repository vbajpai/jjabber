package gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;

import client.JabberModel;
import client.TestThread;

public class Main  {
	JFrame frame;
	JButton addBuddy=new JButton("Add Buddy");
	JabberModel model;
	JTextField textField=new JTextField();
	TestThread clientThread;
	JDialog addBuddyPopup=new JDialog();
	public Main(JabberModel mode, TestThread client_Thread)
	{	this.model=mode;
		frame=model.frame;
		frame.setLayout(new FlowLayout());
		
		clientThread=client_Thread;
		addBuddy.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent arg0) {
				addBuddyPopup.setSize(300,100);
				addBuddyPopup.setTitle("Add Buddy");
				addBuddyPopup.add(textField);
				addBuddyPopup.setVisible(true);
				textField.addActionListener(new ActionListener(){

					public void actionPerformed(ActionEvent arg0) {
						try{
						
						model.sendPresence(textField.getText(),"subscribe",null,null,null);
				        model.sendRosterGet();
				        textField.setText("");
				        addBuddyPopup.dispose();
						}catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}
					
				});
			}
			
		});
		frame.add(addBuddy);
		frame.setSize(300, 500);
		frame.setVisible(true);
		frame.addWindowListener(new WindowListener(){

			public void windowActivated(WindowEvent arg0) {
				System.out.println("Activated");
			}

			@Override
			public void windowClosed(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.out.println("window closing");
				try{
					System.out.println("Window closed");
				model.sendPresence(null,"unavailable",null,null,null);//5
		        model.disconnect();
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
				System.exit(0);
			}

			@Override
			public void windowDeactivated(WindowEvent arg0) {
				System.out.println("window deactivated");
			}

			@Override
			public void windowDeiconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("window opened");	
			}
			
		});
		
		
	}
	
	
	
	
}

