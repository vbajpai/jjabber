package gui;

import jabber.Authenticator;
import jabber.Session;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import client.JabberModel;
import client.TestThread;

public class LoginDialogue extends JDialog{
	JPanel panel[]={new JPanel(),new JPanel(),new JPanel() };
	JLabel user=new JLabel("User Name");
	JLabel pass=new JLabel("Password");
	JTextField txtUser=new JTextField(10);
	JPasswordField txtPass=new JPasswordField(10);
	JButton ok=new JButton("OK");
	JButton reset=new JButton("Reset");
	JButton register=new JButton("Register");
	TestThread clientThread=new TestThread();
	JabberModel model=new JabberModel(clientThread);
	public LoginDialogue()
	{

		Container cp=getContentPane();
		cp.setLayout(new GridLayout(3,1));
		txtPass.setEchoChar('*');
		System.out.print("Initializing authenticator...");
		Authenticator.randomToken();
		System.out.println(" initialized.  Starting client...");
		String server =  System.getProperty("jab.server.name",   "noob");
		String address = System.getProperty("jab.server.address","127.0.0.1");
		String port =    System.getProperty("jab.server.port",   "5222");
		String auth =		 System.getProperty("jab.server.auth",   "plain");
		model.setServerName(server);
		model.setServerAddress(address);
		model.setPort(port);
		model.setAuthMode(auth);


		for(JPanel temp:panel)
		{

			cp.add(temp);
		}
		panel[0].add(user);
		panel[0].add(txtUser);
		panel[1].add(pass);
		panel[1].add(txtPass);
		panel[2].add(ok);
		panel[2].add(reset);
		panel[2].add(register);

		addWindowListener(new WindowAdapter(){

			@Override
			public void windowClosing(WindowEvent arg0) {
				System.exit(0);
			}

		});

		ok.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//Add password verification mechanism later

				model.setUser(txtUser.getText());
				model.setPassword(txtPass.getText());
				model.setResource("dev");
				clientThread.setModel(model);
				try
				{
					model.connect();
				}catch(Exception ex)
				{
					System.out.println(ex);
				}
				clientThread.start();
				new Main(model, clientThread);


				dispose();
			}
		});
		register.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				model.setUser(txtUser.getText());
				model.setPassword(txtPass.getText());
				model.setResource("dev");
				try{model.connect();
					model.register();
				}catch(Exception ex)
				{
					System.out.println(ex);
				}
				clientThread.setModel(model);
				clientThread.start();
				new Main(model, clientThread);
				dispose();
				
			}
		});

		reset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				txtUser.setText("");
				txtPass.setText("");					


			}
		});


		setSize(250,150);
		setVisible(true);


	}
	public static void main(String args[])
	{
		new LoginDialogue();
	}

}
