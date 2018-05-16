
package codeu.model.data;

import java.time.Instant;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

public class VideoTest {

  @Test
  public void testCreate() {
    UUID id = UUID.randomUUID();
    UUID conversation = UUID.randomUUID();
    UUID author = UUID.randomUUID();
    String videoId = "test video id";
    Instant creation = Instant.now();

    Video video = new Video(id, conversation, author, videoId, creation);

    Assert.assertEquals(id, video.getId());
    Assert.assertEquals(conversation, video.getConversationId());
    Assert.assertEquals(author, video.getAuthorId());
    Assert.assertEquals(videoId, video.getVideoId());
    Assert.assertEquals(creation, video.getCreationTime());
  }
}
