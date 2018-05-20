package codeu.model.store.persistence;

import codeu.model.data.Conversation;
import codeu.model.data.Message;
import codeu.model.data.User;
import codeu.model.data.VideoEvent;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for PersistentDataStore.
 *
 * <p>Keeping it an interface so that we can pass in mock versions of it. (... it'd probably
 * still work if it was only a class, but it's still a bit easier to mock interfaces than
 * classes; also, if we ever wanted to swap out the implementation, this'd make it easier).</p>
 */
public interface PersistentDataStore {

  /**
   * Loads all User objects from the Datastore service and returns them in a List.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  List<User> loadUsers() throws PersistentDataStoreException;

  /**
   * Loads all Conversation objects from the Datastore service and returns them in a List.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  List<Conversation> loadConversations() throws PersistentDataStoreException;

  /**
   * Loads all Message objects from the Datastore service and returns them in a List.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  List<Message> loadMessages() throws PersistentDataStoreException;

  /**
   * Loads all Video Event objects from the Datastore service and returns them in a List.
   *
   * @throws PersistentDataStoreException if an error was detected during the load from the
   *     Datastore service
   */
  List<VideoEvent> loadVideoEvents() throws PersistentDataStoreException;

  /** Write a User object to the Datastore service. */
  void writeThrough(User user);

  /** Write a Message object to the Datastore service. */
  void writeThrough(Message message);

  /** Write a VideoEvent object to the Datastore service. */
  void writeThrough(VideoEvent videoEvent);

  /** Write a Conversation object to the Datastore service. */
  void writeThrough(Conversation conversation);
}
