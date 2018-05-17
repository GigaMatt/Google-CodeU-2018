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
<html>
<head>
  <title>Admin Page</title>
  <link rel="stylesheet" href="/css/main.css">
</head>
<body>

  <%@ include file="/include/navbar.jsp" %>
  <%@page import  = "java.util.List"%>
  <%@page import  = "java.util.stream.Collectors"%>
  <%@page import  = "codeu.model.data.Message"%>
  <%@page import  = "codeu.model.data.User"%>
  <%@page import= "codeu.model.data.Conversation" %>

  <div id="container">
      <h1>Statistics</h1>
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

  <div id = "container">
      <h1>Users</h1>
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

  <div id="container">
      <h1>Make Another Admin</h1>
      <p>This will create a user with administrative privileges.</p>
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

  <div id="container">
    <h1>Load Test Data</h1>
    <p>This will load a number of users, conversations, and messages for testing
        purposes. Select preferred population method: </p>
    <form action="/admin" method="POST">
        <select name="method">
        <option value="rj">Romeo and Juliet</option>
        <option value="random">Random</option>
        </select>
        <button type="submit" value="populate" name="populate">Select</button>
    </form>
  </div>
</body>
</html>
