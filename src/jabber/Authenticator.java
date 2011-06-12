package jabber;

import java.security.MessageDigest;
import java.security.SecureRandom;
import jabber.*;
import log.Log;

public class Authenticator {

  static SecureRandom random;
  static {
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
    } catch (Exception ex){
      Log.error("Could not create SecureRandom ", ex);
      System.exit(-1);
    }
  }
  MessageDigest sha;

  public Authenticator() {
    try {
      sha = MessageDigest.getInstance("SHA");
    } catch (Exception ex){
      Log.error("Could not create SHA MessageDigest ", ex);
      System.exit(-1);
    }
  }

  public String getZeroKHash(int sequence, byte[] token, byte[] password){

    // Running hash becomes hash(A)
    byte[] runningHash = sha.digest(password);

    // Running hash becomes hash(0)
    sha.update(HexString.toString(runningHash).getBytes());
    runningHash = sha.digest(token);

    // Increment (sequence - 1) times to get hash(sequence)
    for (int i = 0; i < sequence; i++) {
      runningHash = sha.digest(HexString.toString(runningHash).getBytes());
    }
    return HexString.toString(runningHash);
  }

  public String getDigest(String streamID, String password){
    sha.update(streamID.getBytes());
    return HexString.toString(sha.digest(password.getBytes()));
  }

  public boolean isDigestAuthenticated(String streamID, String password, String digest) {
    return digest.equals(getDigest(streamID,password));
  }

  public boolean isHashAuthenticated(String userHash, String testHash){
    testHash = HexString.toString(sha.digest(testHash.getBytes()));
    return testHash.equals(userHash);
  }

  static public String randomToken(){
    return Integer.toHexString(random.nextInt());
  }
}