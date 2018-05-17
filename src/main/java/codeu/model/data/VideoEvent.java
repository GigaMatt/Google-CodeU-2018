package codeu.model.data;

import java.time.Instant;
import java.util.UUID;

/* Class representing a VideoEvent */
public class VideoEvent {
    private final UUID id;
    private final UUID conversation;
    private final UUID author;
    private final String videoId;
    private final Instant creation;

    /**
     * Constructs a new VideoEvent.
     *
     * @param id the ID of this VideoEvent
     * @param conversation the ID of the Conversation this VideoEvent belongs to
     * @param author the ID of the User who started this VideoEvent
     * @param videoId the video id of this VideoEvent
     * @param creation the creation time of this VideoEvent
     */
    public VideoEvent(UUID id, UUID conversation, UUID author, String videoId, Instant creation) {
        this.id = id;
        this.conversation = conversation;
        this.author = author;
        this.videoId = videoId;
        this.creation = creation;
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
}