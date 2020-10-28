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
			logger.info("Received Event Data : {}", message);
		} catch (JsonProcessingException ex) {
			logger.error("Not able to parse the data", ex);
		}
		List<String> hookList = (List<String>) data.get(Constants.HOOK_VALUE);
		if (hookList != null) {
			NotificationEvent notificationEvent = null;
			for (String hook : hookList) {
				notificationEvent = new NotificationEvent();
				switch (hook) {
				case Constants.FILTER_POST_CREATE:
					handlePostCreate(data, notificationEvent);
					break;
				case Constants.ACTION_POST_UPVOTE:
					handleTopicUpvoteEvent(data, notificationEvent);
					break;
				case Constants.ACTION_POST_DOWNVOTE:
					handleTopicDownVoteEvent(data, notificationEvent);
					break;
				default:
					logger.warn("Unhandled Event: {} received. We should remove this WebHook event from configuration.",
							hook);
					break;
				}
				if (notificationEvent.getRecipients() != null && notificationEvent.getRecipients().size() > 0) {
					notifyHandler.sendNotification(notificationEvent);
				} else {
					logger.info("Ignoring event '{}' as there are no recipients.", hook);
				}
			}
		}
	}

	/**
	 * Handle Post Create Request
	 *
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	private void handlePostCreate(Map<String, Object> data, NotificationEvent nEvent) {
		nEvent.setEventId(Constants.DISCUSSION_REPLY_EVENT_ID);
		Map<String, Object> tagValues = new HashMap<>();

		Map<String, List<String>> recipients = new HashMap<>();
		HubUser topicCreator, currentPostCreator;

		String currentPostCreatorUid = ((List<String>) data.get(Constants.PARAM_POST_UID_CONSTANT)).get(0);
		String topicId = ((List<String>) data.get(Constants.PARAM_POST_TID_CONSTANT)).get(0);
		currentPostCreator = getUserDetails(currentPostCreatorUid);

		if (currentPostCreator == null) {
			logger.error("Failed to process Post Create Event.");
			return;
		}

		String replyText = ((List<String>) data.get(Constants.PARAM_POST_CONTENT_CONSTANT)).get(0);
		if (replyText.length() > configuration.getTopicReplyMaxLength()) {
			replyText = replyText.substring(0, configuration.getTopicReplyMaxLength()).trim() + Constants.EXTRA_DOTS;
		}
		tagValues.put(Constants.REPLY_TAG, replyText);
		tagValues.put(Constants.COMMENTED_BY_NAME_TAG, currentPostCreator.getUsername());
		recipients.put(Constants.COMMENTER_ROLE, Arrays.asList(currentPostCreator.getUsername()));
		tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL, configuration.getDiscussionCreateUrl() + topicId);

		HubTopic topic = getTopicDetails(topicId);
		List<String> authorList = new ArrayList<String>(2);
		if (topic != null) {
			String topicTitle = topic.getTitle();
			if (topicTitle.length() > configuration.getTopicTitleMaxLength()) {
				topicTitle = topicTitle.substring(0, configuration.getTopicTitleMaxLength()).trim()
						+ Constants.EXTRA_DOTS;
			}
			tagValues.put(Constants.DISCUSSION_CREATION_TOPIC_TAG, topicTitle);
			if (!currentPostCreatorUid.equalsIgnoreCase(topic.getUid().toString())) {
				// Topic Creator and Post Creator are different. We need to send notification to
				// topic creator
				topicCreator = getUserDetails(topic.getUid().toString());
				if (topicCreator != null) {
					authorList.add(topicCreator.getUsername());
				}
			}

			List<String> toPostIdList = (List<String>) data.get(Constants.PARAM_POST_TOPID_CONSTANT);
			if (toPostIdList != null) {
				HubPost replyToPost = getPostDetails(toPostIdList.get(0));
				if (replyToPost != null && !replyToPost.getUid().toString().equalsIgnoreCase(currentPostCreatorUid)
						&& !replyToPost.getUid().toString().equalsIgnoreCase(topic.getUid().toString())) {
					HubUser repliedToUser = getUserDetails(replyToPost.getUid().toString());
					if (repliedToUser != null) {
						authorList.add(repliedToUser.getUsername());
					}
				}
			}
		}
		if (!authorList.isEmpty()) {
			recipients.put(Constants.AUTHOR_ROLE, authorList);
			nEvent.setRecipients(recipients);
			nEvent.setTagValues(tagValues);
		}
	}

	/**
	 * Send the notification on upvote Event
	 *
	 * @param data
	 */
	private void handleTopicUpvoteEvent(Map<String, Object> data, NotificationEvent nEvent) {
		nEvent.setEventId(Constants.DISCUSSION_UPVOTE_EVENT);
		Map<String, Object> tagValues = new HashMap<>();
		HubPost hubPost = hubPostRepository
				.findByKey(Constants.POST_ROLE + ":" + ((List<String>) data.get(Constants.PARAMS_PID)).get(0));
		tagValues.put(Constants.COMMENT_TAG, hubPost.getContent());
		List<String> upvotedByUuids = (List<String>) data.get(Constants.PARAM_UID);
		List<HubUser> userList = userRepository.findByUUIDS(Arrays.asList(
				Constants.USER_ROLE + ":" + upvotedByUuids.get(0), Constants.USER_ROLE + ":" + hubPost.getUid()));
		Map<String, HubUser> hubUserMap = userList.stream()
				.collect(Collectors.toMap(HubUser::getKey, hubUser -> hubUser));
		tagValues.put(Constants.UPVOTED_BY_NAME,
				hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername());
		tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL,
				configuration.getDiscussionCreateUrl() + hubPost.getTid());
		Map<String, List<String>> recipients = new HashMap<>();
		List<String> watchersList = getWatchersList(String.valueOf(hubPost.getTid()));
		watchersList = getFinalListOfWatchers(watchersList,
				hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername(),
				hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername());
		recipients.put(Constants.AUTHOR_ROLE, watchersList);
		recipients.put(Constants.UPVOTED_BY,
				Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + upvotedByUuids.get(0)).getUsername()));
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
		HubPost hubPost = hubPostRepository
				.findByKey(Constants.POST_ROLE + ":" + ((List<String>) data.get(Constants.PARAMS_PID)).get(0));
		tagValues.put(Constants.COMMENT_TAG, hubPost.getContent());
		List<HubUser> userList = userRepository.findByUUIDS(
				Arrays.asList(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0),
						Constants.USER_ROLE + ":" + hubPost.getUid()));
		Map<String, HubUser> hubUserMap = userList.stream()
				.collect(Collectors.toMap(HubUser::getKey, hubUser -> hubUser));
		tagValues.put(Constants.DOWNVOTE_BY_NAME, hubUserMap
				.get(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0)).getUsername());
		tagValues.put(Constants.DISCUSSION_CREATION_TARGET_URL,
				configuration.getDiscussionCreateUrl() + hubPost.getTid());
		Map<String, List<String>> recipients = new HashMap<>();
		List<String> watchersList = getWatchersList(String.valueOf(hubPost.getTid()));
		watchersList = getFinalListOfWatchers(watchersList,
				hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername(),
				hubUserMap.get(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0))
						.getUsername());
		recipients.put(Constants.AUTHOR_ROLE,
				Arrays.asList(hubUserMap.get(Constants.USER_ROLE + ":" + hubPost.getUid()).getUsername()));
		recipients.put(Constants.DOWNVOTE_BY, Arrays.asList(hubUserMap
				.get(Constants.USER_ROLE + ":" + ((List<String>) data.get(Constants.PARAM_UID)).get(0)).getUsername()));
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
		List<TopicPoster> hubPosters = topicPostersRepositoy
				.findByKey(Constants.TID_CONSTANTS + ":" + tid + ":" + Constants.POSTERS_CONSTANT);
		List<String> postersUUId = hubPosters.stream().map(poster -> poster.getValue()).collect(Collectors.toList());
		TopicFollower followers = topicFollowerRepository
				.findByKey(Constants.TID_CONSTANTS + ":" + tid + ":" + Constants.FOLLOWERS_CONSTANT);
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

	private HubUser getUserDetails(String uid) {
		HubUser user = userRepository.findByKey(Constants.USER_KEY + uid);
		if (user == null) {
			logger.error("Failed to find the User for UserId: {}", uid);
		}
		return user;
	}

	private HubTopic getTopicDetails(String topicId) {
		HubTopic topic = hubTopicRepository.findByKey(Constants.TOPIC_KEY + topicId);
		if (topic == null) {
			logger.error("Failed to find the Topic Creator details for TopicId: {}", topicId);
		}
		return topic;
	}

	private HubPost getPostDetails(String postId) {
		HubPost post = hubPostRepository.findByKey(Constants.POST_KEY + postId);
		if (post == null) {
			logger.error("Failed to find the Post details for PostId: {}", postId);
		}
		return post;
	}
}
