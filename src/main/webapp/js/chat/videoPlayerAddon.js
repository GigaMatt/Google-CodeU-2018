/**
 * Created by azee on 5/23/2018.
 */


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

// Stores the last video event polled from server
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
            curVideoState.lastModifiedBy = LOGGED_IN_USERNAME;
            break;
    }
}

function removePrefix(str, prefix) {
    if (str.startsWith(prefix)) {
        return str.replace(prefix, '');
    }

    return str;
}

// Loads a youtube video using the entered videoId
function onYoutubeVideoIdSubmitted() {
    //console.log(youtubePlayer);
    let videoIdInput = document.querySelector("#youtube-player-videoid-input");
    let videoId = videoIdInput.value.trim();

    // Getting VideoId from URL
    videoId = removePrefix(videoId, 'https://');
    videoId = removePrefix(videoId, 'http://');
    videoId = removePrefix(videoId, 'www.');

    videoId = removePrefix(videoId, 'youtu.be/');
    videoId = removePrefix(videoId, 'youtube.com/embed/');

    if(videoId.startsWith('youtube.com/watch?')) {
        videoId = videoId.replace('youtube.com/watch?', '');

        let urlParams = videoId.split('&');
        urlParams.forEach(function (param) {
           let keyVal = param.split('=');
           if (keyVal.length === 2) {
               if (keyVal[0] === 'v') {
                   videoId = keyVal[1];
               }
           }
        });
    }

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
    axios.post("/chat/video/" + CONVERSATION_TITLE, createPostString(postData))
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
    axios.post("/chat/video/poll/" + CONVERSATION_TITLE, createPostString(postData))
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
