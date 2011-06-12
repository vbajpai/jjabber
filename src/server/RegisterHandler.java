package server;

import java.io.IOException;
import jabber.*;
import log.Log;

public class RegisterHandler implements PacketListener {

  static UserIndex userIndex;
  Packet required;
  Authenticator auth = new Authenticator();

  public RegisterHandler(UserIndex index) {
    userIndex = index;
    required = new Packet("iq");
    required.setFrom(Server.SERVER_NAME);
    required.setType("result");
    new Packet("username").setParent(required);
    new Packet("password").setParent(required);
    new Packet("hash").setParent(required);
  }

  public void notify(Packet packet){
    Log.trace("Register handling " + packet.toString());

    String type = packet.getType();
    Packet query = packet.getFirstChild("query");

    if (type.equals("get")){
      required.setSession(packet.getSession());
      required.setID(packet.getID());
      MessageHandler.deliverPacket(required);
      return;

    } else if (type.equals("set")) { // type == set
      String username = query.getChildValue("username");
      User user = userIndex.getUser(username);
      if (user != null){ // user exists
        if (packet.getSession().getStatus() != Session.AUTHENTICATED ||
            !username.equals(packet.getSession().getJID().getUser())){
          Packet iq = new Packet("iq");
          iq.setSession(packet.getSession());
          iq.setID(packet.getID());
          ErrorTool.setError(iq,401,"User account already exists");
          MessageHandler.deliverPacket(iq);
          return;
        }
      } else {
        user = userIndex.addUser(username);
      }
      user.setPassword(query.getChildValue("password"));
      user.setHash(query.getChildValue("hash"));
      user.setSequence(query.getChildValue("sequence"));
      user.setToken(query.getChildValue("token"));
      if (user.getHash() == null || user.getSequence() == null || user.getToken() == null){
        if (user.getPassword() != null){
          user.setToken(Authenticator.randomToken());
          user.setSequence("99");
          user.setHash(auth.getZeroKHash(100,
                                         user.getToken().getBytes(),
                                         user.getPassword().getBytes()));
        }
      } else {
        user.setSequence(Integer.toString(Integer.parseInt(user.getSequence()) - 1));
      }
      Packet iq = new Packet("iq");
      iq.setSession(packet.getSession());
      iq.setID(packet.getID());
      iq.setType("result");   // success
      MessageHandler.deliverPacket(iq);
      Log.trace("Register successfully registered " + username + " with password " + query.getChildValue("password"));
    } else {
      Log.info("Register ignoring " + packet.toString());
    }
  }
}
