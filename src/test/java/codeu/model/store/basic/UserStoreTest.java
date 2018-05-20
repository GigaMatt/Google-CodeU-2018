package codeu.model.store.basic;

import codeu.model.data.User;
import codeu.model.store.persistence.PersistentDataStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class UserStoreTest {

  private UserStore userStore;
  private PersistentDataStore mockPersistentStorageAgent;

  private final User USER_ONE =
      new User(UUID.randomUUID(), "test_username_one", "password one", "member",
              Instant.ofEpochMilli(1000), "test description");
  private final User USER_TWO =
      new User(UUID.randomUUID(), "test_username_two", "password two", "member",
              Instant.ofEpochMilli(2000), "test description");
  private final User USER_THREE =
      new User(UUID.randomUUID(), "test_username_three", "password three", "member",
              Instant.ofEpochMilli(3000), "test description");

  @Before
  public void setup() {
    mockPersistentStorageAgent = Mockito.mock(PersistentDataStore.class);
    userStore = new UserStore(mockPersistentStorageAgent,
        Mockito.mock(DefaultDataStore.class));

    final List<User> userList = new ArrayList<>();
    userList.add(USER_ONE);
    userList.add(USER_TWO);
    userList.add(USER_THREE);
    userStore.setUsers(userList);
  }

  @Test
  public void testGetUser_byUsername_found() {
    User resultUser = userStore.getUser(USER_ONE.getName());

    Assert.assertTrue(USER_ONE.equals(resultUser));
  }

  @Test
  public void testGetUser_byId_found() {
    User resultUser = userStore.getUser(USER_ONE.getId());

    Assert.assertTrue(USER_ONE.equals(resultUser));
  }

  @Test
  public void testGetUser_byUsername_notFound() {
    User resultUser = userStore.getUser("fake username");

    Assert.assertNull(resultUser);
  }

  @Test
  public void testGetUser_byId_notFound() {
    User resultUser = userStore.getUser(UUID.randomUUID());

    Assert.assertNull(resultUser);
  }

  @Test
  public void testAddUser() {
    User inputUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    userStore.addUser(inputUser);
    User resultUser = userStore.getUser("test_username");

    Assert.assertTrue(inputUser.equals(resultUser));
    Mockito.verify(mockPersistentStorageAgent).writeThrough(inputUser);
  }

  @Test
  public void testIsUserRegistered_true() {
    Assert.assertTrue(userStore.isUserRegistered(USER_ONE.getName()));
  }

  @Test
  public void testIsUserRegistered_false() {
    Assert.assertFalse(userStore.isUserRegistered("fake username"));
  }

}
