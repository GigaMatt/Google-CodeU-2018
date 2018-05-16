package codeu.controller.chat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoStore;

public class ChatServletAgentTest {
    private ChatServletAgent chatServletAgent;
    
    private ConversationStore conversationStore;
    private MessageStore messageStore;
    private VideoStore videoStore;
    private UserStore userStore;

    @Before
    public void setup() {
        chatServletAgent = new ChatServletAgent();

        conversationStore = Mockito.mock(ConversationStore.class);
        messageStore = Mockito.mock(MessageStore.class);
        videoStore = Mockito.mock(VideoStore.class);
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
    public void testVideoStore() {
        chatServletAgent.setVideoStore(videoStore);
        Assert.assertEquals(videoStore, chatServletAgent.getVideoStore());
    }
    
    @Test
    public void testUserStore() {
        chatServletAgent.setUserStore(userStore);
        Assert.assertEquals(userStore, chatServletAgent.getUserStore());
    }
}