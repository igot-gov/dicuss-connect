package com.eagle.hubnotifier.service;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.model.*;
import com.eagle.hubnotifier.producer.NotifyHookProducer;
import com.eagle.hubnotifier.repository.HubPostRepository;
import com.eagle.hubnotifier.repository.HubTopicRepository;
import com.eagle.hubnotifier.repository.HubUserRepository;
import com.eagle.hubnotifier.repository.TopicFollowerRepository;
import com.eagle.hubnotifier.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private OutboundRequestHandlerServiceImpl requestHandlerService;

    @Autowired
    private HubPostRepository hubPostRepository;

    @Autowired
    private HubTopicRepository hubTopicRepository;

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
            String message = mapper.writeValueAsString(data);
            logger.info("Received Data : {}", message);
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
                    case Constants.FILTER_TOPIC_REPLY:
                        handleTopicReplyEvent(data);
                        break;
                    case Constants.ACTION_POST_UPVOTE:
                        handleTopicUpvoteEvent(data);
                        break;
                    case Constants.ACTION_POST_DOWNVOTE:
                        handleTopicDownVoteEvent(data);
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
        Map<String, Object> tagValues = new HashMap<>();
        List<String> topicTitleList = (List<String>) data.get(Constants.PARAM_TOPIC_TITLE_CONSTANT);
        tagValues.put(Constants.DISCUSSION_CREATION_TOPIC_TAG, topicTitleList.get(0));
        List<String> topicIds = (List<String>) data.get(Constants.PARAM_TOPIC_TID_CONSTANT);
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + topicIds.get(0));
        List<String> topicUids = (List<String>) data.get(Constants.PARAM_TOPIC_UID_CONSTANT);
        HubUser user = userRepository.findByKey(Constants.USER_ROLE + ":" + topicUids.get(0));
        Map<String, List<String>> recipients = new HashMap<>();
        recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(user.getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
        notifyHandler.sendNotification(nEvent);
    }

    /**
     * Handle Post Create Request
     *
     * @param data
     */
    private void handlePostCreate(Map<String, Object> data) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String message = mapper.writeValueAsString(data);
            logger.debug("Received Post Creation Event. Data : {}", message);
        } catch (JsonProcessingException e) {
            logger.error("Data parse exception occured : ", e);
        }

        NotificationEvent nEvent = new NotificationEvent();
        nEvent.setEventId("discussion_comment_creation");

        Map<String, List<String>> recipients = new HashMap<>();

        List<String> tidList = (List<String>) data.get("params[post][tid]");
        String tid = tidList.get(0);
        String tIdFolowerKey = "tid:" + tid + ":followers";
        logger.info("Post Created in Topic Id - {}", tid);

        // Get the Topic Followers
        TopicFollower topicFollower = topicRepository.findByKey(tIdFolowerKey);
        List<String> listeners = new ArrayList<>();
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
    }

    /**
     * Handle the topic reply event and send the notification on replying on the topic
     *
     * @param data
     */
    private void handleTopicReplyEvent(Map<String, Object> data) {
        NotificationEvent nEvent = new NotificationEvent();
        nEvent.setEventId(Constants.DISCUSSION_REPLY_EVENT_ID);
        Map<String, Object> tagValues = new HashMap<>();
        List<String> commentList = (List<String>) data.get(Constants.PARAM_CONTENT_CONSTANT);
        tagValues.put(Constants.COMMENT_TAG, commentList.get(0));
        List<String> repliedByUuids = (List<String>) data.get(Constants.PARAM_UID);
        HubUser repliedByUser = userRepository.findByKey(Constants.USER_ROLE + ":" + repliedByUuids.get(0));
        tagValues.put(Constants.COMMENTED_BY_NAME_TAG, repliedByUser.getUsername());
        List<String> topicIds = (List<String>) data.get(Constants.PARAM_TID);
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + topicIds.get(0));
        Map<String, List<String>> recipients = new HashMap<>();
        HubTopic topicData = hubTopicRepository.findByKey(Constants.TOPIC_VALUE_CONSTANTS+":"+topicIds.get(0));
        if (!ObjectUtils.isEmpty(topicData)) {
            tagValues.put(Constants.DISCUSSION_CREATION_TOPIC_TAG, topicData.getTitle());
            HubUser author = userRepository.findByKey(Constants.USER_ROLE + ":" + topicData.getUid());
            recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(author.getUsername()));
        }
        recipients.put(Constants.COMMENTED_BY_TAG, Arrays.asList(repliedByUser.getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
        notifyHandler.sendNotification(nEvent);
    }

    /**
     * Send the notification on upvote Event
     *
     * @param data
     */
    private void handleTopicUpvoteEvent(Map<String, Object> data) {
        NotificationEvent nEvent = new NotificationEvent();
        nEvent.setEventId(Constants.DISCUSSION_UPVOTE_EVENT);
        Map<String, Object> tagValues = new HashMap<>();
        List<String> postIds = (List<String>) data.get(Constants.PARAMS_PID);
        HubPost hubPost = hubPostRepository.findByKey(Constants.POST_ROLE + ":" + postIds.get(0));
        tagValues.put(Constants.COMMENT_TAG, hubPost.getContent());
        List<String> upvotedByUuids = (List<String>) data.get(Constants.PARAM_UID);
        List<HubUser> userList = userRepository.findByUUIDS(Arrays.asList(Constants.USER_ROLE + ":" + upvotedByUuids.get(0), Constants.USER_ROLE + ":" + hubPost.getUid()));
        Map<String, HubUser> hubUserMap = userList.stream().collect(Collectors.toMap(HubUser::getKey, hubUser -> hubUser));
        tagValues.put(Constants.UPVOTED_BY_NAME, hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername());
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + hubPost.getTid());
        Map<String, List<String>> recipients = new HashMap<>();
        recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername()));
        recipients.put(Constants.UPVOTED_BY, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
        notifyHandler.sendNotification(nEvent);
    }

    /**
     * Send the notification on downvote Event
     *
     * @param data
     */
    private void handleTopicDownVoteEvent(Map<String, Object> data) {
        NotificationEvent nEvent = new NotificationEvent();
        nEvent.setEventId(Constants.DISCUSSION_DOWNVOTE_EVENT);
        Map<String, Object> tagValues = new HashMap<>();
        List<String> postIds = (List<String>) data.get(Constants.PARAMS_PID);
        HubPost hubPost = hubPostRepository.findByKey(Constants.POST_ROLE + ":" + postIds.get(0));
        tagValues.put(Constants.COMMENT_TAG, hubPost.getContent());
        List<String> downvotedByUuids = (List<String>) data.get(Constants.PARAM_UID);
        List<HubUser> userList = userRepository.findByUUIDS(Arrays.asList(Constants.USER_ROLE + ":" + downvotedByUuids.get(0), Constants.USER_ROLE + ":" + hubPost.getUid()));
        Map<String, HubUser> hubUserMap = userList.stream().collect(Collectors.toMap(HubUser::getKey, hubUser -> hubUser));
        tagValues.put(Constants.DOWNVOTE_BY_NAME, hubUserMap.get(Constants.USER_ROLE + ":" + downvotedByUuids.get(0)).getUsername());
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + hubPost.getTid());
        Map<String, List<String>> recipients = new HashMap<>();
        recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername()));
        recipients.put(Constants.DOWNVOTE_BY, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + downvotedByUuids.get(0)).getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
        notifyHandler.sendNotification(nEvent);
    }
}
