package codeu.model.store.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import codeu.model.data.Video;
import codeu.model.store.persistence.PersistentStorageAgent;

/**
 * Store class that uses in-memory data structures to hold values and automatically loads from and
 * saves to PersistentStorageAgent. It's a singleton so all servlet classes can access the same
 * instance.
 */
public class VideoStore {
    /** Singleton instance of VideoStore. */
    private static VideoStore instance;

    /**
     * Returns the singleton instance of VideoStore that should be shared between all servlet
     * classes. Do not call this function from a test; use getTestInstance() instead.
     */
    public static VideoStore getInstance() {
        if (instance == null) {
            instance = new VideoStore(PersistentStorageAgent.getInstance());
        }
        return instance;
    }

    /**
     * Instance getter function used for testing. Supply a mock for PersistentStorageAgent.
     *
     * @param persistentStorageAgent a mock used for testing
     */
    public static VideoStore getTestInstance(PersistentStorageAgent persistentStorageAgent) {
        return new VideoStore(persistentStorageAgent);
    }

    /**
     * The PersistentStorageAgent responsible for loading Videos from and saving Videos to
     * Datastore.
     */
    private PersistentStorageAgent persistentStorageAgent;

    /** The in-memory list of Videos. */
    private List<Video> videos;

    /** This class is a singleton, so its constructor is private. Call getInstance() instead. */
    private VideoStore(PersistentStorageAgent persistentStorageAgent) {
        this.persistentStorageAgent = persistentStorageAgent;
        videos = new ArrayList<>();
    }

    /**
     * Load a set of randomly-generated Video objects.
     *
     * @return false if an error occurs.
     */
    public boolean loadTestData() {
        boolean loaded = false;
        
        // Todo (Azee): Add Random video objects to the list. 
        
        loaded = true;
        
        return loaded;
    }

    /** Add a new video to the current set of videos known to the application. */
    public void addVideo(Video video) {
        videos.add(video);
        persistentStorageAgent.writeThrough(video);
    }

    /** Access the current set of Videos within the given Conversation. */
    public List<Video> getVideosInConversation(UUID conversationId) {

        List<Video> videosInConversation = new ArrayList<>();

        for (Video video : videos) {
            if (video.getConversationId().equals(conversationId)) {
                videosInConversation.add(video);
            }
        }

        return videosInConversation;
    }

    /** Sets the List of Videos stored by this VideoStore. */
    public void setVideos(List<Video> videos) {
        this.videos = videos;
    }

    /** Access all the videos known to the application. */
    public List<Video> getAllVideos() {
        return videos;
    }
}
