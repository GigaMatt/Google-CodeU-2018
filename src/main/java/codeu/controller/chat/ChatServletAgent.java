package codeu.controller.chat;

import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;


/* Stores all the common data used by most Chat Servlets */
public class ChatServletAgent {

  /** Store class that gives access to Conversations. */
  private ConversationStore conversationStore;

  /** Store class that gives access to Messages. */
  private MessageStore messageStore;
  
  /** Store class that gives access to Video Events. */
  private VideoEventStore videoEventStore;

  /** Store class that gives access to Users. */
  private UserStore userStore;

  /**
   * Sets the ConversationStore used by this servlet. This function provides a common setup method
   * for use by the test framework or the servlet's init() function.
   */
  public void setConversationStore(ConversationStore conversationStore) {
    this.conversationStore = conversationStore;
  }

  /**
   * Sets the MessageStore used by this servlet. This function provides a common setup method for
   * use by the test framework or the servlet's init() function.
   */
  public void setMessageStore(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  /**
   * Sets the VideoEventStore used by this servlet. This function provides a common setup method for
   * use by the test framework or the servlet's init() function.
   */
  public void setVideoEventStore(VideoEventStore videoEventStore) {
    this.videoEventStore = videoEventStore;
  }

  /**
   * Sets the UserStore used by this servlet. This function provides a common setup method for use
   * by the test framework or the servlet's init() function.
   */
  public void setUserStore(UserStore userStore) {
    this.userStore = userStore;
  }

  ConversationStore getConversationStore() {
      return this.conversationStore;
  }

  MessageStore getMessageStore() {
      return this.messageStore;
  }
  
  VideoEventStore getVideoEventStore() {
    return this.videoEventStore;
  }

  UserStore getUserStore() {
      return this.userStore;
  }
}