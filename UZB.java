import java.util.ArrayList;
import java.io.*;
import java.net.*;
import java.lang.*;

public class UZB implements Visitor{
  private static boolean fail = false;
  private static boolean end = false;
  private String s;

  private void visitRules(ArrayList<Rule> rules){
    for (Rule rule : rules)
      rule.accept(this);
  }

  public Object visit(Rule_Command rule){
    visitRules(rule.rules);
    return s;
  }

  public Object visit(Rule_cmd rule){
    visitRules(rule.rules);
    switch(rule.spelling){
      case "logout": s = "Logged out."; break;
      case "list": s = "I am not a DCC file sharing bot."; break;
      case "end": s = "Bye!"; end = true; break;
      default: s += "";
    }
    return null;    
  }

  public Object visit(Rule_amount rule){
    Double amount = Double.parseDouble(rule.spelling);
    String id = this.s;
    if(amount < 1 || amount > 25000)
      this.fail = true;
    else
      this.s ="You successfully bid " + amount + " BTC on action number "+ id + ".";
    return null;
  }

  public Object visit(Rule_description rule){
    String dur = this.s;
    String st = "An auction '" + rule.spelling + "' has been created and will end after " + dur +" seconds.";
    if(st.length() < 20 || st.length() > 255)
      this.fail = true;
    else
      this.s = st;
    return null;
  }

  public Object visit(Rule_duration rule){
    int duration = Integer.parseInt(rule.spelling);
    if(duration < 300 || duration > 604800)
      this.fail = true;
    else
      this.s = "" + duration;
    return null;
  }

  public Object visit(Rule_id rule){
    int id = Integer.parseInt(rule.spelling);
    if(id < 1 || id > 65536)
      this.fail = true;
    else
      this.s ="" + id;
    return null;
  }

  public Object visit(Rule_sp rule){ return null;}

  public Object visit(Rule_username rule){
    this.s = "Successfully logged in as " + rule.spelling + "!";
    return null;
  }

  public static void main(String args[]){
    try{
      ServerSocket ss = new ServerSocket(2600);
      try {
        while (!fail && !end) {
          Socket sk = ss.accept();
          try {
            processConnection(sk);
          } finally {
            sk.close();
          }
        }
      } finally {
      ss.close();
      }
    } catch (Exception ex){
      System.out.println(ex.getMessage());
    }
  }

  private static final String ENCODING = "ISO-8859-1";

  private static void processConnection(Socket s)throws IOException{
    InputStream in = s.getInputStream();
    OutputStream out = s.getOutputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(in, ENCODING));
    out = new BufferedOutputStream(out);
    PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, ENCODING),true);
    UZB validator = new UZB();

    try{
      while(!fail && !end){
        Rule uzb = Parser.parse("Command", br.readLine());
        String uzbobj = (String) uzb.accept(validator);
        if(fail) pw.println("INVALID"); 
        else pw.println(uzbobj);
      }
    } catch(ParserException ex){
      pw.println("FAIL"); end = true;
    } finally {
      pw.close();
    }
  }

  public Object visit(Terminal_StringValue value) {return null;}
  public Object visit(Terminal_NumericValue value) {return null;}
}