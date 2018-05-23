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
<%@ page import="codeu.model.data.Message" %>
<%@ page import="codeu.model.store.basic.UserStore" %>
<%
Conversation conversation = (Conversation) request.getAttribute("conversation");
List<Message> messages = (List<Message>) request.getAttribute("messages");
%>

<!DOCTYPE html>
<html>
<head>
  <title><%= conversation.getTitle() %></title>
  <link rel="stylesheet" href="/css/main.css" type="text/css">

  <!-- Attaches the Material Icons  -->
  <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

  <!-- Attaches the Theme Stylesheet for the Quill Editor  -->
  <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">

  <style>
    #chat {
      background-color: white;
      height: 500px;
      overflow-y: scroll
    }

    div.ql-editor {
      background-color: white;
    }

    div.ql-toolbar {
      background-color: white;
    }

    #youtube-player-container {
      position: fixed;
      pointer-events: none;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 999;
    }

    #youtube-player-wrapper {
      display: none;
      position: absolute;
      pointer-events: all;
      border-radius: 5px;
      background: rgba(0, 128, 128, 0.8);
      padding-left: 10px;
      padding-right: 10px;
      padding-top: 10px;
      padding-bottom: 100px;
    }

    #youtube-player {
      border-radius: 6px;
    }

    #youtube-player-tools {
      width: 100%;
    }

    #youtube-player-videoid-input {
      width: calc(100% - 70px);
      float: left;
      border-radius: 6px;
    }

    #youtube-player-videoid-input-submit {
      width: 60px;
      float: right;
      border-radius: 6px;
    }

    #youtube-player-display-toggle {
      pointer-events: all;
      position: absolute;
      right: 25px;
      bottom: 25px;
      width: 30px;
      height: 30px;
      padding: 14px 7px 7px 14px;
      cursor: pointer;
      background-color: teal;
      border-radius: 50px;
    }

  </style>
</head>
<body onload="onBodyLoaded();">


  <%@ include file="/include/navbar.jsp" %>

  <div id="youtube-player-container">
    <div style="position: relative; width: 100%; height:100%">

      <div id="youtube-player-wrapper" style="width: 512px; height: 300px; display: none;">
        <div id="youtube-player"></div>
        <br/>
        <div id="youtube-player-tools">
          <input id="youtube-player-videoid-input" type="url" placeholder="Enter a Youtube Video's ID" onkeypress="onEnterPressed(event, onYoutubeVideoIdSubmitted)"/>
          <input id="youtube-player-videoid-input-submit" type="button" value="Load" onclick="onYoutubeVideoIdSubmitted()" />
        </div>
      </div>

      <% if (request.getSession().getAttribute("user") != null) { %>
      <div id="youtube-player-display-toggle" onclick="toggleYoutubePlayerDisplay()">
          <i class="material-icons" style="color: white">videocam</i>
      </div>
      <% } %>
    </div>
  </div>

  <div id="container">

    <h1><%= conversation.getTitle() %>
      <a style="float: right; cursor: pointer; background: blue; padding: 0 15px; border-radius: 50px; color: white;" onclick="checkForNewMessages()">&#8635;</a>
    </h1>

    <hr/>

    <div id="chat">
      <ul id="chat-list">

      </ul>
    </div>

    <hr/>

    <% if (request.getSession().getAttribute("user") != null) { %>
    <form id="chat-form" action="/chat/<%= conversation.getTitle() %>" method="POST">
        <input id="chat-input-field" type="hidden" name="message">

        <div id="chat-input-toolbar">
          <span class="ql-format">
            <select class="ql-size">
                <option value="10px">Small</option>
                <option selected="">Normal</option>
                <option value="18px">Large</option>
                <option value="32px">Huge</option>
            </select>
          </span>

          <span class="ql-format">
            <button class="ql-bold"></button>
            <button class="ql-italic"></button>
            <button class="ql-underline"></button>
          </span>

          <span class="ql-format">
              <select class="ql-color"></select>
              <select class="ql-background"></select>
          </span>

          <span class="ql-format">
              <select class="ql-font"></select>
          </span>

          <span class="ql-format">
              <button class="ql-clean"></button>
          </span>
        </div>
        <div id="chat-input-editor">

        </div>
        <br/>
        <button type="submit" style="float: right">Send</button>
        <br/>
    </form>

    <% } else { %>
      <p><a href="/login">Login</a> to send a message.</p>
    <% } %>

    <hr/>

  </div>


  <!-- Includes the Quill Editor Library  -->
  <script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>

  <!-- Includes the Axios Async HTTP Request Library  -->
  <script src="https://unpkg.com/axios/dist/axios.min.js"></script>

  <!-- Includes the Interact Library  -->
  <script src="/js/interact.min.js"></script>

  <script>
    const CONVERSATION_TITLE = "<%= conversation.getTitle() %>";
  </script>

  <%-- Includes the main chat js file --%>
  <script src="/js/chat/chat.js"></script>

  <%
      if (request.getSession().getAttribute("user") != null) {
          String username = (String) request.getSession().getAttribute("user");
  %>

    <script>
        const LOGGED_IN_USERNAME = "<%= username %>";
    </script>

    <%-- Includes the video player script --%>
    <script src="/js/chat/videoPlayerAddon.js"></script>

  <% } %>

</body>
</html>
