import org.jibble.pircbot.*;

public class PU_BotMain {

    public static void main(String[] args) {

        // Now start our bot up.
        PU_Bot bot = new PU_Bot();

        // Enable debugging output.
        bot.setVerbose(true);

        try{
          // Connect to the IRC server.
          bot.connect("irc.freenode.net");
        }
        catch(Exception e){
          bot.disconnect();
          System.out.println("Can't connect: " + e);
          return;
        }

        // Join the #pircbot channel.
        bot.joinChannel("#pircbot");

    }

}
