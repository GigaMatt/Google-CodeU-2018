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

package codeu.controller;

import codeu.model.data.Conversation;
import codeu.model.data.Message;
import codeu.model.data.User;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

public class AdminServletTest {

  private AdminServlet adminServlet;
  private HttpServletRequest mockRequest;
  private HttpSession mockSession;
  private HttpServletResponse mockResponse;
  private RequestDispatcher mockRequestDispatcher;
  private ConversationStore mockConversationStore;
  private MessageStore mockMessageStore;
  private UserStore mockUserStore;

  @Captor
  ArgumentCaptor<ArrayList<Conversation>> cArgumentCaptor;

  @Captor
  ArgumentCaptor<ArrayList<Message>> mArgumentCaptor;


  @Before
  public void setup() {
    adminServlet = new AdminServlet();

    mockRequest = Mockito.mock(HttpServletRequest.class);
    mockSession = Mockito.mock(HttpSession.class);
    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);

    mockResponse = Mockito.mock(HttpServletResponse.class);
    mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);
    Mockito.when(mockRequest.getRequestDispatcher("/WEB-INF/view/admin.jsp"))
        .thenReturn(mockRequestDispatcher);

    mockConversationStore = Mockito.mock(ConversationStore.class);
    adminServlet.setConversationStore(mockConversationStore);

    mockMessageStore = Mockito.mock(MessageStore.class);
    adminServlet.setMessageStore(mockMessageStore);

    mockUserStore = Mockito.mock(UserStore.class);
  }

  @Test
  public void testDoGet() throws IOException, ServletException {
    adminServlet.setUserStore(mockUserStore);
    List<User> fakeUserList = new ArrayList<>();
    Mockito.when(mockUserStore.getAllUsers()).thenReturn(fakeUserList);

    List<Conversation> fakeConversationList = new ArrayList<>();
    Mockito.when(mockConversationStore.getAllConversations()).thenReturn(fakeConversationList);

    List<Message> fakeMessageList = new ArrayList<>();
    Mockito.when(mockMessageStore.getAllMessages()).thenReturn(fakeMessageList);

    adminServlet.doGet(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("conversations", fakeConversationList);
    Mockito.verify(mockRequest).setAttribute("users", fakeUserList);
    Mockito.verify(mockRequest).setAttribute("messages", fakeMessageList);
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testNonLoginAccess() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("user")).thenReturn(null);

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockSession).getAttribute("user");
    Mockito.verify(mockResponse).sendRedirect("/login");
  }

  @Test
  public void testNonAdminAccess() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("member");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockSession).getAttribute("role");
    Mockito.verify(mockResponse).sendRedirect("/login");
  }

  @Test
  public void testNonExistentUser() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(null);
    adminServlet.setUserStore(mockUserStore);

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockUserStore).getUser("test username");
    Mockito.verify(mockResponse).sendRedirect("/login");
  }

  @Test
  public void testRandomPopulate() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "admin", creation, "test description");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    adminServlet.setUserStore(mockUserStore);

    Mockito.when(mockRequest.getParameter("method")).thenReturn("random");
    Mockito.when(mockRequest.getParameter("populate")).thenReturn("populate");

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockUserStore).loadTestData();
    Mockito.verify(mockConversationStore).loadTestData();
    Mockito.verify(mockMessageStore).loadTestData();
    Mockito.verify(mockResponse).sendRedirect("/admin");
  }

  @Test
  public void testRJPopulate() throws IOException, ServletException {
    MockitoAnnotations.initMocks(this);
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "admin", creation, "description");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    adminServlet.setUserStore(mockUserStore);

    Mockito.when(mockRequest.getParameter("populate")).thenReturn("populate");
    Mockito.when(mockRequest.getParameter("method")).thenReturn("rj");

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockUserStore, times(28)).addUser(any(User.class));
    Mockito.verify(mockConversationStore).loadTestData(cArgumentCaptor.capture());
    Assert.assertEquals(22, cArgumentCaptor.getValue().size());
    Mockito.verify(mockMessageStore).loadTestData(mArgumentCaptor.capture());
    Assert.assertEquals(822, mArgumentCaptor.getValue().size());
    Mockito.verify(mockResponse).sendRedirect("/admin");
  }

  @Test
  public void testDoPost_CreateValidAdmin() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "admin", creation, "");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    adminServlet.setUserStore(mockUserStore);

    Mockito.when(mockRequest.getParameter("populate")).thenReturn(null);
    Mockito.when(mockRequest.getParameter("create")).thenReturn("create");
    Mockito.when(mockRequest.getParameter("admin name")).thenReturn("new admin");
    Mockito.when(mockRequest.getParameter("admin password")).thenReturn("new admin password");

    adminServlet.doPost(mockRequest, mockResponse);

    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

    Mockito.verify(mockUserStore).addUser(userArgumentCaptor.capture());

    User newUser = userArgumentCaptor.getValue();
    Assert.assertEquals("new admin", newUser.getName());
    Assert.assertEquals(true, BCrypt.checkpw("new admin password", newUser.getPassword()));

    Mockito.verify(mockResponse).sendRedirect("/admin");
  }


  @Test
  public void testDoPost_CreateInvalidAdmin1() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "admin", creation, "");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    adminServlet.setUserStore(mockUserStore);

    Mockito.when(mockRequest.getParameter("populate")).thenReturn(null);
    Mockito.when(mockRequest.getParameter("create")).thenReturn("create");
    Mockito.when(mockRequest.getParameter("admin name")).thenReturn("new admin");
    Mockito.when(mockRequest.getParameter("admin password")).thenReturn(null);

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("admin error", "Incomplete request: missing fields.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoPost_CreateInvalidAdmin2() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "admin", creation, "");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    adminServlet.setUserStore(mockUserStore);

    Mockito.when(mockRequest.getParameter("populate")).thenReturn(null);
    Mockito.when(mockRequest.getParameter("create")).thenReturn("create");
    Mockito.when(mockRequest.getParameter("admin name")).thenReturn("&@#$%^&*()");
    Mockito.when(mockRequest.getParameter("admin password")).thenReturn("test password");

    adminServlet.doPost(mockRequest, mockResponse);


    Mockito.verify(mockRequest).setAttribute("admin error",
            "Please enter only letters, numbers, and spaces for the username.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoPost_CreateInvalidAdmin3() throws IOException, ServletException {
    Mockito.when(mockSession.getAttribute("role")).thenReturn("admin");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "admin", creation, "");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    Mockito.when(mockUserStore.isUserRegistered("test username")).thenReturn(true);
    adminServlet.setUserStore(mockUserStore);

    Mockito.when(mockRequest.getParameter("populate")).thenReturn(null);
    Mockito.when(mockRequest.getParameter("create")).thenReturn("create");
    Mockito.when(mockRequest.getParameter("admin name")).thenReturn("test username");
    Mockito.when(mockRequest.getParameter("admin password")).thenReturn("test password");

    adminServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("admin error", "That username is already taken.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }
}
