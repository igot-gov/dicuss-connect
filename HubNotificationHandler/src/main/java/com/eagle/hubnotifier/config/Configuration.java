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

}
