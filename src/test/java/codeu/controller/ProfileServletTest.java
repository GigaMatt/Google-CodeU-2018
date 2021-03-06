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

import codeu.model.store.basic.UserStore;
import codeu.model.data.User;
import java.time.Instant;
import java.util.UUID;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ProfileServletTest {

  private ProfileServlet profileServlet;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private RequestDispatcher mockRequestDispatcher;
  private HttpSession mockSession;
  private UserStore mockUserStore;

  @Before
  public void setup() {

    profileServlet = new ProfileServlet();

    mockSession = Mockito.mock(HttpSession.class);
    mockUserStore = Mockito.mock(UserStore.class);
    mockRequest = Mockito.mock(HttpServletRequest.class);
    
    User user = new User(UUID.randomUUID(), "test username", "test password", "member", Instant.now(), "test description");

    Mockito.when(mockRequest.getParameter("username")).thenReturn("test username");
    Mockito.when(mockRequest.getParameter("password")).thenReturn("test password");
    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/users/test username");
    Mockito.when(mockUserStore.getUser("test username")).thenReturn(user);


    mockResponse = Mockito.mock(HttpServletResponse.class);

    
    profileServlet.setUserStore(mockUserStore);

    mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);
    Mockito.when(mockRequest.getRequestDispatcher("/WEB-INF/view/profile.jsp"))
        .thenReturn(mockRequestDispatcher);
  }

 @Test
  public void testDoGet() throws IOException, ServletException {

    profileServlet.doGet(mockRequest, mockResponse);
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  } 

  @Test
  public void testDoPost() throws IOException, ServletException {
    
    Mockito.when(mockRequest.getParameter("description")).thenReturn("test description");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test username");
    profileServlet.doPost(mockRequest, mockResponse); 
    Mockito.verify(mockResponse).sendRedirect("/users/test username");
    
  } 

}
