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

package codeu.model.data;

import java.time.Instant;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class UserTest {

  @Test
  public void testCreate() {
    UUID id = UUID.randomUUID();
    String name = "test_username";
    String password = "test password";
    Instant creation = Instant.now();

    User user = new User(id, name, password, creation);

    Assert.assertEquals(id, user.getId());
    Assert.assertEquals(name, user.getName());
    Assert.assertEquals(password, user.getPassword());
    Assert.assertEquals(creation, user.getCreationTime());
  }

  @Test
  public void testEquals() {
    UUID id = UUID.randomUUID();
    String name = "test_username";
    String password = "test password";
    Instant creation = Instant.now();

    User user1 = new User(id, name, password, creation);
    User user2 = new User(id, name, password, creation);
    User user3 = new User(UUID.randomUUID(), name, password, creation);
    User user4 = new User(id, "test_username2", password, creation);
    User user5 = new User(id, name, "test password 2", creation);
    User user6 = new User(id, name, password, creation.plusSeconds(1));

    Assert.assertTrue(user1.equals(user2));
    Assert.assertFalse(user1.equals(user3));
    Assert.assertFalse(user1.equals(user4));
    Assert.assertFalse(user1.equals(user5));
    Assert.assertFalse(user1.equals(user6));
  }
}
