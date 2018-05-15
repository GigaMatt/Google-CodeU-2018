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

    #youtube-player-tools {
      width: 100%;
    }

    #youtube-player-videoid-input {
      width: calc(100% - 70px);
      float: left;
    }
    
    #youtube-player-videoid-input-submit {
      width: 60px;
      float: right;
    }

  </style>
</head>
<body onload="onBodyLoaded();">

  
  <%@ include file="/include/navbar.jsp" %>

  <div id="youtube-player-container">
    <div style="position: relative; width: 100%; height:100%">
        
      <div id="youtube-player-wrapper" style="width: 640px; height: 400px">
        <div id="youtube-player"></div>
        <br/>
        <div id="youtube-player-tools">
          <input id="youtube-player-videoid-input" type="url" placeholder="Enter a Youtube Video's ID" onkeypress="onEnterPressed(event, onYoutubeURLSubmitted)"/>
          <input id="youtube-player-videoid-input-submit" type="button" value="Load" onclick="onYoutubeURLSubmitted()" />
        </div>
      </div>
  </div>
  </div>

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
    insertYoutubeAPI();

    // We will check for new messages using this interval in milliseconds.
    const MESSAGE_POLL_INTERVAL = 3000;
    const YOUTUBE_PLAYER_WRAPPER_PADDING = {
      left: 10,
      right: 10,
      top: 10,
      bottom: 100,
    }

    // Controls message polling (Only one active poll at a time). Will be set to false when a poll is in progress, and back to true when the poll has ended.
    var canPollForMessages = true;

    var youtubePlayer;

    // This function is called when the page had finished loading
    function onBodyLoaded() {
      scrollChat();
      initChatInputEditor();
      initYoutubePlayerInteraction();

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


    function onEnterPressed(e, callback) {
      if (e.keyCode == 13) {
        callback();
      }
    }


    // Makes the Youtube Player Floatable
    function initYoutubePlayerInteraction() {
      interact('#youtube-player-wrapper')
        .draggable({
          // enable inertial throwing
          inertia: true,
          // keep the element within the area of it's parent
          restrict: {
            restriction: "parent",
            endOnly: true,
            elementRect: { top: 0, left: 0, bottom: 1, right: 1 }
          },
          // enable autoScroll
          autoScroll: true,

          // call this function on every dragmove event
          onmove: function (event) {
            let target = event.target,
                // keep the dragged position in the data-x/data-y attributes
                x = (parseFloat(target.getAttribute('data-x')) || 0) + event.dx,
                y = (parseFloat(target.getAttribute('data-y')) || 0) + event.dy;
        
            // translate the element
            target.style.webkitTransform =
            target.style.transform =
              'translate(' + x + 'px, ' + y + 'px)';
        
            // update the posiion attributes
            target.setAttribute('data-x', x);
            target.setAttribute('data-y', y);
          },
          // call this function on every dragend event
          onend: function (event) {
            
          }
        })
        .resizable({
          // resize from all edges and corners
          edges: { left: true, right: true, bottom: true, top: true },
      
          // keep the edges inside the parent
          restrictEdges: {
            outer: 'parent',
            endOnly: true,
          },
      
          // minimum size
          restrictSize: {
            min: { width: 300 + YOUTUBE_PLAYER_WRAPPER_PADDING.left + YOUTUBE_PLAYER_WRAPPER_PADDING.right, 
              height: 150 + YOUTUBE_PLAYER_WRAPPER_PADDING.top + YOUTUBE_PLAYER_WRAPPER_PADDING.bottom },
          },
      
          inertia: true,
        })
        .on('resizemove', function (event) {
          let target = event.target,
              x = (parseFloat(target.getAttribute('data-x')) || 0),
              y = (parseFloat(target.getAttribute('data-y')) || 0);
      
          let width = event.rect.width - (YOUTUBE_PLAYER_WRAPPER_PADDING.left + YOUTUBE_PLAYER_WRAPPER_PADDING.right);
          let height = event.rect.height - (YOUTUBE_PLAYER_WRAPPER_PADDING.top + YOUTUBE_PLAYER_WRAPPER_PADDING.bottom);

          // update the element's style
          target.style.width  = width + 'px';
          target.style.height = height + 'px';
      
          // translate when resizing from top or left edges
          x += event.deltaRect.left;
          y += event.deltaRect.top;
      
          target.style.webkitTransform = target.style.transform =
              'translate(' + x + 'px,' + y + 'px)';
      
          target.setAttribute('data-x', x);
          target.setAttribute('data-y', y);

          youtubePlayer.setSize(width, height);
        });

    }

    // Adds the youtube api script to the document asynchronously
    function insertYoutubeAPI() {  
      let youtubeAPIScriptTag = document.createElement('script');
      youtubeAPIScriptTag.src = "https://www.youtube.com/iframe_api";

      let firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode.insertBefore(youtubeAPIScriptTag, firstScriptTag);
    }

  </script>

  <% if (request.getSession().getAttribute("user") != null) { %>
  <script>
    function onYouTubeIframeAPIReady() {
      youtubePlayer = new YT.Player('youtube-player', {
        videoId: 'ogfYd705cRs',
        events: {
          'onReady': onYoutubePlayerReady,
          'onStateChange': onYoutubePlayerStateChange
        },
        playerVars: {
          disablekb: 1,
          enablejsapi: 1,
          fs: 0,
          rel: 0,
        }
      });
    }

    function onYoutubePlayerReady(event) {
      let youtubePlayerWrapper = document.querySelector("#youtube-player-wrapper");
      youtubePlayerWrapper.style.display = "block";

      let width = +youtubePlayerWrapper.style.width.replace(" ", "").replace("px", "");
      let height = +youtubePlayerWrapper.style.height.replace(" ", "").replace("px", "");

      event.target.setSize(width, height);
      event.target.playVideo();
    }

    function onYoutubePlayerStateChange(event) {
      console.log(event);
    }

    function onYoutubeURLSubmitted() {
      console.log(youtubePlayer);
      let videoIdInput = document.querySelector("#youtube-player-videoid-input");
      let videoId = videoIdInput.value.trim();
      if(videoId != "") {
        youtubePlayer.loadVideoById(videoId);
        videoIdInput.value = "";
      }
    }
  </script>
  <% } %>
</body>
</html>
