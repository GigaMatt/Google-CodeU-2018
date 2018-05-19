package codeu.model.store.basic;

import codeu.model.data.VideoEvent;
import codeu.model.store.persistence.PersistentDataStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class VideoStoreTest {

  private VideoEventStore videoEventStore;
  private PersistentDataStore mockPersistentStorageAgent;

  private final UUID CONVERSATION_ID_ONE = UUID.randomUUID();
  private final VideoEvent VIDEO_EVENT_ONE =
      new VideoEvent(
          UUID.randomUUID(),
          CONVERSATION_ID_ONE,
          UUID.randomUUID(),
          "video id one",
          Instant.ofEpochMilli(1000),
          VideoEvent.getTestVideoStateJSON());
  private final VideoEvent VIDEO_EVENT_TWO =
      new VideoEvent(
          UUID.randomUUID(),
          CONVERSATION_ID_ONE,
          UUID.randomUUID(),
          "video id two",
          Instant.ofEpochMilli(2000),
          VideoEvent.getTestVideoStateJSON());
  private final VideoEvent VIDEO_EVENT_THREE =
      new VideoEvent(
          UUID.randomUUID(),
          UUID.randomUUID(),
          UUID.randomUUID(),
          "video id three",
          Instant.ofEpochMilli(3000),
          VideoEvent.getTestVideoStateJSON());

  @Before
  public void setup() {
    mockPersistentStorageAgent = Mockito.mock(PersistentDataStore.class);
    videoEventStore = new VideoEventStore(mockPersistentStorageAgent);

    final List<VideoEvent> videoEventList = new ArrayList<>();
    videoEventList.add(VIDEO_EVENT_ONE);
    videoEventList.add(VIDEO_EVENT_TWO);
    videoEventList.add(VIDEO_EVENT_THREE);
    videoEventStore.setVideoEvents(videoEventList);
  }

  @Test
  public void testGetVideoEventsInConversation() {
    List<VideoEvent> resultVideoEvents = videoEventStore.getVideoEventsInConversation(CONVERSATION_ID_ONE);

    Assert.assertEquals(2, resultVideoEvents.size());
    assertEquals(VIDEO_EVENT_ONE, resultVideoEvents.get(0));
    assertEquals(VIDEO_EVENT_TWO, resultVideoEvents.get(1));
  }

  @Test
  public void testAddVideoEvent() {
    UUID inputConversationId = UUID.randomUUID();
    VideoEvent inputVideoEvent =
        new VideoEvent(
            UUID.randomUUID(),
            inputConversationId,
            UUID.randomUUID(),
            "test video id",
            Instant.now(),
            VideoEvent.getTestVideoStateJSON());

    videoEventStore.addVideoEvent(inputVideoEvent);
    VideoEvent resultVideoEvent = videoEventStore.getVideoEventsInConversation(inputConversationId).get(0);

    assertEquals(inputVideoEvent, resultVideoEvent);
    Mockito.verify(mockPersistentStorageAgent).writeThrough(inputVideoEvent);
  }

  private void assertEquals(VideoEvent expectedVideoEvent, VideoEvent actualVideoEvent) {
    Assert.assertEquals(expectedVideoEvent.getId(), actualVideoEvent.getId());
    Assert.assertEquals(expectedVideoEvent.getConversationId(), actualVideoEvent.getConversationId());
    Assert.assertEquals(expectedVideoEvent.getAuthorId(), actualVideoEvent.getAuthorId());
    Assert.assertEquals(expectedVideoEvent.getVideoId(), actualVideoEvent.getVideoId());
    Assert.assertEquals(expectedVideoEvent.getCreationTime(), actualVideoEvent.getCreationTime());
  }
}
