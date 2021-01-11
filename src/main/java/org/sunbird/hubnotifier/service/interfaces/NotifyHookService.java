package org.sunbird.hubnotifier.service.interfaces;

import java.util.Map;

public interface NotifyHookService {

	public void handleNotifiyRestRequest(Map<String, Object> data);

	public void handleNotifyKafkaTopicRequest(Map<String, Object> data);
}
