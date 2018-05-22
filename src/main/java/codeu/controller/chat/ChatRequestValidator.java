
package codeu.controller.chat;

import codeu.model.data.Conversation;
import codeu.model.data.User;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class ChatRequestValidator {
  private ChatServletAgent chatServletAgent;

  private Optional<String> usernameOptional;
  private Optional<User> userOptional;
  private Optional<Conversation> conversationOptional;

  public ChatRequestValidator(ChatServletAgent chatServletAgent) {
      this.chatServletAgent = chatServletAgent;
      reset();
  }

  public void reset() {
    usernameOptional = Optional.empty();
    userOptional = Optional.empty();
    conversationOptional = Optional.empty();
  }

  public void validateRequest(HttpServletRequest request, String pathPrefix) {
    reset();

    usernameOptional = Optional.ofNullable((String) request.getSession().getAttribute("user"));
    usernameOptional.ifPresent(username -> userOptional = Optional.ofNullable(chatServletAgent.getUserStore().getUser(username)));

    String conversationTitle = request.getRequestURI().substring(pathPrefix.length());
    conversationOptional = Optional.ofNullable(chatServletAgent.getConversationStore().getConversationWithTitle(conversationTitle));
  }

  public void respondWithErrorMessage(HttpServletResponse response, String errorMessage) throws JSONException, IOException {
    JSONObject responseData = new JSONObject();
    responseData.put("success", false);
    responseData.put("message", errorMessage);
    response.getOutputStream().print(responseData.toString());
  }

  public void setUsernameOptional(Optional<String> usernameOptional) {
    this.usernameOptional = usernameOptional;
  }

  public Optional<String> getUsernameOptional() {
    return usernameOptional;
  }

  public void setUserOptional(Optional<User> userOptional) {
    this.userOptional = userOptional;
  }

  public Optional<User> getUserOptional() {
    return userOptional;
  }

  public void setConversationOptional(Optional<Conversation> conversationOptional) {
    this.conversationOptional = conversationOptional;
  }

  public Optional<Conversation> getConversationOptional() {
    return conversationOptional;
  }
}