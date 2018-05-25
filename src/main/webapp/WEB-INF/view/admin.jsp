<%--
  Copyright 2017 Google Inc.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<!DOCTYPE html>
<html lang="en-US">
<head>
  <title>Admin Page</title>
  <link rel="stylesheet" href="/css/main.css">
</head>
<body>
  <%@page import  = "java.util.List"%>
  <%@page import  = "java.util.stream.Collectors"%>
  <%@page import  = "codeu.model.data.Message"%>
  <%@page import  = "codeu.model.data.User"%>
  <%@page import= "codeu.model.data.Conversation" %>

  <div id="container">
      <header>
          <a href="/">
            <img src="images/chatapp-logo.png" class="logo_header" alt="Incodable Logo">
        </a>
        <% if(request.getSession().getAttribute("user") != null){ %>
              <p class="greeting"> Hello, <%= request.getSession().getAttribute("user") %>!</p>
                <nav>
                    <ul>
                      <li><a href="/conversations"> Conversations </a></li>
                      <% if(request.getSession().getAttribute("role").equals("admin"))  {%>
                          <li style="text-decoration: underline;"><div id="diamond" style="background-color: red;"></div><a href="#admin"> Admin </a></li>
                      <% } %>
                      <li><a href="/users/<%= request.getSession().getAttribute("user") %>"> Profile </a></li>
                      <li><a href="/logout"> Log Out </a></li>
                    </ul>
                </nav>
           <% } else { %>
                <nav>
                  <ul>
                      <li><a href="/">Back to Home</a></li>
                  </ul>
                </nav>
           <% } %>
        </header>
        <!-- Content -->
          <div class="content2" style="text-align:left; padding-left:0;">
          	  <br>
          	  <div class="row" style="display:flex;">
          		  <div class="column">
          			  <p class="heading">Statistics</p>
          			  <p>Who doesn't like basic statistics?</p>
          			  <div class="textblock" style="min-width:300px; text-align:left;">
        			  <%
                            int numUsers = 0;
                            int numMessages = 0;
                            int numConvos = 0;
                            List<User> users = (List<User>) request.getAttribute("users");
                            if(users != null){numUsers += users.size();}
                            List<Conversation> conversations = (List<Conversation>) request.getAttribute("conversations");
                            if(conversations != null){numConvos += conversations.size();}
                            List<Message> messages = (List<Message>) request.getAttribute("messages");
                            if(messages != null){numMessages += messages.size();}

                            %>
                            <p> Total number of users: <%=numUsers %> </p>
                            <p> Total number of conversations: <%=numConvos %> </p>
                            <p> Total number of messages: <%=numMessages %> </p>
                            <p> Average number of messages per user: <%=(double)numMessages/numUsers %> </p>
        			  </div>
                      		  </div>
                      		  <div class="column">
        			  <p class="heading">Users</p>
        			  <p>Look up users.</p>
        			  <div class="textblock" style="min-width:200px;">
        				  <select name="usersBox" size = "10">
                                <%
                                List<String> userNames = users.stream().map(x -> x.getName())
                                  .sorted(String::compareToIgnoreCase)
                                  .collect(Collectors.toList());
                                for (int i = 0; i < numUsers; i ++)
                                {
                                %>
                                  <option value = <%=userNames.get(i) %> > <%=userNames.get(i)%> </option>
                                <%
                                }
                                %>
                          </select>
        			  </div>
        		  </div>
        		  <div class="column">
        			  <p class="heading">Make a New Admin</p>
        			  <p>Create a new user with administrative privileges.</p>
        			  <div class="textblock" style="min-width:250px;">
        			  <% if(request.getAttribute("admin error") != null){ %>
                                  <h2 style="color:red"><%= request.getAttribute("admin error") %></h2>
                            <% } %>
                            <form action="/admin" method="POST">
        				  <label for="admin name">Username: </label>
                <input type="text" name="admin name" id="admin name">
                <br/>
                <label for="admin password">Password: </label>
                <input type="text" name="admin password" id="admin password">
                <br/>
                <button type="submit" value="create" name="create">Submit</button>
                </form>
        			  </div>
        		  </div>
        	</div>
        	  <br>
        	  <p class="heading" style="text-align: center;"> Load Test Data</p>
        	  <div style="text-align: center; margin: 0 auto;">
        	  <p style="display: inline-block; vertical-align: top; text-align: left; width: 60%; max-width: 600px;">This will load a number of users, conversations, and messages for testing purposes. <br><br>There are two population methods: <br>
        		  - <b>Random Population</b>: Adds randomly-selected 20 pre-defined users, 10
               conversations, and 100 messages. <br>
        		  - <b>Shakespearean Population</b>: Based on the chosen Shakespearean play, adds
                the characters as users, the scenes as conversations, and dialogue as
                messages. </p>
        		  <br>
        	  <form action="/admin" method="POST">
                      <select name="method">
                      <option value="rj">Romeo and Juliet</option>
                      <option value="random">Random</option>
                      </select>
                      <button type="submit" value="populate" name="populate">Select</button>
                  </form>
        		  </div>
        	</div>
        <footer>
        	<h4 class="codeu">Google CodeU Chat App</h4>
        	</footer>
        </div>
        </body>
        </html>