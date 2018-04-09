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
</head>
<body>

  <%@ include file="/include/navbar.jsp" %>

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
      <p> Average number of conversations per user: <%=numConvos/numUsers %> </p>
  </div>

  <div id="container">
    <h1>Load Test Data</h1>
    <p>This will load a number of users, conversations, and messages for testing
        purposes.</p>
    <form action="/admin" method="POST">
      <button type="submit" value="confirm" name="confirm">Confirm</button>
      <button type="submit" value="cancel" name="cancel">Do Nothing</button>
    </form>
  </div>
</body>
</html>
