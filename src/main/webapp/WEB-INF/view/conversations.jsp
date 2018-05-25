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
<%@ page import="java.util.List" %>
<%@ page import="codeu.model.data.Conversation" %>

<!doctype html>
<html lang="en-US">
<head>
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Conversations</title>
<link href="/css/main.css" rel="stylesheet" type="text/css">
</head>

<body>
<!-- Main Body -->
<div class="container">
<!-- Navigation Bar -->
  <header>
    <a href="/">
	    <img src="/images/chatapp-logo.png" class="logo_header" alt="Incodable Logo">
	</a>
	<% if(request.getSession().getAttribute("user") != null){ %>
        <p class="greeting"> Hello, <%= request.getSession().getAttribute("user") %>!</p>
          <nav>
              <ul>
                <li style="text-decoration: underline;"><div id="diamond" style="background-color: green;"></div>
                    <a href="/conversations"> Conversations </a></li>
                <% if(request.getSession().getAttribute("role").equals("admin"))  {%>
                    <li><a href="/admin"> Admin </a></li>
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
	<% if(request.getSession().getAttribute("user") != null){ %>
        <div class="content2">
          <% if(request.getAttribute("error") != null){ %>
                <h2 style="color:red; text-align:center;"><%= request.getAttribute("error") %></h2>
          <% } %>
            <p class="heading">New Conversation</p>
            <div style="width:260px; padding: 20px; border: 1px solid #BEBEBE">
                <form action="/conversations" method="POST">
                    <label>Title:</label>
                    <input type="text" name="conversationTitle">
                    <button type="submit" style="margin-left: 15px;">Create</button>
                </form>
            </div>
            <br>
            <p class="heading"> Current Conversations</p>
            <div style="width:50%; padding: 20px; border: 1px solid #BEBEBE">
                    <%
                      List<Conversation> conversations =
                        (List<Conversation>) request.getAttribute("conversations");
                      if(conversations == null || conversations.isEmpty()){
                    %>
                        <p style="text-align:center;">Create a conversation to get started.</p>
                    <%} else { %>
                        <ul>
                    <%
                        for(Conversation conversation : conversations) {
                    %>
                        <li><a href="/chat/<%= conversation.getTitle() %>">
                          <%= conversation.getTitle() %></a></li>
                    <% } %>
                        </ul>
                    <% }%>
            </div>
          </div>
     <% } else { %>
           <p class="heading red" style ="text-align:center;"><a href="/login">Login</a> to view conversations.</p>
     <% } %>
     <footer>
            <h4 class="codeu">Google CodeU Chat App</h4>
     </footer>
    </div>
    </body>
    </html>

