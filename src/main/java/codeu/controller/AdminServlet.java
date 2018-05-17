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
import codeu.model.data.DataParse;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet class responsible for administrative tasks (testing,
 * statistics, adding other admin, etc.). */

public class AdminServlet extends HttpServlet {

  /** Store class that gives access to Conversations. */
  private ConversationStore conversationStore;

  /** Store class that gives access to Messages. */
  private MessageStore messageStore;

  /** Store class that gives access to Users. */
  private UserStore userStore;

  /** Set up state for handling the load test data request. */
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
   *
   * Obtains DataParse object, which is obtained from parsing a specified file.
   */
  public static DataParse parseFile(String fileName) {
    DataParse parse = new DataParse(fileName);
    parse.parse();
    return parse;
  }

  /**
   * This function fires when a user requests the /admin URL. It simply forwards the request to
   * admin.jsp.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    List<Conversation> conversations = conversationStore.getAllConversations();
    request.setAttribute("conversations", conversations);
    List<User> users = userStore.getAllUsers();
    request.setAttribute("users", users);
    List<Message> messages = messageStore.getAllMessages();
    request.setAttribute("messages", messages);
    request.getRequestDispatcher("/WEB-INF/view/admin.jsp").forward(request, response);
  }

  /**
   * This function fires when a user submits the testdata form. It loads test data if the user
   * clicked the confirm button.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    String username = (String) request.getSession().getAttribute("user");
    if (username == null) {
      // user is not logged in, don't let them utilize the admin page
      response.sendRedirect("/login");
      return;
    }

    String role = (String) request.getSession().getAttribute("role");
    if (!role.equals("admin")) {
      // if user is not an admin, don't let them utilize the admin page
      System.out.println("You do not have access to view this page.");
      response.sendRedirect("/login");
      return;
    }

    User user = userStore.getUser(username);
    if (user == null) {
      // user was not found, don't let them utilize the admin page
      System.out.println("User not found: " + username);
      response.sendRedirect("/login");
      return;
    }

    if (request.getParameter("populate") != null) {
      String method = request.getParameter("method");
      if (method.equals("rj")) {
        DataParse parsedData = parseFile("Romeo_and_Juliet");
        parsedData.allUsers.values().stream().forEach(x -> userStore.addUser(x));
        conversationStore.loadTestData(parsedData.allConversations);
        messageStore.loadTestData(parsedData.allMessages);
      } else {
        userStore.loadTestData();
        conversationStore.loadTestData();
        messageStore.loadTestData();
      }
    } else if (request.getParameter("create") != null){
      String adminName = request.getParameter("admin name");
      String adminPassword = request.getParameter("admin password");

      if (adminName == null || adminPassword == null) {
        request.setAttribute("admin error", "Incomplete request: missing fields.");
        request.getRequestDispatcher("/WEB-INF/view/admin.jsp").forward(request, response);
        return;
      }

      if(!adminName.matches("[\\w*\\s*]*")) {
        request.setAttribute("admin error", "Please enter only letters, numbers, and spaces for the username.");
        request.getRequestDispatcher("/WEB-INF/view/admin.jsp").forward(request,response);
        return;
      }

      if(userStore.isUserRegistered(adminName)) {
        request.setAttribute("admin error", "That username is already taken.");
        request.getRequestDispatcher("/WEB-INF/view/admin.jsp").forward(request, response);
        return;
      }

      String passwordHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
      User newAdmin = new User(UUID.randomUUID(), adminName, passwordHash, "admin", Instant.now(),
              "Welcome new administrator!");
      userStore.addUser(newAdmin);
    }

    response.sendRedirect("/admin");
  }
}
