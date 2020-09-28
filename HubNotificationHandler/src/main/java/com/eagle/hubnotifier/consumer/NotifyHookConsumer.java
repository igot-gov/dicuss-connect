package com.eagle.hubnotifier.consumer;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.eagle.hubnotifier.service.NotifyHookService;
import com.eagle.hubnotifier.util.Constants;

@Service
public class NotifyHookConsumer {

	@Autowired
	private NotifyHookService notifyHook;

	@KafkaListener(topics = {
			"${kafka.topics.incoming.notify}" }, containerFactory = Constants.INCOMING_KAFKA_LISTENER)
	public void processMessage(Map<String, Object> data, @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
		notifyHook.handleNotifyKafkaTopicRequest(data);
	}
}
