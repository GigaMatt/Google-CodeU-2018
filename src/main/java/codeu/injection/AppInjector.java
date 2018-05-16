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

/**
 * The dependency injector for the entire app.
 *
 * <p>Instead of using static instance variables, every instance in the app is being created in
 * this class. It can either create objects using their constructors (... if it's up to us to
 * create them), or, if they're created e.g. by the framework, it can set (inject) their
 * dependencies.</p>
 *
 * <p>This one class is still a global static singleton.</p>
 *
 * <p>For testing, you can just create the instances directly by passing in mocks; you don't
 * have to use this class.</p>
 */
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

  /**
   * Fetches (... and, if needed, creates) the main object instance.
   *
   * <p>Actual app objects will be only created on-demand though.</p>
   */
  public static AppInjector getInstance() {
    if (instance == null) {
      instance = new AppInjector();
    }

    return instance;
  }

  // Each of these methods is a kind of "recipe" for creating each of the objects. E.g. "to make
  // a default data store, we'd need a persistent storage object, and then we can just create one".

  // For a more fancy example, see makeChatServletAgent (demonstrating that we can configure objects
  // in more complicated ways than just calling their constructors).

  // The null checks are there so that each object is only created once.

  // Keep these private; the point is that the injector will give you things for you (say, by
  // passing it in the constructor); you don't have to explicitly ask for it.

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

  // For objects that we don't create ourselves, we let the injector give us all the dependencies
  // we need. Call this from each such object (e.g. servlet classes) before doing anything with
  // them.
  //
  // This way it's still obvious what we're passing in (... and we can rely on the injector
  // creating all the things it _can_ create).
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
