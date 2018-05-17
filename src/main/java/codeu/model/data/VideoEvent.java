package codeu.model.data;

import java.time.Instant;
import java.util.UUID;

/* Class representing a VideoEvent */
public class VideoEvent {

    public static String getTestVideoStateJSON() {
      return "{}";
    }

    private final UUID id;
    private final UUID conversation;
    private final UUID author;
    private final String videoId;
    private final Instant creation;
    private final String videoStateJSON;

    /**
     * Constructs a new VideoEvent.
     *
     * @param id the ID of this VideoEvent
     * @param conversation the ID of the Conversation this VideoEvent belongs to
     * @param author the ID of the User who started this VideoEvent
     * @param videoId the video id of this VideoEvent
     * @param creation the creation time of this VideoEvent
     * @param videoStateJSON the state of the video after this VideoEvent in JSON format
     */
    public VideoEvent(UUID id, UUID conversation, UUID author, String videoId, Instant creation, String videoStateJSON) {
        this.id = id;
        this.conversation = conversation;
        this.author = author;
        this.videoId = videoId;
        this.creation = creation;
        this.videoStateJSON = videoStateJSON;
    }

    /** Returns the ID of this VideoEvent. */
    public UUID getId() {
        return id;
    }

    /** Returns the ID of the Conversation this VideoEvent belongs to. */
    public UUID getConversationId() {
        return conversation;
    }

    /** Returns the ID of the User who started this VideoEvent. */
    public UUID getAuthorId() {
        return author;
    }

    /** Returns the video id of this VideoEvent. */
    public String getVideoId() {
        return videoId;
    }

    /** Returns the creation time of this VideoEvent. */
    public Instant getCreationTime() {
        return creation;
    }

    /** Returns the state of the video in this VideoEvent in JSON format. */
    public String getVideoStateJSON() {
        return videoStateJSON;
    }
}