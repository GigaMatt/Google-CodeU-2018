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


//  private User user;

   /**
   * The PersistentStorageAgent responsible for loading Users from and saving Users to Datastore.
   */
//private PersistentStorageAgent persistentStorageAgent;

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
  public void init() throws ServletException {
    super.init();
    setUserStore(UserStore.getInstance());

  }

 @Override
 public void doGet(HttpServletRequest request, HttpServletResponse response)
     throws IOException, ServletException {
    //User user = request.getSession().getAttribute("user");
    //String username = (String)request.getSession().getAttribute("user");
    String requestUrl = request.getRequestURI();
    String username = requestUrl.substring("/users/".length());
    if(username == null){
      response.sendRedirect("/login");
    }else {
      User user =  userStore.getUser(username);
      String descript = user.getDescription();
      request.setAttribute("description", descript);
      request.setAttribute("user", username);
      request.getRequestDispatcher("/WEB-INF/view/profile.jsp").forward(request, response);
  }
 }


@Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  	throws IOException, ServletException {
      String descript = request.getParameter("description");
      String username = (String)request.getSession().getAttribute("user");
      User user =  userStore.getUser(username);
      user.setDescription(descript);
      userStore.updateUser(user);
      //user1.setDescription(descript);
      //persistentStorageAgent.writeThrough(user);
      //userStore.writeThrough(user);
  		response.sendRedirect("/users/" + request.getSession().getAttribute("user"));
	} 
}