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
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/** Servlet class responsible for the chat page. */
public class ChatServlet extends HttpServlet {

  private ChatServletAgent chatServletAgent;
  private ChatRequestValidator chatRequestValidator;

  /** Set up state for handling chat requests. */
  @Override
  public void init() throws ServletException {
    super.init();

    ChatServletAgent chatServletAgent = new ChatServletAgent();
    chatServletAgent.setConversationStore(ConversationStore.getInstance());
    chatServletAgent.setMessageStore(MessageStore.getInstance());
    chatServletAgent.setUserStore(UserStore.getInstance());

    ChatRequestValidator chatRequestValidator = new ChatRequestValidator(chatServletAgent);

    setChatServletAgent(chatServletAgent);
    setChatRequestValidator(chatRequestValidator);
  }

  public void setChatServletAgent(ChatServletAgent chatServletAgent) {
    this.chatServletAgent = chatServletAgent;
  }

  public ChatServletAgent getChatServletAgent() {
    return chatServletAgent;
  }

  public void setChatRequestValidator(ChatRequestValidator chatRequestValidator) {
    this.chatRequestValidator = chatRequestValidator;
  }

  public ChatRequestValidator getChatRequestValidator() {
    return chatRequestValidator;
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

    Conversation conversation = chatServletAgent.getConversationStore().getConversationWithTitle(conversationTitle);
    if (conversation == null) {
      // couldn't find conversation, redirect to conversation list
      System.out.println("Conversation was null: " + conversationTitle);
      response.sendRedirect("/conversations");
      return;
    }

    UUID conversationId = conversation.getId();

    List<Message> messages = chatServletAgent.getMessageStore().getMessagesInConversation(conversationId);

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

    // This will represent the data being sent in the response
    JSONObject responseData = new JSONObject();
    
    try {
      String username = (String) request.getSession().getAttribute("user");
      if (username == null) {
          // user is not logged in
          responseData.put("success", false);
          responseData.put("message", "User not logged in!");
          response.getOutputStream().print(responseData.toString());
          return;
      }

      User user = chatServletAgent.getUserStore().getUser(username);
      if (user == null) {
        // user was not found
        responseData.put("success", false);
        responseData.put("message", "User not found!");
        response.getOutputStream().print(responseData.toString());
        return;
      }

      String requestUrl = request.getRequestURI();
      String conversationTitle = requestUrl.substring("/chat/".length());

      Conversation conversation = chatServletAgent.getConversationStore().getConversationWithTitle(conversationTitle);
      if (conversation == null) {
        // couldn't find conversation
        responseData.put("success", false);
        responseData.put("message", "Conversation not found!");
        response.getOutputStream().print(responseData.toString());
        return;
      }

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

      chatServletAgent.getMessageStore().addMessage(message);

      responseData.put("success", true);
      
    } catch (JSONException e) {
      response.setStatus(500);
      response.getOutputStream().print("Unexpected JSONException Occurred");
      return;
    }
    
    // This will send the data back in JSON format
    response.getOutputStream().print(responseData.toString());
  }
}
