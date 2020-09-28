package com.eagle.hubnotifier.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Override
	public void handleNotifiyRestRequest(Map<String, Object> data) {
		if (logger.isDebugEnabled()) {
			logger.info("Recived request from Rest Controller");
		}
		producer.push(data);
	}

	@Override
	public void handleNotifyKafkaTopicRequest(Map<String, Object> data) {
		logger.info("Recived request from Topic Consumer");
		List<String> hookList = (List<String>) data.get("hook");
		if (hookList != null) {
			for (String hook : hookList) {
				switch (hook) {
				case "filter:topic.create":
					handleTopicCreate(data);
					break;
				case "filter:post.create":
					handlePostCreate(data);
					break;
				default:
					break;
				}
			}
		}
	}

	private void handleTopicCreate(Map<String, Object> data) {
		logger.info("Received Topic Creation Event");
		NotificationEvent nEvent = new NotificationEvent();
		nEvent.setEventId("discussion_creation");
		Map<String, Object> tagValues = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		List<String> topicTitleList = (List<String>) data.get("params[topic][title]");
		tagValues.put("#discussionTopic", topicTitleList.get(0));
		Map<String, List<String>> recipients = new HashMap<String, List<String>>();
		recipients.put("author", Arrays.asList("wid1,wid2"));
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
				String userKey = "user:" + uid;
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
