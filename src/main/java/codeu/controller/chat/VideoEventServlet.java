
package codeu.controller.chat;

import codeu.injection.AppInjector;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import codeu.model.data.VideoEvent;
import org.json.JSONException;
import org.json.JSONObject;

import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;

public class VideoEventServlet extends HttpServlet {
  private ChatServletAgent chatServletAgent;
  private ChatRequestValidator chatRequestValidator;

  /**
   * Set up state for handling chat requests.
   */
  @Override
  public void init() throws ServletException {
    super.init();
    AppInjector.getInstance().inject(this);
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
   * This function is called when the user performs a new video event on the chat screen
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException {
    // This will represent the data being sent in the response
    JSONObject responseData = new JSONObject();

    try {
      chatRequestValidator.validateRequest(request, "/chat/video/");

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

      Instant lastVideoEventInstant = Instant.MIN;

      String lastVideoEventTime = request.getParameter("lastVideoEventTime");
      if (!lastVideoEventTime.equals("-1")) {
        lastVideoEventInstant = Instant.parse(lastVideoEventTime);
      }

      List<VideoEvent> videoEvents = chatServletAgent.getVideoEventStore().getVideoEventsInConversation(chatRequestValidator.getConversationOptional().get().getId());

      VideoEvent latestVideoEvent = null;

      if (!videoEvents.isEmpty()) {
        latestVideoEvent = videoEvents.get(videoEvents.size() - 1);

        if (latestVideoEvent.getCreationTime().compareTo(lastVideoEventInstant) > 0) {
          responseData.put("success", false);
          responseData.put("pendingSync", true);
          response.getOutputStream().print(responseData.toString());
          return;
        }
      }

      String videoId = request.getParameter("videoId");
      String videoStateJSON = request.getParameter("videoStateJSON");
      String curSeekStr = request.getParameter("curSeek");

      if(latestVideoEvent == null) {
        double curSeek = 0;

        if (!curSeekStr.equals("-1")) {
          curSeek = Double.parseDouble(curSeekStr);
        }

        latestVideoEvent = new VideoEvent(UUID.randomUUID(),
                chatRequestValidator.getConversationOptional().get().getId(),
                chatRequestValidator.getUserOptional().get().getId(),
                videoId,
                Instant.now(),
                videoStateJSON,
                chatRequestValidator.getUserOptional().get().getId(),
                curSeek);
        chatServletAgent.getVideoEventStore().addVideoEvent(latestVideoEvent);
      } else {
        if (!curSeekStr.equals("-1")) {
          latestVideoEvent.setSeekOwner(chatRequestValidator.getUserOptional().get().getId());
          latestVideoEvent.setSeekTime(Double.parseDouble(curSeekStr));
        }

        latestVideoEvent.setVideoId(videoId);
        latestVideoEvent.setVideoStateJSON(videoStateJSON);
        latestVideoEvent.setCreation(Instant.now());
        chatServletAgent.getVideoEventStore().updateVideoEvent(latestVideoEvent);
      }

      responseData.put("success", true);
      responseData.put("creationTime", latestVideoEvent.getCreationTime().toString());

    } catch (JSONException e) {
      response.setStatus(500);
      response.getOutputStream().print("Unexpected JSONException Occurred");
      return;
    }

    // This will send the data back in JSON format
    response.getOutputStream().print(responseData.toString());
  }
}