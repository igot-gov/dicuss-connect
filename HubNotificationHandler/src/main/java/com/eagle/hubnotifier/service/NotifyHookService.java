package com.eagle.hubnotifier.service;

import java.util.Map;

public interface NotifyHookService {

	public void handleNotifiyRestRequest(Map<String, Object> data);

	public void handleNotifyKafkaTopicRequest(Map<String, Object> data);
}
