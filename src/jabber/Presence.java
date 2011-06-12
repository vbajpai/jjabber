package jabber;

public class Presence {

  boolean available;
  public boolean isAvailable(){return available;}
  public void    setAvailable(boolean isAvailable){ available = isAvailable;}

  static public final String SHOW_CHAT = "chat";
  static public final String SHOW_AWAY = "away";
  static public final String SHOW_XA   = "xa";
  static public final String SHOW_DND  = "dnd";

  String show;
  public String getShow()               { return show;    }
  public void   setShow(String newShow) { show = newShow; }

  String status;
  public String getStatus()                 { return status;      }
  public void   setStatus(String newStatus) { status = newStatus; }

  String priority;
  public String getPriority()                   { return priority;        }
  public void   setPriority(String newPriority) { priority = newPriority; }
}