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

import codeu.model.data.User;
import codeu.model.store.basic.UserStore;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mindrot.jbcrypt.BCrypt;

public class LoginServletTest {

  private LoginServlet loginServlet;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private RequestDispatcher mockRequestDispatcher;
  private HttpSession mockSession;
  private UserStore mockUserStore;

  @Before
  public void setup() {
    loginServlet = new LoginServlet();

    mockSession = Mockito.mock(HttpSession.class);

    mockRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(mockRequest.getParameter("username")).thenReturn("test username");
    Mockito.when(mockRequest.getParameter("password")).thenReturn("test password");
    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);

    mockResponse = Mockito.mock(HttpServletResponse.class);

    mockUserStore = Mockito.mock(UserStore.class);

    mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);
    Mockito.when(mockRequest.getRequestDispatcher("/WEB-INF/view/login.jsp"))
        .thenReturn(mockRequestDispatcher);
  }

  @Test
  public void testDoGet() throws IOException, ServletException {
    loginServlet.doGet(mockRequest, mockResponse);
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoPost_NonexistentUser() throws IOException, ServletException {
    Mockito.when(mockUserStore.isUserRegistered("test username")).thenReturn(false);
    loginServlet.setUserStore(mockUserStore);

    loginServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("error", "That username was not found.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoPost_SuccessfulExistingUser() throws IOException, ServletException {
    Mockito.when(mockUserStore.isUserRegistered("test username")).thenReturn(true);

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("test password", BCrypt.gensalt()),
            "member", creation);
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    loginServlet.setUserStore(mockUserStore);

    loginServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockUserStore).getUser("test username");

    Mockito.verify(mockSession).setAttribute("user", "test username");
    Mockito.verify(mockSession).setAttribute("role", "member");
    Mockito.verify(mockResponse).sendRedirect("/conversations");
  }

  @Test
  public void testDoPost_BadPassword() throws IOException, ServletException {
    Mockito.when(mockUserStore.isUserRegistered("test username")).thenReturn(true);

    UUID id = UUID.randomUUID();
    Instant creation = Instant.now();
    User fakeUser = new User(id, "test username", BCrypt.hashpw("not test password", BCrypt.gensalt()),
            "member", creation);
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(fakeUser);
    loginServlet.setUserStore(mockUserStore);

    loginServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockUserStore).getUser("test username");

    Mockito.verify(mockRequest).setAttribute("error", "Invalid password.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

}
