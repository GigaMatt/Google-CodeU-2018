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

  <script src="https://unpkg.com/axios/dist/axios.min.js"></script>

  <script>
    // We will check for new messages using this interval in milliseconds.
    const MESSAGE_POLL_INTERVAL = 3000;

    // Controls message polling (Only one active poll at a time). Will be set to false when a poll is in progress, and back to true when the poll has ended.
    var canPollForMessages = true;

    function onBodyLoaded() {
      scrollChat();
      initChatInputEditor();

      initMessagePolling();
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

      let chatForm = document.querySelector('#chat-form');

      if(chatForm) {
        let chatInputEditor = chatForm.querySelector('#chat-input-editor');
        let chatInputToolbar = chatForm.querySelector('#chat-input-toolbar');

        let chatQuill = new Quill(chatInputEditor, {
          modules: {
            toolbar: chatInputToolbar,
            keyboard: {
              bindings: {
                custom: {
                  key: 'enter',
                  handler: function(range, context) {
                    chatForm.querySelector('button[type="submit"]').click();
                  }
                },
              }
            }
          },
          placeholder: 'Type your message and press Send or hit Enter',
          theme: 'snow'
        });

        chatForm.onsubmit = function() {
          let chatInputField = chatForm.querySelector('#chat-input-field');
          let qlEditor = chatInputEditor.querySelector('.ql-editor');
          
          let qlCursor = qlEditor.querySelector('.ql-cursor');
          if(qlCursor) {
            qlCursor.parentNode.removeChild(qlCursor);
          }

          let firstElement = qlEditor.firstChild;          
          if(firstElement) {
            if(firstElement.tagName == 'p' || firstElement.tagName == 'P') {
              let newFirstElement = document.createElement('span');
              newFirstElement.innerHTML = firstElement.innerHTML;

              firstElement.parentNode.replaceChild(newFirstElement, firstElement);
            }
          }

          chatInputField.value = qlEditor.innerHTML;

          return true;
        }

        chatQuill.focus();
      }
    }

    // Helper function to create the URL Encoded Post String to be used for Post Requests
    function createPostString(postData) {
      let str = "", first = true;
      for (let key in postData) {
        // skip loop if the property is from prototype
        if (!postData.hasOwnProperty(key)) continue;
    
        let val = postData[key];
        
        if (first) {
            first = false;
        }
        else {
            str += '&';
        }

        key = encodeURIComponent(key);
        val = encodeURIComponent(val);

        str += key + "=" + val;
      }

      return str;
    }

    // Initializes Message Polling
    function initMessagePolling() {
      setInterval(function () {
        checkForNewMessages();  
      }, MESSAGE_POLL_INTERVAL);
      checkForNewMessages();
    }

    // Asynchronously checks for any new messages
    function checkForNewMessages() {
      if(!canPollForMessages) {
        return;
      }

      let chatList = document.querySelector('#chat-list');
      if(!chatList) {
        return;
      }

      let lastMessageItem = chatList.querySelector('.message-item:last-child');

      // By default, set to "0". The Server identifies "0" as an empty message list and sends back all available messages.
      let lastMessageTime = "0";
      if(lastMessageItem) {
        lastMessageTime = lastMessageItem.getAttribute("creation-time");
      }

      let postData = {
        lastMessageTime: lastMessageTime,
      };

      // Disabling polling temporarily
      canPollForMessages = false;

      // Sending HTTP Request asynchronously
      axios.post("/chat/poll/<%= conversation.getTitle() %>", createPostString(postData))
        .then(function (response) {
          if (response.data.success) {
            if (response.data.foundNewMessages) {
              loadNewMessages(response.data.messages);
            }
          } else {
            // TODO (Azee): Show an error message
          }
          
          // Enabling polling back
          canPollForMessages = true;
        })
        .catch(function (error) {
          // TODO (Azee): Show an error message
          
          // Enabling polling back
          canPollForMessages = true;
        });
    }

    // Adds the new messages to the chat list
    function loadNewMessages(newMessages) {     
      let chatList = document.querySelector('#chat-list');
      if(!chatList) {
        return;
      }

      newMessages.forEach(function (newMessage) {
        let messageItem = document.createElement("li");
        messageItem.classList.add('message-item');
        messageItem.setAttribute('creation-time', newMessage.creationTime);
        messageItem.innerHTML = '<strong>' + newMessage.author.name + ': </strong>' + newMessage.content;

        chatList.appendChild(messageItem);
      });

      // Scroll to the bottom after adding the new messages
      scrollChat();
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
        <button type="submit">Send</button>
    </form>
    <% } else { %>
      <p><a href="/login">Login</a> to send a message.</p>
    <% } %>

    <hr/>

  </div>
</body>
</html>
