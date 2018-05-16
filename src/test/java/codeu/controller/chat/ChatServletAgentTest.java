package codeu.controller.chat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;

public class ChatServletAgentTest {
    private ChatServletAgent chatServletAgent;
    
    private ConversationStore conversationStore;
    private MessageStore messageStore;
    private VideoEventStore videoEventStore;
    private UserStore userStore;

    @Before
    public void setup() {
        chatServletAgent = new ChatServletAgent();

        conversationStore = Mockito.mock(ConversationStore.class);
        messageStore = Mockito.mock(MessageStore.class);
        videoEventStore = Mockito.mock(VideoEventStore.class);
        userStore = Mockito.mock(UserStore.class);
    }

    @Test
    public void testConversationStore() {
        chatServletAgent.setConversationStore(conversationStore);
        Assert.assertEquals(conversationStore, chatServletAgent.getConversationStore());
    }
    
    @Test
    public void testMessageStore() {
        chatServletAgent.setMessageStore(messageStore);
        Assert.assertEquals(messageStore, chatServletAgent.getMessageStore());
    }
      
    @Test
    public void testVideoEventStore() {
        chatServletAgent.setVideoEventStore(videoEventStore);
        Assert.assertEquals(videoEventStore, chatServletAgent.getVideoEventStore());
    }
    
    @Test
    public void testUserStore() {
        chatServletAgent.setUserStore(userStore);
        Assert.assertEquals(userStore, chatServletAgent.getUserStore());
    }
}