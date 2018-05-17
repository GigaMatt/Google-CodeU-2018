package codeu.controller.chat;

import codeu.model.data.Conversation;
import codeu.model.data.User;
import codeu.model.store.basic.ConversationStore;
import codeu.model.store.basic.MessageStore;
import codeu.model.store.basic.UserStore;
import codeu.model.store.basic.VideoEventStore;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Created by azizt on 5/16/2018.
 */
public class ChatRequestValidatorTest {
  private ChatRequestValidator chatRequestValidator;
  private ChatServletAgent chatServletAgent;

  private HttpServletRequest mockRequest;
  private HttpSession mockSession;
  private HttpServletResponse mockResponse;
  private ServletOutputStream mockOutputStream;

  @Before
  public void setup() {
    chatServletAgent = new ChatServletAgent();
    chatServletAgent.setUserStore(Mockito.mock(UserStore.class));
    chatServletAgent.setMessageStore(Mockito.mock(MessageStore.class));
    chatServletAgent.setConversationStore(Mockito.mock(ConversationStore.class));
    chatServletAgent.setVideoEventStore(Mockito.mock(VideoEventStore.class));

    chatRequestValidator = Mockito.spy(new ChatRequestValidator(chatServletAgent));

    mockRequest = Mockito.mock(HttpServletRequest.class);
    mockSession = Mockito.mock(HttpSession.class);
    Mockito.when(mockRequest.getSession()).thenReturn(mockSession);
    Mockito.when(mockRequest.getRequestURI()).thenReturn("/test/prefix/test_conversation");

    mockResponse = Mockito.mock(HttpServletResponse.class);
    mockOutputStream = Mockito.mock(ServletOutputStream.class);
    try {
      Mockito.when(mockResponse.getOutputStream()).thenReturn(mockOutputStream);
    } catch (IOException e) {
      Mockito.doThrow(e);
    }
  }

  @Test
  public void testReset() {
    chatRequestValidator.setUsernameOptional(Optional.of("Test"));
    chatRequestValidator.setUserOptional(Optional.of(Mockito.mock(User.class)));
    chatRequestValidator.setConversationOptional(Optional.of(Mockito.mock(Conversation.class)));

    chatRequestValidator.reset();

    Assert.assertEquals(false, chatRequestValidator.getUsernameOptional().isPresent());
    Assert.assertEquals(false, chatRequestValidator.getUserOptional().isPresent());
    Assert.assertEquals(false, chatRequestValidator.getConversationOptional().isPresent());
  }

  @Test
  public void testValidateRequest_UserNotLoggedIn() {
    Mockito.when(mockSession.getAttribute("user")).thenReturn(null);

    chatRequestValidator.validateRequest(mockRequest, "/test/prefix/");

    Mockito.verify(chatRequestValidator).reset();

    Assert.assertEquals(false, chatRequestValidator.getUsernameOptional().isPresent());
  }

  @Test
  public void testValidateRequest_InvalidUser() {
    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_user");
    Mockito.when(chatServletAgent.getUserStore().getUser("test_user")).thenReturn(null);

    chatRequestValidator.validateRequest(mockRequest, "/test/prefix/");

    Mockito.verify(chatRequestValidator).reset();

    Assert.assertEquals(true, chatRequestValidator.getUsernameOptional().isPresent());
    Assert.assertEquals("test_user", chatRequestValidator.getUsernameOptional().get());
    Assert.assertEquals(false, chatRequestValidator.getUserOptional().isPresent());
  }

  @Test
  public void testValidateRequest_InvalidConversation() {
    User testUser = Mockito.mock(User.class);

    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_user");
    Mockito.when(chatServletAgent.getUserStore().getUser("test_user")).thenReturn(testUser);
    Mockito.when(chatServletAgent.getConversationStore().getConversationWithTitle("test_conversation")).thenReturn(null);

    chatRequestValidator.validateRequest(mockRequest, "/test/prefix/");

    Mockito.verify(chatRequestValidator).reset();

    Assert.assertEquals(true, chatRequestValidator.getUsernameOptional().isPresent());
    Assert.assertEquals("test_user", chatRequestValidator.getUsernameOptional().get());
    Assert.assertEquals(true, chatRequestValidator.getUserOptional().isPresent());
    Assert.assertEquals(testUser, chatRequestValidator.getUserOptional().get());
    Assert.assertEquals(false, chatRequestValidator.getConversationOptional().isPresent());
  }

  @Test
  public void testValidateRequest_ValidRequest() {
    User testUser = Mockito.mock(User.class);
    Conversation testConversation = Mockito.mock(Conversation.class);

    Mockito.when(mockSession.getAttribute("user")).thenReturn("test_user");
    Mockito.when(chatServletAgent.getUserStore().getUser("test_user")).thenReturn(testUser);
    Mockito.when(chatServletAgent.getConversationStore().getConversationWithTitle("test_conversation")).thenReturn(testConversation);

    chatRequestValidator.validateRequest(mockRequest, "/test/prefix/");

    Mockito.verify(chatRequestValidator).reset();

    Assert.assertEquals(true, chatRequestValidator.getUsernameOptional().isPresent());
    Assert.assertEquals("test_user", chatRequestValidator.getUsernameOptional().get());
    Assert.assertEquals(true, chatRequestValidator.getUserOptional().isPresent());
    Assert.assertEquals(testUser, chatRequestValidator.getUserOptional().get());
    Assert.assertEquals(true, chatRequestValidator.getConversationOptional().isPresent());
    Assert.assertEquals(testConversation, chatRequestValidator.getConversationOptional().get());
  }

  @Test
  public void testRespondWithErrorMessage() throws IOException, JSONException {
    chatRequestValidator.respondWithErrorMessage(mockResponse, "Test Error Message!");

    ArgumentCaptor<String> responseDataStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    Mockito.verify(mockResponse.getOutputStream()).print(responseDataStringArgumentCaptor.capture());

    JSONObject responseData = new JSONObject(responseDataStringArgumentCaptor.getValue());
    Assert.assertEquals(false, responseData.getBoolean("success"));
    Assert.assertEquals("Test Error Message!", responseData.getString("message"));
  }
}
