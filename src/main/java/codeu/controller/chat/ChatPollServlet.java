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

import codeu.injection.AppInjector;
import codeu.model.data.Message;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Servlet class responsible for the chat page. */
public class ChatPollServlet extends HttpServlet {
  private ChatServletAgent chatServletAgent;
  private ChatRequestValidator chatRequestValidator;

  /** Set up state for handling chat requests. */
  @Override
  public void init() throws ServletException {
    super.init();
    AppInjector.getInstance().inject(this);
  }

  public void setChatRequestValidator(ChatRequestValidator chatRequestValidator) {
    this.chatRequestValidator = chatRequestValidator;
  }

  public ChatRequestValidator getChatRequestValidator() {
    return chatRequestValidator;
  }

  /**
   * This function fires regularly when the user is on the chat screen, and this function writes back a JSON String
   * that tells if there is any new message available, and if so, the JSON data also carries the new messages.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // This will represent the data being sent in the response
    JSONObject responseData = new JSONObject();

    try {
      chatRequestValidator.validateRequest(request, "/chat/poll/");

      if (!chatRequestValidator.getUsernameOptional().isPresent()) {
        chatRequestValidator.respondWithErrorMessage(response, "User not logged in!");
        return;
      }

      if (!chatRequestValidator.getUserOptional().isPresent()) {
        chatRequestValidator.respondWithErrorMessage(response, "User not found!");
        return;
      }

      if (!chatRequestValidator.getConversationOptional().isPresent()) {
        chatRequestValidator.respondWithErrorMessage(response, "Conversation not found!");
        return;
      }


      String lastMessageTime = request.getParameter("lastMessageTime");
      Instant lastMessageInstant;
      if (lastMessageTime.equals("0")) {
        // If there was no last message (i.e, there was no message at all)
        lastMessageInstant = Instant.MIN;
      } else {
        lastMessageInstant = Instant.parse(lastMessageTime);
      }

      List<Message> messageList = chatServletAgent.getMessageStore().getMessagesInConversation(chatRequestValidator.getConversationOptional().get().getId());

      if(messageList.size() == 0) {
        // There is no message in the message store.
        responseData.put("success", true);
        responseData.put("foundNewMessages", false);
      }
      else {
        Message latestMessage = messageList.get(messageList.size()-1);

        // Checks if the latest message in message store was created after the last available message
        if(latestMessage.getCreationTime().compareTo(lastMessageInstant) > 0) {
          // Traversing back the message list as long as the message is created after the last available message.
          // Helps prevent traversing through the whole list.
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
            authorJsonObject.put("id", message.getAuthorId().toString());
            authorJsonObject.put("name", chatServletAgent.getUserStore().getUser(message.getAuthorId()).getName());

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

    // This will send the data back in JSON format
    response.getOutputStream().print(responseData.toString());
  }

  public void setChatServletAgent(ChatServletAgent chatServletAgent) {
    this.chatServletAgent = chatServletAgent;
  }
}
