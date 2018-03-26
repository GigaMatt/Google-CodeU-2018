package codeu.controller;

import static org.junit.Assume.assumeNoException;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import codeu.model.data.User;
import codeu.model.store.basic.UserStore;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterServletTest {

  private RegisterServlet registerServlet;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private RequestDispatcher mockRequestDispatcher;
  private UserStore mockUserStore;

  @Before
  public void setup() {
    registerServlet = new RegisterServlet();
    mockRequest = Mockito.mock(HttpServletRequest.class);
    Mockito.when(mockRequest.getParameter("username")).thenReturn("test username");
    Mockito.when(mockRequest.getParameter("password")).thenReturn("test password");

    mockResponse = Mockito.mock(HttpServletResponse.class);

    mockUserStore = Mockito.mock(UserStore.class);

    mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);
    Mockito.when(mockRequest.getRequestDispatcher("/WEB-INF/view/register.jsp"))
      .thenReturn(mockRequestDispatcher);
  }

  @Test
  public void testDoGet() throws IOException, ServletException {
    registerServlet.doGet(mockRequest, mockResponse);

    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoPost_InvalidUsername() throws IOException, ServletException {
    Mockito.when(mockRequest.getParameter("username")).thenReturn("<script>alert(\'This isn't safe\')</script>");
    
    registerServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("error", "Please enter only letters, numbers, and spaces.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoPost_ExistingUser() throws IOException, ServletException {
    registerServlet.setUserStore(mockUserStore);
    
    Mockito.when(mockUserStore.isUserRegistered("test username")).thenReturn(true);
    
    registerServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("error", "That username is already taken.");
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }
  
  @Test
  public void testDoPost_NewUser() throws IOException, ServletException {
    registerServlet.setUserStore(mockUserStore);
    
    registerServlet.doPost(mockRequest, mockResponse);

    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
    Mockito.verify(mockUserStore).addUser(userArgumentCaptor.capture());
    
    User newUser = userArgumentCaptor.getValue();
    Assert.assertEquals("test username", newUser.getName());
    Assert.assertEquals(true, BCrypt.checkpw("test password", newUser.getPassword()));

    Mockito.verify(mockResponse).sendRedirect("/login");
  }
}
