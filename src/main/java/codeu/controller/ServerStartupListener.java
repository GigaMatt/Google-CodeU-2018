package codeu.controller;

import codeu.injection.AppInjector;
import codeu.model.data.Conversation;
import codeu.model.data.Message;
import codeu.model.data.User;
import codeu.model.data.VideoEvent;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;
import codeu.model.store.persistence.PersistentDataStore;
import codeu.model.store.persistence.PersistentDataStoreException;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Listener class that fires when the server first starts up, before any servlet classes are
 * instantiated.
 */
public class ServerStartupListener implements ServletContextListener {
  private PersistentDataStore persistentDataStore;
  private ConversationStore conversationStore;
  private MessageStore messageStore;
  private UserStore userStore;
  private VideoEventStore videoEventStore;

  /** Loads data from Datastore. */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      AppInjector.getInstance().inject(this);

      List<User> users = persistentDataStore.loadUsers();
      //Always have a master administrator
      User admin = new User(UUID.randomUUID(), "admin",
              BCrypt.hashpw("admin password", BCrypt.gensalt()), "admin", Instant.now(),
              "Master Administrator");
      users.add(admin);
      userStore.setUsers(users);

      List<Conversation> conversations = persistentDataStore.loadConversations();
      conversationStore.setConversations(conversations);

      List<Message> messages = persistentDataStore.loadMessages();
      messageStore.setMessages(messages);

      List<VideoEvent> videos = persistentDataStore.loadVideoEvents();
      videoEventStore.setVideoEvents(videos);


    } catch (PersistentDataStoreException e) {
      System.err.println("Server didn't start correctly. An error occurred during Datastore load.");
      System.err.println("This is usually caused by loading data that's in an invalid format.");
      System.err.println("Check the stack trace to see exactly what went wrong.");
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}

  public void setPersistentDataStore(PersistentDataStore persistentDataStore) {
    this.persistentDataStore = persistentDataStore;
  }

  public void setConversationStore(ConversationStore conversationStore) {
    this.conversationStore = conversationStore;
  }

  public void setMessageStore(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  public void setUserStore(UserStore userStore) {
    this.userStore = userStore;
  }

  public void setVideoEventStore(VideoEventStore videoEventStore) {
    this.videoEventStore = videoEventStore;
  }
}
