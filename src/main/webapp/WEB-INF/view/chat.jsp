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
    insertYoutubeAPI();

    // We will check for new messages using this interval in milliseconds.
    const MESSAGE_POLL_INTERVAL = 1000;
    // We will check for new video events using this interval in milliseconds.
    const VIDEO_EVENT_POLL_INTERVAL = 1000;
    const YOUTUBE_PLAYER_WRAPPER_PADDING = {
      left: 10,
      right: 10,
      top: 10,
      bottom: 100,
    };

    const FORCE_SEEK_ADJUSTMENT = 1; // A rough estimate to account for the seconds passed between requests

    // Controls message polling (Only one active poll at a time). Will be set to false when a poll is in progress, and back to true when the poll has ended.
    let canPollForMessages = true;
    // Controls video event polling (Only one active poll at a time). Will be set to false when a poll is in progress, and back to true when the poll has ended.
    let canPollForVideoEvents = true;

    let ignorePolledVideoEvent = false;

    let youtubePlayer;

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
    }

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

          sendMessage(chatInputField.value);

          chatInputField.value = "";
          chatQuill.setText('');

          // Prevents the default form submit action since the message is instead sent asynchronously.
          return false;
        };

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
        checkForNewMessages(true);
      }, MESSAGE_POLL_INTERVAL);
      checkForNewMessages(true);
    }

    // Asynchronously checks for any new messages
    function checkForNewMessages(silent = false) {
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
            if (!silent && response.data.message) {
              alert(response.data.message);
            }
          }

          // Enabling polling back
          canPollForMessages = true;
        })
        .catch(function (error) {
          if (!silent) {
            alert("Unexpected error! Please try again!");
          }

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

    // Send the message asynchronously
    function sendMessage(message) {
      message = message.trim();
      if(message === '') {
        return;
      }

      let postData = {
        message: message
      };

      axios.post("/chat/<%= conversation.getTitle() %>", createPostString(postData))
        .then(function (response) {
          if (response.data.success) {
            checkForNewMessages();
          } else {
            if (response.data.message) {
              alert(response.data.message);
            }
          }
        })
        .catch(function (error) {
          alert("Unexpected error! Please try again!");
        });
    }

    // For a key event, checks if Enter key was pressed, and if so, calls the given callback
    function onEnterPressed(e, callback) {
      if (e.keyCode === 13) {
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
          margin: 12,

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

    // Shows/Hides the Youtube Player
    function toggleYoutubePlayerDisplay() {
      let youtubePlayerWrapper = document.querySelector("#youtube-player-wrapper");
      let youtubePlayerDisplayToggle = document.querySelector("#youtube-player-display-toggle");
      if(youtubePlayerWrapper.style.display !== "block") {
        youtubePlayerWrapper.style.display = "block";
        youtubePlayerDisplayToggle.querySelector('.material-icons').innerHTML = "videocam_off";
      } else {
        youtubePlayerWrapper.style.display = "none";
        youtubePlayerDisplayToggle.querySelector('.material-icons').innerHTML = "videocam";
      }
    }

    function isYoutubePlayerVisible() {
        let youtubePlayerWrapper = document.querySelector("#youtube-player-wrapper");
        return (youtubePlayerWrapper.style.display === "block");
    }
  </script>

  <%
      if (request.getSession().getAttribute("user") != null) {
        String username = (String) request.getSession().getAttribute("user");
  %>
  <script>

    const VIDEO_PLAYER_STATE = {
        UNSTARTED: -1,
        PLAYING: 1,
        PAUSED: 2,
        ENDED: 0,
    };

    let curVideoState = {
        videoId: null,
        videoTitle: null,
        videoAuthor: null,
        playerState: VIDEO_PLAYER_STATE.UNSTARTED,
        lastModifiedBy: null,
    };

    let lastVideoEventTime = -1;

    let videoStateChangeCallback = null;

    // Called automatically by the Youtube API once it is loaded
    function onYouTubeIframeAPIReady() {

      // Initializing the youtube player
      youtubePlayer = new YT.Player('youtube-player', {
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

    // Called once the youtube player is ready
    function onYoutubePlayerReady(event) {
      let youtubePlayerWrapper = document.querySelector("#youtube-player-wrapper");
      youtubePlayerWrapper.style.display = "block";

      let width = +youtubePlayerWrapper.style.width.replace(" ", "").replace("px", "");
      let height = +youtubePlayerWrapper.style.height.replace(" ", "").replace("px", "");

      event.target.setSize(width, height);

      youtubePlayerWrapper.style.display = "none";

      initVideoEventPolling();
    }

    // Called every time the youtube player's state changes
    function onYoutubePlayerStateChange(event) {
//      console.log(event);

      let playerState = event.data;
      let videoData = event.target.getVideoData();

      switch(playerState) {
        case VIDEO_PLAYER_STATE.PLAYING:
        case VIDEO_PLAYER_STATE.PAUSED:
        case VIDEO_PLAYER_STATE.ENDED:
            // To account for multiple state changes in quick succession (eg. during seeking)
            setTimeout(function() {
                if (videoStateChangeCallback) {
                    videoStateChangeCallback();
                    videoStateChangeCallback = null;
                } else {
                    sendVideoEvent();
                }
            }, 500);

            curVideoState.videoId = videoData.video_id;
            curVideoState.videoTitle = videoData.title;
            curVideoState.videoAuthor = videoData.author;
            curVideoState.playerState = playerState;
            curVideoState.lastModifiedBy = "<%= username %>";
            break;
      }
    }

    // Loads a youtube video using the entered videoId
    function onYoutubeVideoIdSubmitted() {
      //console.log(youtubePlayer);
      let videoIdInput = document.querySelector("#youtube-player-videoid-input");
      let videoId = videoIdInput.value.trim();
      if(videoId !== "") {
        youtubePlayer.loadVideoById(videoId);
        videoIdInput.value = "";
      }
    }

    function sendVideoEvent() {
        let curSeek = -1;

        if (curVideoState.playerState === VIDEO_PLAYER_STATE.PLAYING
            || curVideoState.playerState === VIDEO_PLAYER_STATE.PAUSED) {
            curSeek = youtubePlayer.getCurrentTime();
        }

        let postData = {
            videoId: curVideoState.videoId,
            videoStateJSON: JSON.stringify(curVideoState),
            lastVideoEventTime: lastVideoEventTime,
            curSeek: curSeek,
        };

        canPollForVideoEvents = false;
        ignorePolledVideoEvent = true;
        axios.post("/chat/video/<%= conversation.getTitle() %>", createPostString(postData))
            .then(function (response) {
                ignorePolledVideoEvent = false;
                canPollForVideoEvents = true;

                if (response.data.success) {
                    lastVideoEventTime = response.data.creationTime;
                } else {
                    if(response.data.pendingSync) {
                        checkForNewVideoEvents();
                    } else if (response.data.message) {
                        alert(response.data.message);
                    }
                }
            })
            .catch(function (error) {
                ignorePolledVideoEvent = false;
                canPollForVideoEvents = true;

                alert("Unexpected error! Please try again!");
            });
    }

    // Initializes Video Event Polling
    function initVideoEventPolling() {
        setInterval(function () {
            checkForNewVideoEvents();
        }, VIDEO_EVENT_POLL_INTERVAL);
        checkForNewVideoEvents();
    }

    function checkForNewVideoEvents() {
        if (!canPollForVideoEvents) {
            return;
        }

        let curSeek = -1;

        if (curVideoState.playerState === VIDEO_PLAYER_STATE.PLAYING
            || curVideoState.playerState === VIDEO_PLAYER_STATE.PAUSED) {
            curSeek = youtubePlayer.getCurrentTime();
        }

        let postData = {
            lastVideoEventTime: lastVideoEventTime,
            curSeek: curSeek,
        };

        canPollForVideoEvents = false;
        axios.post("/chat/video/poll/<%= conversation.getTitle() %>", createPostString(postData))
            .then(function (response) {
//                console.log(response);
                if (ignorePolledVideoEvent) {
                    return;
                }

                canPollForVideoEvents = true;

                if (response.data.success) {
                    if(response.data.foundNewVideoEvent) {
                        let seekTo = null;
                        if (response.data.forceSeek) {
                            seekTo = +response.data.seekTo;
                            seekTo += FORCE_SEEK_ADJUSTMENT;
                        }

                        onNewVideoStateReceived(JSON.parse(response.data.newVideoState), seekTo);
                        lastVideoEventTime = response.data.newVideoEventCreationTime;
                    }

                } else {
                    if (response.data.message) {
                        alert(response.data.message);
                    }
                }
            })
            .catch(function (error) {
                if (ignorePolledVideoEvent) {
                    return;
                }

                canPollForVideoEvents = true;
//                console.log(error);
                alert("Unexpected error! Please try again!");
            });
    }

    function onNewVideoStateReceived(newVideoState, seekTo) {

        if (newVideoState.playerState === VIDEO_PLAYER_STATE.ENDED) {
            return;
        }

//        console.log("loading video state: ");
//        console.log(newVideoState);

        let newPlayerState = newVideoState.playerState;

        if(newVideoState.videoId !== curVideoState.videoId) {
            if (!isYoutubePlayerVisible()) {
                toggleYoutubePlayerDisplay();
            }

            videoStateChangeCallback = function () {
//                console.log("Calling callback.. with state: " + newPlayerState);

                switch(newPlayerState) {
                    case VIDEO_PLAYER_STATE.PLAYING:
                        youtubePlayer.playVideo();
                        break;
                    case VIDEO_PLAYER_STATE.PAUSED:
//                        console.log("Pausing video in callback");
                        youtubePlayer.pauseVideo();
                        break;
                }

                if (seekTo) {
                    youtubePlayer.seekTo(seekTo, true);
                }
            };

            youtubePlayer.loadVideoById(newVideoState.videoId);
        } else {
            videoStateChangeCallback = function () {
                if (seekTo) {
                    youtubePlayer.seekTo(seekTo, true);
                }
            };

            if (newPlayerState !== curVideoState.playerState) {
                switch(newPlayerState) {
                    case VIDEO_PLAYER_STATE.PLAYING:
                        youtubePlayer.playVideo();
                        break;
                    case VIDEO_PLAYER_STATE.PAUSED:
                        youtubePlayer.pauseVideo();
                        break;
                    case VIDEO_PLAYER_STATE.ENDED:
                        // If the video ended for someone else, let it still play for this user
                        //youtubePlayer.stopVideo();
                        break;
                }
            } else {
                videoStateChangeCallback();
                videoStateChangeCallback = null;
            }
        }

        curVideoState = newVideoState;
    }

  </script>
  <% } %>
</body>
</html>
