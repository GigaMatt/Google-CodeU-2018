
package codeu.controller.chat;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import codeu.model.data.Conversation;
import codeu.model.data.User;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoStore;

public class VideoPollServlet extends HttpServlet {
    private ChatServletAgent chatServletAgent;

    public VideoPollServlet() {
      chatServletAgent = new ChatServletAgent();
    }

    /** Set up state for handling chat requests. */
    @Override
    public void init() throws ServletException {
        super.init();
        chatServletAgent.setConversationStore(ConversationStore.getInstance());
        chatServletAgent.setVideoStore(VideoStore.getInstance());
        chatServletAgent.setUserStore(UserStore.getInstance());
    }
    
    /**
     * @return the chatServletAgent
     */
    public ChatServletAgent getChatServletAgent() {
        return chatServletAgent;
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
            String conversationTitle = requestUrl.substring("/chat/video/poll/".length());

            Conversation conversation = chatServletAgent.getConversationStore().getConversationWithTitle(conversationTitle);
            if (conversation == null) {
                // couldn't find conversation
                responseData.put("success", false);
                responseData.put("message", "Conversation not found!");
                response.getOutputStream().print(responseData.toString());
                return;
            }

            // Todo (Azee): Check for new video events and modify response dataaccordingly
            
        } catch (JSONException e) {
            response.setStatus(500);
            response.getOutputStream().print("Unexpected JSONException Occurred");
            return;
        }
        
        // This will send the data back in JSON format
        response.getOutputStream().print(responseData.toString());
    }
}