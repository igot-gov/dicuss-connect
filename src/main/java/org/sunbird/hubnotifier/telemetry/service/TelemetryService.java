package org.sunbird.hubnotifier.telemetry.service;

import java.util.Map;

public interface TelemetryService {
	public void handleNotifyKafkaTopicRequest(Map<String, Object> data);
}
