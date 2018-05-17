
package codeu.model.data;

import java.time.Instant;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class VideoEventTest {

  @Test
  public void testCreate() {
    UUID id = UUID.randomUUID();
    UUID conversation = UUID.randomUUID();
    UUID author = UUID.randomUUID();
    String videoId = "test video id";
    Instant creation = Instant.now();

    VideoEvent video = new VideoEvent(id, conversation, author, videoId, creation);

    Assert.assertEquals(id, video.getId());
    Assert.assertEquals(conversation, video.getConversationId());
    Assert.assertEquals(author, video.getAuthorId());
    Assert.assertEquals(videoId, video.getVideoId());
    Assert.assertEquals(creation, video.getCreationTime());
  }
}
