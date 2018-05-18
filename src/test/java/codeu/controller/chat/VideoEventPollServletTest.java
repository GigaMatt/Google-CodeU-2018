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
import codeu.model.data.VideoEvent;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;

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
import org.mockito.Mock;
import org.mockito.Mockito;

public class VideoEventPollServletTest {

  private VideoEventPollServlet videoEventPollServlet;
  private HttpServletRequest mockRequest;
  private HttpSession mockSession;
  private HttpServletResponse mockResponse;
  private ServletOutputStream mockOutputStream;
  private ConversationStore mockConversationStore;
  private VideoEventStore mockVideoEventStore;
  private UserStore mockUserStore;

  @Before
  public void setup() {
    videoEventPollServlet = new VideoEventPollServlet();

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
    mockVideoEventStore = Mockito.mock(VideoEventStore.class);
    mockUserStore = Mockito.mock(UserStore.class);

    ChatServletAgent chatServletAgent = new ChatServletAgent();
    chatServletAgent.setConversationStore(mockConversationStore);
    chatServletAgent.setVideoEventStore(mockVideoEventStore);
    chatServletAgent.setUserStore(mockUserStore);

    ChatRequestValidator chatRequestValidator = Mockito.spy(new ChatRequestValidator(chatServletAgent));

    videoEventPollServlet.setChatServletAgent(chatServletAgent);
    videoEventPollServlet.setChatRequestValidator(chatRequestValidator);
  }

  @Test
  public void testDoPost_UserNotLoggedIn() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/video/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn(null);

    videoEventPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(videoEventPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/video/poll/");
    Mockito.verify(videoEventPollServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "User not logged in!");
  }

  @Test
  public void testDoPost_InvalidUser() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/video/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");
    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(null);

    videoEventPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(videoEventPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/video/poll/");
    Mockito.verify(videoEventPollServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "User not found!");
  }

  @Test
  public void testDoPost_ConversationNotFound() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/video/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");

    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
        .thenReturn(null);

    videoEventPollServlet.doPost(mockRequest, mockResponse);

    Mockito.verify(videoEventPollServlet.getChatRequestValidator()).validateRequest(mockRequest, "/chat/video/poll/");
    Mockito.verify(videoEventPollServlet.getChatRequestValidator()).respondWithErrorMessage(mockResponse, "Conversation not found!");
  }

  @Test
  public void testDoPost_FoundNewVideoEvent() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/video/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");

    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Conversation fakeConversation = new Conversation(UUID.randomUUID(), fakeUser.getId(),
            "test_conversation", Instant.now());

    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
            .thenReturn(fakeConversation);


    Mockito.when(mockRequest.getParameter("lastVideoEventTime")).thenReturn(Instant.now().minusSeconds(5).toString());

    VideoEvent fakeVideoEvent = new VideoEvent(UUID.randomUUID(), fakeConversation.getId(),
            fakeUser.getId(), "testVideoId", Instant.now(), VideoEvent.getTestVideoStateJSON(), fakeUser.getId(), 5);

    List<VideoEvent> mockVideoEvents = new ArrayList<>();
    mockVideoEvents.add(fakeVideoEvent);

    Mockito.when(mockVideoEventStore.getVideoEventsInConversation(fakeConversation.getId())).thenReturn(mockVideoEvents);

    Mockito.when(mockRequest.getParameter("curSeek")).thenReturn("6");

    videoEventPollServlet.doPost(mockRequest, mockResponse);

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

    JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
    Assert.assertEquals(true, responseData.getBoolean("success"));
    Assert.assertEquals(true, responseData.getBoolean("foundNewVideoEvent"));
    Assert.assertEquals(fakeVideoEvent.getVideoStateJSON(), responseData.getString("newVideoState"));
    Assert.assertEquals(fakeVideoEvent.getCreationTime().toString(), responseData.getString("newVideoEventCreationTime"));
  }

  @Test
  public void testDoPost_NoNewVideoEventFound() throws IOException, ServletException, JSONException {
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/chat/video/poll/test_conversation");
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_username");

    User fakeUser = new User(UUID.randomUUID(), "test_username", "test password", "member",
            Instant.now(), "test description");

    Mockito.when(mockUserStore.getUser("test_username")).thenReturn(fakeUser);

    Conversation fakeConversation = new Conversation(UUID.randomUUID(), fakeUser.getId(),
            "test_conversation", Instant.now());

    Mockito.when(mockConversationStore.getConversationWithTitle("test_conversation"))
            .thenReturn(fakeConversation);

    Instant currentInstant = Instant.now();

    Mockito.when(mockRequest.getParameter("lastVideoEventTime")).thenReturn(currentInstant.toString());

    VideoEvent fakeVideoEvent = new VideoEvent(UUID.randomUUID(), fakeConversation.getId(),
            fakeUser.getId(), "testVideoId", currentInstant, VideoEvent.getTestVideoStateJSON(), fakeUser.getId(), 5);

    List<VideoEvent> mockVideoEvents = new ArrayList<>();
    mockVideoEvents.add(fakeVideoEvent);

    Mockito.when(mockVideoEventStore.getVideoEventsInConversation(fakeConversation.getId())).thenReturn(mockVideoEvents);

    Mockito.when(mockRequest.getParameter("curSeek")).thenReturn("6");

    videoEventPollServlet.doPost(mockRequest, mockResponse);

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

    JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
    Assert.assertEquals(true, responseData.getBoolean("success"));
    Assert.assertEquals(false, responseData.getBoolean("foundNewVideoEvent"));
  }
}
