package jabber;

public class HexString {
  public static String toString(byte[] bytes){
    StringBuffer buf = new StringBuffer(bytes.length * 2);
    for(int i = 0; i < bytes.length; i++){
      int hex = bytes[i];
      if (hex < 0) {
        hex = 256 + hex;
      }
      if (hex >=16) {
        buf.append(Integer.toHexString(hex));
      } else {
        buf.append('0');
        buf.append(Integer.toHexString(hex));
      }
    }
    return buf.toString().toLowerCase();
  }

  public static byte[] toBytes(String hex){
    byte[] bytes = new byte[hex.length() / 2];
    try {
      for (int i = 0; i < bytes.length; i++){
        int num = Integer.parseInt(hex.substring(i*2,(i*2) + 2),16);
        if (num > 127){
          num = num - 256;
        }
        bytes[i] = (byte)num;
      }
    }
    catch (Exception ex){
    }
    return bytes;
  }
}