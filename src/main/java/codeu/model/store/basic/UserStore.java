// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.model.store.basic;

import codeu.model.data.User;
import codeu.model.store.persistence.PersistentDataStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Store class that uses in-memory data structures to hold values and automatically loads from and
 * saves to PersistentStorageAgent. It's a singleton so all servlet classes can access the same
 * instance.
 */
public class UserStore {
  /**
   * The PersistentStorageAgent responsible for loading Users from and saving Users to Datastore.
   */
  private PersistentDataStore persistentStorageAgent;
  private DefaultDataStore defaultDataStore;

  /** The in-memory list of Users. */
  private List<User> users;

  /** This class is a singleton, so its constructor is private. Call getInstance() instead. */
  public UserStore(
      PersistentDataStore persistentStorageAgent,
      DefaultDataStore defaultDataStore) {
    this.persistentStorageAgent = persistentStorageAgent;
    this.defaultDataStore = defaultDataStore;
    users = new ArrayList<>();
  }

  /** Add a set of randomly-generated User objects. */
  public void loadTestData() {
    users.addAll(defaultDataStore.getAllUsers());
  }

  /** Load a set of given User objects. */
  public void loadTestData(List<User> userList) {
    //Make sure master admin remains.
    users.addAll(userList);
  }

  /**
   * Access the User object with the given name.
   *
   * @return null if username does not match any existing User.
   */
  public User getUser(String username) {
    // This approach will be pretty slow if we have many users.
    for (User user : users) {
      if (user.getName().equals(username)) {
        return user;
      }
    }
    return null;
  }

  /**
   * Access the User object with the given UUID.
   *
   * @return null if the UUID does not match any existing User.
   */
  public User getUser(UUID id) {
    for (User user : users) {
      if (user.getId().equals(id)) {
        return user;
      }
    }
    return null;
  }

  /** Add a new user to the current set of users known to the application. */
  public void addUser(User user) {
    users.add(user);
    persistentStorageAgent.writeThrough(user);
  }


  /** Return true if the given username is known to the application. */
  public boolean isUserRegistered(String username) {
    for (User user : users) {
      if (user.getName().equals(username)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the List of Users stored by this UserStore. This should only be called once, when the data
   * is loaded from Datastore.
   */
  public void setUsers(List<User> users) {
    this.users = users;
  }

  /** Method to update the User with the description. */
  public void updateUser(User user) {
    persistentStorageAgent.writeThrough(user);
  }

  /** Access all users known to the application. */
  public List<User> getAllUsers() {
    return users;
  }
}
