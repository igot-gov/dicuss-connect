package com.eagle.hubnotifier.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.util.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eagle.hubnotifier.model.HubUser;
import com.eagle.hubnotifier.model.NotificationEvent;
import com.eagle.hubnotifier.model.TopicFollower;
import com.eagle.hubnotifier.producer.NotifyHookProducer;
import com.eagle.hubnotifier.repository.HubUserRepository;
import com.eagle.hubnotifier.repository.TopicFollowerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotifyHookServiceImpl implements NotifyHookService {
    Logger logger = LogManager.getLogger(NotifyHookServiceImpl.class);

    @Autowired
    private NotifyHookProducer producer;

    @Autowired
    private NotifyHandlerServiceImpl notifyHandler;

    @Autowired
    private TopicFollowerRepository topicRepository;

    @Autowired
    private HubUserRepository userRepository;

    @Autowired
    private Configuration configuration;

    @Override
    public void handleNotifiyRestRequest(Map<String, Object> data) {
        if (logger.isDebugEnabled()) {
            logger.info("Recived request from Rest Controller");
        }
        producer.push(configuration.getNotifyTopic(), data);
    }

    @Override
    public void handleNotifyKafkaTopicRequest(Map<String, Object> data) {
        logger.info("Recived request from Topic Consumer");
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.info("Received Data : {}", mapper.writeValueAsString(data));
        } catch (JsonProcessingException ex) {
            logger.error("Not able to parse the data", ex);
        }
        List<String> hookList = (List<String>) data.get("hook");
        if (hookList != null) {
            for (String hook : hookList) {
                switch (hook) {
                    case Constants.FILTER_TOPIC_CREATE:
                        handleTopicCreate(data);
                        break;
                    case Constants.FILTER_POST_CREATE:
                        handlePostCreate(data);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Send the notification on creating the topic
     *
     * @param data
     */
    @SuppressWarnings("unchecked")
    private void handleTopicCreate(Map<String, Object> data) {
        logger.info("Received Topic Creation Event");
        NotificationEvent nEvent = new NotificationEvent();
        nEvent.setEventId(Constants.DISCUSSION_CREATION_EVENT_ID);
        Map<String, Object> tagValues = new HashMap<String, Object>();
        List<String> topicTitleList = (List<String>) data.get(Constants.PARAM_TOPIC_TITLE_CONSTANT);
        tagValues.put(Constants.DISCUSSION_CREATION_TOPIC_TAG, topicTitleList.get(0));
        List<String> topicIds = (List<String>) data.get(Constants.PARAM_TOPIC_TID_CONSTANT);
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + topicIds.get(0));
        List<String> topicUids = (List<String>) data.get(Constants.PARAM_TOPIC_UID_CONSTANT);
        HubUser user = userRepository.findByKey(Constants.USER_ROLE + ":" + topicUids.get(0));
        Map<String, List<String>> recipients = new HashMap<String, List<String>>();
        recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(user.getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
        notifyHandler.sendNotification(nEvent);
    }

    private void handlePostCreate(Map<String, Object> data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            logger.debug("Received Post Creation Event. Data : {}", mapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
        }

        NotificationEvent nEvent = new NotificationEvent();
        nEvent.setEventId("discussion_comment_creation");

        Map<String, List<String>> recipients = new HashMap<String, List<String>>();

        List<String> tidList = (List<String>) data.get("params[post][tid]");
        String tid = tidList.get(0);
        String tIdFolowerKey = "tid:" + tid + ":followers";
        logger.info("Post Created in Topic Id - {}", tid);

        // Get the Topic Followers
        TopicFollower topicFollower = topicRepository.findByKey(tIdFolowerKey);
        List<String> listeners = new ArrayList<String>();
        if (topicFollower != null) {
            for (String uid : topicFollower.getMembers()) {
                logger.info("Fetching User details for UID - {}", uid);
                String userKey = Constants.USER_ROLE + ":" + uid;
                HubUser user = userRepository.findByKey(userKey);
                if (user != null) {
                    listeners.add(user.getUsername());
                }
            }
            logger.info("Topic: {} has followed by : {}", tid, listeners);
        }

        recipients.put("listeners", listeners);

        //TODO -- Construct the nEvent object and send request to NotificationService.
    }
}
