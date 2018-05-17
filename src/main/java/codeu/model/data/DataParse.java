package codeu.model.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class DataParse {
  public HashMap<String, User> allUsers = new HashMap<>();
  private String tf;
  public List<Conversation> allConversations = new ArrayList<>();
  public List<Message> allMessages = new ArrayList<>();

  public DataParse(String textFile) {
    this.tf = textFile;
  }

  private UUID findPerson(String name) {
    UUID userID;
    if(allUsers.containsKey(name)) {
      userID = allUsers.get(name).getId();
    } else {
      userID = UUID.randomUUID();
      allUsers.put(name, new User(userID, name.charAt(0) + name.substring(1).toLowerCase(),
              "password", "member", Instant.now(), tf));
    }
    return userID;
  }

  public void parse() {
    UUID convID = null;
    Pattern p = Pattern.compile("(\\b[A-Z]{3,}\\b\\s?)+"); //Matches all-uppercase names

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("files/" + tf).getFile());
    try (Scanner scanner = new Scanner(file)) {
      scanner.useDelimiter("\n");
      while (scanner.hasNext()) {
        String token = scanner.next();

        if (token.contains("ACT")) {
          continue;
        } else if (token.contains("SCENE")) {
          //Start of a new conversation
          String nextToken = scanner.next();
          Matcher m = p.matcher(nextToken);
          if (m.find()) {
            String userName = m.group(1);
            UUID userID = findPerson(userName);
            convID = UUID.randomUUID();
            allConversations.add(new Conversation(convID, userID, token, Instant.now()));
          }
        } else if (token.matches("(\\b[A-Z]{3,}\\b\\s?)+") && !token.equals("PROLOGUE")) {
          //Meaning a person is talking.
          UUID userID = findPerson(token);
          String text = "";
          while (scanner.hasNext()) {
            String nextLine = scanner.next();
            if (nextLine.matches("(\\b[A-Z]{3,}\\b\\s?)+")) {
              //Next line is a person responding.
              allMessages.add(new Message(UUID.randomUUID(), convID, userID, text, Instant.now()));
              text = "";
              userID = findPerson(nextLine);
            } else if (nextLine.isEmpty() || p.matcher(nextLine).find()) {
              //Next line is either blank or an action, signifying the message's end.
              allMessages.add(new Message(UUID.randomUUID(), convID, userID, text, Instant.now()));
              break;
            } else {
              //Continue building up the message.
              text = text + nextLine + "\n";
            }
          }
          allMessages.add(new Message(UUID.randomUUID(), convID, userID, text, Instant.now()));
        }
      }
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }
  }
}

