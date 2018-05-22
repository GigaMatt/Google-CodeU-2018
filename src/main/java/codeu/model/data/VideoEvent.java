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
    private String videoId;
    private Instant creation;
    private String videoStateJSON;  // Only used by the front end

    private UUID seekOwner;
    private double seekTime;

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
    public VideoEvent(UUID id, UUID conversation, UUID author, String videoId, Instant creation, String videoStateJSON, UUID seekOwner, double seekTime) {
        this.id = id;
        this.conversation = conversation;
        this.author = author;
        this.videoId = videoId;
        this.creation = creation;
        this.videoStateJSON = videoStateJSON;
        this.seekOwner = seekOwner;
        this.seekTime = seekTime;
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

    public UUID getSeekOwnerId() {
      return seekOwner;
    }

    public double getSeekTime() {
      return seekTime;
    }

    public void setVideoStateJSON(String videoStateJSON) {
      this.videoStateJSON = videoStateJSON;
    }

    public void setVideoId(String videoId) {
      this.videoId = videoId;
    }

    public void setCreation(Instant creation) {
      this.creation = creation;
    }

    public void setSeekOwner(UUID seekOwner) {
      this.seekOwner = seekOwner;
    }

    public void setSeekTime(double seekTime) {
      this.seekTime = seekTime;
    }
}