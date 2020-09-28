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
}
