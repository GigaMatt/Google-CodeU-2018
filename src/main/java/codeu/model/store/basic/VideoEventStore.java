package codeu.model.store.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import codeu.model.data.VideoEvent;
import codeu.model.store.persistence.PersistentStorageAgent;

/**
 * Store class that uses in-memory data structures to hold values and automatically loads from and
 * saves to PersistentStorageAgent. It's a singleton so all servlet classes can access the same
 * instance.
 */
public class VideoEventStore {
    /** Singleton instance of VideoEventStore. */
    private static VideoEventStore instance;

    /**
     * Returns the singleton instance of VideoEventStore that should be shared between all servlet
     * classes. Do not call this function from a test; use getTestInstance() instead.
     */
    public static VideoEventStore getInstance() {
        if (instance == null) {
            instance = new VideoEventStore(PersistentStorageAgent.getInstance());
        }
        return instance;
    }

    /**
     * Instance getter function used for testing. Supply a mock for PersistentStorageAgent.
     *
     * @param persistentStorageAgent a mock used for testing
     */
    public static VideoEventStore getTestInstance(PersistentStorageAgent persistentStorageAgent) {
        return new VideoEventStore(persistentStorageAgent);
    }

    /**
     * The PersistentStorageAgent responsible for loading VideoEvents from and saving VideoEvents to
     * Datastore.
     */
    private PersistentStorageAgent persistentStorageAgent;

    /** The in-memory list of VideoEvents. */
    private List<VideoEvent> videoEvents;

    /** This class is a singleton, so its constructor is private. Call getInstance() instead. */
    private VideoEventStore(PersistentStorageAgent persistentStorageAgent) {
        this.persistentStorageAgent = persistentStorageAgent;
        videoEvents = new ArrayList<>();
    }

    /**
     * Load a set of randomly-generated VideoEvent objects.
     *
     * @return false if an error occurs.
     */
    public boolean loadTestData() {
        boolean loaded = false;
        
        // Todo (Azee): Add Random video event objects to the list. 
        
        loaded = true;
        
        return loaded;
    }

    /** Add a new video event to the current set of video events known to the application. */
    public void addVideoEvent(VideoEvent videoEvent) {
        videoEvents.add(videoEvent);
        persistentStorageAgent.writeThrough(videoEvent);
    }

    /** Updates the existing video event. */
    public void updateVideoEvent(VideoEvent videoEvent) {
        persistentStorageAgent.writeThrough(videoEvent);
    }

    /** Access the current set of Video Events within the given Conversation. */
    public List<VideoEvent> getVideoEventsInConversation(UUID conversationId) {

        List<VideoEvent> videosInConversation = new ArrayList<>();

        for (VideoEvent videoEvent : videoEvents) {
            if (videoEvent.getConversationId().equals(conversationId)) {
                videosInConversation.add(videoEvent);
            }
        }

        return videosInConversation;
    }

    /** Sets the List of Video Events stored by this VideoStore. */
    public void setVideoEvents(List<VideoEvent> videoEvents) {
        this.videoEvents = videoEvents;
    }

    /** Access all the video events known to the application. */
    public List<VideoEvent> getAllVideoEvents() {
        return videoEvents;
    }
}
