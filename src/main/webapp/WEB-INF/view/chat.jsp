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

  <!-- Attaches the Theme Stylesheet for the Quill Editor  -->
  <link href="https://cdn.quilljs.com/1.3.6/quill.snow.css" rel="stylesheet">   
  
  <!-- Includes the Quill Editor Library  -->
  <script src="https://cdn.quilljs.com/1.3.6/quill.js"></script>
  
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
  </style>

  <script>
    function onBodyLoaded() {
      scrollChat();
      initChatInputEditor();
    }
    
    // scroll the chat div to the bottom
    function scrollChat() {
      var chatDiv = document.getElementById('chat');
      chatDiv.scrollTop = chatDiv.scrollHeight;
    };

    // Initialize the Chat Input Editor
    function initChatInputEditor() {
      Quill.register(Quill.import('attributors/style/background'), true);
      Quill.register(Quill.import('attributors/style/color'), true);
      Quill.register(Quill.import('attributors/style/font'), true);
      Quill.register(Quill.import('attributors/style/size'), true);

      let chatInputEditor = new Quill('#chat-input-editor', {
        debug: 'info',
        modules: {
          toolbar: '#chat-input-toolbar'
        },
        placeholder: 'Type your message and press Send',
        theme: 'snow'
      });

      let chatForm = document.querySelector('#chat-form');
      chatForm.onsubmit = function() {
        let chatInputField = chatForm.querySelector('#chat-input-field');
        let chatInputEditor = chatForm.querySelector('#chat-input-editor');

        let messageContent = chatInputEditor.querySelector('.ql-editor').firstChild.innerHTML;

        chatInputField.value = messageContent;

        return true;
      }
    }

  </script>
</head>
<body onload="onBodyLoaded();">

  
  <%@ include file="/include/navbar.jsp" %>

  <div id="container">

    <h1><%= conversation.getTitle() %>
      <a href="" style="float: right">&#8635;</a></h1>

    <hr/>

    <div id="chat">
      <ul>
    <%
      for (Message message : messages) {
        String author = UserStore.getInstance()
          .getUser(message.getAuthorId()).getName();
    %>
      <li><strong><%= author %>:</strong> <%= message.getContent() %></li>
    <%
      }
    %>
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
        <button type="submit">Send</button>
    </form>
    <% } else { %>
      <p><a href="/login">Login</a> to send a message.</p>
    <% } %>

    <hr/>

  </div>
</body>
</html>
