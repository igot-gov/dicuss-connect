package com.eagle.hubnotifier.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class Configuration {

	@Value("${notify.service.host}")
	private String notifyServiceHost;

	@Value("${notify.service.path}")
	private String notifyServicePath;

	@Value("${kafka.topics.incoming.notify}")
	private String notifyTopic;

	@Value("${hub.notification.rootOrg}")
	private String hubRootOrg;

	@Value("${discussion.create.targetUrl}")
	private String discussionCreateUrl;

	@Value("${hub.service.topic.path}")
	private String topicSearchPath;

	@Value("${hub.service.host}")
	private String hubServiceHost;

	@Value("${hub.service.get.path}")
	private String hubServiceGetPath;

	@Value("${topic.reply.max.length}")
	private int topicReplyMaxLength;

	@Value("${topic.title.max.length}")
	private int topicTitleMaxLength;

	@Value("${telemetry.service.host}")
	private String telemetryServiceHost;

	@Value("${telemetry.service.path}")
	private String telemetryServicePath;

	@Value("${telemetry.id}")
	private String telemetryId;

	@Value("${telemetry.version}")
	private String telemetryVersion;

	@Value("${telemetry.event.version}")
	private String telemetryEventVerion;

	@Value("${telemetry.event.eid.interact}")
	private String telemetryEventEidInteract;

	@Value("${telemetry.event.eid.impression}")
	private String telemetryEventEidImpression;

	@Value("${telemetry.edata.id}")
	private String telemetryEDataId;

	@Value("${telemetry.edata.create.type}")
	private String telemetryEDataCreateType;

	@Value("${telemetry.edata.view.type}")
	private String telemetryEdataViewType;

	@Value("${telemetry.edata.topic.target}")
	private String telemetryEDataTopicTarget;

	@Value("${telemetry.edata.post.target}")
	private String telemetryEDataPostTarget;

	@Value("${telemetry.context.channel}")
	private String telemetryContextChannel;

	@Value("${telemetry.context.env}")
	private String telemetryContextEnv;

	@Value("${telemetry.context.pdata.id}")
	private String telemetryContextPDataId;

	@Value("${telemetry.context.pdata.ver}")
	private String telemetryContextPDataVer;

	@Value("${telemetry.context.pdata.pid}")
	private String telemetryContextPDataPid;

	@Value("${telemetry.edata.tag.target}")
	private String telemetryEDataTagTarget;
}
