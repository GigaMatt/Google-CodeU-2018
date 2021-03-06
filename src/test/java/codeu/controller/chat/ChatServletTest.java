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

package codeu.controller.chat;

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
import javax.print.DocFlavor.CHAR_ARRAY;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ChatServletTest {

  private ChatServlet chatServlet;
  private HttpServletRequest mockRequest;
  private HttpSession mockSession;
  private HttpServletResponse mockResponse;
  private RequestDispatcher mockRequestDispatcher;
  private ServletOutputStream mockOutputStream;
  private ConversationStore mockConversationStore;
  private MessageStore mockMessageStore;
  private UserStore mockUserStore;

  @Before
  public void setup() {
    chatServlet = new ChatServlet();

    mockRequest = Mockito.mock(HttpServletRequest.class);
    mockSession = Mockito.mock(HttpSession.class);
    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);

    mockResponse = Mockito.mock(HttpServletResponse.class);
    mockRequestDispatcher = Mockito.mock(RequestDispatcher.class);
    Mockito.when(mockRequest.getRequestDispatcher("/WEB-INF/view/chat.jsp"))
        .thenReturn(mockRequestDispatcher);

    mockOutputStream = Mockito.mock(ServletOutputStream.class);
    try {
		Mockito.when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);
	} catch (IOException e) {
        Mockito.doThrow(e);
	}

    mockConversationStore = Mockito.mock(ConversationStore.class);
    mockMessageStore = Mockito.mock(MessageStore.class);
    mockUserStore = Mockito.mock(UserStore.class);

	  ChatServletAgent agent = new ChatServletAgent();
    agent.setConversationStore(mockConversationStore);
    agent.setMessageStore(mockMessageStore);
    agent.setUserStore(mockUserStore);

    chatServlet.setChatServletAgent(agent);
    ChatRequestValidator chatRequestValidator = Mockito.spy(new ChatRequestValidator(agent));
    chatServlet.setChatRequestValidator(chatRequestValidator);
  }

  @Test
  public void testDoGet() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/test_conversation");

    UUID fakeConversationId = UUID.randomUUID();
    Conversation fakeConversation =
        new Conversation(fakeConversationId, UUID.randomUUID(), "test_conversation", Instant.now());
    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(fakeConversation);

    List<Message> fakeMessageList = new ArrayList<>();
    fakeMessageList.add(
        new Message(
            UUID.randomUUID(),
            fakeConversationId,
            UUID.randomUUID(),
            "test message",
            Instant.now()));
    Mockito.when(mockMessageStore.getMessagesInConversation(fakeConversationId))
        .thenReturn(fakeMessageList);

    chatServlet.doGet(mockRequest, mockResponse);

    Mockito.verify(mockRequest).setAttribute("conversation", fakeConversation);
    Mockito.verify(mockRequest).setAttribute("messages", fakeMessageList);
    Mockito.verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
  }

  @Test
  public void testDoGet_badConversation() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/bad_conversation");
    Mockito.when(mockConversationStore.getConversationWithTitle("bad_conversation"))
        .thenReturn(null);

    chatServlet.doGet(mockRequest, mockResponse);

    Mockito.verify(mockResponse).sendRedirect("/conversations");
  }

  @Test
  public void testDoPost_UserNotLoggedIn() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn(null);

    chatServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockMessageStore, Mockito.never()).addMessage(Mockito.any(Message.class));

    Mockito.verify(chatServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/");
    Mockito.verify(chatServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "User not logged in!");
  }

  @Test
  public void testDoPost_InvalidUser() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(null);

    chatServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockMessageStore, Mockito.never()).addMessage(Mockito.any(Message.class));

    Mockito.verify(chatServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/");
    Mockito.verify(chatServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "User not found!");
  }

  @Test
  public void testDoPost_ConversationNotFound() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");

    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(null);

    chatServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(mockMessageStore, Mockito.never()).addMessage(Mockito.any(Message.class));

    Mockito.verify(chatServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/");
    Mockito.verify(chatServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "Conversation not found!");
  }

  @Test
  public void testDoPost_StoresMessage() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");

    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Conversation fakeConversation =
        new Conversation(UUID.randomUUID(), UUID.randomUUID(), "test_conversation", Instant.now());
    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(fakeConversation);

    Mockito.when(mockRequest.getParameter("message")).thenReturn("Test message.");

    chatServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/");

    ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
    Mockito.verify(mockMessageStore).addMessage(messageArgumentCaptor.capture());
    Assert.assertEquals("Test message.", messageArgumentCaptor.getValue().getContent());

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

    try {
		JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
        Assert.assertEquals(true, responseData.getBoolean("success"));
	} catch (JSONException e) {
        Mockito.doThrow(e);
    }
  }

  @Test
  public void testDoPost_CleansBadHtmlContent() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Conversation fakeConversation =
        new Conversation(UUID.randomUUID(), UUID.randomUUID(), "test_conversation", Instant.now());
    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(fakeConversation);

    Mockito.when(mockRequest.getParameter("message"))
        .thenReturn("Contains bad html <img src=\'bad_image.png\'/> and <script>JavaScript</script> content.");

    chatServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/");

    ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
    Mockito.verify(mockMessageStore).addMessage(messageArgumentCaptor.capture());
    Assert.assertEquals(
        "Contains bad html  and  content.", messageArgumentCaptor.getValue().getContent());

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

    try {
		JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
        Assert.assertEquals(true, responseData.getBoolean("success"));
	} catch (JSONException e) {
        Mockito.doThrow(e);
    }
  }
}
