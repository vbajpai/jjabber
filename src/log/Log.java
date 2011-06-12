package log;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Log {

  static public void trace(String msg)  { log(msg,TRACE); }
  static public void info(String msg)   { log(msg,INFO);  }
  static public void warn(String msg)   { log(msg,WARN);  }
  static public void fatal(String msg)  { log(msg,FATAL); }

  public final static int TRACE = 0;
  public final static int INFO  = 1;
  public final static int WARN  = 2;
  public final static int ERROR = 3;
  public final static int FATAL = 4;

  static int logLevel = 0;
  public static void setLogLevel(int level) { logLevel = level; }
  public static int  getLogLevel()          { return logLevel;  }

  static boolean showThread = false;
  public static void    setShowingThread(boolean show) { showThread = show; }
  public static boolean isShowingThread()              { return showThread; }

  static SimpleDateFormat dateFormat =
//    new SimpleDateFormat("yy-MM-dd HH:mm:ss:SSS ");
    new SimpleDateFormat("HH:mm:ss:SSS ");

  static public void error(String msg, Exception ex)  {
    StringWriter writer = new StringWriter();
    ex.printStackTrace(new PrintWriter(writer));
    StringTokenizer tokenizer = new StringTokenizer(writer.toString(),"\n");
    while (tokenizer.hasMoreTokens()){
      log(msg + tokenizer.nextToken(),ERROR);
    }
  }

  static public void log(String msg, int level){
    if (level >= logLevel){
      System.out.print(dateFormat.format(new Date()));
      if (showThread){
        System.out.print(msg);
        System.out.print(" *** ");
        System.out.println(Thread.currentThread().toString());
      } else {
        System.out.println(msg);
      }
    }
  }
}
