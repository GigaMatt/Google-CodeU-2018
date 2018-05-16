package codeu.model.store.basic;

import codeu.model.data.Video;
import codeu.model.store.persistence.PersistentStorageAgent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class VideoStoreTest {

  private VideoStore videoStore;
  private PersistentStorageAgent mockPersistentStorageAgent;

  private final UUID CONVERSATION_ID_ONE = UUID.randomUUID();
  private final Video VIDEO_ONE =
      new Video(
          UUID.randomUUID(),
          CONVERSATION_ID_ONE,
          UUID.randomUUID(),
          "video id one",
          Instant.ofEpochMilli(1000));
  private final Video VIDEO_TWO =
      new Video(
          UUID.randomUUID(),
          CONVERSATION_ID_ONE,
          UUID.randomUUID(),
          "video id two",
          Instant.ofEpochMilli(2000));
  private final Video VIDEO_THREE =
      new Video(
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID(),
          "video id three",
          Instant.ofEpochMilli(3000));

  @Before
  public void setup() {
    mockPersistentStorageAgent = Mockito.mock(PersistentStorageAgent.class);
    videoStore = VideoStore.getTestInstance(mockPersistentStorageAgent);

    final List<Video> videoList = new ArrayList<>();
    videoList.add(VIDEO_ONE);
    videoList.add(VIDEO_TWO);
    videoList.add(VIDEO_THREE);
    videoStore.setVideos(videoList);
  }

  @Test
  public void testGetVideosInConversation() {
    List<Video> resultVideos = videoStore.getVideosInConversation(CONVERSATION_ID_ONE);

    Assert.assertEquals(2, resultVideos.size());
    assertEquals(VIDEO_ONE, resultVideos.get(0));
    assertEquals(VIDEO_TWO, resultVideos.get(1));
  }

  @Test
  public void testAddVideo() {
    UUID inputConversationId = UUID.randomUUID();
    Video inputVideo =
        new Video(
            UUID.randomUUID(),
            inputConversationId,
            UUID.randomUUID(),
            "test video id",
            Instant.now());

    videoStore.addVideo(inputVideo);
    Video resultVideo = videoStore.getVideosInConversation(inputConversationId).get(0);

    assertEquals(inputVideo, resultVideo);
    Mockito.verify(mockPersistentStorageAgent).writeThrough(inputVideo);
  }

  private void assertEquals(Video expectedVideo, Video actualVideo) {
    Assert.assertEquals(expectedVideo.getId(), actualVideo.getId());
    Assert.assertEquals(expectedVideo.getConversationId(), actualVideo.getConversationId());
    Assert.assertEquals(expectedVideo.getAuthorId(), actualVideo.getAuthorId());
    Assert.assertEquals(expectedVideo.getVideoId(), actualVideo.getVideoId());
    Assert.assertEquals(expectedVideo.getCreationTime(), actualVideo.getCreationTime());
  }
}
