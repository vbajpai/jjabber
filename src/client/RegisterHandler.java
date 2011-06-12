package client;
import javax.swing.JOptionPane;

import jabber.*;

public class RegisterHandler implements PacketListener {

// called only on register results/errors
  JabberModel jabberModel;
  public RegisterHandler(JabberModel model){
    jabberModel = model;
  }

  public void notify(Packet packet){
    try {
      if (packet.getType().equals("result")){
        jabberModel.authenticate();
      } else {
        String message = "Failed to register";
        JOptionPane.showMessageDialog(jabberModel.frame, "User Already Registered.. Registeration Failed");
        System.exit(0);
        if (packet.getType().equals("error")){
          message = message + ": " + packet.getChildValue("error");
        }
        System.out.println("Register Handler: " + message);
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }
}