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
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ChatPollServletTest {

  private ChatPollServlet chatPollServlet;
  private HttpServletRequest mockRequest;
  private HttpSession mockSession;
  private HttpServletResponse mockResponse;
  private ServletOutputStream mockOutputStream;
  private ConversationStore mockConversationStore;
  private MessageStore mockMessageStore;
  private UserStore mockUserStore;

  @Before
  public void setup() {
    chatPollServlet = new ChatPollServlet();

    mockRequest = Mockito.mock(HttpServletRequest.class);
    mockSession = Mockito.mock(HttpSession.class);
    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);

    mockResponse = Mockito.mock(HttpServletResponse.class);
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

    chatPollServlet.setChatServletAgent(agent);

    ChatRequestValidator chatRequestValidator = Mockito.spy(new ChatRequestValidator(agent));
    chatPollServlet.setChatRequestValidator(chatRequestValidator);
  }

  @Test
  public void testDoPost_UserNotLoggedIn() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn(null);

    chatPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/poll/");
    Mockito.verify(chatPollServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "User not logged in!");
  }

  @Test
  public void testDoPost_InvalidUser() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(null);

    chatPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/poll/");
    Mockito.verify(chatPollServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "User not found!");
  }

  @Test
  public void testDoPost_ConversationNotFound() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");

    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(null);

    chatPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/poll/");
    Mockito.verify(chatPollServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "Conversation not found!");
  }

  @Test
  public void testDoPost_checkNewMessage_EmptyList() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Conversation fakeConversation =
        new Conversation(UUID.randomUUID(), UUID.randomUUID(), "test_conversation", Instant.now());
    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(fakeConversation);

    Mockito.when(mockRequest.getParameter("lastMessageTime")).thenReturn(Instant.now().toString());

    Mockito.when(mockMessageStore.getMessagesInConversation(fakeConversation.getId())).thenReturn(new ArrayList<>());

    chatPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/poll/");

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

	try {
		JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
        Assert.assertEquals(true, responseData.getBoolean("success"));
        Assert.assertEquals(false, responseData.getBoolean("foundNewMessages"));
	} catch (JSONException e) {
        Mockito.doThrow(e);
	}
  }

  @Test
  public void testDoPost_checkNewMessage_Found() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);
    Mockito.when(mockUserStore.getUser(fakeUser.getId())).thenReturn(fakeUser);

    Conversation fakeConversation =
        new Conversation(UUID.randomUUID(), UUID.randomUUID(), "test_conversation", Instant.now());
    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(fakeConversation);

    Instant currentInstant = Instant.now();

    Mockito.when(mockRequest.getParameter("lastMessageTime")).thenReturn(currentInstant.minusSeconds(10).toString());

    List<Message> fakeMessageList = new ArrayList<>();

    // Should not be added in the response
    Message actualMessage0 = new Message(
        UUID.randomUUID(),
        fakeConversation.getId(),
        fakeUser.getId(),
        "test message 0",
        currentInstant.minusSeconds(10));

    // Should be added in the response
    Message actualMessage1 = new Message(
        UUID.randomUUID(),
        fakeConversation.getId(),
        fakeUser.getId(),
        "test message 1",
        currentInstant.minusSeconds(5));

    // Should be added in the response
    Message actualMessage2 = new Message(
        UUID.randomUUID(),
        fakeConversation.getId(),
        fakeUser.getId(),
        "test message 2",
        currentInstant);

    fakeMessageList.add(actualMessage0);
    fakeMessageList.add(actualMessage1);
    fakeMessageList.add(actualMessage2);

    Mockito.when(mockMessageStore.getMessagesInConversation(fakeConversation.getId()))
        .thenReturn(fakeMessageList);

    chatPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/poll/");

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

	try {
		JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
        Assert.assertEquals(true, responseData.getBoolean("success"));
        Assert.assertEquals(true, responseData.getBoolean("foundNewMessages"));

        JSONArray messageJsonArray = responseData.getJSONArray("messages");
        JSONObject messageJsonObject1 = messageJsonArray.getJSONObject(0);
        JSONObject messageJsonObject2 = messageJsonArray.getJSONObject(1);

        Assert.assertEquals(2, messageJsonArray.length());

        Assert.assertEquals(actualMessage1.getAuthorId().toString(), messageJsonObject1.getJSONObject("author").getString("id"));
        Assert.assertEquals(mockUserStore.getUser(actualMessage1.getAuthorId()).getName(), messageJsonObject1.getJSONObject("author").getString("name"));
        Assert.assertEquals(actualMessage1.getCreationTime().toString(), messageJsonObject1.getString("creationTime"));
        Assert.assertEquals(actualMessage1.getContent(), messageJsonObject1.getString("content"));

        Assert.assertEquals(actualMessage2.getAuthorId().toString(), messageJsonObject2.getJSONObject("author").getString("id"));
        Assert.assertEquals(mockUserStore.getUser(actualMessage2.getAuthorId()).getName(), messageJsonObject2.getJSONObject("author").getString("name"));
        Assert.assertEquals(actualMessage2.getCreationTime().toString(), messageJsonObject2.getString("creationTime"));
        Assert.assertEquals(actualMessage2.getContent(), messageJsonObject2.getString("content"));
	} catch (JSONException e) {
        Mockito.doThrow(e);
	}
  }

  @Test
  public void testDoPost_checkNewMessage_NotFound() throws IOException, ServletException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);
    Mockito.when(mockUserStore.getUser(fakeUser.getId())).thenReturn(fakeUser);

    Conversation fakeConversation =
        new Conversation(UUID.randomUUID(), UUID.randomUUID(), "test_conversation", Instant.now());
    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(fakeConversation);

    Instant currentInstant = Instant.now();

    Mockito.when(mockRequest.getParameter("lastMessageTime")).thenReturn(currentInstant.toString());

    List<Message> fakeMessageList = new ArrayList<>();
    fakeMessageList.add(
        new Message(
            UUID.randomUUID(),
            fakeConversation.getId(),
            fakeUser.getId(),
            "test message",
            currentInstant));
    Mockito.when(mockMessageStore.getMessagesInConversation(fakeConversation.getId()))
        .thenReturn(fakeMessageList);

    chatPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(chatPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/poll/");

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

	try {
		JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
        Assert.assertEquals(true, responseData.getBoolean("success"));
        Assert.assertEquals(false, responseData.getBoolean("foundNewMessages"));
	} catch (JSONException e) {
        Mockito.doThrow(e);
	}
  }
}
