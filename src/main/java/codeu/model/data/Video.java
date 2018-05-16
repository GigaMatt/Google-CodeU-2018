package codeu.model.data;

import java.time.Instant;
import java.util.UUID;

/* Class representing a Video */
public class Video {
    private final UUID id;
    private final UUID conversation;
    private final UUID author;
    private final String videoId;
    private final Instant creation;

    /**
     * Constructs a new Video.
     *
     * @param id the ID of this Video
     * @param conversation the ID of the Conversation this Video belongs to
     * @param author the ID of the User who started this Video
     * @param videoId the video id of this Video
     * @param creation the creation time of this Video
     */
    public Video(UUID id, UUID conversation, UUID author, String videoId, Instant creation) {
        this.id = id;
        this.conversation = conversation;
        this.author = author;
        this.videoId = videoId;
        this.creation = creation;
    }

    /** Returns the ID of this Video. */
    public UUID getId() {
        return id;
    }

    /** Returns the ID of the Conversation this Video belongs to. */
    public UUID getConversationId() {
        return conversation;
    }

    /** Returns the ID of the User who started this Video. */
    public UUID getAuthorId() {
        return author;
    }

    /** Returns the video id of this Video. */
    public String getVideoId() {
        return videoId;
    }

    /** Returns the creation time of this Video. */
    public Instant getCreationTime() {
        return creation;
    }
}