package codeu.injection;

import codeu.controller.AdminServlet;
import codeu.controller.ConversationServlet;
import codeu.controller.LoginServlet;
import codeu.controller.ProfileServlet;
import codeu.controller.RegisterServlet;
import codeu.controller.ServerStartupListener;
import codeu.controller.chat.ChatPollServlet;
import codeu.controller.chat.ChatRequestValidator;
import codeu.controller.chat.ChatServlet;
import codeu.controller.chat.ChatServletAgent;
import codeu.controller.chat.VideoEventPollServlet;
import codeu.controller.chat.VideoEventServlet;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.DefaultDataStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;
import codeu.model.store.persistence.PersistentDataStore;
import codeu.model.store.persistence.PersistentDataStoreImpl;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

public class AppInjector {
  private static AppInjector instance;

  private ConversationStore conversationStore;
  private MessageStore messageStore;
  private UserStore userStore;
  private PersistentDataStore persistentDataStore;
  private DefaultDataStore defaultDataStore;
  private ChatServletAgent chatServletAgent;
  private ChatRequestValidator chatRequestValidator;
  private VideoEventStore videoEventStore;

  public static AppInjector getInstance() {
    if (instance == null) {
      instance = new AppInjector();
    }

    return instance;
  }

  private DefaultDataStore makeDefaultDataStore() {
    if (defaultDataStore == null) {
      defaultDataStore = new DefaultDataStore(makePersistentDataStoreAgent());
    }

    return defaultDataStore;
  }

  private ConversationStore makeConversationStore() {
    if (conversationStore == null) {
      conversationStore = new ConversationStore(
          makePersistentDataStoreAgent(),
          makeDefaultDataStore());
    }
    return conversationStore;
  }

  private MessageStore makeMessageStore() {
    if (messageStore == null) {
      messageStore = new MessageStore(
          makePersistentDataStoreAgent(),
          makeDefaultDataStore());
    }
    return messageStore;
  }

  private PersistentDataStore makePersistentDataStoreAgent() {
    if (persistentDataStore == null) {
      persistentDataStore =
          new PersistentDataStoreImpl(DatastoreServiceFactory.getDatastoreService());
    }
    return persistentDataStore;
  }

  private UserStore makeUserStore() {
    if (userStore == null) {
      userStore = new UserStore(
          makePersistentDataStoreAgent(),
          makeDefaultDataStore());
    }

    return userStore;
  }

  private ChatServletAgent makeChatServletAgent() {
    if (chatServletAgent == null) {
      chatServletAgent = new ChatServletAgent();
      chatServletAgent.setConversationStore(makeConversationStore());
      chatServletAgent.setUserStore(makeUserStore());
      chatServletAgent.setMessageStore(makeMessageStore());
    }
    return chatServletAgent;
  }

  private ChatRequestValidator makeChatRequestValidator() {
    if (chatRequestValidator == null) {
      chatRequestValidator = new ChatRequestValidator(makeChatServletAgent());
    }
    return chatRequestValidator;
  }

  private VideoEventStore makeVideoEventStore() {
    if (videoEventStore == null) {
      videoEventStore = new VideoEventStore(makePersistentDataStoreAgent());
    }
    return videoEventStore;
  }

  public void inject(AdminServlet servlet) {
    servlet.setConversationStore(makeConversationStore());
    servlet.setMessageStore(makeMessageStore());
    servlet.setUserStore(makeUserStore());
  }

  public void inject(ConversationServlet conversationServlet) {
    conversationServlet.setUserStore(makeUserStore());
    conversationServlet.setConversationStore(makeConversationStore());
  }

  public void inject(LoginServlet loginServlet) {
    loginServlet.setUserStore(makeUserStore());
  }

  public void inject(ProfileServlet profileServlet) {
    profileServlet.setUserStore(makeUserStore());
  }

  public void inject(ServerStartupListener serverStartupListener) {
    serverStartupListener.setConversationStore(makeConversationStore());
    serverStartupListener.setMessageStore(makeMessageStore());
    serverStartupListener.setPersistentDataStore(makePersistentDataStoreAgent());
    serverStartupListener.setUserStore(makeUserStore());
    serverStartupListener.setVideoEventStore(makeVideoEventStore());
  }

  public void inject(ChatPollServlet servlet) {
    servlet.setChatServletAgent(makeChatServletAgent());
    servlet.setChatRequestValidator(makeChatRequestValidator());
  }

  public void inject(ChatServlet chatServlet) {
    chatServlet.setChatServletAgent(makeChatServletAgent());
  }

  public void inject(RegisterServlet registerServlet) {
    registerServlet.setUserStore(makeUserStore());
  }

  public void inject(VideoEventPollServlet videoEventPollServlet) {
    videoEventPollServlet.setChatRequestValidator(makeChatRequestValidator());
    videoEventPollServlet.setChatServletAgent(makeChatServletAgent());
  }

  public void inject(VideoEventServlet videoEventServlet) {
    videoEventServlet.setChatRequestValidator(makeChatRequestValidator());
    videoEventServlet.setChatServletAgent(makeChatServletAgent());
  }
}
