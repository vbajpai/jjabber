package server;

import java.util.ListIterator;

import jabber.*;
import log.Log;
public class AuthHandler implements PacketListener {

  static UserIndex userIndex;
  public AuthHandler(UserIndex index) { userIndex = index; }

  Packet iq = new Packet("iq");
  User user;
  String username;
  String resource;
  Session session;
  Authenticator auth = new Authenticator();

  public void notify(Packet packet){
    Log.trace("Auth handling " + packet.toString());

    String type = packet.getType();
    Packet query = packet.getFirstChild("query");

    username = query.getChildValue("username");

    iq.setID(packet.getID());
    iq.setSession(packet.getSession());
    iq.getChildren().clear();
    iq.setType("result");     // It will probably be a result

    user = userIndex.getUser(username);
    if (user == null){ // invalid user
      sendErrorPacket(404,"User not found");
      return;
    }

    if (type.equals("get")){
      sendGetPacket();
      return;
    } else if (type.equals("set")){
      session = packet.getSession();
      resource = query.getChildValue("resource");
      if (resource == null){
        sendErrorPacket(400,"You must send a resource");
        return;
      }
      handleSetPacket(query);
    } else {
      Log.trace("Auth dropping " + packet.toString());
    }
  }

  void handleSetPacket(Packet query){

    String password = query.getChildValue("password");
    String digest = query.getChildValue("digest");
    String hash = query.getChildValue("hash");

    if (password != null){
      if (user.getPassword().equals(password)){
        authenticated();
        return;
      }
    } else if (digest != null){
      if (auth.isDigestAuthenticated(session.getStreamID(),password,digest)){
        authenticated();
        return;
      }
    } else if (hash != null){
      if (auth.isHashAuthenticated(user.getHash(),hash)){
        user.setHash(hash);
        user.setSequence(Integer.toString(Integer.parseInt(user.getSequence()) - 1));
        authenticated();
        return;
      }
    }
    sendErrorPacket(401,"Bad user name or password");
  }

  void authenticated(){

    // Deliver authenticated confirmation packet, configure session/user index
    MessageHandler.deliverPacket(iq);
    session.setJID(new JabberID(username,Server.SERVER_NAME,resource));
    session.setStatus(Session.AUTHENTICATED);
    userIndex.addSession(session);
  }

  void sendErrorPacket(int code, String msg){
    ErrorTool.setError(iq,code,msg);
    MessageHandler.deliverPacket(iq);
  }

  void sendGetPacket () {
    // We only care about the username
    Packet reply = new Packet("query");
    reply.setAttribute("xmlns","jabber:iq:auth");
    reply.setParent(iq);
    new Packet("username",username).setParent(reply);
    new Packet("resource").setParent(reply);
    new Packet("password").setParent(reply);
    new Packet("digest").setParent(reply);
    new Packet("sequence",user.getSequence()).setParent(reply);
    new Packet("token",user.getToken()).setParent(reply);
    MessageHandler.deliverPacket(iq);
  }
}
