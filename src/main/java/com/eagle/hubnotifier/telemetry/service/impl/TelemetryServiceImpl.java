package com.eagle.hubnotifier.telemetry.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.eagle.hubnotifier.model.HubPost;
import com.eagle.hubnotifier.model.HubTopic;
import com.eagle.hubnotifier.repository.HubPostRepository;
import com.eagle.hubnotifier.repository.HubTopicRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eagle.hubnotifier.cache.CategoryCacheManager;
import com.eagle.hubnotifier.config.Configuration;
import com.eagle.hubnotifier.constants.Constants;
import com.eagle.hubnotifier.model.HubUser;
import com.eagle.hubnotifier.repository.HubUserRepository;
import com.eagle.hubnotifier.service.impl.OutboundRequestHandlerServiceImpl;
import com.eagle.hubnotifier.telemetry.model.Actor;
import com.eagle.hubnotifier.telemetry.model.Context;
import com.eagle.hubnotifier.telemetry.model.EData;
import com.eagle.hubnotifier.telemetry.model.Event;
import com.eagle.hubnotifier.telemetry.model.TelemetryData;
import com.eagle.hubnotifier.telemetry.service.TelemetryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ObjectUtils;

@Service
public class TelemetryServiceImpl implements TelemetryService {
	Logger logger = LogManager.getLogger(TelemetryServiceImpl.class);

	@Autowired
	private Configuration config;

	@Autowired
	private OutboundRequestHandlerServiceImpl serviceRepo;

	@Autowired
	private HubUserRepository userRepository;

	@Autowired
	private CategoryCacheManager categoryCacheManager;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private HubPostRepository hubPostRepository;

	@Autowired
	private HubTopicRepository hubTopicRepository;

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
			for (String hook : hookList) {
				TelemetryData tData = createTelemetryData();

				boolean notificationEnabledForEvent = true;
				switch (hook) {
					case Constants.FILTER_TOPIC_CREATE:
						handleTopicCreate(data, tData);
						break;
					case Constants.FILTER_POST_CREATE:
						handlePostCreate(data, tData);
						break;
					case Constants.FILTER_TOPIC_REPLY:
						handleTopicReplyEvent(data, tData);
						break;
					case Constants.ACTION_POST_UPVOTE:
						handleTopicUpvoteEvent(data, tData);
						break;
					case Constants.ACTION_POST_DOWNVOTE:
						handleTopicDownVoteEvent(data, tData);
						break;
					default:
						notificationEnabledForEvent = false;
						break;
				}
				if (notificationEnabledForEvent) {
					postTelemetryData(tData);
				}
			}
		}
	}

	private void handleTopicCreate(Map<String, Object> data, TelemetryData tData) {
		EData eData = EData.builder().id(config.getTelemetryEDataId()).type(config.getTelemetryEDataCreateType())
				.target(config.getTelemetryEDataTopicTarget())
				.pageid(config.getDiscussionCreateUrl() + getTopicId(data, Constants.PARAM_TOPIC_TID_CONSTANT))
				.topicName(getTopicTitle(data, Constants.PARAM_TOPIC_TITLE_CONSTANT))
				.categoryName(getCategoryName(data)).build();
		tData.getEvent().setEData(eData);

		tData.getEvent().setActor(Actor.builder().id(getUserName(data, Constants.PARAM_TOPIC_UID_CONSTANT))
				.type(Constants.USER_ROLE).build());
		tData.getEvent().setEid(config.getTelemetryEventEidInteract());
		handleTagCreateEvent(data);
		logger.info("Created TelemetryData: {}", tData);
	}

	private void handlePostCreate(Map<String, Object> data, TelemetryData tData) {
		EData eData = EData.builder().id(config.getTelemetryEDataId()).type(config.getTelemetryEDataCreateType())
				.target(config.getTelemetryEDataPostTarget())
				.pageid(config.getDiscussionCreateUrl() + getTopicId(data, Constants.PARAM_POST_TID_CONSTANT))
				.topicName(getTopicTitle(data, Constants.PARAM_POST_TOPIC_TITLE)).categoryName(getCategoryName(data))
				.build();
		tData.getEvent().setEData(eData);
		tData.getEvent().setActor(Actor.builder().id(getUserName(data, Constants.PARAM_POST_UID_CONSTANT))
				.type(Constants.USER_ROLE).build());
		tData.getEvent().setEid(config.getTelemetryEventEidImpression());
	}

	private void handleTopicReplyEvent(Map<String, Object> data, TelemetryData tData) {

	}

	/**
	 * Construct TelemeterData
	 * @param data event data
	 * @param tData telemetry object
	 */
	@SuppressWarnings("unchecked")
	private void handleTopicUpvoteEvent(Map<String, Object> data, TelemetryData tData) {
		HubPost hubPost = hubPostRepository.findByKey(Constants.POST_ROLE + ":" + ((List<String>) data.get(Constants.PARAMS_PID)).get(0));
		HubTopic topic = hubTopicRepository.findByKey(Constants.TOPIC_KEY + hubPost.getTid());
		String userId = ((List<String>) data.get(Constants.PARAM_UID)).get(0);
		HubUser user = userRepository.findByKey(Constants.USER_ROLE + ":" + userId);
		EData eData = EData.builder().id(config.getTelemetryEDataId()).type(config.getTelemetryEdataOtherType())
				.subType(config.getTelemetryEdataUpvoteType())
				.target(config.getTelemetryEDataPostTarget())
				.pageid(config.getDiscussionCreateUrl() + hubPost.getTid())
				.topicName(topic.getTitle())
				.categoryName(getCategoryName(topic.getCid())).build();
		tData.getEvent().setEData(eData);
		tData.getEvent().setActor(Actor.builder().id(user.getUsername())
				.type(Constants.USER_ROLE).build());
		tData.getEvent().setEid(config.getTelemetryEventEidInteract());
		try {
			logger.info("Created TelemetryData: {}", mapper.writeValueAsString(tData));
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while writing the data into string");
		}
	}

	/**
	 * Construct TelemeterData
	 * @param data event data
	 * @param tData telemetry object
	 */
	private void handleTopicDownVoteEvent(Map<String, Object> data, TelemetryData tData) {
		HubPost hubPost = hubPostRepository.findByKey(Constants.POST_ROLE + ":" + ((List<String>) data.get(Constants.PARAMS_PID)).get(0));
		HubTopic topic = hubTopicRepository.findByKey(Constants.TOPIC_KEY + hubPost.getTid());
		String userId = ((List<String>) data.get(Constants.PARAM_UID)).get(0);
		HubUser user = userRepository.findByKey(Constants.USER_ROLE + ":" + userId);
		EData eData = EData.builder().id(config.getTelemetryEDataId()).type(config.getTelemetryEdataOtherType())
				.subType(config.getTelemetryEdataDownvoteType())
				.target(config.getTelemetryEDataPostTarget())
				.pageid(config.getDiscussionCreateUrl() + hubPost.getTid())
				.topicName(topic.getTitle())
				.categoryName(getCategoryName(topic.getCid())).build();
		tData.getEvent().setEData(eData);
		tData.getEvent().setActor(Actor.builder().id(user.getUsername())
				.type(Constants.USER_ROLE).build());
		tData.getEvent().setEid(config.getTelemetryEventEidInteract());
		try {
			logger.info("Created TelemetryData: {}", mapper.writeValueAsString(tData));
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while writing the data into string");
		}
	}

	/**
	 * Send telemetry data
	 *
	 * @param data Event received
	 */
	private void handleTagCreateEvent(Map<String, Object> data) {
		TelemetryData tData = createTelemetryData();
		if (ObjectUtils.isEmpty(data.get(Constants.TAG_PREFIX_CONSTANT + 0 + Constants.CLOSING_TAG)))
			return;
		EData eData = EData.builder().id(config.getTelemetryEDataId()).type(config.getTelemetryEDataCreateType())
				.target(config.getTelemetryEDataTagTarget())
				.pageid(config.getDiscussionCreateUrl() + getTopicId(data, Constants.PARAM_TOPIC_TID_CONSTANT))
				.topicName(getTopicTitle(data, Constants.PARAM_TOPIC_TITLE_CONSTANT))
				.categoryName(getCategoryName(data)).build();
		tData.getEvent().setEData(eData);
		tData.getEvent().setActor(Actor.builder().id(getUserName(data, Constants.PARAM_TOPIC_UID_CONSTANT))
				.type(Constants.USER_ROLE).build());
		tData.getEvent().setTags(getTagList(data));
		tData.getEvent().setEid(config.getTelemetryEventEidInteract());
		try {
			logger.info("Created TelemetryData: {}", mapper.writeValueAsString(tData));
		} catch (JsonProcessingException e) {
			logger.error("Exception occurred while writing the data into string");
		}
		postTelemetryData(tData);
	}

	/**
	 *
	 * @param data
	 * @return Tag list from received data
	 */
	private List<String> getTagList(Map<String, Object> data) {
		List<String> taglist = new ArrayList<>();
		int i = 0;
		while (i >= 0) {
			if (ObjectUtils.isEmpty(data.get(Constants.TAG_PREFIX_CONSTANT + i + Constants.CLOSING_TAG)))
				break;
			taglist.add(((List<String>) data.get(Constants.TAG_PREFIX_CONSTANT + i + Constants.CLOSING_TAG)).get(0));
			i++;
		}
		return taglist;
	}

	private TelemetryData createTelemetryData() {
		TelemetryData tData = new TelemetryData();
		tData.setId(config.getTelemetryId());
		tData.setVer(config.getTelemetryVersion());
		tData.setEts(System.currentTimeMillis());
		List<Event> events = new ArrayList<Event>();
		Event event = new Event();
		event.setVer(config.getTelemetryEventVerion());
		event.setEts(System.currentTimeMillis());

		Context context = new Context();
		context.setChannel(config.getTelemetryContextChannel());
		context.setEnv(config.getTelemetryContextEnv());

		// TODO - Create a random string to cover the session id
		context.setSid("");

		// TODO - create a uuid for the did value
		context.setDid("");
		event.setContext(context);

		events.add(event);
		tData.setEvents(events);

		return tData;
	}

	private String getCategoryName(Map<String, Object> data) {
		return categoryCacheManager
				.getCategoryName(Integer.parseInt(((List<String>) data.get(Constants.PARAM_POST_CID_CONSTANT)).get(0)));
	}

	private String getCategoryName(Long cid) {
		return categoryCacheManager
				.getCategoryName(Math.toIntExact(cid));
	}

	private String getTopicId(Map<String, Object> data, String key) {
		return ((List<String>) data.get(key)).get(0);
	}

	private String getTopicTitle(Map<String, Object> data, String key) {
		return ((List<String>) data.get(key)).get(0);
	}

	private String getUserName(Map<String, Object> data, String key) {
		List<String> topicUids = (List<String>) data.get(key);
		HubUser user = userRepository.findByKey(Constants.USER_ROLE + ":" + topicUids.get(0));
		return user.getUsername();
	}

	/**
	 * Post to the Notification service
	 *
	 * @param tData
	 * @throws Exception
	 */
	public void postTelemetryData(TelemetryData tData) {
		StringBuilder builder = new StringBuilder();
		builder.append(config.getTelemetryServiceHost()).append(config.getTelemetryServicePath());
		try {
			serviceRepo.fetchResultUsingPost(builder, tData, TelemetryData.class);
		} catch (Exception e) {
			logger.error("Exception while posting the data in notification service: ", e);
		}
	}
}
