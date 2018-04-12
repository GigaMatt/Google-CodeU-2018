package codeu.controller;
import java.io.IOException;
import codeu.model.data.User;
import codeu.model.store.basic.UserStore;
import codeu.model.data.Conversation;
import codeu.model.store.basic.ConversationStore;
import java.time.Instant;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
  * Servlet class responsible for profile page viewing.
  */
public class ProfileServlet extends HttpServlet {

	/**Store class that gives access to Users.*/
	private UserStore userStore;

	  /** Store class that gives access to Conversations. */
 	private ConversationStore conversationStore; 

     /**
   * Sets the UserStore used by this servlet. This function provides a common setup method for use
   * by the test framework or the servlet's init() function.
   */
  void setUserStore(UserStore userStore) {
    this.userStore = userStore;
  }

 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws IOException, ServletException {

   request.getRequestDispatcher("/WEB-INF/view/profile.jsp").forward(request, response);
 }


@Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  	throws IOException, ServletException {
      String username = request.getParameter("username");
      //User user =  userStore.getUser(username);
      String description = request.getParameter("description");
      request.getSession().setAttribute("description", description);
      //user.setDescription(description);
      //persistentStorageAgent.writeThrough(user);
  		response.sendRedirect("/users/");
	} 
}