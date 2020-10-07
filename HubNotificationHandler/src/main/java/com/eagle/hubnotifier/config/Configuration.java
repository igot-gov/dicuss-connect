package com.eagle.hubnotifier.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
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

    public String getNotifyServiceHost() {
        return notifyServiceHost;
    }

    public String getNotifyServicePath() {
        return notifyServicePath;
    }

    public String getNotifyTopic() {
        return notifyTopic;
    }

    public String getHubRootOrg() {
        return hubRootOrg;
    }

    public String getHubServiceHost() {return hubServiceHost;}

    public String getDiscussionCreateUrl() {return discussionCreateUrl;}

    public String getHubServiceGetPath() {return hubServiceGetPath;}

    public String getTopicSearchPath() {return topicSearchPath;}

}
