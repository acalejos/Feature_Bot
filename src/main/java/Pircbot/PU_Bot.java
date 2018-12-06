import org.jibble.pircbot.*;
import java.util.List;
import java.io.*;
import java.nio.charset.Charset;
import java.net.*;
import java.util.regex.Pattern;
import java.util.ArrayList;
//JSON Parser
import org.json.*;
// Imports the Google Cloud client library
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.translate.*;
//Language Code Support
import com.neovisionaries.i18n.LanguageCode;




public class PU_Bot extends PircBot {
    public static JSONArray readJsonFromUrl(String searchUrl) throws IOException, JSONException {
      URL url = new URL(searchUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setDoOutput(true);
      connection.connect();
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder results = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
            results.append(line);
        }
      connection.disconnect();
      String s = results.toString();
      JSONArray json = new JSONArray(s);
      return json;
    }

    public PU_Bot() {
        this.setName("PUB_Bot_3");

    }

    @Override
    public void onMessage(String channel, String sender,
                       String login, String hostname, String message) {
        if (message.equalsIgnoreCase("|time")) {
            String time = new java.util.Date().toString();
            sendMessage(channel, sender + ": The time is now " + time);
        }
        else if (message.contains("|translate")){
          try{
            String[] splitUp = message.split(":");
            String sentence = splitUp[1];
            System.out.println("Sentence: "+sentence);
            String toLang = splitUp[0].split(" ")[1];
            System.out.println("toLang: "+toLang);
            Pattern name = Pattern.compile(toLang,Pattern.CASE_INSENSITIVE);
            List<LanguageCode> lang = LanguageCode.findByName(name);
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            Detection detection = translate.detect(sentence);
            String target = String.format("%s",lang.get(0));
            System.out.println("Target: " + target);
            Translation translation = translate.translate(sentence,
              TranslateOption.sourceLanguage(detection.getLanguage()),
              TranslateOption.targetLanguage(target));
            String translated = translation.getTranslatedText();
            System.out.println("Translated Text: "+translated);
            sendMessage(channel,"Translated Text: "+translated);

          }
          catch (Exception e){
            System.out.println(e);
            sendMessage(channel,"|translate {desired language}: {string}\n");
          }
        }
        else if (message.contains("|detect")){
          try{
            String[] splitUp = message.split(":");
            String sentence = splitUp[1];
            System.out.println("Sentence: "+sentence);
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            Detection detection = translate.detect(sentence);
            LanguageCode detected = LanguageCode.getByCodeIgnoreCase(detection.getLanguage());
            System.out.println("Detected Language: "+detected.getName());
            sendMessage(channel,"Detected Language: "+detected.getName());
          }
          catch (Exception e){
            System.out.println(e);
            sendMessage(channel,"|detect : {string you want to know the language of}\n");
          }
        }
        else if (message.contains("|wiki")){
          try{
            String[] splitUp = message.split(":");
            String topic = splitUp[1].trim();
            topic = topic.replaceAll(" ", "_");
            System.out.println("Topic: "+topic);
            String searchURL = "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search="+topic;
            JSONArray json = readJsonFromUrl(searchURL);
            JSONArray summary = json.getJSONArray(2);
            JSONArray link = json.getJSONArray(3);
            String sumStrings[] = new String[summary.length()];
            String linkStrings[] = new String[link.length()];
            for (int i=0; i<sumStrings.length;i++){
              sumStrings[i] = summary.getString(i);
            }
            for (int i=0; i<linkStrings.length;i++){
              linkStrings[i] = link.getString(i);
            }
            String finSum = sumStrings[0];
            String finLink = linkStrings[0];
            System.out.println("Summary: "+summary+"\n");
            System.out.println("Link: "+link+"\n" );
            if (finSum != null && !finSum.isEmpty())
              sendMessage(channel,finSum+"\n");
            sendMessage(channel,"Link to full article: "+finLink+"\n");
          }
          catch (Exception e){
            System.out.println(e);
            sendMessage(channel,"|wiki : {topic to search}\n");
          }
        }
        else if (message.equalsIgnoreCase("|help")){
          String commands[];
          commands = new String[5];
          commands[0] = "PUBot Commands:\n";
          commands[1] = "|time -- Displays current date and time\n";
          commands[2] = "|translate {desired language}: {string}\n";
          commands[3] = "|detect : {string you want to know the language of}\n";
          commands[4] = "|wiki : {topic to search}\n";
          for (int i = 0; i <commands.length;i++){
            sendMessage(channel,commands[i]);
          }
        }
    }

    @Override
    public void onKick(String channel, String kickerNick, String login,
                        String hostname, String recepientNick, String reason){
        if (recepientNick.equalsIgnoreCase(getNick())){
          joinChannel(channel);
        }
    }

    @Override
    public void onDisconnect() {
      while (!isConnected()) {
        try { reconnect();
        }
        catch (Exception e) {
         // Couldn’t reconnect. // Pause for a short while before retrying?
        }
      }
    }

    @Override
    public void onUserList(String channel, User[] users){
      for (int i = 0; i < users.length; i++){
        User user = users[i];
        String nick = user.getNick();
        System.out.println(nick);
      }
    }

    @Override
    public void onNickChange(String oldNick, String login, String hostname, String newNick){
      if (oldNick != getNick()){
        String[] channels = getChannels();
        sendMessage(channels[0],oldNick +" has changed their nickname to "+newNick);
      }
    }
}
