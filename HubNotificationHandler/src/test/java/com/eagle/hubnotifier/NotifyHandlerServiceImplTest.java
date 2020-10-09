package com.eagle.hubnotifier;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.constants.Constants;
import com.eagle.hubnotifier.model.HubUser;
import com.eagle.hubnotifier.model.NotificationEvent;
import com.eagle.hubnotifier.repository.HubPostRepository;
import com.eagle.hubnotifier.repository.HubTopicRepository;
import com.eagle.hubnotifier.repository.HubUserRepository;
import com.eagle.hubnotifier.service.impl.NotifyHandlerServiceImpl;
import com.eagle.hubnotifier.service.impl.NotifyHookServiceImpl;
import com.eagle.hubnotifier.service.impl.OutboundRequestHandlerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
class NotifyHandlerServiceImplTest {

    @InjectMocks
    private NotifyHookServiceImpl notifyHandlerService;

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

    @Mock
    private HashMap<String, Object> mapTest;

    @InjectMocks
    private OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

    @Test
    void testing() {
        assertEquals("amd", "amd");
    }


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void handleTopicCreate() throws Exception {
        Map<String, Object> topicData = new HashMap<>();
        HubUser user = mock(HubUser.class);
        topicData.put(Constants.PARAM_TOPIC_TITLE_CONSTANT, Arrays.asList("New topic check"));
        topicData.put(Constants.PARAM_TOPIC_TID_CONSTANT, Arrays.asList("1"));
        topicData.put(Constants.PARAM_TOPIC_UID_CONSTANT, Arrays.asList("1"));
        when(userRepository.findByKey("user")).thenReturn(user);
        notifyHandlerService.handleTopicCreate(topicData);
    }

    @Test
    void sendNotification() {
        NotificationEvent notificationEvent = mock(NotificationEvent.class);
        doNothing().when(notifyHandlerServiceImpl).sendNotification(notificationEvent);
    }

    @Test
    void OutBoundGetRequest() {
        NotificationEvent notificationEvent = mock(NotificationEvent.class);
        StringBuilder builder = new StringBuilder();
        builder.append("http//localhost:9090/notifyHandler");
        ResponseEntity entity  =  (ResponseEntity) outboundRequestHandlerService.fetchResultUsingPost(builder, notificationEvent);
        assertTrue(entity!=null);
        assertTrue(entity.getStatusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
