package com.eagle.hubnotifier;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.model.HubPost;
import com.eagle.hubnotifier.model.HubTopic;
import com.eagle.hubnotifier.model.HubUser;
import com.eagle.hubnotifier.model.NotificationEvent;
import com.eagle.hubnotifier.repository.HubPostRepository;
import com.eagle.hubnotifier.repository.HubTopicRepository;
import com.eagle.hubnotifier.repository.HubUserRepository;
import com.eagle.hubnotifier.service.impl.NotifyHandlerServiceImpl;
import com.eagle.hubnotifier.service.impl.NotifyHookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
class NotifyHandlerServiceImplTest {

    @Mock
    private HubUserRepository userRepository;

    @Mock
    public Configuration configuration;

    @Mock
    private HubPostRepository hubPostRepository;

    @Mock
    private HubTopicRepository hubTopicRepository;

    @Mock
    private NotifyHandlerServiceImpl notifyHandlerServiceImpl;

    final String DISCUSSION_BASE_URL = "base_url";
    final String USER_CONST = "user";
    final String TOPIC_DATA = "topicData";
    final String POST_DATA = "postData";
    final String NOTIFICATION_SERVICE_HOST = "ipaddress";
    final String NOTIFICATION_SERVICE_PATH = "path";
    final String EVENT_ID = "eventId";
    final String ROOT_ORG = "root_Org";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void handleNotifyKafkaTopicRequest() {
        NotifyHookServiceImpl notifyHookService = mock(NotifyHookServiceImpl.class);
        doNothing().when(notifyHookService).handleNotifyKafkaTopicRequest(Matchers.any(Map.class));
    }

    @Test
    void handleTopicCreate() throws Exception {
        HubUser user = mock(HubUser.class);
        NotifyHookServiceImpl notifyHookService = mock(NotifyHookServiceImpl.class);
        when(configuration.getDiscussionCreateUrl()).thenReturn(DISCUSSION_BASE_URL);
        when(userRepository.findByKey(USER_CONST)).thenReturn(user);
        doNothing().when(notifyHookService).handleTopicCreate(Matchers.any(Map.class));
    }

    @Test
    void handleTopicReplyEvent() throws Exception {
        HubUser user = mock(HubUser.class);
        HubTopic topic = mock(HubTopic.class);
        Map<String, List<String>> recipients = new HashMap<>();
        Map<String, Object> values = new HashMap<>();
        NotifyHookServiceImpl notifyHookService = mock(NotifyHookServiceImpl.class);
        NotificationEvent notificationEvent = mock(NotificationEvent.class);
        when(notificationEvent.getEventId()).thenReturn(EVENT_ID);
        when(notificationEvent.getRecipients()).thenReturn(recipients);
        when(notificationEvent.getRootOrg()).thenReturn(ROOT_ORG);
        when(notificationEvent.getTagValues()).thenReturn(values);
        when(notificationEvent.getTargetData()).thenReturn(values);
        when(configuration.getDiscussionCreateUrl()).thenReturn(DISCUSSION_BASE_URL);
        when(hubTopicRepository.findByKey(TOPIC_DATA)).thenReturn(topic);
        when(userRepository.findByKey(USER_CONST)).thenReturn(user);
        doNothing().when(notifyHookService).handleTopicReplyEvent(Matchers.any(Map.class));
    }

    @Test
    void handleTopicUpvoteEvent() {
        HubPost post = mock(HubPost.class);
        HubUser user = mock(HubUser.class);
        NotifyHookServiceImpl notifyHookService = mock(NotifyHookServiceImpl.class);
        when(hubPostRepository.findByKey(POST_DATA)).thenReturn(post);
        List<HubUser> userList = new ArrayList<>();
        userList.add(user);
        when(userRepository.findByUUIDS(Arrays.asList("1", "2"))).thenReturn(userList);
        doNothing().when(notifyHookService).handleTopicUpvoteEvent(Matchers.any(Map.class));
    }

    @Test
    void handleTopicDownVoteEvent() {
        HubPost post = mock(HubPost.class);
        HubUser user = mock(HubUser.class);
        NotifyHookServiceImpl notifyHookService = mock(NotifyHookServiceImpl.class);
        when(hubPostRepository.findByKey(POST_DATA)).thenReturn(post);
        List<HubUser> userList = new ArrayList<>();
        userList.add(user);
        when(userRepository.findByUUIDS(Arrays.asList("1", "2"))).thenReturn(userList);
        doNothing().when(notifyHookService).handleTopicDownVoteEvent(Matchers.any(Map.class));
    }

    @Test
    void sendNotification() {
        NotificationEvent notificationEvent = mock(NotificationEvent.class);
        when(configuration.getNotifyServiceHost()).thenReturn(NOTIFICATION_SERVICE_HOST);
        when(configuration.getNotifyServicePath()).thenReturn(NOTIFICATION_SERVICE_PATH);
        doNothing().when(notifyHandlerServiceImpl).sendNotification(notificationEvent);
    }
}
