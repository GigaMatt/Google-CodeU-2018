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
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/** Servlet class responsible for the chat page. */
public class ChatServlet extends HttpServlet {

  /** Store class that gives access to Conversations. */
  private ConversationStore conversationStore;

  /** Store class that gives access to Messages. */
  private MessageStore messageStore;

  /** Store class that gives access to Users. */
  private UserStore userStore;

  /** Set up state for handling chat requests. */
  @Override
  public void init() throws ServletException {
    super.init();
    setConversationStore(ConversationStore.getInstance());
    setMessageStore(MessageStore.getInstance());
    setUserStore(UserStore.getInstance());
  }

  /**
   * Sets the ConversationStore used by this servlet. This function provides a common setup method
   * for use by the test framework or the servlet's init() function.
   */
  void setConversationStore(ConversationStore conversationStore) {
    this.conversationStore = conversationStore;
  }

  /**
   * Sets the MessageStore used by this servlet. This function provides a common setup method for
   * use by the test framework or the servlet's init() function.
   */
  void setMessageStore(MessageStore messageStore) {
    this.messageStore = messageStore;
  }

  /**
   * Sets the UserStore used by this servlet. This function provides a common setup method for use
   * by the test framework or the servlet's init() function.
   */
  void setUserStore(UserStore userStore) {
    this.userStore = userStore;
  }

  /**
   * This function fires when a user navigates to the chat page. It gets the conversation title from
   * the URL, finds the corresponding Conversation, and fetches the messages in that Conversation.
   * It then forwards to chat.jsp for rendering.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    String requestUrl = request.getRequestURI();
    String conversationTitle = requestUrl.substring("/chat/".length());

    Conversation conversation = conversationStore.getConversationWithTitle(conversationTitle);
    if (conversation == null) {
      // couldn't find conversation, redirect to conversation list
      System.out.println("Conversation was null: " + conversationTitle);
      response.sendRedirect("/conversations");
      return;
    }

    UUID conversationId = conversation.getId();

    List<Message> messages = messageStore.getMessagesInConversation(conversationId);

    request.setAttribute("conversation", conversation);
    request.setAttribute("messages", messages);
    request.getRequestDispatcher("/WEB-INF/view/chat.jsp").forward(request, response);
  }

  /**
   * This function fires when a user submits the form on the chat page. It gets the logged-in
   * username from the session, the conversation title from the URL, and the chat message from the
   * submitted form data. It creates a new Message from that data, adds it to the model, and then
   * redirects back to the chat page.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String username = (String) request.getSession().getAttribute("user");
    if (username == null) {
      // user is not logged in, don't let them add a message
      response.sendRedirect("/login");
      return;
    }

    User user = userStore.getUser(username);
    if (user == null) {
      // user was not found, don't let them add a message
      response.sendRedirect("/login");
      return;
    }

    String requestUrl = request.getRequestURI();
    String conversationTitle = requestUrl.substring("/chat/".length());

    Conversation conversation = conversationStore.getConversationWithTitle(conversationTitle);
    if (conversation == null) {
      // couldn't find conversation, redirect to conversation list
      response.sendRedirect("/conversations");
      return;
    }

    String action = request.getParameter("action");
    
    if (action.equals("send-message")) {
      String messageContent = request.getParameter("message");

      // Creates a basic whitelist to allow a few HTML text tags
      Whitelist whitelist = Whitelist.basic();
      whitelist.addAttributes(":all", "style");

      // this removes any HTML from the message content
      String cleanedMessageContent = Jsoup.clean(messageContent, whitelist);

      Message message =
          new Message(
              UUID.randomUUID(),
              conversation.getId(),
              user.getId(),
              cleanedMessageContent,
              Instant.now());

      messageStore.addMessage(message);

      // redirect to a GET request
      response.sendRedirect("/chat/" + conversationTitle);
    } else if (action.equals("check-new-messages")) {
      JSONObject responseData = new JSONObject();

      String lastMessageTime = request.getParameter("lastMessageTime");
      Instant lastMessageInstant;
      if (lastMessageTime.equals("0")) {
        lastMessageInstant = Instant.MIN;
      } else {
        lastMessageInstant = Instant.parse(lastMessageTime);
      }

      List<Message> messageList = messageStore.getMessagesInConversation(conversation.getId());

      try {
        if(messageList.size() == 0) {
          responseData.put("success", true);
          responseData.put("foundNewMessages", false);
        }
        else {
          Message latestMessage = messageList.get(messageList.size()-1);
          if(latestMessage.getCreationTime().compareTo(lastMessageInstant) > 0) {
            int startId = messageList.size() - 1;
            for (; startId >= 0; startId--) {
              if (messageList.get(startId).getCreationTime().compareTo(lastMessageInstant) <= 0) {
                break;
              }
            }
            startId++;

            JSONArray messageJsonArray = new JSONArray();
            for (int i = startId; i < messageList.size(); i++) {
              Message message = messageList.get(i);

              JSONObject messageJsonObject = new JSONObject();

              JSONObject authorJsonObject = new JSONObject();
              authorJsonObject.put("id", message.getAuthorId());
              authorJsonObject.put("name", userStore.getUser(message.getAuthorId()).getName());

              messageJsonObject.put("author", authorJsonObject);
              messageJsonObject.put("creationTime", message.getCreationTime().toString());
              messageJsonObject.put("content", message.getContent());

              messageJsonArray.put(messageJsonObject);
            }

            responseData.put("success", true);
            responseData.put("foundNewMessages", true);
            responseData.put("messages", messageJsonArray);
          } else {  
            responseData.put("success", true);
            responseData.put("foundNewMessages", false);
          }
        }
      } catch (JSONException e) {
        response.setStatus(500);
        response.getOutputStream().print("Unexpected JSONException Occurred");
        return;
      }
      
      response.getOutputStream().print(responseData.toString());
    }
  }
}
