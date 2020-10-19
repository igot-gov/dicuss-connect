package com.eagle.hubnotifier.service.impl;

import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.constants.Constants;
import com.eagle.hubnotifier.model.*;
import com.eagle.hubnotifier.producer.NotifyHookProducer;
import com.eagle.hubnotifier.repository.*;
import com.eagle.hubnotifier.service.interfaces.NotifyHookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
    private HubPostRepository hubPostRepository;

    @Autowired
    private HubTopicRepository hubTopicRepository;

    @Autowired
    private TopicPostersRepositoy topicPostersRepositoy;

    @Autowired
    private TopicFollowerRepository topicFollowerRepository;

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
        List<String> hookList = (List<String>) data.get(Constants.HOOK_VALUE);
        if (hookList != null) {
            NotificationEvent notificationEvent = null;
            for (String hook : hookList) {
                notificationEvent = new NotificationEvent();
                boolean notificationEnabledForEvent = true;
                switch (hook) {
                    case Constants.FILTER_TOPIC_CREATE:
                        handleTopicCreate(data, notificationEvent);
                        break;
                    case Constants.FILTER_POST_CREATE:
                        handlePostCreate(data, notificationEvent);
                        break;
                    case Constants.FILTER_TOPIC_REPLY:
                        handleTopicReplyEvent(data, notificationEvent);
                        break;
                    case Constants.ACTION_POST_UPVOTE:
                        handleTopicUpvoteEvent(data, notificationEvent);
                        break;
                    case Constants.ACTION_POST_DOWNVOTE:
                        handleTopicDownVoteEvent(data, notificationEvent);
                        break;
                    default:
                        notificationEnabledForEvent = false;
                        break;
                }
                if (notificationEnabledForEvent) {
                    notifyHandler.sendNotification(notificationEvent);
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
    private void handleTopicCreate(Map<String, Object> data, NotificationEvent nEvent) {
        logger.info("Received Topic Creation Event");
        nEvent.setEventId(Constants.DISCUSSION_CREATION_EVENT_ID);
        Map<String, Object> tagValues = new HashMap<>();
        tagValues.put(Constants.DISCUSSION_CREATION_TOPIC_TAG, ((List<String>) data.get(Constants.PARAM_TOPIC_TITLE_CONSTANT)).get(0));
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + ((List<String>) data.get(Constants.PARAM_TOPIC_TID_CONSTANT)).get(0));
        List<String> topicUids = (List<String>) data.get(Constants.PARAM_TOPIC_UID_CONSTANT);
        HubUser user = userRepository.findByKey(Constants.USER_ROLE + ":" + topicUids.get(0));
        Map<String, List<String>> recipients = new HashMap<>();
        recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(user.getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
    }

    /**
     * Handle Post Create Request
     *
     * @param data
     */
    private void handlePostCreate(Map<String, Object> data, NotificationEvent nEvent) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String message = mapper.writeValueAsString(data);
            logger.debug("Received Post Creation Event. Data : {}", message);
        } catch (JsonProcessingException e) {
            logger.error("Data parse exception occured : ", e);
        }
        nEvent.setEventId("discussion_comment_creation");

        Map<String, List<String>> recipients = new HashMap<>();

        String tid = ((List<String>) data.get("params[post][tid]")).get(0);
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
    private void handleTopicReplyEvent(Map<String, Object> data, NotificationEvent nEvent) {
        nEvent.setEventId(Constants.DISCUSSION_REPLY_EVENT_ID);
        Map<String, Object> tagValues = new HashMap<>();
        tagValues.put(Constants.COMMENT_TAG, ((List<String>) data.get(Constants.PARAM_CONTENT_CONSTANT)).get(0));
        HubUser repliedByUser = userRepository.findByKey(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0));
        tagValues.put(Constants.COMMENTED_BY_NAME_TAG, repliedByUser.getUsername());
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + ((List<String>) data.get(Constants.PARAM_TID)).get(0));
        Map<String, List<String>> recipients = new HashMap<>();
        HubTopic topicData = hubTopicRepository.findByKey(Constants.TOPIC_VALUE_CONSTANTS + ":" + ((List<String>) data.get(Constants.PARAM_TID)).get(0));
        if (!ObjectUtils.isEmpty(topicData)) {
            tagValues.put(Constants.DISCUSSION_CREATION_TOPIC_TAG, topicData.getTitle());
            HubUser author = userRepository.findByKey(Constants.USER_ROLE + ":" + topicData.getUid());
            List<String> watchersList = getWatchersList(String.valueOf(topicData.getTid()));
            watchersList = getFinalListOfWatchers(watchersList, author.getUsername(), repliedByUser.getUsername());
            recipients.put(Constants.AUTHOR_ROLE, watchersList);
        }
        recipients.put(Constants.COMMENTED_BY_TAG, Arrays.asList(repliedByUser.getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
    }

    /**
     * Send the notification on upvote Event
     *
     * @param data
     */
    private void handleTopicUpvoteEvent(Map<String, Object> data, NotificationEvent nEvent) {
        nEvent.setEventId(Constants.DISCUSSION_UPVOTE_EVENT);
        Map<String, Object> tagValues = new HashMap<>();
        HubPost hubPost = hubPostRepository.findByKey(Constants.POST_ROLE + ":" + ((List<String>) data.get(Constants.PARAMS_PID)).get(0));
        tagValues.put(Constants.COMMENT_TAG, hubPost.getContent());
        List<String> upvotedByUuids = (List<String>) data.get(Constants.PARAM_UID);
        List<HubUser> userList = userRepository.findByUUIDS(Arrays.asList(Constants.USER_ROLE + ":" + upvotedByUuids.get(0), Constants.USER_ROLE + ":" + hubPost.getUid()));
        Map<String, HubUser> hubUserMap = userList.stream().collect(Collectors.toMap(HubUser::getKey, hubUser -> hubUser));
        tagValues.put(Constants.UPVOTED_BY_NAME, hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername());
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + hubPost.getTid());
        Map<String, List<String>> recipients = new HashMap<>();
        List<String> watchersList = getWatchersList(String.valueOf(hubPost.getTid()));
        watchersList = getFinalListOfWatchers(watchersList, hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername(), hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername());
        recipients.put(Constants.AUTHOR_ROLE, watchersList);
        recipients.put(Constants.UPVOTED_BY, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
    }

    /**
     * Send the notification on downvote Event
     *
     * @param data
     */
    private void handleTopicDownVoteEvent(Map<String, Object> data, NotificationEvent nEvent) {
        nEvent.setEventId(Constants.DISCUSSION_DOWNVOTE_EVENT);
        Map<String, Object> tagValues = new HashMap<>();
        HubPost hubPost = hubPostRepository.findByKey(Constants.POST_ROLE + ":" + ((List<String>) data.get(Constants.PARAMS_PID)).get(0));
        tagValues.put(Constants.COMMENT_TAG, hubPost.getContent());
        List<HubUser> userList = userRepository.findByUUIDS(Arrays.asList(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0), Constants.USER_ROLE + ":" + hubPost.getUid()));
        Map<String, HubUser> hubUserMap = userList.stream().collect(Collectors.toMap(HubUser::getKey, hubUser -> hubUser));
        tagValues.put(Constants.DOWNVOTE_BY_NAME, hubUserMap.get(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0)).getUsername());
        tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + hubPost.getTid());
        Map<String, List<String>> recipients = new HashMap<>();
        List<String> watchersList = getWatchersList(String.valueOf(hubPost.getTid()));
        watchersList = getFinalListOfWatchers(watchersList, hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername(), hubUserMap.get(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0)).getUsername());
        recipients.put(Constants.AUTHOR_ROLE, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername()));
        recipients.put(Constants.DOWNVOTE_BY, Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0)).getUsername()));
        nEvent.setTagValues(tagValues);
        nEvent.setRecipients(recipients);
    }

    /**
     * Get the watcher list including followers, watchers and posters
     *
     * @param tid
     * @return List of username of users
     */
    public List<String> getWatchersList(String tid) {
        List<TopicPoster> hubPosters = topicPostersRepositoy.findByKey(Constants.TID_CONSTANTS + ":" + tid + ":" + Constants.POSTERS_CONSTANT);
        List<String> postersUUId = hubPosters.stream().map(poster -> poster.getValue()).collect(Collectors.toList());
        TopicFollower followers = topicFollowerRepository.findByKey(Constants.TID_CONSTANTS + ":" + tid + ":" + Constants.FOLLOWERS_CONSTANT);
        List<String> followersUUId = new ArrayList<>();
        if (!ObjectUtils.isEmpty(followers)) {
            followersUUId = followers.getMembers();
        }
        Set<String> finalWatcherIds = new HashSet<>();
        List<String> watchersUserName = new ArrayList<>();
        if (!CollectionUtils.isEmpty(postersUUId)) {
            finalWatcherIds.addAll(postersUUId);
        }
        if (!CollectionUtils.isEmpty(followersUUId)) {
            finalWatcherIds.addAll(followersUUId);
        }
        if (CollectionUtils.isEmpty(finalWatcherIds))
            return watchersUserName;
        ArrayList<String> userRoleIds = new ArrayList<>();
        for (String uuid : finalWatcherIds) {
            userRoleIds.add(Constants.USER_ROLE + ":" + uuid);
        }
        List<HubUser> userList = userRepository.findByUUIDS(userRoleIds);
        if (!CollectionUtils.isEmpty(userList)) {
            watchersUserName = userList.stream().map(user -> user.getUsername()).collect(Collectors.toList());
        }
        return watchersUserName;
    }

    /**
     * @param watchersList
     * @param addUser
     * @param removeUser
     * @return Get final list of watchers
     */
    private List<String> getFinalListOfWatchers(List<String> watchersList, String addUser, String removeUser) {
        if (!watchersList.isEmpty()) {
            if (!watchersList.contains(addUser)) {
                watchersList.add(addUser);
            }
            if (watchersList.contains(removeUser)) {
                watchersList.remove(removeUser);
            }
        } else {
            watchersList = new ArrayList<>();
            watchersList.add(addUser);
        }
        return watchersList;
    }
}
